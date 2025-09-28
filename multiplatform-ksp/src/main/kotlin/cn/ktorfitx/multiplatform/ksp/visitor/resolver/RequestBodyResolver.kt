package cn.ktorfitx.multiplatform.ksp.visitor.resolver

import cn.ktorfitx.common.ksp.util.check.ktorfitxCheck
import cn.ktorfitx.common.ksp.util.check.ktorfitxCheckNotNull
import cn.ktorfitx.common.ksp.util.expends.*
import cn.ktorfitx.common.ksp.util.message.invoke
import cn.ktorfitx.common.ksp.util.resolver.isSerializableType
import cn.ktorfitx.multiplatform.ksp.constants.TypeNames
import cn.ktorfitx.multiplatform.ksp.message.*
import cn.ktorfitx.multiplatform.ksp.model.*
import com.google.devtools.ksp.getAllSuperTypes
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.squareup.kotlinpoet.ParameterizedTypeName
import com.squareup.kotlinpoet.WildcardTypeName
import com.squareup.kotlinpoet.ksp.toTypeName

internal fun KSFunctionDeclaration.getRequestBodyModel(): RequestBodyModel? {
	val classNames = this.parameters.mapNotNull { parameter ->
		requestBodyKindMap.keys.find { parameter.hasAnnotation(it) }
	}.toSet()
	if (classNames.isEmpty()) return null
	val useRequestBodyMap = classNames.groupBy { requestBodyKindMap[it]!! }
	ktorfitxCheck(useRequestBodyMap.size == 1, this) {
		val useTypeNames = useRequestBodyMap.values.flatten().joinToString { "@${it.simpleName}" }
		MESSAGE_FUNCTION_USE_INCOMPATIBLE_ANNOTATIONS(simpleName, useTypeNames)
	}
	return when (useRequestBodyMap.keys.single()) {
		RequestBodyKind.BODY -> this.getBodyModel()
		RequestBodyKind.PART -> this.getPartRequestBodyModel()
		RequestBodyKind.FIELD -> this.getFieldRequestBodyModel()
	}
}

private fun KSFunctionDeclaration.getBodyModel(): BodyModel? {
	val filters = this.parameters.filter {
		it.hasAnnotation(TypeNames.Body)
	}
	if (filters.isEmpty()) return null
	ktorfitxCheck(filters.size == 1, this) {
		MESSAGE_FUNCTION_NOT_ALLOW_USE_MULTIPLE_BODY_ANNOTATIONS(simpleName)
	}
	val parameter = filters.single()
	val varName = parameter.name!!.asString()
	val typeName = parameter.type.toTypeName()
	ktorfitxCheck(typeName.isSerializableType(), parameter) {
		MESSAGE_PARAMETER_TYPE_NOT_MEET_SERIALIZATION_REQUIREMENTS(simpleName, parameter.name!!)
	}
	val annotation = parameter.getKSAnnotationByType(TypeNames.Body)!!
	val formatClassName = annotation.getClassNameOrNull("format") ?: TypeNames.SerializationFormatJson
	return BodyModel(varName, formatClassName)
}

private enum class RequestBodyKind {
	BODY,
	PART,
	FIELD
}

private val requestBodyKindMap = mapOf(
	TypeNames.Body to RequestBodyKind.BODY,
	TypeNames.Part to RequestBodyKind.PART,
	TypeNames.Parts to RequestBodyKind.PART,
	TypeNames.Field to RequestBodyKind.FIELD,
	TypeNames.Fields to RequestBodyKind.FIELD
)

private fun KSFunctionDeclaration.getFieldRequestBodyModel(): FieldRequestBodyModel {
	val fieldModels = this.parameters.mapNotNull { parameter ->
		val annotation = parameter.getKSAnnotationByType(TypeNames.Field) ?: return@mapNotNull null
		val varName = parameter.name!!.asString()
		var name = annotation.getValueOrNull<String>("name")
		if (name.isNullOrBlank()) {
			name = varName
		}
		val type = parameter.type.resolve()
		val typeName = type.toTypeName()
		val isString = typeName.equals(TypeNames.String, ignoreNullable = true)
		FieldModel(name, varName, isString, type.isMarkedNullable)
	}
	val fieldsModels = this.parameters.mapNotNull { parameter ->
		parameter.getKSAnnotationByType(TypeNames.Fields) ?: return@mapNotNull null
		val varName = parameter.name!!.asString()
		val type = parameter.type.resolve()
		val fieldsKind = when {
			type.isMapOfStringToAny() -> FieldsKind.MAP
			type.isListOfStringPair() -> FieldsKind.LIST
			else -> null
		}
		ktorfitxCheckNotNull(fieldsKind, parameter) {
			MESSAGE_PARAMETER_ONLY_ALLOW_USE_SUPPORTED_BY_FIELD(simpleName, varName)
		}
		val typeName = type.toTypeName() as ParameterizedTypeName
		val valueTypeName = when (fieldsKind) {
			FieldsKind.LIST -> (typeName.typeArguments.single() as ParameterizedTypeName).typeArguments[1]
			FieldsKind.MAP -> typeName.typeArguments[1]
		}
		FieldsModel(
			varName,
			fieldsKind,
			type.isMarkedNullable,
			valueTypeName.equals(TypeNames.String, ignoreNullable = true),
			valueTypeName is WildcardTypeName || valueTypeName.isNullable
		)
	}
	return FieldRequestBodyModel(fieldModels, fieldsModels)
}

private fun KSFunctionDeclaration.getPartRequestBodyModel(): PartRequestBodyModel {
	val partModels = this.parameters.mapNotNull { parameter ->
		val annotation = parameter.getKSAnnotationByType(TypeNames.Part) ?: return@mapNotNull null
		val varName = parameter.name!!.asString()
		val headerMap = annotation.getValuesOrNull<String>("headers")?.associate {
			ktorfitxCheckNotNull(it.parseHeader(), parameter) {
				MESSAGE_PARAMETER_PART_FORMAT_IS_INCORRECT(simpleName, varName)
			}
		}
		val name = annotation.getValueOrNull<String>("name")?.takeIf { it.isNotBlank() } ?: varName
		val type = parameter.type.resolve()
		ktorfitxCheck(!type.isMarkedNullable, parameter) {
			MESSAGE_PARAMETER_NOT_ALLOW_USE_NULLABLE_TYPE(simpleName, varName)
		}
		val typeName = type.toTypeName()
		val partKind = when {
			typeName in TypeNames.formPartSupportValueTypes || TypeNames.formPartSupportValueTypes.any { typeName ->
				val declaration = type.declaration
				declaration is KSClassDeclaration && declaration.getAllSuperTypes().any {
					it.toTypeName() == typeName
				}
			} -> PartKind.KEY_VALUE
			
			typeName is ParameterizedTypeName && typeName.rawType == TypeNames.FormPart -> PartKind.DIRECT
			else -> PartKind.FORM_PART
		}
		PartModel(name, varName, headerMap, partKind)
	}
	val partsModels = this.parameters.mapNotNull { parameter ->
		if (!parameter.hasAnnotation(TypeNames.Parts)) return@mapNotNull null
		val type = parameter.type.resolve()
		val varName = parameter.name!!.asString()
		val partsKind = when {
			type.isMapOfStringToAny(false) -> PartsKind.MAP
			type.isListOfStringPair() -> PartsKind.LIST_PAIR
			type.isListOfFormPart() -> PartsKind.LIST_FORM_PART
			else -> null
		}
		ktorfitxCheckNotNull(partsKind, parameter) {
			MESSAGE_PARAMETER_ONLY_ALLOW_USE_SUPPORTED_BY_PARTS(simpleName, varName)
		}
		val valueKind = when (partsKind) {
			PartsKind.MAP -> {
				fun getValueKind(): PartsValueKind? {
					val declaration = type.arguments[1].type?.resolve()?.declaration as? KSClassDeclaration ?: return null
					val isKeyValue = declaration.getAllSuperTypes().any { it.toTypeName() in TypeNames.formPartSupportValueTypes }
					return if (isKeyValue) PartsValueKind.KEY_VALUE else PartsValueKind.FORM_PART
				}
				getValueKind()
			}
			
			PartsKind.LIST_PAIR -> {
				fun getValueKind(): PartsValueKind? {
					val pairType = type.arguments.single().type?.resolve() ?: return null
					val declaration = pairType.arguments[1].type?.resolve()?.declaration as? KSClassDeclaration ?: return null
					val isKeyValue = declaration.getAllSuperTypes().any { it.toTypeName() in TypeNames.formPartSupportValueTypes }
					return if (isKeyValue) PartsValueKind.KEY_VALUE else PartsValueKind.FORM_PART
				}
				getValueKind()
			}
			
			PartsKind.LIST_FORM_PART -> null
		}
		PartsModel(varName, type.isMarkedNullable, partsKind, valueKind)
	}
	return PartRequestBodyModel(partModels, partsModels)
}

private fun KSType.isListOfFormPart(): Boolean {
	val declaration = this.declaration as? KSClassDeclaration ?: return false
	val typeName = (declaration.getAllSuperTypes().find {
		if (it.declaration !is KSClassDeclaration) return@find false
		it.declaration.qualifiedName?.asString() == TypeNames.List.canonicalName
	}?.toTypeName() ?: this.toTypeName()) as? ParameterizedTypeName ?: return false
	val first = typeName.typeArguments.single()
	return first is ParameterizedTypeName && first.rawType == TypeNames.FormPart
}
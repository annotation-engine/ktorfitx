package cn.ktorfitx.multiplatform.ksp.visitor.resolver

import cn.ktorfitx.common.ksp.util.check.compileCheck
import cn.ktorfitx.common.ksp.util.expends.*
import cn.ktorfitx.multiplatform.ksp.constants.TypeNames
import cn.ktorfitx.multiplatform.ksp.model.*
import com.google.devtools.ksp.getAllSuperTypes
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.ParameterizedTypeName
import com.squareup.kotlinpoet.WildcardTypeName
import com.squareup.kotlinpoet.ksp.toTypeName

internal fun KSFunctionDeclaration.getRequestBodyModel(): RequestBodyModel? {
	val classNames = this.parameters.mapNotNull { parameter ->
		requestBodyKindMap.keys.find { parameter.hasAnnotation(it) }
	}.toSet()
	if (classNames.isEmpty()) return null
	val useRequestBodyMap = classNames.groupBy { requestBodyKindMap[it]!! }
	this.compileCheck(useRequestBodyMap.size == 1) {
		val useTypeNames = useRequestBodyMap.values.flatten().joinToString { "@${it.simpleName}" }
		"${simpleName.asString()} 函数使用了不兼容的注解 $useTypeNames"
	}
	return when (useRequestBodyMap.entries.first().key) {
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
	this.compileCheck(filters.size == 1) {
		"${simpleName.asString()} 函数不允许使用多个 @Body 注解"
	}
	val parameter = filters.first()
	val varName = parameter.name!!.asString()
	val typeName = parameter.type.toTypeName()
	this.compileCheck(typeName is ClassName || typeName is ParameterizedTypeName) {
		"${simpleName.asString()} 函数的参数列表中标记了 @Body 注解，但是未找到参数类型"
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
		parameter.compileCheck(fieldsKind != null) {
			"${simpleName.asString()} 函数的 $varName 参数只允许使用 Map<String, *> 或 List<Pair<String, *>> 类型或是它的具体化子类型或派生类型"
		}
		val typeName = type.toTypeName() as ParameterizedTypeName
		val valueTypeName = when (fieldsKind) {
			FieldsKind.LIST -> (typeName.typeArguments.first() as ParameterizedTypeName).typeArguments[1]
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
		val headers = annotation.getValuesOrNull<String>("headers")?.associate {
			val splits = it.split(":")
			parameter.compileCheck(splits.size == 2) {
				"${simpleName.asString()} 函数的 $varName 参数的 @Part 注解上的 headers 参数格式有误，需要以 <key>:<value> 格式"
			}
			splits.first().trim() to splits[1].trim()
		}
		val name = annotation.getValueOrNull<String>("name")?.takeIf { it.isNotBlank() } ?: varName
		val type = parameter.type.resolve()
		parameter.compileCheck(!type.isMarkedNullable) {
			"${simpleName.asString()} 函数的 $varName 参数不允许使用可空类型"
		}
		val typeName = type.toTypeName()
		val partKind = when {
			typeName in TypeNames.formPartSupportValueTypes || TypeNames.formPartSupportValueTypes.any { typeName ->
				val declaration = type.declaration as? KSClassDeclaration ?: return@any false
				declaration.getAllSuperTypes().any {
					it.toTypeName() == typeName
				}
			} -> PartKind.KEY_VALUE
			
			(typeName as? ParameterizedTypeName)?.rawType == TypeNames.FormPart -> PartKind.DIRECT
			else -> PartKind.FORM_PART
		}
		PartModel(name, varName, headers, partKind)
	}
	val partsModels = this.parameters.mapNotNull { parameter ->
		if (!parameter.hasAnnotation(TypeNames.Parts)) return@mapNotNull null
		val type = parameter.type.resolve()
		val varName = parameter.name!!.asString()
		parameter.compileCheck(!type.isMarkedNullable) {
			"${simpleName.asString()} 函数的 $varName 参数不允许使用可空类型"
		}
		val partsKind = when {
			type.isMapOfStringToAny(false) -> PartsKind.MAP
			type.isListOfStringPair() -> PartsKind.LIST_PAIR
			type.isListOfFormPart() -> PartsKind.LIST_FORM_PART
			else -> null
		}
		parameter.compileCheck(partsKind != null) {
			"${simpleName.asString()} 函数的 $varName 参数只允许使用 Map<String, Any> 或 List<Pair<String, Any>> 或 List<FormPart<*>> 类型或是它的具体化子类型或派生类型"
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
					val pairType = type.arguments.first().type?.resolve() ?: return null
					val declaration = pairType.arguments[1].type?.resolve()?.declaration as? KSClassDeclaration ?: return null
					val isKeyValue = declaration.getAllSuperTypes().any { it.toTypeName() in TypeNames.formPartSupportValueTypes }
					return if (isKeyValue) PartsValueKind.KEY_VALUE else PartsValueKind.FORM_PART
				}
				getValueKind()
			}
			
			PartsKind.LIST_FORM_PART -> null
		}
		PartsModel(varName, partsKind, valueKind)
	}
	return PartRequestBodyModel(partModels, partsModels)
}

private fun KSType.isListOfFormPart(): Boolean {
	val declaration = this.declaration as? KSClassDeclaration ?: return false
	val typeName = (declaration.getAllSuperTypes().find {
		if (it.declaration !is KSClassDeclaration) return@find false
		it.declaration.qualifiedName?.asString() == TypeNames.List.canonicalName
	}?.toTypeName() ?: this.toTypeName()) as? ParameterizedTypeName ?: return false
	val first = typeName.typeArguments.first()
	return first is ParameterizedTypeName && first.rawType == TypeNames.FormPart
}
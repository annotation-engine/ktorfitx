package cn.ktorfitx.multiplatform.ksp.visitor.resolver

import cn.ktorfitx.common.ksp.util.check.compileCheck
import cn.ktorfitx.common.ksp.util.expends.*
import cn.ktorfitx.multiplatform.ksp.constants.TypeNames
import cn.ktorfitx.multiplatform.ksp.model.*
import com.google.devtools.ksp.getAllSuperTypes
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.ParameterizedTypeName
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
	val type = useRequestBodyMap.entries.first().key
	return when (type) {
		RequestBodyKind.BODY -> this.getBodyModel()
		RequestBodyKind.PART -> this.getPartModels()
		RequestBodyKind.FIELD -> this.getFieldModels()
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
	TypeNames.Field to RequestBodyKind.FIELD,
	TypeNames.Fields to RequestBodyKind.FIELD
)

private fun KSFunctionDeclaration.getFieldModels(): FieldModels {
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
		val name = parameter.name!!.asString()
		val type = parameter.type.resolve()
		val fieldsKind = when {
			type.isMapOfStringToAny() -> FieldsKind.MAP
			type.isListOfStringPair() -> FieldsKind.LIST
			else -> null
		}
		parameter.compileCheck(fieldsKind != null) {
			"${simpleName.asString()} 函数的 $name 参数只允许使用 Map<String, *> 或 List<Pair<String, *>> 类型或是它的具体化子类型或派生类型"
		}
		val typeName = type.toTypeName() as ParameterizedTypeName
		val valueTypeName = when (fieldsKind) {
			FieldsKind.LIST -> (typeName.typeArguments.first() as ParameterizedTypeName).typeArguments[1]
			FieldsKind.MAP -> typeName.typeArguments[1]
		}
		FieldsModel(name, fieldsKind, valueTypeName.equals(TypeNames.String, ignoreNullable = true), valueTypeName.isNullable)
	}
	return FieldModels(fieldModels, fieldsModels)
}

private fun KSFunctionDeclaration.getPartModels(): PartModels {
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
			typeName in TypeNames.formPartValueTypeNames || TypeNames.formPartValueTypeNames.any { typeName ->
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
	return PartModels(partModels)
}
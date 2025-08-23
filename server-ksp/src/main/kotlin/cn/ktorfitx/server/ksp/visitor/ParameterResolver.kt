package cn.ktorfitx.server.ksp.visitor

import cn.ktorfitx.common.ksp.util.check.compileCheck
import cn.ktorfitx.common.ksp.util.check.ktorfitxCompilationError
import cn.ktorfitx.common.ksp.util.expends.*
import cn.ktorfitx.server.ksp.constants.TypeNames
import cn.ktorfitx.server.ksp.model.*
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSValueParameter
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.ksp.toTypeName

internal fun KSFunctionDeclaration.getVarNames(): List<String> {
	return this.parameters.map { parameter ->
		val count = TypeNames.parameterAnnotations.count { parameter.hasAnnotation(it) }
		parameter.compileCheck(count > 0) {
			val annotations = TypeNames.parameterAnnotations.joinToString { "@${it.simpleName}" }
			"${simpleName.asString()} 函数的 ${parameter.name!!.asString()} 参数必须使用 $annotations 注解中的一个"
		}
		parameter.compileCheck(count == 1) {
			val annotations = TypeNames.parameterAnnotations.joinToString { "@${it.simpleName}" }
			"${simpleName.asString()} 函数的 ${parameter.name!!.asString()} 参数只允许使用 $annotations 注解中的一个"
		}
		parameter.name!!.asString()
	}
}

internal fun KSFunctionDeclaration.getPrincipalModels(): List<PrincipalModel> {
	return this.parameters.mapNotNull { parameter ->
		val annotation = parameter.getKSAnnotationByType(TypeNames.Principal) ?: return@mapNotNull null
		val varName = parameter.name!!.asString()
		val type = parameter.type.resolve()
		val typeName = type.toTypeName().asNotNullable()
		val provider = annotation.getValueOrNull<String>("provider")?.takeIf { it.isNotBlank() }
		PrincipalModel(varName, typeName, type.isMarkedNullable, provider)
	}
}

internal fun KSFunctionDeclaration.getQueryModels(): List<QueryModel> {
	return this.parameters.mapNotNull { parameter ->
		val annotation = parameter.getKSAnnotationByType(TypeNames.Query) ?: return@mapNotNull null
		val varName = parameter.name!!.asString()
		val name = annotation.getValueOrNull<String>("name")?.takeIf { it.isNotBlank() } ?: varName
		val type = parameter.type.resolve()
		val typeName = type.toTypeName().asNotNullable()
		if (type.isMarkedNullable) {
			parameter.compileCheck(typeName == TypeNames.String) {
				"${simpleName.asString()} 函数的 ${parameter.name!!.asString()} 参数可空类型只允许 String?"
			}
		}
		QueryModel(name, varName, typeName, type.isMarkedNullable)
	}
}

internal fun KSFunctionDeclaration.getPathModels(routeModel: RouteModel): List<PathModel> {
	val pathParameters = extractPathParameters(routeModel)
	val residuePathParameters = pathParameters.toMutableSet()
	val pathModels = this.parameters.mapNotNull { parameter ->
		val annotation = parameter.getKSAnnotationByType(TypeNames.Path) ?: return@mapNotNull null
		val varName = parameter.name!!.asString()
		val name = annotation.getValueOrNull<String>("name")?.takeIf { it.isNotBlank() } ?: varName
		parameter.compileCheck(name in pathParameters) {
			"${simpleName.asString()} 函数的 ${parameter.name!!.asString()} 参数未在 url 中找到"
		}
		parameter.compileCheck(name in residuePathParameters) {
			"${simpleName.asString()} 函数的 ${parameter.name!!.asString()} 参数重复解析 path 参数"
		}
		residuePathParameters -= name
		
		val typeName = parameter.type.toTypeName()
		parameter.compileCheck(!typeName.isNullable) {
			"${simpleName.asString()} 函数的 ${parameter.name!!.asString()} 参数不允许可空"
		}
		PathModel(name, varName, typeName)
	}
	this.compileCheck(residuePathParameters.isEmpty()) {
		"${simpleName.asString()} 函数未解析以下 ${residuePathParameters.size} 个 path 参数：${residuePathParameters.joinToString { it }}"
	}
	return pathModels
}

private val pathRegex = "\\{([^}]+)}".toRegex()

private fun KSFunctionDeclaration.extractPathParameters(routeModel: RouteModel): Set<String> {
	val matches = pathRegex.findAll(routeModel.path)
	val params = mutableSetOf<String>()
	for (match in matches) {
		val param = match.groupValues[1]
		routeModel.annotation.compileCheck(param !in params) {
			"${simpleName.asString()} 函数的 ${routeModel.annotation} 注解的 path 参数中不允许使用相同的 path 参数名称"
		}
		params += param
	}
	return params
}

internal fun KSFunctionDeclaration.getRequestBody(): RequestBodyModel? {
	val classNames = mapOf(
		BodyModel::class to TypeNames.Body,
		FieldModels::class to TypeNames.Field,
		PartModels::class to arrayOf(TypeNames.PartForm, TypeNames.PartFile, TypeNames.PartBinary, TypeNames.PartBinaryChannel)
	)
	val modelKClasses = classNames.mapNotNull { entity ->
		val exists = this.parameters.any { parameter ->
			when (val value = entity.value) {
				is ClassName -> parameter.hasAnnotation(value)
				is Array<*> -> value.any { parameter.hasAnnotation(it as ClassName) }
				else -> error("Unsupported type.")
			}
		}
		if (exists) entity.key else null
	}
	if (modelKClasses.isEmpty()) return null
	this.compileCheck(modelKClasses.size == 1) {
		"${simpleName.asString()} 函数参数不允许同时使用 @Body, @Field 或 @PartForm, @PartFile, @PartBinary, @PartBinaryChannel 注解"
	}
	val modelKClass = modelKClasses.single()
	return when (modelKClass) {
		BodyModel::class -> this.getBodyModel()
		FieldModels::class -> this.getFieldModels()
		else -> this.getPartModels()
	}
}

private fun KSFunctionDeclaration.getBodyModel(): BodyModel {
	val filters = this.parameters.filter { it.hasAnnotation(TypeNames.Body) }
	this.compileCheck(filters.size == 1) {
		"${simpleName.asString()} 函数参数中不允许使用多个 @Body"
	}
	val body = filters.single()
	val varName = body.name!!.asString()
	val type = body.type.resolve()
	val typeName = type.toTypeName().asNotNullable()
	return BodyModel(varName, typeName, type.isMarkedNullable)
}

private fun KSFunctionDeclaration.getFieldModels(): FieldModels {
	val parameters = this.parameters.filter { it.hasAnnotation(TypeNames.Field) }
	val fieldModels = parameters.map { parameter ->
		val varName = parameter.name!!.asString()
		val annotation = parameter.getKSAnnotationByType(TypeNames.Field)!!
		val name = annotation.getValueOrNull<String>("name")?.takeIf { it.isNotBlank() } ?: varName
		val type = parameter.type.resolve()
		val typeName = type.toTypeName().asNotNullable()
		if (type.isMarkedNullable) {
			parameter.compileCheck(typeName == TypeNames.String) {
				"${simpleName.asString()} 函数的 $varName 参数可空类型只允许 String?"
			}
		}
		FieldModel(name, varName, typeName, type.isMarkedNullable)
	}
	return FieldModels(fieldModels)
}

private fun KSFunctionDeclaration.getPartModels(): PartModels {
	// TODO
	val configs = listOf(
		PartModelConfig(
			annotation = TypeNames.PartForm,
			classNames = listOf(
				TypeNames.FormItem,
				TypeNames.String
			),
			errorMessage = { "${simpleName.asString()} 函数的 ${it.name!!.asString()} 参数只允许使用 String 和 PartData.FormItem 类型" }
		),
		PartModelConfig(
			annotation = TypeNames.PartFile,
			classNames = listOf(
				TypeNames.FileItem,
				TypeNames.ByteArray
			),
			errorMessage = { "${simpleName.asString()} 函数的 ${it.name!!.asString()} 参数只允许使用 ByteArray 和 PartData.FileItem 类型" }
		),
		PartModelConfig(
			annotation = TypeNames.PartBinary,
			classNames = listOf(
				TypeNames.BinaryItem,
				TypeNames.ByteArray
			),
			errorMessage = { "${simpleName.asString()} 函数的 ${it.name!!.asString()} 参数只允许使用 ByteArray 和 PartData.BinaryItem 类型" }
		),
		PartModelConfig(
			annotation = TypeNames.PartBinaryChannel,
			classNames = listOf(
				TypeNames.BinaryChannelItem
			),
			errorMessage = { "${simpleName.asString()} 函数的 ${it.name!!.asString()} 参数只允许使用 PartData.BinaryChannelItem 类型" }
		),
	)
	val allPartModels = configs.flatMap { getPartModels(it) }
	return PartModels(allPartModels)
}

private fun KSFunctionDeclaration.getPartModels(
	config: PartModelConfig
): List<PartModel> {
	val partForms = this.parameters.filter { it.hasAnnotation(config.annotation) }
	return partForms.map { parameter ->
		val varName = parameter.name!!.asString()
		val type = parameter.type.resolve()
		val typeName = type.toTypeName().asNotNullable()
		val annotation = parameter.getKSAnnotationByType(config.annotation)!!
		val name = annotation.getValueOrNull<String>("name")?.takeIf { it.isNotBlank() } ?: varName
		config.classNames.forEach { className ->
			if (typeName == className) {
				val isPartData = className in TypeNames.partDatas
				return@map PartModel(name, varName, config.annotation, type.isMarkedNullable, isPartData)
			}
		}
		parameter.ktorfitxCompilationError {
			config.errorMessage(parameter)
		}
	}
}

internal fun KSFunctionDeclaration.getHeaderModels(): List<HeaderModel> {
	return this.parameters.mapNotNull { parameter ->
		val annotation = parameter.getKSAnnotationByType(TypeNames.Header) ?: return@mapNotNull null
		val varName = parameter.name!!.asString()
		val name = annotation.getValueOrNull<String>("name")?.takeIf { it.isNotBlank() } ?: varName.camelToHeaderCase()
		val type = parameter.type.resolve()
		val typeName = type.toTypeName()
		parameter.compileCheck(typeName.equals(TypeNames.String, ignoreNullable = true)) {
			"${simpleName.asString()} 函数的 $varName 参数只允许使用 String 类型"
		}
		HeaderModel(name, varName, type.isMarkedNullable)
	}
}

internal fun KSFunctionDeclaration.getCookieModels(): List<CookieModel> {
	return this.parameters.mapNotNull { parameter ->
		val annotation = parameter.getKSAnnotationByType(TypeNames.Cookie) ?: return@mapNotNull null
		val type = parameter.type.resolve()
		val typeName = type.toTypeName()
		val varName = parameter.name!!.asString()
		parameter.compileCheck(typeName.equals(TypeNames.String, ignoreNullable = true)) {
			"${simpleName.asString()} 函数的 $varName 参数只允许使用 String 类型"
		}
		val name = annotation.getValueOrNull<String>("name")?.takeIf { it.isNotBlank() } ?: varName
		val encoding = annotation.getClassNameOrNull("encoding")?.simpleName?.let { simpleName ->
			when (simpleName) {
				TypeNames.CookieEncodingRaw.simpleName -> TypeNames.CookieEncodingRaw
				TypeNames.CookieEncodingDQuotes.simpleName -> TypeNames.CookieEncodingDQuotes
				TypeNames.CookieEncodingURIEncoding.simpleName -> TypeNames.CookieEncodingURIEncoding
				else -> TypeNames.CookieEncodingBase64Encoding
			}
		} ?: TypeNames.CookieEncodingURIEncoding
		CookieModel(name, varName, type.isMarkedNullable, encoding)
	}
}

internal fun KSFunctionDeclaration.getAttributeModels(): List<AttributeModel> {
	return this.parameters.mapNotNull { parameter ->
		val annotation = parameter.getKSAnnotationByType(TypeNames.Attribute) ?: return@mapNotNull null
		val type = parameter.type.resolve()
		val typeName = type.toTypeName().asNotNullable()
		val varName = parameter.name!!.asString()
		val name = annotation.getValueOrNull<String>("name")?.takeIf { it.isNotBlank() } ?: varName
		AttributeModel(name, varName, typeName, type.isMarkedNullable)
	}
}

private data class PartModelConfig(
	val annotation: ClassName,
	val classNames: List<ClassName>,
	val errorMessage: (KSValueParameter) -> String
)
package cn.ktorfitx.server.ksp.visitor

import cn.ktorfitx.common.ksp.util.check.compileCheck
import cn.ktorfitx.common.ksp.util.check.ktorfitxCompilationError
import cn.ktorfitx.common.ksp.util.expends.*
import cn.ktorfitx.common.ksp.util.message.format
import cn.ktorfitx.server.ksp.constants.TypeNames
import cn.ktorfitx.server.ksp.hint.ServerMessage
import cn.ktorfitx.server.ksp.model.*
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.ksp.toTypeName

internal fun KSFunctionDeclaration.getVarNames(): List<String> {
	return this.parameters.map { parameter ->
		val count = TypeNames.parameterAnnotations.count { parameter.hasAnnotation(it) }
		parameter.compileCheck(count > 0) {
			val annotations = TypeNames.parameterAnnotations.joinToString { "@${it.simpleName}" }
			ServerMessage.PARAMETER_MUST_USE_ONE_OF_ANNOTATIONS.format(simpleName, parameter.name!!, annotations)
		}
		parameter.compileCheck(count == 1) {
			val annotations = TypeNames.parameterAnnotations.joinToString { "@${it.simpleName}" }
			ServerMessage.PARAMETER_ONLY_USE_ONE_OF_ANNOTATIONS.format(simpleName, parameter.name!!, annotations)
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
				ServerMessage.PARAMETER_NULLABLE_ONLY_STRING.format(simpleName, parameter.name!!)
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
			ServerMessage.PARAMETER_WAS_NOT_FOUND_IN_THE_URL.format(simpleName, parameter.name!!)
		}
		parameter.compileCheck(name in residuePathParameters) {
			ServerMessage.PARAMETER_REDUNDANTLY_PARSED_AS_THE_PATH_PARAMETER.format(simpleName, parameter.name!!)
		}
		residuePathParameters -= name
		
		val typeName = parameter.type.toTypeName()
		parameter.compileCheck(!typeName.isNullable) {
			ServerMessage.PARAMETER_NOT_ALLOWED_NULLABLE.format(simpleName, parameter.name!!)
		}
		PathModel(name, varName, typeName)
	}
	this.compileCheck(residuePathParameters.isEmpty()) {
		ServerMessage.FUNCTION_FAILED_PARSE_FOLLOWING_PATH_PARAMETER.format(simpleName, residuePathParameters.joinToString())
	}
	return pathModels
}

private val pathRegex = "\\{([^}]+)}".toRegex()

private fun KSFunctionDeclaration.extractPathParameters(routeModel: RouteModel): Set<String> {
	val matches = pathRegex.findAll(routeModel.path)
	val names = mutableSetOf<String>()
	for (match in matches) {
		val name = match.groupValues[1]
		routeModel.annotation.compileCheck(name !in names) {
			ServerMessage.ANNOTATION_NOT_ALLOW_USE_SAME_PATH_PARAMETER.format(simpleName, routeModel.annotation, name)
		}
		names += name
	}
	return names
}

private val requestBodyClassNameMap by lazy {
	mapOf(
		BodyModel::class to listOf(TypeNames.Body),
		FieldModels::class to listOf(TypeNames.Field),
		PartModels::class to listOf(TypeNames.PartForm, TypeNames.PartFile, TypeNames.PartBinary, TypeNames.PartBinaryChannel)
	)
}

internal fun KSFunctionDeclaration.getRequestBody(): RequestBodyModel? {
	val modelKClasses = requestBodyClassNameMap.mapNotNull { entity ->
		val exists = this.parameters.any { parameter ->
			entity.value.any { parameter.hasAnnotation(it) }
		}
		if (exists) entity.key else null
	}
	if (modelKClasses.isEmpty()) return null
	this.compileCheck(modelKClasses.size == 1) {
		ServerMessage.FUNCTION_NOT_ALLOW_USE_BODY_FIELD_PART_ANNOTATION.format(simpleName)
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
		ServerMessage.FUNCTION_PARAMETER_NOT_ALLOW_USE_MULTIPLE_BODY.format(simpleName)
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

private val partModelConfigs by lazy {
	listOf(
		PartModelConfig(
			annotation = TypeNames.PartForm,
			supportTypeNames = listOf(
				TypeNames.FormItem,
				TypeNames.String
			),
			errorMessage = "%s 函数的 %s 参数只允许使用 String 和 PartData.FormItem 类型"
		),
		PartModelConfig(
			annotation = TypeNames.PartFile,
			supportTypeNames = listOf(
				TypeNames.FileItem,
				TypeNames.ByteArray
			),
			errorMessage = "%s 函数的 %s 参数只允许使用 ByteArray 和 PartData.FileItem 类型"
		),
		PartModelConfig(
			annotation = TypeNames.PartBinary,
			supportTypeNames = listOf(
				TypeNames.BinaryItem,
				TypeNames.ByteArray
			),
			errorMessage = "%s 函数的 %s 参数只允许使用 ByteArray 和 PartData.BinaryItem 类型"
		),
		PartModelConfig(
			annotation = TypeNames.PartBinaryChannel,
			supportTypeNames = listOf(
				TypeNames.BinaryChannelItem
			),
			errorMessage = "%s 函数的 %s 参数只允许使用 PartData.BinaryChannelItem 类型"
		),
	)
}

private fun KSFunctionDeclaration.getPartModels(): PartModels {
	val names = mutableSetOf<String>()
	return partModelConfigs.flatMap { config ->
		val partForms = this.parameters.filter { it.hasAnnotation(config.annotation) }
		partForms.map { parameter ->
			val varName = parameter.name!!.asString()
			val type = parameter.type.resolve()
			val typeName = type.toTypeName().asNotNullable()
			val annotation = parameter.getKSAnnotationByType(config.annotation)!!
			val name = annotation.getValueOrNull<String>("name")?.takeIf { it.isNotBlank() } ?: varName
			parameter.compileCheck(name !in names) {
				"${simpleName.asString()} 函数的 ${parameter.name!!.asString()} 参数重复获取了 $name 参数"
			}
			names += name
			config.supportTypeNames.forEach { className ->
				if (typeName == className) {
					val isPartData = className in TypeNames.partDatas
					return@map PartModel(name, varName, config.annotation, type.isMarkedNullable, isPartData)
				}
			}
			parameter.ktorfitxCompilationError {
				config.errorMessage.format(simpleName.asString(), parameter.name!!.asString())
			}
		}
	}.let(::PartModels)
}

private data class PartModelConfig(
	val annotation: ClassName,
	val supportTypeNames: List<ClassName>,
	val errorMessage: String
)

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
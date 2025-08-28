package cn.ktorfitx.server.ksp.visitor

import cn.ktorfitx.common.ksp.util.check.ktorfitxCheck
import cn.ktorfitx.common.ksp.util.check.ktorfitxCompilationError
import cn.ktorfitx.common.ksp.util.expends.*
import cn.ktorfitx.common.ksp.util.message.MessageConfig
import cn.ktorfitx.common.ksp.util.message.getString
import cn.ktorfitx.server.ksp.constants.TypeNames
import cn.ktorfitx.server.ksp.message.*
import cn.ktorfitx.server.ksp.model.*
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.ksp.toTypeName

internal fun KSFunctionDeclaration.getVarNames(): List<String> {
	return this.parameters.map { parameter ->
		val count = TypeNames.parameterAnnotations.count { parameter.hasAnnotation(it) }
		ktorfitxCheck(count > 0, parameter) {
			val annotations = TypeNames.parameterAnnotations.joinToString { "@${it.simpleName}" }
			MESSAGE_PARAMETER_MUST_USE_ONE_OF_ANNOTATIONS.getString(simpleName, parameter.name!!, annotations)
		}
		ktorfitxCheck(count == 1, parameter) {
			val annotations = TypeNames.parameterAnnotations.joinToString { "@${it.simpleName}" }
			MESSAGE_PARAMETER_ONLY_USE_ONE_OF_ANNOTATIONS.getString(simpleName, parameter.name!!, annotations)
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
			ktorfitxCheck(typeName == TypeNames.String, parameter) {
				MESSAGE_PARAMETER_NULLABLE_ONLY_STRING.getString(simpleName, parameter.name!!)
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
		ktorfitxCheck(name in pathParameters, parameter) {
			MESSAGE_PARAMETER_WAS_NOT_FOUND_IN_THE_URL.getString(simpleName, parameter.name!!)
		}
		ktorfitxCheck(name in residuePathParameters, parameter) {
			MESSAGE_PARAMETER_REDUNDANTLY_PARSED_AS_THE_PATH_PARAMETER.getString(simpleName, parameter.name!!)
		}
		residuePathParameters -= name
		
		val typeName = parameter.type.toTypeName()
		ktorfitxCheck(!typeName.isNullable, parameter) {
			MESSAGE_PARAMETER_NOT_ALLOWED_NULLABLE.getString(simpleName, parameter.name!!)
		}
		PathModel(name, varName, typeName)
	}
	ktorfitxCheck(residuePathParameters.isEmpty(), this) {
		MESSAGE_FUNCTION_FAILED_PARSE_FOLLOWING_PATH_PARAMETER.getString(simpleName, residuePathParameters.joinToString())
	}
	return pathModels
}

private val pathRegex = "\\{([^}]+)}".toRegex()

private fun KSFunctionDeclaration.extractPathParameters(routeModel: RouteModel): Set<String> {
	val matches = pathRegex.findAll(routeModel.path)
	val names = mutableSetOf<String>()
	for (match in matches) {
		val name = match.groupValues[1]
		ktorfitxCheck(name !in names, routeModel.annotation) {
			MESSAGE_ANNOTATION_NOT_ALLOW_USE_SAME_PATH_PARAMETER.getString(simpleName, routeModel.annotation, name)
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
	ktorfitxCheck(modelKClasses.size == 1, this) {
		MESSAGE_FUNCTION_NOT_ALLOW_USE_BODY_FIELD_PART_ANNOTATION.getString(simpleName)
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
	ktorfitxCheck(filters.size == 1, this) {
		MESSAGE_FUNCTION_PARAMETER_NOT_ALLOW_USE_MULTIPLE_BODY.getString(simpleName)
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
			ktorfitxCheck(typeName == TypeNames.String, parameter) {
				MESSAGE_PARAMETER_NULLABLE_ONLY_STRING.getString(simpleName, varName)
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
			errorMessage = MESSAGE_PARAMETER_ONLY_USE_STRING_OR_FORM_ITEM
		),
		PartModelConfig(
			annotation = TypeNames.PartFile,
			supportTypeNames = listOf(
				TypeNames.FileItem,
				TypeNames.ByteArray
			),
			errorMessage = MESSAGE_PARAMETER_ONLY_USE_BYTE_ARRAY_OR_FILE_ITEM
		),
		PartModelConfig(
			annotation = TypeNames.PartBinary,
			supportTypeNames = listOf(
				TypeNames.BinaryItem,
				TypeNames.ByteArray
			),
			errorMessage = MESSAGE_PARAMETER_ONLY_USE_BYTE_ARRAY_OR_BINARY_ITEM
		),
		PartModelConfig(
			annotation = TypeNames.PartBinaryChannel,
			supportTypeNames = listOf(
				TypeNames.BinaryChannelItem
			),
			errorMessage = MESSAGE_PARAMETER_ONLY_USE_BINARY_CHANNEL_ITEM
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
			ktorfitxCheck(name !in names, parameter) {
				MESSAGE_PARAMETER_RETRIEVED_TWICE_WITH_PART_PARAMETER.getString(simpleName, parameter.name!!, name)
			}
			names += name
			config.supportTypeNames.forEach { className ->
				if (typeName == className) {
					val isPartData = className in TypeNames.partDatas
					return@map PartModel(name, varName, config.annotation, type.isMarkedNullable, isPartData)
				}
			}
			ktorfitxCompilationError(parameter, config.errorMessage.getString(simpleName, parameter.name!!))
		}
	}.let(::PartModels)
}

private data class PartModelConfig(
	val annotation: ClassName,
	val supportTypeNames: List<ClassName>,
	val errorMessage: MessageConfig
)

internal fun KSFunctionDeclaration.getHeaderModels(): List<HeaderModel> {
	return this.parameters.mapNotNull { parameter ->
		val annotation = parameter.getKSAnnotationByType(TypeNames.Header) ?: return@mapNotNull null
		val varName = parameter.name!!.asString()
		val name = annotation.getValueOrNull<String>("name")?.takeIf { it.isNotBlank() } ?: varName.camelToHeaderCase()
		val type = parameter.type.resolve()
		val typeName = type.toTypeName()
		ktorfitxCheck(typeName.equals(TypeNames.String, ignoreNullable = true), parameter) {
			MESSAGE_PARAMETER_ONLY_USE_STRING.getString(simpleName, varName)
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
		ktorfitxCheck(typeName.equals(TypeNames.String, ignoreNullable = true), parameter) {
			MESSAGE_PARAMETER_ONLY_USE_STRING.getString(simpleName, varName)
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
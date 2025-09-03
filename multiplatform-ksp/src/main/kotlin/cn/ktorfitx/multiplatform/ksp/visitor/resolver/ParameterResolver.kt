package cn.ktorfitx.multiplatform.ksp.visitor.resolver

import cn.ktorfitx.common.ksp.util.check.ktorfitxCheck
import cn.ktorfitx.common.ksp.util.check.ktorfitxCheckNotNull
import cn.ktorfitx.common.ksp.util.expends.*
import cn.ktorfitx.common.ksp.util.message.getString
import cn.ktorfitx.multiplatform.ksp.constants.TypeNames
import cn.ktorfitx.multiplatform.ksp.message.*
import cn.ktorfitx.multiplatform.ksp.model.*
import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.Modifier
import com.squareup.kotlinpoet.ParameterizedTypeName
import com.squareup.kotlinpoet.ksp.toTypeName

internal fun KSFunctionDeclaration.getQueryModels(): List<QueryModel> {
	return this.parameters.mapNotNull { parameter ->
		val annotation = parameter.getKSAnnotationByType(TypeNames.Query) ?: return@mapNotNull null
		var name = annotation.getValueOrNull<String>("name")
		val varName = parameter.name!!.asString()
		if (name.isNullOrBlank()) {
			name = varName
		}
		QueryModel(name, varName)
	}
}

internal fun KSFunctionDeclaration.getQueriesModels(): List<QueriesModel> {
	return this.parameters.mapNotNull { parameter ->
		if (!parameter.hasAnnotation(TypeNames.Queries)) return@mapNotNull null
		val name = parameter.name!!.asString()
		val type = parameter.type.resolve()
		ktorfitxCheck(type.isMapOfStringToAny() || type.isListOfStringPair(), parameter) {
			MESSAGE_PARAMETER_ONLY_ALLOW_UES_SUPPORTED_BY_QUERIES.getString(simpleName, name)
		}
		QueriesModel(name)
	}
}

internal fun KSFunctionDeclaration.getCookieModels(): List<CookieModel> {
	return this.parameters.mapNotNull { parameter ->
		val annotation = parameter.getKSAnnotationByType(TypeNames.Cookie) ?: return@mapNotNull null
		val varName = parameter.name!!.asString()
		val typeName = parameter.type.toTypeName()
		ktorfitxCheck(!typeName.isNullable, this) {
			MESSAGE_PARAMETER_NOT_ALLOW_USE_NULLABLE_TYPE.getString(simpleName, varName)
		}
		ktorfitxCheck(typeName == TypeNames.String, this) {
			MESSAGE_PARAMETER_MUST_USE_STRING_TYPE.getString(simpleName, varName)
		}
		val name = annotation.getValueOrNull<String>("name")?.takeIf { it.isNotBlank() } ?: varName
		val maxAge = annotation.getValueOrNull<Int>("maxAge")?.takeIf { it >= 0 }
		val expires = annotation.getValueOrNull<Long>("expires")?.takeIf { it >= 0L }
		val domain = annotation.getValueOrNull<String>("domain")?.takeIf { it.isNotBlank() }
		val path = annotation.getValueOrNull<String>("path")?.takeIf { it.isNotBlank() }
		val secure = annotation.getValueOrNull<Boolean>("secure")
		val httpOnly = annotation.getValueOrNull<Boolean>("httpOnly")
		val extensions = annotation.getValuesOrNull<String>("extensions")
			?.associate { entry ->
				ktorfitxCheckNotNull(entry.parseHeader(), parameter) {
					MESSAGE_PARAMETER_COOKIE_FORMAT_IS_INCORRECT.getString(simpleName, varName)
				}
			}?.takeIf { it.isNotEmpty() }
		CookieModel(varName, name, maxAge, expires, domain, path, secure, httpOnly, extensions)
	}
}

internal fun KSFunctionDeclaration.getAttributeModels(): List<AttributeModel> {
	return this.parameters.mapNotNull { parameter ->
		val annotation = parameter.getKSAnnotationByType(TypeNames.Attribute) ?: return@mapNotNull null
		val varName = parameter.name!!.asString()
		val name = annotation.getValueOrNull<String>("name")?.takeIf { it.isNotBlank() } ?: varName
		val type = parameter.type.resolve()
		ktorfitxCheck(!type.isMarkedNullable, parameter) {
			MESSAGE_PARAMETER_NOT_ALLOW_USE_NULLABLE_TYPE.getString(simpleName, varName)
		}
		AttributeModel(name, varName)
	}
}

internal fun KSFunctionDeclaration.getAttributesModels(): List<AttributesModel> {
	return this.parameters.mapNotNull { parameter ->
		if (!parameter.hasAnnotation(TypeNames.Attributes)) return@mapNotNull null
		val varName = parameter.name!!.asString()
		val type = parameter.type.resolve()
		val kind = when {
			type.isMapOfStringToAny(false) -> AttributesKind.MAP
			type.isListOfStringPair(false) -> AttributesKind.LIST
			else -> null
		}
		ktorfitxCheckNotNull(kind, parameter) {
			MESSAGE_PARAMETER_ONLY_ALLOW_UES_SUPPORTED_BY_ATTRIBUTES.getString(simpleName, varName)
		}
		AttributesModel(varName, type.isMarkedNullable, kind)
	}
}

internal fun KSFunctionDeclaration.hasBearerAuth(): Boolean {
	return hasAnnotation(TypeNames.BearerAuth)
}

internal fun KSFunctionDeclaration.getHeaderModels(): List<HeaderModel> {
	return this.parameters.mapNotNull { parameter ->
		val annotation = parameter.getKSAnnotationByType(TypeNames.Header) ?: return@mapNotNull null
		var name = annotation.getValueOrNull<String>("name")
		val varName = parameter.name!!.asString()
		if (name.isNullOrBlank()) {
			name = varName.camelToHeaderCase()
		}
		HeaderModel(name, varName)
	}
}

internal fun KSFunctionDeclaration.getHeadersModel(): HeadersModel? {
	val annotation = getKSAnnotationByType(TypeNames.Headers) ?: return null
	val headers = annotation.getValuesOrNull<String>("headers") ?: return null
	val headerMap = headers.associate {
		ktorfitxCheckNotNull(it.parseHeader(), annotation) {
			MESSAGE_FUNCTION_HEADERS_FORMAT_IS_INCORRECT.getString(simpleName)
		}
	}
	return HeadersModel(headerMap)
}

internal fun KSFunctionDeclaration.getMockModel(isWebSocket: Boolean): MockModel? {
	val annotation = getKSAnnotationByType(TypeNames.Mock) ?: return null
	ktorfitxCheck(!isWebSocket, this) {
		MESSAGE_FUNCTION_NOT_ALLOW_SIMULTANEOUS_USE_MOCK_AND_WEBSOCKET_ANNOTATIONS.getString(simpleName)
	}
	val providerClassName = annotation.getClassName("provider")
	val delay = annotation.getValueOrNull("delay") ?: 0L
	
	ktorfitxCheck(providerClassName != TypeNames.MockProvider, annotation) {
		MESSAGE_FUNCTION_MUST_USE_MOCK_PROVIDER_DERIVED_CLASS.getString(simpleName)
	}
	val providerClassDeclaration = annotation.getArgumentKSClassDeclaration("provider")
	val classKind = providerClassDeclaration.classKind
	ktorfitxCheck(classKind == ClassKind.OBJECT, providerClassDeclaration) {
		MESSAGE_CLASS_MUST_USE_OBJECT_TYPE.getString(providerClassName.simpleName)
	}
	ktorfitxCheck(Modifier.PRIVATE !in providerClassDeclaration.modifiers, providerClassDeclaration) {
		MESSAGE_CLASS_NOT_ALLOW_USE_PRIVATE_ACCESS_MODIFIER.getString(providerClassName.simpleName)
	}
	
	val mockReturnType = providerClassDeclaration.superTypes
		.map { it.resolve() }
		.firstNotNullOfOrNull { superType ->
			if (superType.toTypeName().rawType == TypeNames.MockProvider) {
				superType.arguments.firstOrNull()?.type?.toTypeName()
			} else null
		}
	
	ktorfitxCheckNotNull(mockReturnType, providerClassDeclaration) {
		MESSAGE_CLASS_MUST_IMPLEMENT_MOCK_PROVIDER_INTERFACE.getString(providerClassName.simpleName)
	}
	val returnType = this.returnType!!.toTypeName().let {
		if (it.rawType == TypeNames.Result) {
			it as ParameterizedTypeName
			it.typeArguments.first()
		} else it
	}
	ktorfitxCheck(returnType == mockReturnType, this) {
		MESSAGE_FUNCTION_USE_MOCK_PROVIDER_IMPLEMENTATION_CLASS_THAT_IS_INCOMPATIBLE_WITH_RETURN_TYPE.getString(simpleName, returnType)
	}
	ktorfitxCheck(delay >= 0L, annotation) {
		MESSAGE_PARAMETER_DELAY_MUST_BE_GREATER_THAN_OR_EQUAL_TO_ZERO.getString(simpleName)
	}
	return MockModel(providerClassName, delay)
}

internal fun KSFunctionDeclaration.getParameterModels(isWebSocket: Boolean): List<ParameterModel> {
	ktorfitxCheck(!this.isGeneric(), this) {
		MESSAGE_FUNCTION_NOT_ALLOWED_TO_CONTAIN_GENERICS.getString(simpleName)
	}
	return if (isWebSocket) {
		val errorMessage = {
			MESSAGE_FUNCTION_ONLY_ACCEPTS_ONE_PARAMETER_AND_TYPE_IS_SUPPORTED_BY_WEB_SOCKET.getString(simpleName)
		}
		ktorfitxCheck(this.parameters.size == 1, this, errorMessage)
		val valueParameter = this.parameters.first()
		val typeName = valueParameter.type.toTypeName()
		ktorfitxCheck(
			typeName == TypeNames.WebSocketSessionHandler || typeName == TypeNames.DefaultClientWebSocketSessionLambda,
			this, errorMessage
		)
		val varName = valueParameter.name!!.asString()
		return listOf(ParameterModel(varName, typeName))
	} else {
		this.parameters.map { parameter ->
			val varName = parameter.name!!.asString()
			val count = TypeNames.parameters.count {
				parameter.hasAnnotation(it)
			}
			ktorfitxCheck(count > 0, this) {
				MESSAGE_PARAMETER_NOT_USE_ANY_FUNCTIONAL_ANNOTATIONS.getString(simpleName, varName)
			}
			ktorfitxCheck(count == 1, this) {
				val useAnnotations = this.annotations.joinToString()
				MESSAGE_PARAMETER_NOT_ALLOW_USE_MORE_THAN_ONE_FUNCTIONALITY_ANNOTATION_AT_SAME_TIME.getString(simpleName, varName, useAnnotations)
			}
			ktorfitxCheck(varName.isLowerCamelCase(), this) {
				val varNameSuggestion = varName.toLowerCamelCase()
				MESSAGE_PARAMETER_NOT_FOLLOW_LOWERCASE_CAMEL_CASE_NAMING_CONVENTION.getString(simpleName, varName, varNameSuggestion)
			}
			val typeName = parameter.type.toTypeName()
			ParameterModel(varName, typeName)
		}
	}
}

internal fun KSFunctionDeclaration.getPathModels(
	url: Url,
	isWebSocket: Boolean
): List<PathModel> {
	return when (url) {
		is DynamicUrl -> {
			this.parameters.mapNotNull { parameter ->
				val annotation = parameter.getKSAnnotationByType(TypeNames.Path) ?: return@mapNotNull null
				val varName = parameter.name!!.asString()
				val name = annotation.getValueOrNull<String>("name")?.takeIf { it.isNotBlank() } ?: varName
				val typeName = parameter.type.toTypeName()
				ktorfitxCheck(!typeName.isNullable, parameter) {
					MESSAGE_PARAMETER_NOT_ALLOW_USE_NULLABLE_TYPE.getString(simpleName, parameter.name!!)
				}
				PathModel(name, varName)
			}
		}
		
		is StaticUrl -> {
			val pathParameters = extractUrlPathParameters(url.url)
			if (isWebSocket) {
				ktorfitxCheck(pathParameters.isEmpty(), this) {
					MESSAGE_FUNCTION_NOT_ALLOW_USE_PATH_PARAMETER.getString(simpleName)
				}
			}
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
					MESSAGE_PARAMETER_NOT_ALLOW_USE_NULLABLE_TYPE.getString(simpleName, parameter.name!!)
				}
				PathModel(name, varName)
			}
			ktorfitxCheck(residuePathParameters.isEmpty(), this) {
				MESSAGE_FUNCTION_FAILED_PARSE_FOLLOWING_PATH_PARAMETER.getString(simpleName, residuePathParameters.joinToString())
			}
			pathModels
		}
	}
}

private val pathRegex = "\\{([^}]+)}".toRegex()

private fun extractUrlPathParameters(url: String): Set<String> {
	val matches = pathRegex.findAll(url)
	val params = mutableSetOf<String>()
	for (match in matches) {
		params += match.groupValues[1]
	}
	return params
}


internal fun KSFunctionDeclaration.getTimeoutModel(): TimeoutModel? {
	val annotation = this.getKSAnnotationByType(TypeNames.Timeout) ?: return null
	val requestTimeoutMillis = annotation.getValueOrNull<Long>("requestTimeoutMillis")?.takeIf { it >= 0L }
	val connectTimeoutMillis = annotation.getValueOrNull<Long>("connectTimeoutMillis")?.takeIf { it >= 0L }
	val socketTimeoutMillis = annotation.getValueOrNull<Long>("socketTimeoutMillis")?.takeIf { it >= 0L }
	if (requestTimeoutMillis == null && connectTimeoutMillis == null && socketTimeoutMillis == null) return null
	return TimeoutModel(requestTimeoutMillis, connectTimeoutMillis, socketTimeoutMillis)
}

internal fun KSFunctionDeclaration.getDynamicUrl(): DynamicUrl? {
	val annotations = this.parameters.filter { it.hasAnnotation(TypeNames.DynamicUrl) }
	if (annotations.isEmpty()) return null
	ktorfitxCheck(annotations.size == 1, this) {
		MESSAGE_FUNCTION_NOT_ALLOW_USE_ONE_PARAMETER_MARKED_DYNAMIC_URL_ANNOTATION.getString(simpleName)
	}
	val annotation = annotations.first()
	val typeName = annotation.type.toTypeName()
	val varName = annotation.name!!.asString()
	ktorfitxCheck(typeName == TypeNames.String, annotation) {
		MESSAGE_PARAMETER_ONLY_USE_STRING.getString(simpleName, varName)
	}
	return DynamicUrl(varName)
}
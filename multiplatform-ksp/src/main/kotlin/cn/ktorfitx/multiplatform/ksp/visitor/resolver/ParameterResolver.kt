package cn.ktorfitx.multiplatform.ksp.visitor.resolver

import cn.ktorfitx.common.ksp.util.check.ktorfitxCheck
import cn.ktorfitx.common.ksp.util.check.ktorfitxCheckNotNull
import cn.ktorfitx.common.ksp.util.expends.*
import cn.ktorfitx.multiplatform.ksp.constants.TypeNames
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
			"${simpleName.asString()} 函数的 $name 参数只允许使用 Map<String, *> 或 List<Pair<String, *>> 类型或是它的具体化子类型或派生类型"
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
			"${simpleName.asString()} 函数的 $varName 参数不允许为可空类型"
		}
		ktorfitxCheck(typeName == TypeNames.String, this) {
			"${simpleName.asString()} 函数的 $varName 参数只允许为 String 类型"
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
					"${simpleName.asString()} 函数的 $varName 参数的 @Cookie 注解上 extensions 参数格式错误，需要以 <key>:<value> 格式"
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
			"${simpleName.asString()} 函数的 $varName 参数不允许使用可空类型"
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
			"${simpleName.asString()} 函数的 $varName 参数只允许使用 Map<String, Any> 或 List<Pair<String, Any>> 类型或是它的具体化子类型或派生类型"
		}
		AttributesModel(varName, type.isMarkedNullable, kind)
	}
}

internal fun KSFunctionDeclaration.hasBearerAuth(): Boolean {
	return hasAnnotation(TypeNames.BearerAuth)
}

internal fun KSFunctionDeclaration.isPrepareType(
	isWebSocket: Boolean,
	isMock: Boolean
): Boolean {
	val isPrepareType = hasAnnotation(TypeNames.Prepare)
	if (isPrepareType) {
		val returnType = this.returnType!!.toTypeName()
		ktorfitxCheck(returnType == TypeNames.HttpStatement, this) {
			"${simpleName.asString()} 函数必须使用 ${TypeNames.HttpStatement.canonicalName} 返回类型"
		}
		ktorfitxCheck(!isMock, this) {
			"${simpleName.asString()} 函数不允许同时用 @Prepare 和 @Mock 注解，因为不支持此操作"
		}
		ktorfitxCheck(!isWebSocket, this) {
			"${simpleName.asString()} 函数不允许同时用 @Prepare 和 @WebSocket 注解，因为不支持此操作"
		}
	}
	return isPrepareType
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
			"${simpleName.asString()} 函数的 @Headers 注解上的参数格式有错误，需要以 <key>:<value> 格式"
		}
	}
	return HeadersModel(headerMap)
}

internal fun KSFunctionDeclaration.getMockModel(isWebSocket: Boolean): MockModel? {
	val annotation = getKSAnnotationByType(TypeNames.Mock) ?: return null
	ktorfitxCheck(!isWebSocket, this) {
		"${simpleName.asString()} 函数不支持同时使用 @Mock 和 @WebSocket 注解"
	}
	val className = annotation.getClassName("provider")
	val delay = annotation.getValueOrNull("delay") ?: 0L
	
	ktorfitxCheck(className != TypeNames.MockProvider, annotation) {
		"${simpleName.asString()} 函数上的 @Mock 注解的 provider 参数不允许使用 MockProvider::class"
	}
	val classDeclaration = annotation.getArgumentKSClassDeclaration("provider")
	val classKind = classDeclaration.classKind
	ktorfitxCheck(classKind == ClassKind.OBJECT, classDeclaration) {
		"${className.simpleName} 类不允许使用 ${classKind.code} 类型，请使用 object 类型"
	}
	ktorfitxCheck(Modifier.PRIVATE !in classDeclaration.modifiers, classDeclaration) {
		"${className.simpleName} 类不允许使用 private 访问权限"
	}
	
	val mockReturnType = classDeclaration.superTypes
		.map { it.resolve() }
		.find { it.toTypeName().rawType == TypeNames.MockProvider }
		?.arguments
		?.firstOrNull()
		?.type
		?.toTypeName()
	ktorfitxCheckNotNull(mockReturnType, classDeclaration) {
		"${className.simpleName} 类必须实现 MockProvider<T> 接口"
	}
	val returnType = this.returnType!!.toTypeName().let {
		if (it.rawType == TypeNames.Result) {
			it as ParameterizedTypeName
			it.typeArguments.first()
		} else it
	}
	ktorfitxCheck(returnType == mockReturnType, this) {
		"${simpleName.asString()} 函数的 provider 类型与返回值不一致，应该为 $returnType, 实际为 $mockReturnType"
	}
	ktorfitxCheck(delay >= 0L, annotation) {
		val funName = simpleName.asString()
		"$funName 的注解的 delay 参数的值必须不小于 0L"
	}
	return MockModel(className, delay)
}

internal fun KSFunctionDeclaration.getParameterModels(isWebSocket: Boolean): List<ParameterModel> {
	ktorfitxCheck(!this.isGeneric(), this) {
		"${simpleName.asString()} 函数不允许包含泛型"
	}
	return if (isWebSocket) {
		val errorMessage = {
			"${simpleName.asString()} 函数只允许一个参数，且类型为 WebSocketSessionHandler 别名 或 suspend DefaultClientWebSocketSession.() -> Unit"
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
				"${simpleName.asString()} 函数上的 $varName 参数未使用任何功能注解"
			}
			ktorfitxCheck(count == 1, this) {
				val useAnnotations = this.annotations.joinToString()
				"${simpleName.asString()} 函数上的 $varName 参数不允许同时使用 $useAnnotations 多个功能注解"
			}
			ktorfitxCheck(varName.isLowerCamelCase(), this) {
				val varNameSuggestion = varName.toLowerCamelCase()
				"${simpleName.asString()} 函数上的 $varName 参数不符合小驼峰命名规则，建议修改为 $varNameSuggestion"
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
					"${simpleName.asString()} 函数的 ${parameter.name!!.asString()} 参数不允许可空"
				}
				PathModel(name, varName)
			}
		}
		
		is StaticUrl -> {
			val pathParameters = extractUrlPathParameters(url.url)
			if (isWebSocket) {
				ktorfitxCheck(pathParameters.isEmpty(), this) {
					"${simpleName.asString()} 函数不支持使用 path 参数"
				}
			}
			val residuePathParameters = pathParameters.toMutableSet()
			val pathModels = this.parameters.mapNotNull { parameter ->
				val annotation = parameter.getKSAnnotationByType(TypeNames.Path) ?: return@mapNotNull null
				val varName = parameter.name!!.asString()
				val name = annotation.getValueOrNull<String>("name")?.takeIf { it.isNotBlank() } ?: varName
				ktorfitxCheck(name in pathParameters, parameter) {
					"${simpleName.asString()} 函数的 ${parameter.name!!.asString()} 参数未在 url 中找到"
				}
				ktorfitxCheck(name in residuePathParameters, parameter) {
					"${simpleName.asString()} 函数的 ${parameter.name!!.asString()} 参数重复解析 path 参数"
				}
				residuePathParameters -= name
				val typeName = parameter.type.toTypeName()
				ktorfitxCheck(!typeName.isNullable, parameter) {
					"${simpleName.asString()} 函数的 ${parameter.name!!.asString()} 参数不允许可空"
				}
				PathModel(name, varName)
			}
			ktorfitxCheck(residuePathParameters.isEmpty(), this) {
				"${simpleName.asString()} 函数未解析以下 ${residuePathParameters.size} 个 path 参数：${residuePathParameters.joinToString { it }}"
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
		"${simpleName.asString()} 函数只允许使用一个 @DynamicUrl 参数来动态设置 url 参数"
	}
	val annotation = annotations.first()
	val typeName = annotation.type.toTypeName()
	val varName = annotation.name!!.asString()
	ktorfitxCheck(typeName == TypeNames.String, annotation) {
		"${simpleName.asString()} 函数的 $varName 参数只允许使用 String 类型"
	}
	return DynamicUrl(varName)
}
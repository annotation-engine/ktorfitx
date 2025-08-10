package cn.ktorfitx.multiplatform.ksp.visitor

import cn.ktorfitx.common.ksp.util.check.compileCheck
import cn.ktorfitx.common.ksp.util.expends.*
import cn.ktorfitx.multiplatform.ksp.constants.TypeNames
import cn.ktorfitx.multiplatform.ksp.model.*
import cn.ktorfitx.multiplatform.ksp.model.FieldsKind.LIST
import cn.ktorfitx.multiplatform.ksp.model.FieldsKind.MAP
import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.Modifier
import com.squareup.kotlinpoet.ClassName
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
		parameter.compileCheck(type.isMapOfStringToAny() || type.isListOfStringPair()) {
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
		compileCheck(!typeName.isNullable) {
			"${simpleName.asString()} 函数的 $varName 参数不允许为可空类型"
		}
		compileCheck(typeName == TypeNames.String) {
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
				val parts = entry.split(":")
				parameter.compileCheck(parts.size == 2) {
					"${simpleName.asString()} 函数的 $varName 参数的 @Cookie 注解上 extensions 参数格式错误，应该为 <key>:<value> 形式"
				}
				val key = parts[0].trim()
				val value = parts[1].takeIf { it.isNotBlank() }
				parameter.compileCheck(key.isNotBlank()) {
					"${simpleName.asString()} 函数的 $varName 参数的 @Cookie 注解上 extensions 参数格式错误，key 不能为空"
				}
				key to value
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
		parameter.compileCheck(!type.isMarkedNullable) {
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
		parameter.compileCheck(type.isMapOfStringToAny(false) || type.isListOfStringPair(false)) {
			"${simpleName.asString()} 函数的 $varName 参数只允许使用 Map<String, Any> 或 List<Pair<String, Any>> 类型或是它的具体化子类型或派生类型"
		}
		AttributesModel(varName)
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
		this.compileCheck(returnType == TypeNames.HttpStatement) {
			"${simpleName.asString()} 函数必须使用 ${TypeNames.HttpStatement.canonicalName} 返回类型"
		}
		this.compileCheck(!isMock) {
			"${simpleName.asString()} 函数不允许同时用 @Prepare 和 @Mock 注解，因为不支持此操作"
		}
		this.compileCheck(!isWebSocket) {
			"${simpleName.asString()} 函数不允许同时用 @Prepare 和 @WebSocket 注解，因为不支持此操作"
		}
	}
	return isPrepareType
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
			LIST -> (typeName.typeArguments.first() as ParameterizedTypeName).typeArguments[1]
			MAP -> typeName.typeArguments[1]
		}
		FieldsModel(name, fieldsKind, valueTypeName.equals(TypeNames.String, ignoreNullable = true), valueTypeName.isNullable)
	}
	return FieldModels(fieldModels, fieldsModels)
}

private fun KSFunctionDeclaration.getPartModels(): PartModels {
	val partModels = this.parameters.mapNotNull { parameter ->
		val annotation = parameter.getKSAnnotationByType(TypeNames.Part) ?: return@mapNotNull null
		val varName = parameter.name!!.asString()
		var name = annotation.getValueOrNull<String>("name")
		if (name.isNullOrBlank()) {
			name = varName
		}
		PartModel(name, varName)
	}
	return PartModels(partModels)
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

private val headersRegex = "^([^:=]+)[:=]([^:=]+)$".toRegex()

internal fun KSFunctionDeclaration.getHeadersModel(): HeadersModel? {
	val annotation = getKSAnnotationByType(TypeNames.Headers) ?: return null
	val headers = annotation.getValuesOrNull<String>("headers") ?: return null
	return headers.associate {
		val (name, value) = headersRegex.matchEntire(it)?.destructured
			?: error("${qualifiedName!!.asString()} 函数的 @Headers 格式错误")
		name.trim() to value.trim()
	}.let { HeadersModel(it) }
}

internal fun KSFunctionDeclaration.getMockModel(isWebSocket: Boolean): MockModel? {
	val annotation = getKSAnnotationByType(TypeNames.Mock) ?: return null
	this.compileCheck(!isWebSocket) {
		"${simpleName.asString()} 函数不支持同时使用 @Mock 和 @WebSocket 注解"
	}
	val className = annotation.getClassName("provider")
	val delay = annotation.getValueOrNull("delay") ?: 0L
	
	annotation.compileCheck(className != TypeNames.MockProvider) {
		"${simpleName.asString()} 函数上的 @Mock 注解的 provider 参数不允许使用 MockProvider::class"
	}
	val classDeclaration = annotation.getArgumentKSClassDeclaration("provider")!!
	val classKind = classDeclaration.classKind
	classDeclaration.compileCheck(classKind == ClassKind.OBJECT) {
		"${className.simpleName} 类不允许使用 ${classKind.code} 类型，请使用 object 类型"
	}
	classDeclaration.compileCheck(Modifier.PRIVATE !in classDeclaration.modifiers) {
		"${className.simpleName} 类不允许使用 private 访问权限"
	}
	
	val mockReturnType = classDeclaration.superTypes
		.map { it.resolve() }
		.find { it.toTypeName().rawType == TypeNames.MockProvider }
		?.arguments
		?.firstOrNull()
		?.type
		?.toTypeName()
	classDeclaration.compileCheck(mockReturnType != null) {
		"${className.simpleName} 类必须实现 MockProvider<T> 接口"
	}
	val returnType = this.returnType!!.toTypeName().let {
		when (it.rawType) {
			TypeNames.Result -> (it as ParameterizedTypeName).typeArguments.first()
			else -> it
		}
	}
	this.compileCheck(returnType == mockReturnType) {
		"${simpleName.asString()} 函数的 provider 类型与返回值不一致，应该为 $returnType, 实际为 $mockReturnType"
	}
	annotation.compileCheck(delay >= 0L) {
		val funName = simpleName.asString()
		"$funName 的注解的 delay 参数的值必须不小于 0L"
	}
	return MockModel(className, delay)
}

internal fun KSFunctionDeclaration.getParameterModels(isWebSocket: Boolean): List<ParameterModel> {
	this.compileCheck(!(this.isGeneric())) {
		"${simpleName.asString()} 函数不允许包含泛型"
	}
	return if (isWebSocket) {
		val errorMessage = {
			"${simpleName.asString()} 函数只允许一个参数，且类型为 WebSocketSessionHandler 别名 或 suspend DefaultClientWebSocketSession.() -> Unit"
		}
		this.compileCheck(
			value = this.parameters.size == 1,
			errorMessage = errorMessage
		)
		val valueParameter = this.parameters.first()
		val typeName = valueParameter.type.toTypeName()
		this.compileCheck(
			value = typeName == TypeNames.WebSocketSessionHandler || typeName == TypeNames.DefaultClientWebSocketSessionLambda,
			errorMessage = errorMessage
		)
		val varName = valueParameter.name!!.asString()
		return listOf(ParameterModel(varName, typeName))
	} else {
		this.parameters.map { parameter ->
			val varName = parameter.name!!.asString()
			val count = TypeNames.parameters.count {
				parameter.hasAnnotation(it)
			}
			this.compileCheck(count > 0) {
				"${simpleName.asString()} 函数上的 $varName 参数未使用任何功能注解"
			}
			this.compileCheck(count == 1) {
				val useAnnotations = this.annotations.joinToString()
				"${simpleName.asString()} 函数上的 $varName 参数不允许同时使用 $useAnnotations 多个功能注解"
			}
			this.compileCheck(varName.isLowerCamelCase()) {
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
				parameter.compileCheck(!typeName.isNullable) {
					"${simpleName.asString()} 函数的 ${parameter.name!!.asString()} 参数不允许可空"
				}
				PathModel(name, varName)
			}
		}
		
		is StaticUrl -> {
			val pathParameters = extractUrlPathParameters(url.url)
			if (isWebSocket) {
				this.compileCheck(pathParameters.isEmpty()) {
					"${simpleName.asString()} 函数不支持使用 path 参数"
				}
			}
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
				PathModel(name, varName)
			}
			this.compileCheck(residuePathParameters.isEmpty()) {
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
	this.compileCheck(annotations.size == 1) {
		"${simpleName.asString()} 函数只允许使用一个 @DynamicUrl 参数来动态设置 url 参数"
	}
	val annotation = annotations.first()
	val typeName = annotation.type.toTypeName()
	val varName = annotation.name!!.asString()
	annotation.compileCheck(typeName == TypeNames.String) {
		"${simpleName.asString()} 函数的 $varName 参数只允许使用 String 类型"
	}
	return DynamicUrl(varName)
}
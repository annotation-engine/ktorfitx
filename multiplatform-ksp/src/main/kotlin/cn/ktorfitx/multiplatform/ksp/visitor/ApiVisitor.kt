package cn.ktorfitx.multiplatform.ksp.visitor

import cn.ktorfitx.common.ksp.util.check.compileCheck
import cn.ktorfitx.common.ksp.util.expends.*
import cn.ktorfitx.common.ksp.util.message.format
import cn.ktorfitx.multiplatform.ksp.constants.TypeNames
import cn.ktorfitx.multiplatform.ksp.message.MultiplatformMessage
import cn.ktorfitx.multiplatform.ksp.model.*
import cn.ktorfitx.multiplatform.ksp.visitor.resolver.*
import com.google.devtools.ksp.getDeclaredFunctions
import com.google.devtools.ksp.getVisibility
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSNode
import com.google.devtools.ksp.symbol.Modifier
import com.google.devtools.ksp.symbol.Visibility.INTERNAL
import com.google.devtools.ksp.symbol.Visibility.PUBLIC
import com.google.devtools.ksp.visitor.KSEmptyVisitor
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterizedTypeName
import com.squareup.kotlinpoet.ksp.toClassName
import com.squareup.kotlinpoet.ksp.toTypeName

internal object ApiVisitor : KSEmptyVisitor<List<CustomHttpMethodModel>, ClassModel>() {
	
	private val apiUrlRegex = "^\\S*[a-zA-Z0-9]+\\S*$".toRegex()
	
	private lateinit var customHttpMethodModels: List<CustomHttpMethodModel>
	
	override fun visitClassDeclaration(
		classDeclaration: KSClassDeclaration,
		data: List<CustomHttpMethodModel>
	): ClassModel {
		this.customHttpMethodModels = data
		return classDeclaration.getClassModel()
	}
	
	private fun KSClassDeclaration.getClassModel(): ClassModel {
		this.compileCheck(!(this.isGeneric())) {
			MultiplatformMessage.INTERFACE_NOT_ALLOW_GENERICS.format(simpleName)
		}
		val className = ClassName("${packageName.asString()}.impls", "${simpleName.asString()}Impl")
		val superinterface = this.toClassName()
		return ClassModel(
			className = className,
			superinterface = superinterface,
			kModifier = this.getVisibilityKModifier(),
			apiUrl = this.getApiUrl(),
			apiScopeModels = this.getApiScopeModels(),
			funModels = getFunModel()
		)
	}
	
	private fun KSClassDeclaration.getApiUrl(): String? {
		val annotation = getKSAnnotationByType(TypeNames.Api)!!
		var url = annotation.getValueOrNull<String>("url")?.takeIf { it.isNotBlank() } ?: return null
		annotation.compileCheck(!url.containsSchemeSeparator()) {
			MultiplatformMessage.ANNOTATION_NOT_ALLOW_USE_PROTOCOL_FROM_STRINGS.format(simpleName)
		}
		url = url.trim().trim('/')
		annotation.compileCheck(apiUrlRegex.matches(url)) {
			MultiplatformMessage.ANNOTATION_URL_PARAMETER_FORMAT_INCORRECT.format(simpleName)
		}
		return url
	}
	
	private fun KSClassDeclaration.getApiScopeModels(): List<ApiScopeModel> {
		val apiScopeAnnotation = getKSAnnotationByType(TypeNames.ApiScope) ?: return listOf(ApiScopeModel(TypeNames.DefaultApiScope))
		val apiScopeClassNames = apiScopeAnnotation.getClassNamesOrNull("scopes")?.takeIf { it.isNotEmpty() }
		apiScopeAnnotation.compileCheck(apiScopeClassNames != null) {
			MultiplatformMessage.ANNOTATION_SCOPES_PARAMETER_NOT_ALLOW_NULLABLE.format(simpleName)
		}
		val groupSize = apiScopeClassNames.groupBy { it.simpleNames.joinToString(".") }.size
		this.compileCheck(apiScopeClassNames.size == groupSize) {
			MultiplatformMessage.ANNOTATION_SCOPES_NOT_ALLOWED_USE_SAME_CLASS_NAME_K_CLASS.format(simpleName)
		}
		return apiScopeClassNames.map { ApiScopeModel(it) }
	}
	
	/**
	 * 获取访问权限的 KModifier
	 */
	private fun KSClassDeclaration.getVisibilityKModifier(): KModifier {
		val visibility = this.getVisibility()
		this.compileCheck(visibility == PUBLIC || visibility == INTERNAL) {
			MultiplatformMessage.INTERFACE_MUST_BE_DECLARED_PUBLIC_OR_INTERNAL_ACCESS_PERMISSION.format(simpleName)
		}
		return KModifier.entries.first { it.name == visibility.name }
	}
	
	private fun KSClassDeclaration.getFunModel(): List<FunModel> {
		return this.getDeclaredFunctions().toList()
			.filter { it.isAbstract }
			.map { function ->
				function.compileCheck(Modifier.SUSPEND in function.modifiers) {
					MultiplatformMessage.FUNCTION_LACKS_SUSPEND_MODIFIER.format(function.simpleName)
				}
				val routeModel = function.getRouteModel()
				val isWebSocket = routeModel is WebSocketModel
				val mockModel = function.getMockModel(isWebSocket)
				FunModel(
					funName = function.simpleName.asString(),
					returnModel = function.getReturnModel(isWebSocket),
					parameterModels = function.getParameterModels(isWebSocket),
					routeModel = routeModel,
					mockModel = mockModel,
					hasBearerAuth = function.hasBearerAuth(),
					isPrepareType = function.isPrepareType(isWebSocket, mockModel != null),
					timeoutModel = function.getTimeoutModel(),
					queryModels = function.getQueryModels(),
					pathModels = function.getPathModels(routeModel.url, isWebSocket),
					cookieModels = function.getCookieModels(),
					attributeModels = function.getAttributeModels(),
					headerModels = function.getHeaderModels(),
					headersModel = function.getHeadersModel(),
					requestBodyModel = function.getRequestBodyModel(),
					queriesModels = function.getQueriesModels(),
					attributesModels = function.getAttributesModels(),
				)
			}
	}
	
	private val urlRegex = "^\\S*[a-zA-Z0-9]+\\S*$".toRegex()
	
	private fun KSFunctionDeclaration.getRouteModel(): RouteModel {
		val customClassNames = customHttpMethodModels.map { it.className }
		val availableRoutes = TypeNames.routes + customClassNames
		val classNames = availableRoutes.filter { hasAnnotation(it) }
		this.compileCheck(classNames.size <= 1) {
			val useAnnotations = classNames.joinToString { "@${it.simpleName}" }
			val useSize = classNames.size
			"${simpleName.asString()} 函数只允许使用一种类型注解，而您使用了 $useAnnotations $useSize 个"
		}
		this.compileCheck(classNames.size == 1) {
			val routes = TypeNames.routes.joinToString { "@${it.simpleName}" }
			if (customClassNames.isEmpty()) {
				"函数 ${simpleName.asString()} 未添加路由注解，请选择：\n内置注解：$routes\n自定义注解：无"
			} else {
				val availableRoutes = customClassNames.joinToString { "@${it.simpleName}" }
				"函数 ${simpleName.asString()} 未添加路由注解，请选择：\n内置注解：$routes\n自定义注解：$availableRoutes"
			}
		}
		val className = classNames.first()
		val isWebSocket = className == TypeNames.WebSocket
		val dynamicUrl = this.getDynamicUrl()
		
		if (isWebSocket) {
			this.compileCheck(dynamicUrl == null) {
				"${simpleName.asString()} 函数不支持使用 @Path 参数"
			}
		}
		val rawUrl = getKSAnnotationByType(className)!!.getValueOrNull<String>("url")?.trim('/')
		val url = if (dynamicUrl != null) {
			this.compileCheck(rawUrl.isNullOrBlank()) {
				"${simpleName.asString()} 函数参数中使用了 @DynamicUrl 注解，因此函数上的 @${className.simpleName} 注解不允许设置 url 参数"
			}
			dynamicUrl
		} else {
			this.compileCheck(!rawUrl.isNullOrBlank()) {
				"${simpleName.asString()} 函数的参数上需要设置 @DynamicUrl 注解 或为 @${className.simpleName} 注解设置 url"
			}
			if (isWebSocket) {
				this.compileCheck(!rawUrl.containsSchemeSeparator() || rawUrl.isWSOrWSS()) {
					"${simpleName.asString()} 函数上的 @${className.simpleName} 注解中的 url 参数仅支持 ws:// 和 wss:// 协议"
				}
			} else {
				this.compileCheck(!rawUrl.containsSchemeSeparator() || rawUrl.isHttpOrHttps()) {
					"${simpleName.asString()} 函数上的 @${className.simpleName} 注解中的 url 参数仅支持 http:// 和 https:// 协议"
				}
			}
			this.compileCheck(urlRegex.matches(rawUrl)) {
				"${simpleName.asString()} 函数上的 @${className.simpleName} 注解上的 url 参数格式错误"
			}
			StaticUrl(rawUrl)
		}
		return when (className) {
			TypeNames.WebSocket -> WebSocketModel(url as StaticUrl)
			in TypeNames.httpMethods -> HttpRequestModel(url, className.simpleName, false)
			else -> {
				val method = customHttpMethodModels.first { it.className == className }.method
				HttpRequestModel(url, method, true)
			}
		}
	}
	
	private fun KSFunctionDeclaration.getReturnModel(
		isWebSocket: Boolean
	): ReturnModel {
		val returnType = this.returnType!!
		val typeName = returnType.toTypeName()
		val returnKind = when {
			isWebSocket -> {
				returnType.compileCheck(!typeName.isNullable && typeName == TypeNames.Unit) {
					"${simpleName.asString()} 函数必须使用 ${TypeNames.Unit.canonicalName} 作为返回类型，因为您标注了 @WebSocket 注解"
				}
				ReturnKind.Unit
			}
			
			typeName.rawType == TypeNames.Result -> {
				returnType.compileCheck(!typeName.isNullable && typeName is ParameterizedTypeName) {
					"${simpleName.asString()} 函数不允许为 Result 返回类型设置为可空"
				}
				ReturnKind.Result
			}
			
			typeName == TypeNames.Unit -> {
				returnType.compileCheck(!typeName.isNullable) {
					"${simpleName.asString()} 函数不允许使用 Unit? 返回类型"
				}
				ReturnKind.Unit
			}
			
			else -> {
				returnType.compileCheck(typeName != TypeNames.Nothing) {
					"${simpleName.asString()} 函数不允许使用 Nothing 返回类型"
				}
				ReturnKind.Any
			}
		}
		return ReturnModel(typeName, returnKind)
	}
	
	override fun defaultHandler(node: KSNode, data: List<CustomHttpMethodModel>): ClassModel = error("Not Implemented.")
}
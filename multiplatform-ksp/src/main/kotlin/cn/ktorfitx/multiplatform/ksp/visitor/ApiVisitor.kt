package cn.ktorfitx.multiplatform.ksp.visitor

import cn.ktorfitx.common.ksp.util.check.compileCheck
import cn.ktorfitx.common.ksp.util.check.ktorfitxError
import cn.ktorfitx.common.ksp.util.expends.*
import cn.ktorfitx.multiplatform.ksp.constants.TypeNames
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
			"${simpleName.asString()} 接口不允许包含泛型"
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
		val url = annotation.getValueOrNull<String>("url")?.trim('/') ?: return null
		if (url.isBlank()) return null
		annotation.compileCheck(!url.isHttpOrHttps() && !url.isWSOrWSS()) {
			"${simpleName.asString()} 接口上的 @Api 注解的 url 参数不允许开头是 http:// 或 https:// 或 ws:// 或 wss://"
		}
		annotation.compileCheck(apiUrlRegex.matches(url)) {
			"${simpleName.asString()} 接口上的 @Api 注解的 url 参数格式错误"
		}
		return url
	}
	
	private fun KSClassDeclaration.getApiScopeModels(): List<ApiScopeModel> {
		val apiScopeAnnotation = getKSAnnotationByType(TypeNames.ApiScope) ?: return listOf(ApiScopeModel(TypeNames.DefaultApiScope))
		val apiScopeClassNames = apiScopeAnnotation.getClassNamesOrNull("scopes")?.distinct()?.takeIf { it.isNotEmpty() }
			?: this.ktorfitxError { "${simpleName.asString()} 接口上的 @ApiScope 注解的参数不允许为空" }
		val groupSize = apiScopeClassNames.groupBy { it.simpleNames.joinToString(".") }.size
		this.compileCheck(apiScopeClassNames.size == groupSize) {
			"${simpleName.asString()} 函数不允许使用类名相同的 KClass"
		}
		return apiScopeClassNames.map { ApiScopeModel(it) }
	}
	
	/**
	 * 获取访问权限的 KModifier
	 */
	private fun KSClassDeclaration.getVisibilityKModifier(): KModifier {
		val visibility = this.getVisibility()
		this.compileCheck(visibility == PUBLIC || visibility == INTERNAL) {
			"${simpleName.asString()} 接口标记了 @Api，所以必须是 public 或 internal 访问权限的"
		}
		return KModifier.entries.first { it.name == visibility.name }
	}
	
	private fun KSClassDeclaration.getFunModel(): List<FunModel> {
		return this.getDeclaredFunctions().toList()
			.filter { it.isAbstract }
			.map {
				it.compileCheck(Modifier.SUSPEND in it.modifiers) {
					"${it.simpleName.asString()} 函数缺少 suspend 修饰符"
				}
				val routeModel = it.getRouteModel()
				val isWebSocket = routeModel is WebSocketModel
				val mockModel = it.getMockModel(isWebSocket)
				FunModel(
					funName = it.simpleName.asString(),
					returnModel = it.getReturnModel(isWebSocket),
					parameterModels = it.getParameterModels(isWebSocket),
					routeModel = routeModel,
					mockModel = mockModel,
					hasBearerAuth = it.hasBearerAuth(),
					isPrepareType = it.isPrepareType(isWebSocket, mockModel != null),
					timeoutModel = it.getTimeoutModel(),
					queryModels = it.getQueryModels(),
					pathModels = it.getPathModels(routeModel.url, isWebSocket),
					cookieModels = it.getCookieModels(),
					attributeModels = it.getAttributeModels(),
					headerModels = it.getHeaderModels(),
					headersModel = it.getHeadersModel(),
					requestBodyModel = it.getRequestBodyModel(),
					queriesModels = it.getQueriesModels(),
					attributesModels = it.getAttributesModels(),
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
		val rawUrl = getKSAnnotationByType(className)!!.getValueOrNull<String>("url")?.removePrefix("/")?.removeSuffix("/")
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
	
	override fun defaultHandler(node: KSNode, data: List<CustomHttpMethodModel>): ClassModel = error("Not Implemented")
}
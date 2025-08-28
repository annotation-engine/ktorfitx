package cn.ktorfitx.multiplatform.ksp.visitor

import cn.ktorfitx.common.ksp.util.check.ktorfitxCheck
import cn.ktorfitx.common.ksp.util.check.ktorfitxCheckNotNull
import cn.ktorfitx.common.ksp.util.expends.*
import cn.ktorfitx.common.ksp.util.message.getString
import cn.ktorfitx.multiplatform.ksp.constants.TypeNames
import cn.ktorfitx.multiplatform.ksp.message.*
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
		ktorfitxCheck(!this.isGeneric(), this) {
			MESSAGE_INTERFACE_NOT_ALLOW_GENERICS.getString(simpleName)
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
		ktorfitxCheck(!url.containsSchemeSeparator(), annotation) {
			MESSAGE_ANNOTATION_NOT_ALLOW_USE_PROTOCOL_FROM_STRINGS.getString(simpleName)
		}
		url = url.trim().trim('/')
		ktorfitxCheck(apiUrlRegex.matches(url), annotation) {
			MESSAGE_ANNOTATION_URL_PARAMETER_FORMAT_INCORRECT.getString(simpleName)
		}
		return url
	}
	
	private fun KSClassDeclaration.getApiScopeModels(): List<ApiScopeModel> {
		val apiScopeAnnotation = getKSAnnotationByType(TypeNames.ApiScope) ?: return listOf(ApiScopeModel(TypeNames.DefaultApiScope))
		val apiScopeClassNames = apiScopeAnnotation.getClassNamesOrNull("scopes")?.takeIf { it.isNotEmpty() }
		ktorfitxCheckNotNull(apiScopeClassNames, apiScopeAnnotation) {
			MESSAGE_ANNOTATION_SCOPES_PARAMETER_NOT_ALLOW_NULLABLE_TYPE.getString(simpleName)
		}
		val groupSize = apiScopeClassNames.groupBy { it.simpleNames.joinToString(".") }.size
		ktorfitxCheck(apiScopeClassNames.size == groupSize, this) {
			MESSAGE_ANNOTATION_SCOPES_NOT_ALLOWED_USE_SAME_CLASS_NAME_K_CLASS.getString(simpleName)
		}
		return apiScopeClassNames.map { ApiScopeModel(it) }
	}
	
	/**
	 * 获取访问权限的 KModifier
	 */
	private fun KSClassDeclaration.getVisibilityKModifier(): KModifier {
		val visibility = this.getVisibility()
		ktorfitxCheck(visibility == PUBLIC || visibility == INTERNAL, this) {
			MESSAGE_INTERFACE_MUST_BE_DECLARED_PUBLIC_OR_INTERNAL_ACCESS_PERMISSION.getString(simpleName)
		}
		return KModifier.entries.first { it.name == visibility.name }
	}
	
	private fun KSClassDeclaration.getFunModel(): List<FunModel> {
		return this.getDeclaredFunctions().toList()
			.filter { it.isAbstract }
			.map { function ->
				ktorfitxCheck(Modifier.SUSPEND in function.modifiers, function) {
					MESSAGE_FUNCTION_LACKS_SUSPEND_MODIFIER.getString(function.simpleName)
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
		ktorfitxCheck(classNames.size <= 1, this) {
			val useAnnotations = classNames.joinToString { "@${it.simpleName}" }
			MESSAGE_FUNCTION_ONLY_ALLOW_USE_ONE_REQUEST_TYPE_ANNOTATION
				.getString(simpleName, useAnnotations, if (classNames.size > 1) "s" else "")
		}
		ktorfitxCheck(classNames.size == 1, this) {
			MESSAGE_FUNCTION_NOT_USE_ROUTE_ANNOTATION.getString(simpleName)
		}
		val className = classNames.first()
		val isWebSocket = className == TypeNames.WebSocket
		val dynamicUrl = this.getDynamicUrl()
		
		if (isWebSocket) {
			ktorfitxCheck(dynamicUrl == null, this) {
				MESSAGE_FUNCTION_NOT_ALLOW_USE_PATH_PARAMETER.getString(simpleName)
			}
		}
		val rawUrl = getKSAnnotationByType(className)!!.getValueOrNull<String>("url")?.trim('/')
		val url = if (dynamicUrl != null) {
			ktorfitxCheck(rawUrl.isNullOrBlank(), this) {
				MESSAGE_FUNCTION_NOW_ALLOW_SETTING_URL_WHEN_MARKED_DYNAMIC_URL.getString(simpleName, className.simpleName)
			}
			dynamicUrl
		} else {
			ktorfitxCheck(!rawUrl.isNullOrBlank(), this) {
				MESSAGE_ANNOTATION_NOT_SET_URL_OR_ADDED_DYNAMIC_URL.getString(simpleName, className.simpleName)
			}
			if (isWebSocket) {
				ktorfitxCheck(!rawUrl.containsSchemeSeparator() || rawUrl.isWSOrWSS(), this) {
					MESSAGE_ANNOTATION_URL_ONLY_SUPPORTED_WS_AND_WSS_PROTOCOLS.getString(simpleName, className.simpleName)
				}
			} else {
				ktorfitxCheck(!rawUrl.containsSchemeSeparator() || rawUrl.isHttpOrHttps(), this) {
					MESSAGE_ANNOTATION_URL_ONLY_SUPPORTED_HTTP_AND_HTTPS_PROTOCOLS.getString(simpleName, className.simpleName)
				}
			}
			ktorfitxCheck(urlRegex.matches(rawUrl), this) {
				MESSAGE_ANNOTATION_URL_FORMAT_INCORRECT.getString(simpleName, className.simpleName)
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
				ktorfitxCheck(!typeName.isNullable && typeName == TypeNames.Unit, returnType) {
					MESSAGE_FUNCTION_HAS_BEEN_WEBSOCKET_SO_RETURN_TYPE_MUST_BE_UNIT.getString(simpleName)
				}
				ReturnKind.Unit
			}
			
			typeName.rawType == TypeNames.Result -> {
				ktorfitxCheck(!typeName.isNullable && typeName is ParameterizedTypeName, returnType) {
					MESSAGE_FUNCTION_NOT_ALLOW_RETURN_TYPE_RESULT_SET_NULLABLE_TYPE.getString(simpleName)
				}
				ReturnKind.Result
			}
			
			typeName == TypeNames.Unit -> {
				ktorfitxCheck(!typeName.isNullable, returnType) {
					MESSAGE_FUNCTION_NOT_ALLOW_RETURN_TYPE_UNIT_USE_NULLABLE_TYPE.getString(simpleName)
				}
				ReturnKind.Unit
			}
			
			else -> {
				ktorfitxCheck(!typeName.equals(TypeNames.Nothing, ignoreNullable = true), returnType) {
					MESSAGE_FUNCTION_NOT_ALLOW_USE_RETURN_TYPE_NOTHING.getString(simpleName, if (typeName.isNullable) "?" else "")
				}
				ReturnKind.Any
			}
		}
		return ReturnModel(typeName, returnKind)
	}
	
	override fun defaultHandler(node: KSNode, data: List<CustomHttpMethodModel>): ClassModel = error("Not Implemented.")
}
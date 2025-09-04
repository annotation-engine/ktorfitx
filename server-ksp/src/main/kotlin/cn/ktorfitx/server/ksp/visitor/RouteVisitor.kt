package cn.ktorfitx.server.ksp.visitor

import cn.ktorfitx.common.ksp.util.check.ktorfitxCheck
import cn.ktorfitx.common.ksp.util.expends.*
import cn.ktorfitx.common.ksp.util.message.getString
import cn.ktorfitx.common.ksp.util.resolver.isSerializableType
import cn.ktorfitx.server.ksp.constants.TypeNames
import cn.ktorfitx.server.ksp.message.*
import cn.ktorfitx.server.ksp.model.*
import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSNode
import com.google.devtools.ksp.visitor.KSEmptyVisitor
import com.squareup.kotlinpoet.ksp.toClassName
import com.squareup.kotlinpoet.ksp.toTypeName

internal class RouteVisitor : KSEmptyVisitor<List<CustomHttpMethodModel>, FunModel>() {
	
	override fun visitFunctionDeclaration(
		function: KSFunctionDeclaration,
		data: List<CustomHttpMethodModel>
	): FunModel {
		ktorfitxCheck(!function.isGeneric(), function) {
			MESSAGE_FUNCTION_NOT_ALLOWED_TO_CONTAIN_GENERICS.getString(function.simpleName)
		}
		val routeModel = function.getRouteModel(data)
		val isReturnNullable = function.getReturnNullableAndCheck(routeModel)
		return FunModel(
			funName = function.simpleName.asString(),
			canonicalName = function.getCanonicalName(),
			isExtension = function.extensionReceiver != null,
			isReturnNullable = isReturnNullable,
			authenticationModel = function.getAuthenticationModel(),
			routeModel = routeModel,
			regexModel = function.getRegexModel(routeModel),
			timeoutModel = function.getTimeoutModel(routeModel),
			varNames = function.getVarNames(),
			principalModels = function.getPrincipalModels(),
			queryModels = function.getQueryModels(),
			pathModels = function.getPathModels(routeModel),
			headerModels = function.getHeaderModels(),
			cookieModels = function.getCookieModels(),
			attributeModels = function.getAttributeModels(),
			requestBodyModel = function.getRequestBody(),
		)
	}
	
	private fun KSFunctionDeclaration.getCanonicalName(): String {
		return when (val parent = this.parentDeclaration) {
			is KSClassDeclaration -> parent.toClassName().canonicalName
			else -> this.packageName.asString()
		}
	}
	
	private fun KSFunctionDeclaration.getReturnNullableAndCheck(
		routeModel: RouteModel
	): Boolean {
		val returnType = this.returnType!!
		val type = returnType.resolve()
		val typeName = returnType.toTypeName()
		if (routeModel is HttpRequestModel) {
			ktorfitxCheck(typeName != TypeNames.Unit && typeName != TypeNames.Nothing, returnType) {
				MESSAGE_FUNCTION_NOT_ALLOW_USE_UNIT_AND_NOTHING.getString(simpleName)
			}
			ktorfitxCheck(typeName.isSerializableType(), returnType) {
				MESSAGE_FUNCTION_RETURN_TYPE_NOT_MEET_SERIALIZATION_REQUIREMENTS.getString(simpleName)
			}
		} else {
			ktorfitxCheck(typeName == TypeNames.Unit, this) {
				MESSAGE_FUNCTION_IS_WEBSOCKET_TYPE_NOT_ALLOW_USE_UNIT.getString(simpleName, routeModel.annotation)
			}
		}
		return type.isMarkedNullable
	}
	
	private fun KSFunctionDeclaration.getAuthenticationModel(): AuthenticationModel? {
		val annotation = this.getKSAnnotationByType(TypeNames.Authentication) ?: return null
		val configurations = annotation.getValues<String>("configurations")
		val strategy = annotation.getClassNameOrNull("strategy") ?: TypeNames.AuthenticationStrategyFirstSuccessful
		return AuthenticationModel(configurations, strategy)
	}
	
	private fun KSFunctionDeclaration.getRouteModel(
		customHttpMethodModels: List<CustomHttpMethodModel>
	): RouteModel {
		val customHttpMethodClassNames = customHttpMethodModels.map { it.className }
		val dataList = (TypeNames.routeAnnotationTypes + customHttpMethodClassNames)
			.mapNotNull { this.getKSAnnotationByType(it)?.let(it::to) }
		ktorfitxCheck(dataList.size == 1, this) {
			MESSAGE_FUNCTION_NOT_ALLOW_ADDING_MULTIPLE_REQUEST_TYPES_SIMULTANEOUSLY.getString(simpleName)
		}
		val data = dataList.single()
		val className = data.first
		val annotation = data.second
		val path = this.parseFullPath(annotation)
		val isExtension = this.extensionReceiver != null
		return when (className) {
			TypeNames.WebSocket -> {
				val protocol = annotation.getValueOrNull<String>("protocol")?.takeIf { it.isNotBlank() }
				if (isExtension) {
					val valid = this.isExtension(TypeNames.DefaultWebSocketServerSession)
					ktorfitxCheck(valid, this) {
						MESSAGE_FUNCTION_ONLY_ALLOW_CREATION_OF_EXTENSION_METHODS_FOR_DEFAULT_WEB_SOCKET_SERVER_SESSION.getString(simpleName)
					}
				}
				WebSocketModel(path, protocol, annotation)
			}
			
			TypeNames.WebSocketRaw -> {
				val protocol = annotation.getValueOrNull<String>("protocol")?.takeIf { it.isNotBlank() }
				val negotiateExtensions = annotation.getValueOrNull("negotiateExtensions") ?: false
				if (isExtension) {
					val valid = this.isExtension(TypeNames.WebSocketServerSession)
					ktorfitxCheck(valid, this) {
						MESSAGE_FUNCTION_ONLY_ALLOW_CREATION_OF_EXTENSION_METHODS_FOR_WEB_SOCKET_SERVER_SESSION.getString(simpleName)
					}
				}
				WebSocketRawModel(path, protocol, negotiateExtensions, annotation)
			}
			
			else -> {
				if (isExtension) {
					ktorfitxCheck(this.isExtension(TypeNames.RoutingContext), this) {
						MESSAGE_FUNCTION_ONLY_ALLOW_CREATION_OF_EXTENSION_METHODS_FOR_ROUTING_CONTEXT.getString(simpleName)
					}
				}
				if (className in TypeNames.httpMethodAnnotationTypes) {
					HttpRequestModel(path, annotation, className.simpleName, false)
				} else {
					val method = customHttpMethodModels.first { it.className == className }.method
					HttpRequestModel(path, annotation, method, true)
				}
			}
		}
	}
	
	private fun KSFunctionDeclaration.parseFullPath(
		annotation: KSAnnotation
	): String {
		var path = annotation.getValue<String>("path").trim('/')
		var node = this.parent
		while (node is KSClassDeclaration) {
			val annotation = node.getKSAnnotationByType(TypeNames.Controller) ?: break
			val parentPath = annotation.getValue<String>("path").trim('/').takeIf { it.isNotBlank() } ?: break
			path = "$parentPath/$path"
			node = node.parent
		}
		return path
	}
	
	private fun KSFunctionDeclaration.getRegexModel(
		routeModel: RouteModel
	): RegexModel? {
		val annotation = this.getKSAnnotationByType(TypeNames.Regex) ?: return null
		val routeAnnotation = routeModel.annotation
		ktorfitxCheck(routeModel is HttpRequestModel, annotation) {
			MESSAGE_ANNOTATION_NOT_ALLOW_USE_REGEX_WHEN_WEBSOCKET_HAS_BEEN_MARKED.getString(simpleName, routeAnnotation)
		}
		val classNames = annotation.getClassNamesOrNull("options")?.toSet() ?: emptySet()
		val options = classNames.map { RegexOption.valueOf(it.simpleName) }.toSet()
		ktorfitxCheck(routeModel.path.isValidRegex(options), routeAnnotation) {
			MESSAGE_ANNOTATION_PATH_PARAMETER_NOT_VALID_REGULAR_EXPRESSION.getString(simpleName, routeAnnotation)
		}
		return RegexModel(classNames)
	}
	
	private fun KSFunctionDeclaration.getTimeoutModel(
		routeModel: RouteModel
	): TimeoutModel? {
		val annotation = this.getKSAnnotationByType(TypeNames.Timeout) ?: return null
		ktorfitxCheck(routeModel is HttpRequestModel, annotation) {
			MESSAGE_ANNOTATION_NOT_ALLOW_USE_TIMEOUT_WHEN_WEBSOCKET_HAS_BEEN_MARKED.getString(simpleName, routeModel.annotation)
		}
		val value = annotation.getValue<Long>("value")
		val unit = annotation.getClassNameOrNull("unit")?.simpleName?.lowercase() ?: "milliseconds"
		ktorfitxCheck(value > 0L, annotation) {
			MESSAGE_ANNOTATION_VALUE_PARAMETER_MUST_BE_GREATER_THAN_ZERO.getString(simpleName, annotation)
		}
		return TimeoutModel(value, unit)
	}
	
	override fun defaultHandler(node: KSNode, data: List<CustomHttpMethodModel>): FunModel = error("Not Implemented.")
}
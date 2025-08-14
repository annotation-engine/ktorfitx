package cn.ktorfitx.server.ksp.kotlinpoet

import cn.ktorfitx.common.ksp.util.builders.buildFileSpec
import cn.ktorfitx.common.ksp.util.builders.buildFunSpec
import cn.ktorfitx.common.ksp.util.builders.fileSpecBuilder
import cn.ktorfitx.common.ksp.util.builders.fileSpecBuilderLocal
import cn.ktorfitx.server.ksp.constants.PackageNames
import cn.ktorfitx.server.ksp.constants.TypeNames
import cn.ktorfitx.server.ksp.model.*
import com.squareup.kotlinpoet.*
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

internal class RouteKotlinPoet {
	
	private val fileComment = """
        该文件是由 cn.ktorfitx:server-ksp 在编译期间根据注解生成的代码，
        所有手动修改将会在下次构建时被覆盖，
        若需修改行为，请修改对应的注解或源代码定义，而不是此文件本身。
        
        生成时间：%L
        """.trimIndent()
	
	fun getFileSpec(
		funModels: List<FunModel>,
		packageName: String,
		fileName: String,
		funName: String
	): FileSpec = buildFileSpec(packageName, fileName) {
		fileSpecBuilderLocal.set(this)
		addFileComment(fileComment, LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
		indent("\t")
		val funSpec = getFunctionSpec(funName, funModels)
		addFunction(funSpec)
		fileSpecBuilderLocal.remove()
	}
	
	private fun getFunctionSpec(
		funName: String,
		funModels: List<FunModel>
	): FunSpec = buildFunSpec(funName) {
		receiver(TypeNames.Routing)
		val codeBlock = getCodeBlock(funModels)
		addCode(codeBlock)
	}
	
	private fun getCodeBlock(
		funModels: List<FunModel>
	): CodeBlock = buildCodeBlock {
		funModels.forEach { funModel ->
			buildAuthenticationIfNeed(funModel) { routeModel ->
				when (routeModel) {
					is HttpRequestModel -> buildHttpRequest(funModel, routeModel)
					is WebSocketRawModel -> buildWebRawSocket(funModel, routeModel)
					is WebSocketModel -> buildWebSocket(funModel, routeModel)
				}
			}
		}
	}
	
	private fun CodeBlock.Builder.buildAuthenticationIfNeed(
		funModel: FunModel,
		block: (RouteModel) -> Unit
	) {
		if (funModel.authenticationModel != null) {
			val configurations = funModel.authenticationModel.configurations
			val strategy = funModel.authenticationModel.strategy
			fileSpecBuilder.addImport(PackageNames.KTOR_SERVER_AUTH, "authenticate")
			if (configurations.isEmpty() && strategy == TypeNames.AuthenticationStrategyFirstSuccessful) {
				beginControlFlow("authenticate")
			} else {
				addStatement("authenticate(")
				indent()
				if (configurations.isNotEmpty()) {
					val parameters = configurations.joinToString { "%S" }
					addStatement("configurations = arrayOf($parameters),", *configurations)
				}
				if (strategy != TypeNames.AuthenticationStrategyFirstSuccessful) {
					addStatement("strategy = %T", strategy)
				}
				unindent()
				beginControlFlow(")")
			}
		}
		block(funModel.routeModel)
		if (funModel.authenticationModel != null) {
			endControlFlow()
		}
	}
	
	private fun CodeBlock.Builder.buildHttpRequest(
		funModel: FunModel,
		httpRequestModel: HttpRequestModel
	) {
		if (httpRequestModel.isCustom) {
			fileSpecBuilder.addImport(PackageNames.KTOR_HTTP, "HttpMethod")
			fileSpecBuilder.addImport(PackageNames.KTOR_SERVER_ROUTING, "route")
			beginControlFlow(
				"""
				route(
					path = %S%L,
					method = HttpMethod(%S)
				)
				""".trimIndent(),
				httpRequestModel.path,
				getRegexCode(funModel.regexModel),
				httpRequestModel.method
			)
			beginControlFlow("handle")
			buildCodeBlock(funModel)
			endControlFlow()
			endControlFlow()
		} else {
			val method = httpRequestModel.method.lowercase()
			fileSpecBuilder.addImport(PackageNames.KTOR_SERVER_ROUTING, method)
			beginControlFlow(
				"""
				%N(
					path = %S%L
				)
				""".trimIndent(),
				method,
				httpRequestModel.path,
				getRegexCode(funModel.regexModel)
			)
			buildCodeBlock(funModel)
			endControlFlow()
		}
	}
	
	private fun CodeBlock.Builder.buildWebRawSocket(
		funModel: FunModel,
		webSocketRawModel: WebSocketRawModel
	) {
		fileSpecBuilder.addImport(PackageNames.KTOR_SERVER_WEBSOCKET, "webSocketRaw")
		addStatement("webSocketRaw(")
		indent()
		addStatement("path = %S%L,", webSocketRawModel.path, getRegexCode(funModel.regexModel))
		webSocketRawModel.protocol?.let { addStatement("protocol = %S", it) }
		if (webSocketRawModel.negotiateExtensions) {
			addStatement("negotiateExtensions = true")
		}
		unindent()
		beginControlFlow(")")
		buildCodeBlock(funModel)
		endControlFlow()
	}
	
	private fun CodeBlock.Builder.buildWebSocket(
		funModel: FunModel,
		webSocketModel: WebSocketModel
	) {
		fileSpecBuilder.addImport(PackageNames.KTOR_SERVER_WEBSOCKET, "webSocket")
		addStatement("webSocket(")
		indent()
		addStatement("path = %S%L,", webSocketModel.path, getRegexCode(funModel.regexModel))
		webSocketModel.protocol?.let { addStatement("protocol = %S", it) }
		unindent()
		beginControlFlow(")")
		buildCodeBlock(funModel)
		endControlFlow()
	}
	
	private fun CodeBlock.Builder.buildCodeBlock(
		funModel: FunModel
	) {
		with(RouteCodeBlock(funModel)) {
			val funName = getFunNameAndImport(funModel)
			addCodeBlock(funName)
		}
	}
	
	private val funNameCanonicalNamesMap = mutableMapOf<String, MutableSet<String>>()
	
	private fun getFunNameAndImport(funModel: FunModel): String {
		val canonicalNames = funNameCanonicalNamesMap.getOrPut(funModel.funName) { mutableSetOf() }
		if (canonicalNames.isEmpty()) {
			canonicalNames += funModel.canonicalName
			fileSpecBuilder.addImport(funModel.canonicalName, funModel.funName)
			return funModel.funName
		}
		if (funModel.canonicalName in canonicalNames) {
			return funModel.funName
		}
		var i = 0
		var funName: String
		do {
			funName = funModel.funName + i++
		} while (funName in funNameCanonicalNamesMap)
		funNameCanonicalNamesMap[funName] = mutableSetOf(funModel.canonicalName)
		val memberName = MemberName(funModel.canonicalName, funModel.funName, funModel.isExtension)
		fileSpecBuilder.addAliasedImport(memberName, funName)
		return funName
	}
	
	private fun getRegexCode(regexModel: RegexModel?): String {
		if (regexModel == null) return ""
		if (regexModel.classNames.isEmpty()) return ".toRegex()"
		if (regexModel.classNames.size == 1) {
			val className = regexModel.classNames.first()
			val option = className.simpleNames.joinToString(".")
			return ".toRegex($option)"
		}
		val options = regexModel.classNames.joinToString {
			it.simpleNames.joinToString(".")
		}
		return ".toRegex(setOf($options))"
	}
}
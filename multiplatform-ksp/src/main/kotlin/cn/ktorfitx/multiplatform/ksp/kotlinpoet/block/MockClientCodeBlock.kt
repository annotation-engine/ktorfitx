package cn.ktorfitx.multiplatform.ksp.kotlinpoet.block

import cn.ktorfitx.common.ksp.util.builders.fileSpecBuilder
import cn.ktorfitx.common.ksp.util.builders.toCodeBlock
import cn.ktorfitx.common.ksp.util.expends.replaceFirstToUppercase
import cn.ktorfitx.multiplatform.ksp.constants.PackageNames
import cn.ktorfitx.multiplatform.ksp.constants.TypeNames
import cn.ktorfitx.multiplatform.ksp.model.*
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.buildCodeBlock

/**
 * MockClient 代码块
 */
internal class MockClientCodeBlock(
	private val mockModel: MockModel,
	private val returnModel: ReturnModel
) : ClientCodeBlock {
	
	override fun CodeBlock.Builder.buildClientCodeBlock(
		httpRequestModel: HttpRequestModel,
		isPrepareType: Boolean,
		builder: CodeBlock.Builder.() -> Unit,
	) {
		check(!isPrepareType)
		fileSpecBuilder.addImport(PackageNames.KTORFITX_MOCK_CONFIG, "mockClient")
		fileSpecBuilder.addImport(PackageNames.KTOR_HTTP, "HttpMethod")
		when (returnModel.returnKind) {
			ReturnKind.Unit -> addStatement("this.config.mockClient.request(")
			ReturnKind.Result -> addStatement("val result = this.config.mockClient.request(")
			ReturnKind.Any -> {
				add("return ")
				addStatement("this.config.mockClient.request(")
			}
		}
		indent()
		if (httpRequestModel.isCustom) {
			addStatement("method = HttpMethod(%S),", httpRequestModel.method)
		} else {
			addStatement("method = HttpMethod.%N,", httpRequestModel.method.lowercase().replaceFirstToUppercase())
		}
		addStatement("mockProvider = %T,", mockModel.provider)
		if (mockModel.delay > 0L) {
			addStatement("delay = %LL", mockModel.delay)
		}
		unindent()
		beginControlFlow(")")
		builder()
		endControlFlow()
		if (returnModel.returnKind == ReturnKind.Result) {
			addStatement("Result.success(result)")
		}
	}
	
	override fun CodeBlock.Builder.buildStaticUrl(
		url: String,
		jointApiUrl: Boolean,
	) {
		if (jointApiUrl) {
			addStatement($$"this.url(\"$API_URL/$$url\")")
		} else {
			addStatement("this.url(\"$url\")")
		}
	}
	
	override fun CodeBlock.Builder.buildDynamicUrl(
		dynamicUrl: DynamicUrl,
		jointApiUrl: Boolean,
		pathModels: List<PathModel>
	) {
		val apiUrl = if (jointApiUrl) "API_URL" else "null"
		if (pathModels.isEmpty()) {
			addStatement("this.url(%T.parseDynamicUrl(%N, %N, null))", TypeNames.UrlUtil, dynamicUrl.varName, apiUrl)
		} else {
			val mapCode = pathModels.joinToString { "\"${it.name}\" to ${it.varName}" }
			addStatement("this.url(%T.parseDynamicUrl(%N, %N, mapOf(%L)))", TypeNames.UrlUtil, dynamicUrl.varName, apiUrl, mapCode)
		}
	}
	
	override fun CodeBlock.Builder.buildTimeoutCodeBlock(
		timeoutModel: TimeoutModel
	) {
		beginControlFlow("this.timeout")
		if (timeoutModel.requestTimeoutMillis != null) {
			addStatement("this.requestTimeoutMillis = %LL", timeoutModel.requestTimeoutMillis)
		}
		if (timeoutModel.connectTimeoutMillis != null) {
			addStatement("this.connectTimeoutMillis = %LL", timeoutModel.connectTimeoutMillis)
		}
		if (timeoutModel.socketTimeoutMillis != null) {
			addStatement("this.socketTimeoutMillis = %LL", timeoutModel.socketTimeoutMillis)
		}
		endControlFlow()
	}
	
	override fun CodeBlock.Builder.buildBearerAuth(
		varName: String
	) {
		beginControlFlow("if (%N != null)", varName)
		addStatement("this.bearerAuth(%N)", varName)
		endControlFlow()
	}
	
	override fun CodeBlock.Builder.buildHeadersCodeBlock(
		headersModel: HeadersModel?,
		headerModels: List<HeaderModel>,
	) {
		beginControlFlow("this.headers")
		headersModel?.headerMap?.forEach { (name, value) ->
			addStatement("this.append(%S, %S)", name, value)
		}
		headerModels.forEach {
			addStatement("this.append(%S, %N)", it.name, it.varName)
		}
		endControlFlow()
	}
	
	override fun CodeBlock.Builder.buildQueries(
		queryModels: List<QueryModel>,
		queriesModels: List<QueriesModel>
	) {
		beginControlFlow("this.queries")
		queryModels.forEach {
			addStatement("this.append(%S, %N)", it.name, it.varName)
		}
		queriesModels.forEach {
			beginControlFlow("%N.forEach { (key, value) ->", it.varName)
			addStatement("this.append(key, value)")
			endControlFlow()
		}
		endControlFlow()
	}
	
	override fun CodeBlock.Builder.buildParts(
		partModels: List<PartModel>,
		partsModels: List<PartsModel>
	) {
		beginControlFlow("this.parts")
		partModels.forEach {
			when {
				it.partKind == PartKind.DIRECT -> addStatement("this.append(%N)", it.varName)
				it.headers.isNullOrEmpty() -> {
					if (it.partKind == PartKind.KEY_VALUE) {
						addStatement("this.append(%S, %N)", it.name, it.varName)
					} else {
						addStatement("this.append(%T(%S, %N))", TypeNames.FormPart, it.name, it.varName)
					}
				}
				
				else -> {
					fileSpecBuilder.addImport(PackageNames.KTOR_UTILS, "buildHeaders")
					val headersCodeBlock = buildCodeBlock {
						addStatement("buildHeaders {")
						indent()
						it.headers.forEach { (key, value) ->
							addStatement("this[%S] = %S", key, value)
						}
						unindent()
						add("}")
					}
					if (it.partKind == PartKind.KEY_VALUE) {
						add("this.append(%S, %N, %L)\n", it.name, it.varName, headersCodeBlock)
					} else {
						add("this.append(%T(%S, %N, %L))\n", TypeNames.FormPart, it.name, it.varName, headersCodeBlock)
					}
				}
			}
		}
		partsModels.forEach {
			when (it.partsKind) {
				PartsKind.MAP if it.valueKind == PartsValueKind.KEY_VALUE ->
					addStatement("%N.forEach { this.append(it.key, it.value) }", it.varName)
				
				PartsKind.MAP if it.valueKind == PartsValueKind.FORM_PART ->
					addStatement("%N.forEach { this.append(%T(it.key, it.value)) }", it.varName, TypeNames.FormPart)
				
				PartsKind.LIST_PAIR if it.valueKind == PartsValueKind.KEY_VALUE ->
					addStatement("%N.forEach { this.append(it.first, it.second) }", it.varName)
				
				PartsKind.LIST_PAIR if it.valueKind == PartsValueKind.FORM_PART ->
					addStatement("%N.forEach { this.append(%T(it.first, it.second)) }", it.varName, TypeNames.FormPart)
				
				PartsKind.LIST_FORM_PART ->
					addStatement("%N.forEach { this.append(it) }", it.varName)
				
				else -> {}
			}
		}
		endControlFlow()
	}
	
	override fun CodeBlock.Builder.buildFields(
		fieldModels: List<FieldModel>,
		fieldsModels: List<FieldsModel>
	) {
		beginControlFlow("this.fields")
		fieldModels.forEach {
			addStatement("this.append(%S, %N)", it.name, it.varName)
		}
		fieldsModels.forEach {
			when (it.fieldsKind) {
				FieldsKind.LIST -> addStatement("%N%L.forEach { this.append(it.first, it.second) }", it.varName, if (it.isNullable) "?" else "")
				FieldsKind.MAP -> addStatement("%N%L.forEach { this.append(it.key, it.value) }", it.varName, if (it.isNullable) "?" else "")
			}
		}
		endControlFlow()
	}
	
	override fun CodeBlock.Builder.buildCookies(
		cookieModels: List<CookieModel>
	) {
		beginControlFlow("this.cookies")
		fileSpecBuilder.addImport(PackageNames.KTORFITX_MOCK, "MockClient")
		cookieModels.forEach { model ->
			val codeBlock = buildCodeBlock {
				addStatement("this.append(")
				indent()
				addStatement("name = %S,", model.name)
				addStatement("value = %N,", model.varName)
				model.maxAge?.let { addStatement("maxAge = %L,", it) }
				model.expires?.let {
					fileSpecBuilder.addImport(PackageNames.KTOR_UTIL_DATE, "GMTDate")
					addStatement("expires = GMTDate(%LL),", it)
				}
				model.domain?.let { addStatement("domain = %S,", it) }
				model.path?.let { addStatement("path = %S,", it) }
				model.secure?.let { addStatement("secure = %L,", it) }
				model.httpOnly?.let { addStatement("httpOnly = %L,", it) }
				model.extensions?.let { addStatement("extensions = %L,", it.toCodeBlock()) }
				unindent()
				addStatement(")")
			}
			add(codeBlock)
		}
		endControlFlow()
	}
	
	override fun CodeBlock.Builder.buildAttributes(
		attributeModels: List<AttributeModel>,
		attributesModels: List<AttributesModel>
	) {
		beginControlFlow("this.attributes")
		attributeModels.forEach {
			addStatement("this.append(%S, %N)", it.name, it.varName)
		}
		attributesModels.forEach {
			when (it.attributesKind) {
				AttributesKind.MAP -> addStatement("%N%L.forEach { this.append(it.key, it.value) }", it.varName, if (it.isNullable) "?" else "")
				AttributesKind.LIST -> addStatement("%N%L.forEach { this.append(it.first, it.second) }", it.varName, if (it.isNullable) "?" else "")
			}
		}
		endControlFlow()
	}
	
	fun CodeBlock.Builder.buildPaths(
		pathModels: List<PathModel>
	) {
		beginControlFlow("this.paths")
		pathModels.forEach {
			addStatement("this.append(%S, %N)", it.name, it.varName)
		}
		endControlFlow()
	}
	
	override fun CodeBlock.Builder.buildBody(
		bodyModel: BodyModel
	) {
		addStatement("this.body(%N, %T)", bodyModel.varName, bodyModel.formatClassName)
	}
}
package cn.ktorfitx.multiplatform.ksp.kotlinpoet.block

import cn.ktorfitx.common.ksp.util.builders.fileSpecBuilder
import cn.ktorfitx.common.ksp.util.builders.toMapCode
import cn.ktorfitx.common.ksp.util.expends.asNotNullable
import cn.ktorfitx.multiplatform.ksp.constants.PackageNames
import cn.ktorfitx.multiplatform.ksp.constants.TypeNames
import cn.ktorfitx.multiplatform.ksp.model.*
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.buildCodeBlock

internal class HttpClientCodeBlock(
	private val returnModel: ReturnModel
) : ClientCodeBlock {
	
	override fun CodeBlock.Builder.buildClientCodeBlock(
		httpRequestModel: HttpRequestModel,
		isPrepareType: Boolean,
		builder: CodeBlock.Builder.() -> Unit
	) {
		val isCustom = httpRequestModel.isCustom
		val funName = when {
			isCustom && isPrepareType -> "prepareRequest"
			isCustom && !isPrepareType -> "request"
			isPrepareType -> "prepare${httpRequestModel.method.lowercase().replaceFirstChar { it.uppercase() }}"
			else -> httpRequestModel.method.lowercase()
		}
		fileSpecBuilder.addImport(PackageNames.KTOR_REQUEST, funName)
		
		if (returnModel.returnKind == ReturnKind.Unit) {
			beginControlFlow("this.config.httpClient.%N", funName)
			customHttpMethodCodeBlock(httpRequestModel, builder)
			endControlFlow()
			return
		}
		val rawType = returnModel.serializedTypeName.asNotNullable()
		val bodyFunName = when (rawType) {
			TypeNames.String -> "bodyAsText"
			TypeNames.ByteArray -> "bodyAsBytes"
			TypeNames.ByteReadChannel -> "bodyAsChannel"
			else -> "body"
		}
		if (!isPrepareType) {
			fileSpecBuilder.addImport(
				if (bodyFunName == "body") PackageNames.KTOR_CALL else PackageNames.KTOR_STATEMENT,
				bodyFunName
			)
		}
		if (returnModel.returnKind == ReturnKind.Any) {
			if (isPrepareType) {
				add("return ")
				beginControlFlow("this.config.httpClient.%N {", funName)
			} else {
				beginControlFlow("val response = this.config.httpClient.%N {", funName)
			}
			customHttpMethodCodeBlock(httpRequestModel, builder)
			endControlFlow()
			if (!isPrepareType) {
				addStatement("return response.%N()", bodyFunName)
			}
			return
		}
		
		beginControlFlow("val response = this.config.httpClient.%N", funName)
		customHttpMethodCodeBlock(httpRequestModel, builder)
		endControlFlow()
		addStatement("Result.success(response.%N())", bodyFunName)
	}
	
	private fun CodeBlock.Builder.customHttpMethodCodeBlock(
		httpRequestModel: HttpRequestModel,
		builder: CodeBlock.Builder.() -> Unit
	) {
		if (httpRequestModel.isCustom) {
			fileSpecBuilder.addImport(PackageNames.KTOR_HTTP, "HttpMethod")
			addStatement("this.method = HttpMethod(%S)", httpRequestModel.method)
		}
		builder()
		if (httpRequestModel.isCustom) {
			addStatement("this.method = HttpMethod(%S)", httpRequestModel.method)
		}
	}
	
	override fun CodeBlock.Builder.buildStaticUrl(
		url: String,
		jointApiUrl: Boolean,
	) {
		fileSpecBuilder.addImport(PackageNames.KTOR_REQUEST, "url")
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
		fileSpecBuilder.addImport(PackageNames.KTOR_REQUEST, "url")
		val argsCode = pathModels.joinToString(
			prefix = if (pathModels.isEmpty()) "" else ", ",
			transform = { "\"${it.name}\" to ${it.varName}" }
		)
		val apiUrl = if (jointApiUrl) "API_URL" else "null"
		if (jointApiUrl) {
			addStatement(
				"this.url(%T.parseDynamicUrl(%N, %L%L))",
				TypeNames.UrlUtil,
				dynamicUrl.varName,
				apiUrl,
				argsCode
			)
		} else {
			addStatement(
				"this.url(%T.parseDynamicUrl(%N, %L%L))",
				TypeNames.UrlUtil,
				dynamicUrl.varName,
				apiUrl,
				argsCode
			)
		}
	}
	
	override fun CodeBlock.Builder.buildTimeoutCodeBlock(
		timeoutModel: TimeoutModel
	) {
		fileSpecBuilder.addImport(PackageNames.KTOR_PLUGINS, "timeout")
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
		fileSpecBuilder.addImport(PackageNames.KTOR_REQUEST, "bearerAuth")
		addStatement("%N?.let { this.bearerAuth(it) }", varName)
	}
	
	override fun CodeBlock.Builder.buildHeadersCodeBlock(
		headersModel: HeadersModel?,
		headerModels: List<HeaderModel>
	) {
		fileSpecBuilder.addImport(PackageNames.KTOR_REQUEST, "headers")
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
		fileSpecBuilder.addImport(PackageNames.KTOR_REQUEST, "parameter")
		queryModels.forEach {
			addStatement("this.parameter(%S, %N)", it.name, it.varName)
		}
		queriesModels.forEach {
			beginControlFlow("%N.forEach { (key, value) ->", it.varName)
			addStatement("this.parameter(key, value)")
			endControlFlow()
		}
	}
	
	override fun CodeBlock.Builder.buildParts(
		partModels: List<PartModel>,
		partsModels: List<PartsModel>
	) {
		fileSpecBuilder.addImport(PackageNames.KTOR_HTTP, "contentType", "ContentType")
		fileSpecBuilder.addImport(PackageNames.KTOR_REQUEST, "setBody")
		fileSpecBuilder.addImport(PackageNames.KTOR_REQUEST_FORMS, "formData", "MultiPartFormDataContent")
		addStatement("this.contentType(ContentType.MultiPart.FormData)")
		addStatement("formData {")
		indent()
		partModels.forEach {
			when {
				it.partKind == PartKind.DIRECT -> addStatement("this.append(%N)", it.varName)
				it.headerMap.isNullOrEmpty() -> {
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
						val headers = it.headerMap
						headers.forEach { (key, value) ->
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
			val nullOperator = if (it.isNullable) "?" else ""
			when (it.partsKind) {
				PartsKind.MAP if it.valueKind == PartsValueKind.KEY_VALUE ->
					addStatement("%N%L.forEach { this.append(it.key, it.value) }", it.varName, nullOperator)
				
				PartsKind.MAP if it.valueKind == PartsValueKind.FORM_PART ->
					addStatement(
						"%N%L.forEach { this.append(%T(it.key, it.value)) }",
						it.varName,
						nullOperator,
						TypeNames.FormPart
					)
				
				PartsKind.LIST_PAIR if it.valueKind == PartsValueKind.KEY_VALUE ->
					addStatement("%N%L.forEach { this.append(it.first, it.second) }", it.varName, nullOperator)
				
				PartsKind.LIST_PAIR if it.valueKind == PartsValueKind.FORM_PART ->
					addStatement(
						"%N%L.forEach { this.append(%T(it.first, it.second)) }",
						it.varName,
						nullOperator,
						TypeNames.FormPart
					)
				
				PartsKind.LIST_FORM_PART -> addStatement("%N%L.forEach { this.append(it) }", it.varName, nullOperator)
				
				else -> {}
			}
		}
		unindent()
		addStatement("}.let { this.setBody(MultiPartFormDataContent(it)) }")
	}
	
	override fun CodeBlock.Builder.buildFields(
		fieldModels: List<FieldModel>,
		fieldsModels: List<FieldsModel>
	) {
		fileSpecBuilder.addImport(PackageNames.KTOR_HTTP, "contentType", "ContentType", "formUrlEncode")
		fileSpecBuilder.addImport(PackageNames.KTOR_REQUEST, "setBody")
		addStatement("this.contentType(ContentType.Application.FormUrlEncoded)")
		addStatement("buildList<Pair<String, String?>> {")
		indent()
		fieldModels.forEach {
			when {
				it.isStringType -> addStatement("this += %S to %N", it.name, it.varName)
				it.isNullable -> addStatement("this += %S to %N?.toString()", it.name, it.varName)
				else -> addStatement("this += %S to %N.toString()", it.name, it.varName)
			}
		}
		fieldsModels.forEach {
			val nullOperator = if (it.isNullable) "?" else ""
			when (it.fieldsKind) {
				FieldsKind.LIST -> {
					when {
						it.valueIsString -> addStatement("%N%L.forEach { this += it }", it.varName, nullOperator)
						it.valueIsNullable -> addStatement(
							"%N%L.forEach { this += it.first to it.second?.toString() }",
							it.varName,
							nullOperator
						)
						
						else -> addStatement(
							"%N%L.forEach { this += it.first to it.second.toString() }",
							it.varName,
							nullOperator
						)
					}
				}
				
				FieldsKind.MAP -> {
					when {
						it.valueIsString -> addStatement(
							"%N%L.forEach { this += it.key to it.value }",
							it.varName,
							nullOperator
						)
						
						it.valueIsNullable -> addStatement(
							"%N%L.forEach { this += it.key to it.value?.toString() }",
							it.varName,
							nullOperator
						)
						
						else -> addStatement(
							"%N%L.forEach { this += it.key to it.value.toString() }",
							it.varName,
							nullOperator
						)
					}
				}
			}
		}
		unindent()
		addStatement("}.let { this.setBody(it.formUrlEncode()) }")
	}
	
	override fun CodeBlock.Builder.buildCookies(
		cookieModels: List<CookieModel>
	) {
		fileSpecBuilder.addImport(PackageNames.KTOR_REQUEST, "cookie")
		cookieModels.forEach { model ->
			val codeBlock = buildCodeBlock {
				addStatement("this.cookie(")
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
				model.extensions?.let { addStatement("extensions = %L", it.toMapCode()) }
				unindent()
				addStatement(")")
			}
			add(codeBlock)
		}
	}
	
	override fun CodeBlock.Builder.buildAttributes(
		attributeModels: List<AttributeModel>,
		attributesModels: List<AttributesModel>
	) {
		beginControlFlow("this.setAttributes")
		attributeModels.forEach {
			addStatement("this[%T(%S)] = %L", TypeNames.AttributeKey, it.name, it.varName)
		}
		attributesModels.forEach {
			val nullOperator = if (it.isNullable) "?" else ""
			when (it.attributesKind) {
				AttributesKind.MAP -> addStatement(
					"%N%L.forEach { this[%T(it.key)] = it.value }",
					it.varName,
					nullOperator,
					TypeNames.AttributeKey
				)
				
				AttributesKind.LIST -> addStatement(
					"%N%L.forEach { this[%T(it.first)] = it.second }",
					it.varName,
					nullOperator,
					TypeNames.AttributeKey
				)
			}
		}
		endControlFlow()
	}
	
	override fun CodeBlock.Builder.buildBody(
		bodyModel: BodyModel
	) {
		fileSpecBuilder.addImport(PackageNames.KTOR_HTTP, "contentType", "ContentType")
		fileSpecBuilder.addImport(PackageNames.KTOR_REQUEST, "setBody")
		val format = when (bodyModel.formatClassName) {
			TypeNames.SerializationFormatJson -> "Json"
			TypeNames.SerializationFormatXml -> "Xml"
			TypeNames.SerializationFormatCbor -> "Cbor"
			else -> "ProtoBuf"
		}
		addStatement("this.contentType(ContentType.Application.%N)", format)
		addStatement("this.setBody(%N)", bodyModel.varName)
	}
}
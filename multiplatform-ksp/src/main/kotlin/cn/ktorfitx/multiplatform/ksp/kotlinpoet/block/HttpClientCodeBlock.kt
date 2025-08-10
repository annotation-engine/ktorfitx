package cn.ktorfitx.multiplatform.ksp.kotlinpoet.block

import cn.ktorfitx.common.ksp.util.builders.fileSpecBuilder
import cn.ktorfitx.common.ksp.util.builders.toCodeBlock
import cn.ktorfitx.common.ksp.util.expends.asNotNullable
import cn.ktorfitx.multiplatform.ksp.constants.PackageNames
import cn.ktorfitx.multiplatform.ksp.constants.TypeNames
import cn.ktorfitx.multiplatform.ksp.model.*
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.ParameterizedTypeName
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
		
		var typeName = returnModel.typeName
		if (returnModel.returnKind == ReturnKind.Result) {
			typeName = (typeName as ParameterizedTypeName).typeArguments.first()
		}
		val rawType = typeName.asNotNullable()
		val bodyFunName = when (rawType) {
			TypeNames.String -> "bodyAsText"
			TypeNames.ByteArray -> "bodyAsBytes"
			TypeNames.ByteReadChannel -> "bodyAsChannel"
			else -> "body"
		}
		fileSpecBuilder.addImport(if (bodyFunName == "body") PackageNames.KTOR_CALL else PackageNames.KTOR_STATEMENT, bodyFunName)
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
		beginControlFlow("if (%N != null)", varName)
		addStatement("this.bearerAuth(%N)", varName)
		endControlFlow()
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
		partModels: List<PartModel>
	) {
		fileSpecBuilder.addImport(PackageNames.KTOR_HTTP, "contentType", "ContentType")
		fileSpecBuilder.addImport(PackageNames.KTOR_REQUEST, "setBody")
		fileSpecBuilder.addImport(PackageNames.KTOR_REQUEST_FORMS, "formData", "MultiPartFormDataContent")
		addStatement("this.contentType(ContentType.MultiPart.FormData)")
		beginControlFlow("formData {")
		partModels.forEach {
			addStatement("this.append(%S, %N)", it.name, it.varName)
		}
		nextControlFlow(".let")
		addStatement("this.setBody(MultiPartFormDataContent(it))")
		endControlFlow()
	}
	
	override fun CodeBlock.Builder.buildFields(
		fieldModels: List<FieldModel>,
		fieldsModels: List<FieldsModel>
	) {
		fileSpecBuilder.addImport(PackageNames.KTOR_HTTP, "contentType", "ContentType", "formUrlEncode")
		fileSpecBuilder.addImport(PackageNames.KTOR_REQUEST, "setBody")
		addStatement("this.contentType(ContentType.Application.FormUrlEncoded)")
		
		val single = fieldModels.size + fieldsModels.size == 1
		val fieldCode = fieldModels.joinToString {
			if (it.isStringType) {
				"\"${it.name}\" to ${it.varName}"
			} else {
				"\"${it.name}\" to ${it.varName}${if (it.isNullable) "?" else ""}.toString()"
			}
		}.let { if (it.isEmpty()) it else "listOf($it)" }
		val fieldsCode = fieldsModels.joinToString(separator = " + ") {
			when (it.fieldsKind) {
				FieldsKind.MAP if it.valueIsString -> "${it.varName}.toList()"
				FieldsKind.MAP if it.valueIsNullable -> "${it.varName}.map { it.key to it.value?.toString() }"
				FieldsKind.MAP -> "${it.varName}.map { it.key to it.value.toString() }"
				FieldsKind.LIST if it.valueIsString -> it.varName
				FieldsKind.LIST if it.valueIsNullable -> "${it.varName}.map { it.first to it.second?.toString() }"
				FieldsKind.LIST -> "${it.varName}.map { it.first to it.second.toString() }"
			}
		}
		val left = if (single) "" else "("
		val right = if (single) "" else ")"
		val separator = if (fieldModels.isEmpty() || fieldsModels.isEmpty()) "" else " + "
		addStatement("this.setBody(%L$fieldCode%L$fieldsCode%L.formUrlEncode())", left, separator, right)
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
				model.extensions?.let { addStatement("extensions = %L", it.toCodeBlock()) }
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
			beginControlFlow("%N.forEach { (key, value) ->", it.varName)
			addStatement("this[%T(key)] = value", TypeNames.AttributeKey)
			endControlFlow()
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
			TypeNames.SerializationFormatProtoBuf -> "ProtoBuf"
			else -> error("不支持的类型 ${bodyModel.formatClassName.simpleName}")
		}
		addStatement("this.contentType(ContentType.Application.%N)", format)
		addStatement("this.setBody(%N)", bodyModel.varName)
	}
}
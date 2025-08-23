package cn.ktorfitx.multiplatform.ksp.kotlinpoet.block

import cn.ktorfitx.common.ksp.util.expends.isHttpOrHttps
import cn.ktorfitx.multiplatform.ksp.constants.TypeNames
import cn.ktorfitx.multiplatform.ksp.model.*
import com.squareup.kotlinpoet.CodeBlock

internal class HttpCodeBlockBuilder(
	private val classModel: ClassModel,
	private val funModel: FunModel,
	private val httpRequestModel: HttpRequestModel,
	private val tokenVarName: String?
) {
	
	fun CodeBlock.Builder.buildCodeBlock() {
		buildTryCatchIfNeed {
			with(getClientCodeBlock()) {
				buildClientCodeBlock(httpRequestModel, funModel.isPrepareType) {
					when (val url = httpRequestModel.url) {
						is DynamicUrl -> buildDynamicUrl(url, classModel.apiUrl != null, funModel.pathModels)
						is StaticUrl -> {
							val (parseUrl, jointApiUrl) = parseStaticUrl(url.url)
							buildStaticUrl(parseUrl, jointApiUrl)
						}
					}
					funModel.timeoutModel?.let { buildTimeoutCodeBlock(it) }
					tokenVarName?.let { buildBearerAuth(it) }
					val headersModel = funModel.headersModel
					val headerModels = funModel.headerModels
					if (headersModel != null || headerModels.isNotEmpty()) {
						buildHeadersCodeBlock(headersModel, headerModels)
					}
					val queryModels = funModel.queryModels
					val queriesModel = funModel.queriesModels
					if (queryModels.isNotEmpty() || queriesModel.isNotEmpty()) {
						buildQueries(queryModels, queriesModel)
					}
					val cookieModels = funModel.cookieModels
					if (cookieModels.isNotEmpty()) {
						buildCookies(cookieModels)
					}
					val attributeModels = funModel.attributeModels
					val attributesModels = funModel.attributesModels
					if (attributeModels.isNotEmpty() || attributesModels.isNotEmpty()) {
						buildAttributes(attributeModels, attributesModels)
					}
					if (this@with is MockClientCodeBlock) {
						val pathModels = funModel.pathModels
						if (pathModels.isNotEmpty()) {
							buildPaths(pathModels)
						}
					}
					val requestBodyModel = funModel.requestBodyModel ?: return@buildClientCodeBlock
					when (requestBodyModel) {
						is BodyModel -> buildBody(requestBodyModel)
						is FieldRequestBodyModel -> buildFields(requestBodyModel.fieldModels, requestBodyModel.fieldsModels)
						is PartRequestBodyModel -> buildParts(requestBodyModel.partModels, requestBodyModel.partsModels)
					}
				}
			}
		}
	}
	
	private fun CodeBlock.Builder.buildTryCatchIfNeed(
		builder: CodeBlock.Builder.() -> Unit
	) {
		if (funModel.returnModel.returnKind == ReturnKind.Result) {
			beginControlFlow("return try")
			builder()
			nextControlFlow("catch (e: %T)", TypeNames.CancellationException)
			addStatement("throw e")
			nextControlFlow("catch (e: Exception)")
			addStatement("Result.failure(e)")
			endControlFlow()
		} else {
			builder()
		}
	}
	
	private fun getClientCodeBlock(): ClientCodeBlock {
		val mockModel = funModel.mockModel
		return if (mockModel != null) {
			MockClientCodeBlock(mockModel, funModel.returnModel)
		} else {
			HttpClientCodeBlock(funModel.returnModel)
		}
	}
	
	private fun parseStaticUrl(url: String): Pair<String, Boolean> {
		val parseUrl = funModel.pathModels.fold(url) { acc, it ->
			acc.replace("{${it.name}}", $$"${$${it.varName}}")
		}
		val jointApiUrl = classModel.apiUrl != null && !url.isHttpOrHttps()
		return parseUrl to jointApiUrl
	}
}
package cn.ktorfitx.multiplatform.ksp.kotlinpoet.block

import cn.ktorfitx.multiplatform.ksp.model.*
import com.squareup.kotlinpoet.CodeBlock

internal sealed interface ClientCodeBlock {
	
	/**
	 * httpClient or mockClient
	 */
	fun CodeBlock.Builder.buildClientCodeBlock(
		httpRequestModel: HttpRequestModel,
		isPrepareType: Boolean,
		builder: CodeBlock.Builder.() -> Unit
	)
	
	/**
	 * StaticUrl
	 */
	fun CodeBlock.Builder.buildStaticUrl(
		url: String,
		jointApiUrl: Boolean,
	)
	
	/**
	 * DynamicUrl
	 */
	fun CodeBlock.Builder.buildDynamicUrl(
		dynamicUrl: DynamicUrl,
		jointApiUrl: Boolean,
		pathModels: List<PathModel>
	)
	
	fun CodeBlock.Builder.buildTimeoutCodeBlock(
		timeoutModel: TimeoutModel
	)
	
	/**
	 * bearerAuth
	 */
	fun CodeBlock.Builder.buildBearerAuth(
		varName: String
	)
	
	/**
	 * headers
	 */
	fun CodeBlock.Builder.buildHeadersCodeBlock(
		headersModel: HeadersModel?,
		headerModels: List<HeaderModel>
	)
	
	/**
	 * queries
	 */
	fun CodeBlock.Builder.buildQueries(
		queryModels: List<QueryModel>,
		queriesModels: List<QueriesModel>
	)
	
	/**
	 * parts
	 */
	fun CodeBlock.Builder.buildParts(
		partModels: List<PartModel>,
		partsModels: List<PartsModel>
	)
	
	/**
	 * fields
	 */
	fun CodeBlock.Builder.buildFields(
		fieldModels: List<FieldModel>,
		fieldsModels: List<FieldsModel>
	)
	
	/**
	 * cookies
	 */
	fun CodeBlock.Builder.buildCookies(
		cookieModels: List<CookieModel>
	)
	
	/**
	 * cookies
	 */
	fun CodeBlock.Builder.buildAttributes(
		attributeModels: List<AttributeModel>,
		attributesModels: List<AttributesModel>
	)
	
	/**
	 * body
	 */
	fun CodeBlock.Builder.buildBody(
		bodyModel: BodyModel
	)
}
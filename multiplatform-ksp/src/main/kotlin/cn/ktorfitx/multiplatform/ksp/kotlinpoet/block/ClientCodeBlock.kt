package cn.ktorfitx.multiplatform.ksp.kotlinpoet.block

import cn.ktorfitx.multiplatform.ksp.model.*
import com.squareup.kotlinpoet.CodeBlock

internal sealed interface ClientCodeBlock {
	
	fun CodeBlock.Builder.buildClientCodeBlock(
		httpRequestModel: HttpRequestModel,
		isPrepareType: Boolean,
		builder: CodeBlock.Builder.() -> Unit
	)
	
	fun CodeBlock.Builder.buildStaticUrl(
		url: String,
		jointApiUrl: Boolean,
	)
	
	fun CodeBlock.Builder.buildDynamicUrl(
		dynamicUrl: DynamicUrl,
		jointApiUrl: Boolean,
		pathModels: List<PathModel>
	)
	
	fun CodeBlock.Builder.buildTimeoutCodeBlock(
		timeoutModel: TimeoutModel
	)
	
	fun CodeBlock.Builder.buildBearerAuth(
		varName: String
	)
	
	fun CodeBlock.Builder.buildHeadersCodeBlock(
		headersModel: HeadersModel?,
		headerModels: List<HeaderModel>
	)
	
	fun CodeBlock.Builder.buildQueries(
		queryModels: List<QueryModel>,
		queriesModels: List<QueriesModel>
	)
	
	fun CodeBlock.Builder.buildParts(
		partModels: List<PartModel>,
		partsModels: List<PartsModel>
	)
	
	fun CodeBlock.Builder.buildFields(
		fieldModels: List<FieldModel>,
		fieldsModels: List<FieldsModel>
	)
	
	fun CodeBlock.Builder.buildCookies(
		cookieModels: List<CookieModel>
	)
	
	fun CodeBlock.Builder.buildAttributes(
		attributeModels: List<AttributeModel>,
		attributesModels: List<AttributesModel>
	)
	
	fun CodeBlock.Builder.buildBody(
		bodyModel: BodyModel
	)
}
package cn.ktorfitx.server.ksp.model

internal class FunModel(
	val funName: String,
	val canonicalName: String,
	val isExtension: Boolean,
	val authenticationModel: AuthenticationModel?,
	val routeModel: RouteModel,
	val regexModel: RegexModel?,
	val timeoutModel: TimeoutModel?,
	val varNames: List<String>,
	val principalModels: List<PrincipalModel>,
	val queryModels: List<QueryModel>,
	val pathModels: List<PathModel>,
	val headerModels: List<HeaderModel>,
	val cookieModels: List<CookieModel>,
	val attributeModels: List<AttributeModel>,
	val requestBodyModel: RequestBodyModel?
)
package cn.ktorfitx.multiplatform.sample.api

import cn.ktorfitx.multiplatform.annotation.Api
import cn.ktorfitx.multiplatform.annotation.GET
import cn.ktorfitx.multiplatform.sample.api.impls.testWebMainApi
import cn.ktorfitx.multiplatform.sample.http.defaultKtorfitx

@Api
interface TestWebMainApi {
	
	@GET("test01")
	suspend fun test01(): String
}

suspend fun testWebMainApi() {
	defaultKtorfitx.testWebMainApi.test01()
}
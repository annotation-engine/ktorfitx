package cn.ktorfitx.multiplatform.sample.api

import cn.ktorfitx.multiplatform.annotation.Api
import cn.ktorfitx.multiplatform.annotation.GET
import cn.ktorfitx.multiplatform.sample.api.impls.testAndroidMainApi
import cn.ktorfitx.multiplatform.sample.http.defaultKtorfitx

@Api
interface TestAndroidMainApi {
	
	@GET("test01")
	suspend fun test01(): String
}

suspend fun testAndroidMainApi() {
	defaultKtorfitx.testAndroidMainApi.test01()
}
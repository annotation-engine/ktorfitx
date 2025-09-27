package cn.ktorfitx.multiplatform.sample.api

import cn.ktorfitx.multiplatform.annotation.Api
import cn.ktorfitx.multiplatform.annotation.GET
import cn.ktorfitx.multiplatform.sample.api.impls.testDesktopMainApi
import cn.ktorfitx.multiplatform.sample.http.defaultKtorfitx

@Api
interface TestDesktopMainApi {
	
	@GET("test01")
	suspend fun test01(): String
}

suspend fun testDesktopMainApi() {
	defaultKtorfitx.testDesktopMainApi.test01()
}
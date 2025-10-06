package cn.ktorfitx.multiplatform.sample.api

import cn.ktorfitx.multiplatform.annotation.Api
import cn.ktorfitx.multiplatform.annotation.GET
import cn.ktorfitx.multiplatform.sample.api.impls.desktopMainApi
import cn.ktorfitx.multiplatform.sample.http.defaultKtorfitx

@Api
interface DesktopMainApi {
	
	@GET("test01")
	suspend fun test01(): String
}

suspend fun useDesktopMainApi() {
	defaultKtorfitx.desktopMainApi.test01()
}
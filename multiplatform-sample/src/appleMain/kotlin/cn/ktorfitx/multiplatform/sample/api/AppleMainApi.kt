package cn.ktorfitx.multiplatform.cn.ktorfitx.multiplatform.sample.api

import cn.ktorfitx.multiplatform.annotation.Api
import cn.ktorfitx.multiplatform.annotation.GET
import cn.ktorfitx.multiplatform.cn.ktorfitx.multiplatform.sample.api.impls.appleMainApi
import cn.ktorfitx.multiplatform.sample.http.defaultKtorfitx

@Api
interface AppleMainApi {
	
	@GET("test01")
	suspend fun test01(): String
}

suspend fun useAppleMainApi() {
	defaultKtorfitx.appleMainApi.test01()
}
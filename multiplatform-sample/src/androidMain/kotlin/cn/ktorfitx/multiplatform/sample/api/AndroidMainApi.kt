package cn.ktorfitx.multiplatform.sample.api

import cn.ktorfitx.multiplatform.annotation.Api
import cn.ktorfitx.multiplatform.annotation.GET
import cn.ktorfitx.multiplatform.sample.api.impls.androidMainApi
import cn.ktorfitx.multiplatform.sample.http.defaultKtorfitx

@Api
interface AndroidMainApi {
	
	@GET("test01")
	suspend fun test01(): String
}

suspend fun useAndroidMainApi() {
	defaultKtorfitx.androidMainApi.test01()
}
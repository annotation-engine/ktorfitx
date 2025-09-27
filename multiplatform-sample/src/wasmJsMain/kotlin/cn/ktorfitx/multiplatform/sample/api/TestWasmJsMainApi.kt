package cn.ktorfitx.multiplatform.sample.api

import cn.ktorfitx.multiplatform.annotation.Api
import cn.ktorfitx.multiplatform.annotation.GET
import cn.ktorfitx.multiplatform.sample.api.impls.testWasmJsMainApi
import cn.ktorfitx.multiplatform.sample.http.defaultKtorfitx

@Api
interface TestWasmJsMainApi {
	
	@GET("test01")
	suspend fun test01(): String
}

suspend fun testWasmJsMainApi() {
	defaultKtorfitx.testWasmJsMainApi.test01()
}
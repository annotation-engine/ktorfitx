package cn.ktorfitx.multiplatform.sample.api

import cn.ktorfitx.multiplatform.annotation.Api
import cn.ktorfitx.multiplatform.annotation.GET
import cn.ktorfitx.multiplatform.sample.api.impls.testIosSimulatorArm64MainApi
import cn.ktorfitx.multiplatform.sample.http.defaultKtorfitx

@Api
interface TestIosSimulatorArm64MainApi {
	
	@GET("test01")
	suspend fun test01(): String
}

suspend fun testIosSimulatorArm64MainApi() {
	defaultKtorfitx.testIosSimulatorArm64MainApi.test01()
}
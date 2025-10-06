package cn.ktorfitx.multiplatform.sample.api

import cn.ktorfitx.multiplatform.annotation.Api
import cn.ktorfitx.multiplatform.annotation.GET
import cn.ktorfitx.multiplatform.sample.api.impls.iosSimulatorArm64MainApi
import cn.ktorfitx.multiplatform.sample.http.defaultKtorfitx

@Api
interface IosSimulatorArm64MainApi {
	
	@GET("test01")
	suspend fun test01(): String
}

suspend fun useIosSimulatorArm64MainApi() {
	defaultKtorfitx.iosSimulatorArm64MainApi.test01()
}
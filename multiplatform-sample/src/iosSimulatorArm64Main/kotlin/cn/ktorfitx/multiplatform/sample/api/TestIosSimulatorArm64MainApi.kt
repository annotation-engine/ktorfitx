package cn.ktorfitx.multiplatform.sample.api

import cn.ktorfitx.multiplatform.annotation.Api
import cn.ktorfitx.multiplatform.annotation.GET

@Api
interface TestIosSimulatorArm64MainApi {
	
	@GET("test01")
	suspend fun test01(): String
}
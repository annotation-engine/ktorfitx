package cn.ktorfitx.android.sample.api

import cn.ktorfitx.multiplatform.annotation.Api
import cn.ktorfitx.multiplatform.annotation.GET

@Api
interface TestOnlyAndroidApi {
	
	@GET("test01")
	suspend fun test01(): String
}
package cn.ktorfitx.multiplatform.sample.api

import cn.ktorfitx.multiplatform.annotation.Api
import cn.ktorfitx.multiplatform.annotation.GET

@Api
interface TestDesktopMainApi {
	
	@GET("test01")
	suspend fun test01(): String
}
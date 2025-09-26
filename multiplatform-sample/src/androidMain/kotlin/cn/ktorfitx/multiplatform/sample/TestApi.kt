package cn.ktorfitx.multiplatform.sample

import cn.ktorfitx.multiplatform.annotation.Api
import cn.ktorfitx.multiplatform.annotation.GET

@Api
interface TestApi {
	
	@GET("test01")
	suspend fun test01(): String
}
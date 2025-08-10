package cn.ktorfitx.multiplatform.sample.http.api

import cn.ktorfitx.multiplatform.annotation.*

@Api("httpMethod")
interface HttpMethodApi {
	
	@GET("testGet")
	suspend fun testGet(): String
	
	@BearerAuth
	@POST("testPost")
	suspend fun testPost(): Result<String>
	
	@PUT("testPut")
	suspend fun testPut(): List<String>
	
	@BearerAuth
	@DELETE("testDelete")
	suspend fun testDelete(): Result<List<String>>
	
	@PATCH("testPatch")
	suspend fun testPatch(): Result<Int>
	
	@BearerAuth
	@HEAD("testGet")
	suspend fun testHead(): Result<Int>
	
	@OPTIONS("testOptions")
	suspend fun testOptions()
	
	@BearerAuth
	@CUSTOM("testCustom")
	suspend fun testCustom()
}

@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.FUNCTION)
@HttpMethod("CUSTOM")
annotation class CUSTOM(
	val url: String = ""
)
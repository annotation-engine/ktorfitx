package cn.ktorfitx.multiplatform.sample.http.api

import cn.ktorfitx.multiplatform.annotation.Api
import cn.ktorfitx.multiplatform.annotation.GET
import cn.ktorfitx.multiplatform.annotation.Mock
import cn.ktorfitx.multiplatform.annotation.Timeout
import cn.ktorfitx.multiplatform.sample.http.mock.StringMockProvider

@Api("timeout")
interface TimeoutApi {
	
	@GET("test01")
	@Timeout(
		connectTimeoutMillis = 2000L
	)
	suspend fun test01(): String
	
	@GET("test02")
	@Timeout(
		requestTimeoutMillis = 1000L,
		connectTimeoutMillis = 2000L,
		socketTimeoutMillis = 3000L
	)
	suspend fun test02(): String
	
	@Mock(provider = StringMockProvider::class)
	@GET("testMock01")
	@Timeout(
		connectTimeoutMillis = 2000L
	)
	suspend fun testMock01(): String
	
	@Mock(provider = StringMockProvider::class)
	@GET("testMock02")
	@Timeout(
		requestTimeoutMillis = 1000L,
		connectTimeoutMillis = 2000L,
		socketTimeoutMillis = 3000L
	)
	suspend fun testMock02(): String
}
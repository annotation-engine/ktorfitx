package cn.ktorfitx.multiplatform.sample.http.api

import cn.ktorfitx.multiplatform.annotation.*
import cn.ktorfitx.multiplatform.sample.http.mock.StringMockProvider

@Api("header")
interface HeaderApi {
	
	@Headers("Accept: application/json", "Content-Type: application/json")
	@POST("test01")
	suspend fun test01(): String
	
	@POST("test02")
	suspend fun test02(
		@Header contentType: String,
		@Header("Accept") accept: String
	): String
	
	@Headers("Accept: application/json")
	@POST("test03")
	suspend fun test03(
		@Header contentType: String
	): Result<String>
	
	@Mock(provider = StringMockProvider::class)
	@Headers("Accept: application/json", "Content-Type: application/json")
	@POST("testMock01")
	suspend fun testMock01(): String
	
	@Mock(provider = StringMockProvider::class)
	@POST("testMock02")
	suspend fun testMock02(
		@Header contentType: String,
		@Header("Accept") accept: String
	): String
	
	@Mock(provider = StringMockProvider::class)
	@Headers("Accept: application/json")
	@POST("testMock03")
	suspend fun testMock03(
		@Header contentType: String
	): Result<String>
}
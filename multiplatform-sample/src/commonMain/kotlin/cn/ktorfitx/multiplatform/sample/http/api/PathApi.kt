package cn.ktorfitx.multiplatform.sample.http.api

import cn.ktorfitx.multiplatform.annotation.*
import cn.ktorfitx.multiplatform.sample.http.mock.StringMockProvider

@Api(url = "path")
interface PathApi {
	
	@GET("{user}/{id}")
	suspend fun test01(
		@Path user: String,
		@Path("id") userId: String
	): String
	
	@GET
	suspend fun test02(
		@DynamicUrl url: String
	): String
	
	@GET
	suspend fun test03(
		@DynamicUrl url: String,
		@Path name: String,
		@Path("customId") id: String
	): String
	
	@CUSTOM
	suspend fun test04(
		@DynamicUrl api: String,
	): String
	
	@Mock(provider = StringMockProvider::class)
	@GET("{user}/{id}")
	suspend fun testMock01(
		@Path user: String,
		@Path("id") userId: String
	): String
	
	@Mock(provider = StringMockProvider::class)
	@GET
	suspend fun testMock02(
		@DynamicUrl url: String
	): String
	
	@Mock(provider = StringMockProvider::class)
	@GET
	suspend fun testMock03(
		@DynamicUrl url: String,
		@Path name: String,
		@Path("customId") id: String
	): String
	
	@Mock(provider = StringMockProvider::class)
	@CUSTOM
	suspend fun testMock04(
		@DynamicUrl api: String,
	): String
}
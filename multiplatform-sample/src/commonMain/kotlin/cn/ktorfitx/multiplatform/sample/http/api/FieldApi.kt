package cn.ktorfitx.multiplatform.sample.http.api

import cn.ktorfitx.multiplatform.annotation.*
import cn.ktorfitx.multiplatform.sample.http.mock.StringMockProvider

@Api("field")
interface FieldApi {
	
	@POST("test01")
	suspend fun test01(
		@Field field1: String,
		@Field field2: Int
	): String
	
	@POST("test02")
	suspend fun test02(
		@Fields fields1: List<Pair<String, *>>?,
		@Fields fields2: Map<String, Int>
	): String
	
	@POST("test03")
	suspend fun test03(
		@Field field1: String,
		@Field field2: Int,
		@Fields fields3: List<Pair<String, *>>,
		@Fields fields4: Map<String, Int>
	): String
	
	@Mock(provider = StringMockProvider::class)
	@POST("testMock01")
	suspend fun testMock01(
		@Field field1: String,
		@Field field2: Int
	): String
	
	@Mock(provider = StringMockProvider::class)
	@POST("testMock02")
	suspend fun testMock02(
		@Fields fields1: List<Pair<String, *>>,
		@Fields fields2: Map<String, Int>
	): String
	
	@Mock(provider = StringMockProvider::class)
	@POST("testMock03")
	suspend fun testMock03(
		@Field field1: String,
		@Field field2: Int,
		@Fields fields3: List<Pair<String, *>>,
		@Fields fields4: Map<String, Int>
	): String
}
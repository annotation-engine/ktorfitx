package cn.ktorfitx.multiplatform.sample.http.api

import cn.ktorfitx.multiplatform.annotation.*
import cn.ktorfitx.multiplatform.sample.http.mock.StringMockProvider

@Api("attribute")
interface AttributeApi {
	
	@POST("test01")
	suspend fun test01(
		@Attribute name: String,
		@Attribute("custom") age: Int
	): String
	
	@POST("test01")
	suspend fun test02(
		@Attributes attributes1: Map<String, String>,
		@Attributes attributes2: List<Pair<String, Int>>
	): String
	
	@POST("test03")
	suspend fun test03(
		@Attribute name: String,
		@Attribute("custom") age: Int,
		@Attributes attributes1: Map<String, String>,
		@Attributes attributes2: List<Pair<String, Int>>
	): String
	
	@Mock(provider = StringMockProvider::class)
	@POST("testMock01")
	suspend fun testMock01(
		@Attribute name: String,
		@Attribute("custom") age: Int
	): String
	
	@Mock(provider = StringMockProvider::class)
	@POST("testMock01")
	suspend fun testMock02(
		@Attributes attributes1: Map<String, String>,
		@Attributes attributes2: List<Pair<String, Int>>
	): String
	
	@Mock(provider = StringMockProvider::class)
	@POST("testMock03")
	suspend fun testMock03(
		@Attribute name: String,
		@Attribute("custom") age: Int,
		@Attributes attributes1: Map<String, String>,
		@Attributes attributes2: List<Pair<String, Int>>
	): String
}
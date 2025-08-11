package cn.ktorfitx.multiplatform.sample.http.api

import cn.ktorfitx.multiplatform.annotation.*
import cn.ktorfitx.multiplatform.sample.http.mock.StringMockProvider
import io.ktor.client.request.forms.*

@Api("part")
interface PartApi {
	
	@POST("test01")
	suspend fun test01(
		@Part part: FormPart<String>
	): String
	
	@POST("test02")
	suspend fun test02(
		@Part item1: String,
		@Part("custom1") part2: Int,
		@Part item3: FormPart<Int>,
		@Part("custom2") part4: FormPart<Int>
	): String
	
	@POST("test03")
	suspend fun test03(
		@Part(headers = ["Content-Type: text/plain"]) item1: String
	): String
	
	@POST("test04")
	suspend fun test04(
		@Part part: FormPart<String>,
		@Part("custom1") part2: Int,
		@Parts parts3: List<FormPart<*>>,
		@Parts parts4: List<FormPart<Int>>?,
		@Parts parts5: List<Pair<String, Int>>,
		@Parts parts6: List<Pair<String, Any>>?,
		@Parts parts7: Map<String, Int>,
		@Parts parts8: Map<String, Any>?
	): String
	
	@Mock(provider = StringMockProvider::class)
	@POST("testMock01")
	suspend fun testMock01(
		@Part part: FormPart<String>
	): String
	
	@Mock(provider = StringMockProvider::class)
	@POST("testMock02")
	suspend fun testMock02(
		@Part item1: String,
		@Part("custom1") part2: Int,
		@Part item3: FormPart<Int>,
		@Part("custom2") part4: FormPart<Int>
	): String
	
	@Mock(provider = StringMockProvider::class)
	@POST("testMock03")
	suspend fun testMock03(
		@Part(headers = ["Content-Type: text/plain"]) item1: String
	): String
	
	@Mock(provider = StringMockProvider::class)
	@POST("testMock04")
	suspend fun testMock04(
		@Part part: FormPart<String>,
		@Part("custom1") part2: Int,
		@Parts parts3: List<FormPart<*>>,
		@Parts parts4: List<FormPart<Int>>?,
		@Parts parts5: List<Pair<String, Int>>,
		@Parts parts6: List<Pair<String, Any>>?,
		@Parts parts7: Map<String, Int>,
		@Parts parts8: Map<String, Any>?
	): String
}
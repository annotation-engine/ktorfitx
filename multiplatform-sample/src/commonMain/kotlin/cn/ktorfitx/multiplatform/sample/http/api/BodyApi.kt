package cn.ktorfitx.multiplatform.sample.http.api

import cn.ktorfitx.multiplatform.annotation.*
import cn.ktorfitx.multiplatform.sample.http.ApiResult
import cn.ktorfitx.multiplatform.sample.http.mock.ApiResultMockProvider
import kotlinx.serialization.Serializable

@Api("body")
interface BodyApi {
	
	@POST("test01")
	suspend fun test01(
		@Body body: BodyTest
	): Result<String>
	
	@POST("test02")
	suspend fun test02(
		@Body(format = SerializationFormat.XML) body: BodyTest
	): ApiResult<Unit>
	
	@POST("test03")
	suspend fun test03(
		@Body(format = SerializationFormat.PROTO_BUF) body: List<String>
	): ApiResult<Unit>
	
	@POST("test04")
	suspend fun test04(
		@Body(format = SerializationFormat.CBOR) body: List<Int>
	): ApiResult<Unit>
	
	@POST("test05")
	suspend fun test05(
		@Body enum: EnumTest
	): ApiResult<Unit>
	
	@Mock(provider = ApiResultMockProvider::class)
	@POST("testMock01")
	suspend fun testMock01(
		@Body body: BodyTest
	): ApiResult<Unit>
	
	@Mock(provider = ApiResultMockProvider::class)
	@POST("testMock02")
	suspend fun testMock02(
		@Body(format = SerializationFormat.XML) body: BodyTest
	): ApiResult<Unit>
	
	@Mock(provider = ApiResultMockProvider::class)
	@POST("testMock03")
	suspend fun testMock03(
		@Body(format = SerializationFormat.PROTO_BUF) body: List<String>
	): ApiResult<Unit>
	
	@Mock(provider = ApiResultMockProvider::class)
	@POST("testMock04")
	suspend fun testMock04(
		@Body(format = SerializationFormat.CBOR) body: List<Int>
	): ApiResult<Unit>
}

@Serializable
data class BodyTest(
	val data: String
)

@Serializable
enum class EnumTest {
	ENUM1,
	ENUM2
}
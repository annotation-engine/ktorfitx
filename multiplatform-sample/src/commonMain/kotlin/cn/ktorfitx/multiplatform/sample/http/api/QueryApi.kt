package cn.ktorfitx.multiplatform.sample.http.api

import cn.ktorfitx.multiplatform.annotation.*
import cn.ktorfitx.multiplatform.sample.http.mock.StringMockProvider

@Api("query")
interface QueryApi {
	
	@GET("test01")
	suspend fun test01(
		@Query query1: String,
		@Query("custom2") query2: Int
	): String
	
	@GET("test02")
	suspend fun test02(
		@Query query1: String,
		@Query("custom2") query2: Int,
		@Queries queries1: List<Pair<String, *>>,
		@Queries queries2: List<Pair<String, Int>>,
		@Queries queries3: Map<String, *>,
		@Queries queries4: Map<String, Int>
	): String
	
	@Mock(provider = StringMockProvider::class)
	@GET("testMock01")
	suspend fun testMock01(
		@Query query1: String,
		@Query("custom2") query2: Int
	): String
	
	@Mock(provider = StringMockProvider::class)
	@GET("testMock02")
	suspend fun testMock02(
		@Query query1: String,
		@Query("custom2") query2: Int,
		@Queries queries1: List<Pair<String, *>>,
		@Queries queries2: List<Pair<String, Int>>,
		@Queries queries3: Map<String, *>,
		@Queries queries4: Map<String, Int>
	): String
}
package cn.ktorfitx.multiplatform.sample.http.api

import cn.ktorfitx.multiplatform.annotation.*
import cn.ktorfitx.multiplatform.sample.http.mock.StringMockProvider

@Api("testDynamicValue")
interface TestDynamicValueApi {
	
	@POST(url = "queries01")
	suspend fun queries01(
		@Queries queries: List<Pair<String, Int>>
	): String
	
	@POST(url = "queries02")
	suspend fun queries02(
		@Queries queries: Map<String, *>,
		@Query id: Int
	): String
	
	@POST(url = "queries03")
	suspend fun queries03(
		@Queries queries1: Map<String, *>,
		@Queries queries2: List<Pair<String, String>>,
		@Query id: Int,
		@Query name: String
	): String
	
	@Mock(provider = StringMockProvider::class)
	@POST(url = "queriesMock01")
	suspend fun queriesMock01(
		@Queries queries: Map<String, *>
	): String
	
	@Mock(provider = StringMockProvider::class)
	@POST(url = "queriesMock02")
	suspend fun queriesMock02(
		@Queries queries: Map<String, *>,
		@Query id: Int
	): String
	
	@Mock(provider = StringMockProvider::class)
	@POST(url = "queriesMock03")
	suspend fun queriesMock03(
		@Queries queries1: Map<String, *>,
		@Queries queries2: Map<String, *>,
		@Query id: Int,
		@Query name: String
	): String
	
	@POST(url = "fields01")
	suspend fun fields01(
		@Fields fields: Map<String, String>
	): Result<List<String>>
	
	@POST(url = "fields02")
	suspend fun fields02(
		@Fields fields: List<Pair<String, Int?>>,
		@Field field: String
	): Result<List<String>>
	
	@POST(url = "fields03")
	suspend fun fields03(
		@Fields fields1: Map<String, Int>,
		@Fields fields2: List<Pair<String, String?>>,
		@Field field1: String,
		@Field field2: Int?
	): Result<List<String>>
	
	@Mock(provider = StringMockProvider::class)
	@POST(url = "fieldsMock01")
	suspend fun fieldsMock01(
		@Fields fields: Map<String, String>
	): String
	
	@Mock(provider = StringMockProvider::class)
	@POST(url = "fieldsMock02")
	suspend fun fieldsMock02(
		@Fields fields: List<Pair<String, Int?>>,
		@Field field: String
	): String
	
	@Mock(provider = StringMockProvider::class)
	@POST(url = "fieldsMock03")
	suspend fun fieldsMock03(
		@Fields fields1: Map<String, Int>,
		@Fields fields2: Map<String, String?>,
		@Field field1: String,
		@Field field2: Int?
	): String
	
	@POST(url = "attributes01")
	suspend fun attributes01(
		@Attributes attributes: Map<String, Int>
	): String
	
	@POST(url = "attributes01")
	suspend fun attributes02(
		@Attributes attributes: Map<String, Int>,
		@Attribute attribute1: String,
	): String
	
	@Mock(provider = StringMockProvider::class)
	@POST(url = "attributesMock01")
	suspend fun attributesMock01(
		@Attributes attributes: Map<String, Any>
	): String
	
	@Mock(provider = StringMockProvider::class)
	@POST(url = "attributesMock01")
	suspend fun attributesMock02(
		@Attributes attributes: Map<String, Int>,
		@Attribute attribute1: String,
	): String
}
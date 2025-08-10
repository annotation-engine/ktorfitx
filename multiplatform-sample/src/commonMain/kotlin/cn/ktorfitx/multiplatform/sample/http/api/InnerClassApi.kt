package cn.ktorfitx.multiplatform.sample.http.api

import cn.ktorfitx.multiplatform.annotation.Api
import cn.ktorfitx.multiplatform.annotation.GET
import cn.ktorfitx.multiplatform.annotation.Mock
import cn.ktorfitx.multiplatform.mock.MockProvider
import kotlinx.serialization.Serializable

@Api(url = "test3")
interface InnerClassApi {
	
	@GET("test1")
	suspend fun test01(): Result<Test1>
	
	@GET("test2")
	suspend fun test02(): Result<Test2>
	
	@GET("test3")
	suspend fun test03(): Result<Test3Class.Test3>
	
	@GET("test4")
	suspend fun test04(): Result<Test4Class.Test4>
	
	@Mock(provider = Test1MockProvider::class)
	@GET("test1")
	suspend fun testMock01(): Result<Test1>
	
	@Mock(provider = Test2MockProvider::class)
	@GET("test2")
	suspend fun testMock02(): Result<Test2>
	
	@Mock(provider = Test3MockProvider::class)
	@GET("test3")
	suspend fun testMock03(): Result<Test3Class.Test3>
	
	@Mock(provider = Test4MockProvider::class)
	@GET("test4")
	suspend fun testMock04(): Result<Test4Class.Test4>
	
	@Serializable
	data class Test1(val data: String)
	
	class Test3Class {
		
		@Serializable
		data class Test3(val data: String)
	}
}

@Serializable
data class Test2(val data: String)

class Test4Class {
	
	@Serializable
	data class Test4(val data: String)
}

object Test1MockProvider : MockProvider<InnerClassApi.Test1> {
	override fun provide(): InnerClassApi.Test1 {
		throw IllegalStateException()
	}
}

object Test2MockProvider : MockProvider<Test2> {
	override fun provide(): Test2 {
		throw IllegalStateException()
	}
}

object Test3MockProvider : MockProvider<InnerClassApi.Test3Class.Test3> {
	override fun provide(): InnerClassApi.Test3Class.Test3 {
		throw IllegalStateException()
	}
}

object Test4MockProvider : MockProvider<Test4Class.Test4> {
	override fun provide(): Test4Class.Test4 {
		throw IllegalStateException()
	}
}
package cn.ktorfitx.server.sample.routes

import cn.ktorfitx.server.annotation.Body
import cn.ktorfitx.server.annotation.POST
import kotlinx.serialization.Serializable

@POST("body/test1")
fun bodyTest1(
	@Body body: BodyTest1
): String = ""

@POST("body/test2")
fun bodyTest2(
	@Body body: BodyTest1?
): ApiResult<String> = error("It's not implemented.")

@POST("body/test3")
fun bodyTest3(
	@Body body: List<String>
): String = ""

@POST("body/test4")
fun bodyTest4(
	@Body body: Map<String, Boolean>
): String = ""

@POST("body/test5")
fun bodyTest5(
	@Body body: List<Pair<Int, String>>
): ApiResult<Unit> = error("It's not implemented.")

@Serializable
data class BodyTest1(
	val test: String,
)

@Serializable
data class ApiResult<T : Any>(
	val code: Int,
	val msg: String,
	val data: T?
)
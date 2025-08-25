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
): String = ""

@Serializable
data class BodyTest1(
	val test: String
)
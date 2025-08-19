package cn.ktorfitx.server.sample.routes

import cn.ktorfitx.server.annotation.POST
import cn.ktorfitx.server.annotation.Timeout

@Timeout(millis = 0L)
@POST(path = "/timeoutTest01")
fun timeoutTest01(): String {
	return ""
}
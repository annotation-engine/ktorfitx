package cn.ktorfitx.server.sample.routes

import cn.ktorfitx.server.annotation.POST
import cn.ktorfitx.server.annotation.PartForm
import cn.ktorfitx.server.annotation.Timeout
import io.ktor.http.content.*
import kotlin.time.DurationUnit

@Timeout(value = 5000L)
@POST(path = "/timeoutTest01")
fun timeoutTest01(): String = ""

@Timeout(value = 30L, unit = DurationUnit.SECONDS)
@POST(path = "/timeoutTest02")
fun timeoutTest02(
	@PartForm name: String?,
): String = ""

@Timeout(value = 1L, unit = DurationUnit.MINUTES)
@POST(path = "/timeoutTest03")
fun timeoutTest03(
	@PartForm item: PartData.FormItem,
): String = ""
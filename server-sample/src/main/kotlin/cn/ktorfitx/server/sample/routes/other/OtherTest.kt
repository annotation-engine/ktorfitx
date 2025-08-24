package cn.ktorfitx.server.sample.routes.other

import cn.ktorfitx.server.annotation.POST
import cn.ktorfitx.server.annotation.PartFile
import cn.ktorfitx.server.annotation.PartForm
import io.ktor.http.content.*

@POST("part/test1")
fun partTest1(
	@PartForm name: String,
	@PartForm("custom1") name2: PartData.FormItem?,
	@PartFile file: ByteArray?,
	@PartFile("custom2") file2: PartData.FileItem
): String = ""
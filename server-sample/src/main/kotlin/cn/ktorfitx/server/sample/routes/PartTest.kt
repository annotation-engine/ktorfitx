package cn.ktorfitx.server.sample.routes

import cn.ktorfitx.server.annotation.*
import io.ktor.http.content.*

@POST("part/test1")
fun partTest1(
	@PartForm name: String,
	@PartForm("custom1") name2: PartData.FormItem?,
	@PartFile("custom2") file2: PartData.FileItem
): String = ""

@POST("part/test2")
fun partTest2(
	@PartBinary binary: ByteArray,
	@PartBinary("custom1") binary2: PartData.BinaryItem?,
	@PartBinaryChannel channel: PartData.BinaryChannelItem,
	@PartBinaryChannel("custom2") channel2: PartData.BinaryChannelItem?
): String = ""

@POST("part/test3")
fun partTest3(
	@PartForm name: String,
	@PartFile file: PartData.FileItem,
	@PartBinary binary: ByteArray
): String = ""
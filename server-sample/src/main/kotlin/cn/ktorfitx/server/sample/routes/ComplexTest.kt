package cn.ktorfitx.server.sample.routes

import cn.ktorfitx.server.annotation.*
import cn.ktorfitx.server.annotation.Authentication
import cn.ktorfitx.server.annotation.Principal
import io.ktor.http.content.*
import io.ktor.server.auth.*

@Authentication
@POST("complex/test1")
fun complexTest1(
	@Field field: String,
	@Principal principal: UserIdPrincipal,
	@Query query: String,
): String = ""

@Authentication
@POST("complex/{name}")
fun complexTest2(
	@PartForm part: String,
	@PartForm("custom1") part2: String,
	@PartFile file: PartData.FileItem,
	@PartBinary binary: PartData.BinaryItem,
	@PartBinaryChannel binaryChannel: PartData.BinaryChannelItem,
	@Principal principal: UserIdPrincipal,
	@Query query: String,
	@Query("custom2") query2: Int,
	@Path name: String,
	@Cookie cookie: String,
	@Cookie("custom3") cookie2: String?,
	@Header authentication: String,
	@Header("Custom4") header1: String?,
	@Attribute attribute1: Int,
	@Attribute("custom5") attribute2: String?,
): String = ""
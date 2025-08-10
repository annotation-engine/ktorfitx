package cn.ktorfitx.multiplatform.sample.http.api

import cn.ktorfitx.multiplatform.annotation.*
import io.ktor.client.statement.*

@Api(url = "prepare")
interface PrepareApi {
	
	@Prepare
	@GET(url = "test01")
	suspend fun test01(): HttpStatement
	
	@Prepare
	@BearerAuth
	@POST(url = "test02")
	suspend fun test02(): HttpStatement
	
	@Prepare
	@CUSTOM(url = "test03")
	suspend fun test03(): HttpStatement
}
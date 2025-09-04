package cn.ktorfitx.server.sample.routes

import cn.ktorfitx.server.annotation.*
import cn.ktorfitx.server.sample.model.ApiResult
import cn.ktorfitx.server.sample.model.Custom
import io.ktor.server.routing.*

@GET(path = "/test1")
fun test1(): String? {
	return null
}

@Authentication
@POST(path = "/test2")
fun test2(): ApiResult<String> {
	return ApiResult(-1, "", null)
}

object Test3 {
	
	@Authentication
	@PUT(path = "/test3")
	fun RoutingContext.test3(): Int {
		return -1
	}
	
	object Test4 {
		
		@Authentication
		@DELETE(path = "/test4")
		fun RoutingContext.test4(): ApiResult<Custom> {
			return ApiResult(-1, "", Custom("xxx"))
		}
	}
}
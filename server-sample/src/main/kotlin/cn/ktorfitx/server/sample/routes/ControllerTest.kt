package cn.ktorfitx.server.sample.routes

import cn.ktorfitx.server.annotation.*
import cn.ktorfitx.server.annotation.Authentication
import cn.ktorfitx.server.sample.model.ApiResult
import io.ktor.server.auth.*
import io.ktor.server.routing.*

@GET(path = "test/test5")
fun RoutingContext.test5(): String {
	return ""
}

@Authentication
@GET(path = "test/test6")
fun test6(): ApiResult<String> {
	return ApiResult(-1, "", null)
}

@Controller(path = "test7")
object Test7 {
	
	@Authentication
	@PUT(path = "/test7")
	fun test7(): Int {
		return -1
	}
	
	@Controller(path = "test8")
	object Test8 {
		
		@Authentication(
			configurations = ["test1", "test2"],
			strategy = AuthenticationStrategy.Optional
		)
		@DELETE(path = "/test8")
		fun test8(): ApiResult<Nothing> {
			return ApiResult(-1, "", null)
		}
	}
}
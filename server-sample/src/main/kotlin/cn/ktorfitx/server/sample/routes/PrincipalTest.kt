package cn.ktorfitx.server.sample.routes

import cn.ktorfitx.server.annotation.Authentication
import cn.ktorfitx.server.annotation.GET
import cn.ktorfitx.server.annotation.Principal
import io.ktor.server.auth.*
import io.ktor.server.routing.*

@Authentication
@GET(path = "principal/test1")
fun RoutingContext.testPrincipal1(
	@Principal result: UserIdPrincipal
): String = ""

@Authentication
@GET(path = "principal/test2")
fun RoutingContext.testPrincipal2(
	@Principal principal: UserIdPrincipal?
): String = ""

@Authentication
@GET(path = "principal/test3")
fun RoutingContext.testPrincipal3(
	@Principal("custom") principal: UserIdPrincipal
): String = ""

@Authentication
@GET(path = "principal/test4")
fun RoutingContext.testPrincipal4(
	@Principal("custom") principal: UserIdPrincipal?
): String = ""

@Authentication
@GET(path = "principal/test5")
fun RoutingContext.testPrincipal5(
	@Principal principal: UserIdPrincipal,
	@Principal("custom") principal2: UserIdPrincipal,
	@Principal principal3: UserIdPrincipal?,
	@Principal("custom2") principal4: UserIdPrincipal?
): String = ""
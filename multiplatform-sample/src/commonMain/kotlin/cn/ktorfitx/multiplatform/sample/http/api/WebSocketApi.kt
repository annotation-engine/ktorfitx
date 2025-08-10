package cn.ktorfitx.multiplatform.sample.http.api

import cn.ktorfitx.multiplatform.annotation.Api
import cn.ktorfitx.multiplatform.annotation.BearerAuth
import cn.ktorfitx.multiplatform.annotation.WebSocket
import cn.ktorfitx.multiplatform.websockets.WebSocketSessionHandler
import io.ktor.client.plugins.websocket.*

@Api("websocket")
interface WebSocketApi {
	
	@WebSocket("test")
	suspend fun test(handler: suspend DefaultClientWebSocketSession.() -> Unit)
	
	@BearerAuth
	@WebSocket("/test2")
	suspend fun test2(handler: WebSocketSessionHandler)
}
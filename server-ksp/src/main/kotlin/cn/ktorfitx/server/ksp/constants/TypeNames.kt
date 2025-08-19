package cn.ktorfitx.server.ksp.constants

import com.squareup.kotlinpoet.*

internal object TypeNames {
	
	val routes by lazy {
		listOf(
			GET, POST, PUT, DELETE, PATCH, HEAD, OPTIONS,
			WebSocket, WebSocketRaw
		)
	}
	
	val httpMethods by lazy {
		listOf(GET, POST, PUT, DELETE, PATCH, HEAD, OPTIONS)
	}
	
	val partDatas by lazy {
		listOf(FormItem, FileItem, BinaryItem, BinaryChannelItem)
	}
	
	val parameterAnnotations by lazy {
		listOf(
			Principal, Body, Field, Path, Query, PartForm, PartFile,
			PartBinary, PartBinaryChannel, Header, Cookie, Attribute
		)
	}
	
	val Unit = UNIT
	
	val Nothing = NOTHING
	
	val String = STRING
	
	val ByteArray = BYTE_ARRAY
	
	val Controller = ClassName("cn.ktorfitx.server.annotation", "Controller")
	
	val GET = ClassName("cn.ktorfitx.server.annotation", "GET")
	
	val POST = ClassName("cn.ktorfitx.server.annotation", "POST")
	
	val PUT = ClassName("cn.ktorfitx.server.annotation", "PUT")
	
	val DELETE = ClassName("cn.ktorfitx.server.annotation", "DELETE")
	
	val PATCH = ClassName("cn.ktorfitx.server.annotation", "PATCH")
	
	val HEAD = ClassName("cn.ktorfitx.server.annotation", "HEAD")
	
	val OPTIONS = ClassName("cn.ktorfitx.server.annotation", "OPTIONS")
	
	val WebSocket = ClassName("cn.ktorfitx.server.annotation", "WebSocket")
	
	val WebSocketRaw = ClassName("cn.ktorfitx.server.annotation", "WebSocketRaw")
	
	val Authentication = ClassName("cn.ktorfitx.server.annotation", "Authentication")
	
	val Regex = ClassName("cn.ktorfitx.server.annotation", "Regex")
	
	val Timeout = ClassName("cn.ktorfitx.server.annotation", "Timeout")
	
	val Principal = ClassName("cn.ktorfitx.server.annotation", "Principal")
	
	val Body = ClassName("cn.ktorfitx.server.annotation", "Body")
	
	val Field = ClassName("cn.ktorfitx.server.annotation", "Field")
	
	val PartForm = ClassName("cn.ktorfitx.server.annotation", "PartForm")
	
	val PartFile = ClassName("cn.ktorfitx.server.annotation", "PartFile")
	
	val PartBinary = ClassName("cn.ktorfitx.server.annotation", "PartBinary")
	
	val PartBinaryChannel = ClassName("cn.ktorfitx.server.annotation", "PartBinaryChannel")
	
	val Query = ClassName("cn.ktorfitx.server.annotation", "Query")
	
	val Path = ClassName("cn.ktorfitx.server.annotation", "Path")
	
	val Header = ClassName("cn.ktorfitx.server.annotation", "Header")
	
	val Cookie = ClassName("cn.ktorfitx.server.annotation", "Cookie")
	
	val Attribute = ClassName("cn.ktorfitx.server.annotation", "Attribute")
	
	val HttpMethod = ClassName("cn.ktorfitx.server.annotation", "HttpMethod")
	
	val FormItem = ClassName("io.ktor.http.content", "PartData", "FormItem")
	
	val FileItem = ClassName("io.ktor.http.content", "PartData", "FileItem")
	
	val BinaryItem = ClassName("io.ktor.http.content", "PartData", "BinaryItem")
	
	val BinaryChannelItem = ClassName("io.ktor.http.content", "PartData", "BinaryChannelItem")
	
	val RoutingContext = ClassName("io.ktor.server.routing", "RoutingContext")
	
	val Routing = ClassName("io.ktor.server.routing", "Routing")
	
	val WebSocketServerSession = ClassName("io.ktor.server.websocket", "WebSocketServerSession")
	
	val DefaultWebSocketServerSession = ClassName("io.ktor.server.websocket", "DefaultWebSocketServerSession")
	
	val CookieEncodingRaw = ClassName("io.ktor.http", "CookieEncoding", "RAW")
	
	val CookieEncodingDQuotes = ClassName("io.ktor.http", "CookieEncoding", "DQUOTES")
	
	val CookieEncodingURIEncoding = ClassName("io.ktor.http", "CookieEncoding", "URI_ENCODING")
	
	val CookieEncodingBase64Encoding = ClassName("io.ktor.http", "CookieEncoding", "BASE64_ENCODING")
	
	val AuthenticationStrategyFirstSuccessful = ClassName("io.ktor.server.auth", "AuthenticationStrategy", "FirstSuccessful")
}
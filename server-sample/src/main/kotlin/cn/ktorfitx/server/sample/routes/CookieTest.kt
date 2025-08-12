package cn.ktorfitx.server.sample.routes

import cn.ktorfitx.server.annotation.Cookie
import cn.ktorfitx.server.annotation.CookieEncoding
import cn.ktorfitx.server.annotation.POST

@POST("/cookie/test1")
fun cookieTest1(
	@Cookie cookie: String,
	@Cookie("custom") cookie2: String?,
	@Cookie(encoding = CookieEncoding.RAW) cookie3: String
): String = ""

@POST("/cookie/test2")
fun cookieTest2(
	@Cookie cookie: String,
	@Cookie("custom") cookie2: String?,
	@Cookie(encoding = CookieEncoding.URI_ENCODING) cookie3: String
): String = ""

@POST("/cookie/test3")
fun cookieTest3(
	@Cookie cookie: String,
	@Cookie("custom") cookie2: String?,
	@Cookie(encoding = CookieEncoding.BASE64_ENCODING) cookie3: String
): String = ""

@POST("/cookie/test4")
fun cookieTest4(
	@Cookie cookie: String,
	@Cookie("custom") cookie2: String?,
	@Cookie(encoding = CookieEncoding.DQUOTES) cookie3: String
): String = ""
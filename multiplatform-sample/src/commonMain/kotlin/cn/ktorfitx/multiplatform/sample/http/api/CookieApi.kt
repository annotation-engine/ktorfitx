package cn.ktorfitx.multiplatform.sample.http.api

import cn.ktorfitx.multiplatform.annotation.Api
import cn.ktorfitx.multiplatform.annotation.Cookie
import cn.ktorfitx.multiplatform.annotation.Mock
import cn.ktorfitx.multiplatform.annotation.POST
import cn.ktorfitx.multiplatform.sample.http.mock.StringMockProvider

@Api("cookie")
interface CookieApi {
	
	@POST("test2")
	suspend fun getTest1(
		@Cookie(
			expires = 2000L,
			extensions = ["key:"]
		) name: String
	): String
	
	@POST("test2")
	suspend fun getTest2(
		@Cookie(
			maxAge = 10,
			expires = 2000L,
			domain = "cn.ktorfitx",
			path = "test2",
			secure = true,
			httpOnly = true,
			extensions = ["a:b"]
		) name: String,
		@Cookie(
			name = "custom",
			maxAge = 20,
			expires = 4000L,
			domain = "cn.ktorfitx",
			path = "test3",
			httpOnly = true,
			extensions = ["c:d", "e:f"]
		) name2: String
	): String
	
	@Mock(StringMockProvider::class)
	@POST("test3")
	suspend fun getTest3(
		@Cookie(
			maxAge = 10,
			expires = 2000L,
			domain = "cn.ktorfitx",
			path = "test2",
			secure = true,
			httpOnly = true,
			extensions = ["a:b"]
		) name: String,
		@Cookie(
			name = "custom",
			maxAge = 20,
			expires = 4000L,
			domain = "cn.ktorfitx",
			path = "test3",
			httpOnly = true
		) name2: String
	): String
}
package com.example.demo

import cn.ktorfitx.multiplatform.annotation.Api
import cn.ktorfitx.multiplatform.annotation.GET
import cn.ktorfitx.multiplatform.sample.http.defaultKtorfitx
import com.example.demo.impls.testApi

@Api
interface TestApi {
	
	@GET("test01")
	suspend fun test01(): String
}

suspend fun a() {
	defaultKtorfitx.testApi.test01()
}
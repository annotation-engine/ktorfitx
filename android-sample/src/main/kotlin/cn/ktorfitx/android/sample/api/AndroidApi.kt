package cn.ktorfitx.android.sample.api

import cn.ktorfitx.android.sample.api.impls.androidOnlyApi
import cn.ktorfitx.multiplatform.annotation.Api
import cn.ktorfitx.multiplatform.annotation.GET
import cn.ktorfitx.multiplatform.core.ktorfitx
import cn.ktorfitx.multiplatform.mock.config.mockClient
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

@Api
interface AndroidOnlyApi {
	
	@GET("test01")
	suspend fun test01(): String
}

suspend fun useAndroidOnlyApi() {
	defaultKtorfitx.androidOnlyApi.test01()
}

val defaultKtorfitx = ktorfitx {
	token { "<token>" }
	baseUrl = "http://localhost:8080/api/"
	httpClient(CIO) {
		engine {
			requestTimeout = 10_000L
			maxConnectionsCount = 200
		}
		install(ContentNegotiation) {
			json(
				Json {
					prettyPrint = true
					ignoreUnknownKeys = true
				}
			)
		}
	}
	mockClient {
		log {
			level = LogLevel.ALL
		}
	}
}
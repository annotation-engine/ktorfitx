package cn.ktorfitx.multiplatform.core.config

import cn.ktorfitx.multiplatform.core.Ktorfitx
import cn.ktorfitx.multiplatform.core.annotation.KtorfitDsl
import io.ktor.client.*
import io.ktor.client.engine.*
import io.ktor.client.plugins.*

@KtorfitDsl
class KtorfitxConfig internal constructor() {
	
	var baseUrl: String? = null
	
	var token: (suspend () -> String?)? = null
		private set
	
	private var _httpClient: HttpClient? = null
	
	val httpClient: HttpClient
		get() = _httpClient!!
	
	private var httpClientBlock: HttpClientBlock? = null
	
	fun <T : HttpClientEngineConfig> httpClient(
		engineFactory: HttpClientEngineFactory<T>,
		block: HttpClientConfig<T>.() -> Unit = {}
	) {
		@Suppress("UNCHECKED_CAST")
		this.httpClientBlock = HttpClientBlock(engineFactory, block as (HttpClientConfig<*>.() -> Unit))
	}
	
	fun token(token: suspend () -> String?) {
		this.token = token
	}
	
	fun build(): Ktorfitx {
		val block: HttpClientConfig<*>.() -> Unit = {
			defaultRequest {
				if (baseUrl != null) {
					url(urlString = baseUrl!!)
				}
			}
			httpClientBlock?.block?.invoke(this)
		}
		this._httpClient = httpClientBlock.let {
			if (it != null) HttpClient(it.engine, block) else HttpClient(block)
		}
		return Ktorfitx(this)
	}
	
	private class HttpClientBlock(
		val engine: HttpClientEngineFactory<*>,
		val block: HttpClientConfig<*>.() -> Unit
	)
}
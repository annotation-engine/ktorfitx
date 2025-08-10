package cn.ktorfitx.multiplatform.mock

import cn.ktorfitx.multiplatform.annotation.MockDsl
import cn.ktorfitx.multiplatform.annotation.SerializationFormat
import io.ktor.client.request.forms.*
import io.ktor.http.*
import io.ktor.util.date.*
import kotlinx.serialization.StringFormat
import kotlinx.serialization.encodeToString

@MockDsl
class MockRequestBuilder internal constructor(
	val stringFormat: StringFormat?
) {
	
	internal var urlString: String? = null
		private set
	
	internal var timeout: TimeoutConfig? = null
		private set
	
	private val _headers = mutableMapOf<String, Any>()
	internal val headers: Map<String, Any> = _headers
	
	private val _queries = mutableMapOf<String, Any?>()
	internal val queries: Map<String, Any?> = _queries
	
	private val _parts = mutableListOf<FormPart<*>>()
	internal val parts: List<FormPart<*>> = _parts
	
	private val _fields = mutableMapOf<String, Any?>()
	internal val fields: Map<String, Any?> = _fields
	
	private val _paths = mutableMapOf<String, Any>()
	internal val paths: Map<String, Any> = _paths
	
	private val _cookies = mutableMapOf<String, CookieConfig>()
	internal val cookies: Map<String, CookieConfig> = _cookies
	
	private val _attributes = mutableMapOf<String, Any>()
	internal val attributes: Map<String, Any> = _attributes
	
	var body: MockBody? = null
	
	fun url(urlString: String) {
		this.urlString = urlString
	}
	
	fun timeout(block: TimeoutConfig.() -> Unit) {
		this.timeout = TimeoutConfigImpl().apply(block)
	}
	
	fun bearerAuth(token: String) {
		this._headers[HttpHeaders.Authorization] = "Bearer $token"
	}
	
	fun headers(block: MutableMap<String, Any>.() -> Unit) {
		this._headers += mutableMapOf<String, Any>().apply(block)
	}
	
	fun queries(block: MutableMap<String, Any?>.() -> Unit) {
		this._queries += mutableMapOf<String, Any?>().apply(block)
	}
	
	fun parts(block: MutableList<FormPart<*>>.() -> Unit) {
		this._parts += mutableListOf<FormPart<*>>().apply(block)
	}
	
	fun fields(block: MutableMap<String, Any?>.() -> Unit) {
		this._fields += mutableMapOf<String, Any?>().apply(block)
	}
	
	fun paths(block: MutableMap<String, Any>.() -> Unit) {
		this._paths += mutableMapOf<String, Any>().apply(block)
	}
	
	fun cookies(block: CookieBuilder.() -> Unit) {
		this._cookies += CookieBuilderImpl().apply(block).cookies
	}
	
	fun attributes(block: MutableMap<String, Any>.() -> Unit) {
		this._attributes += mutableMapOf<String, Any>().apply(block)
	}
	
	inline fun <reified T : Any> body(body: T, format: SerializationFormat) {
		this.body = MockBody(stringFormat?.encodeToString(body) ?: body.toString(), format)
	}
	
	fun <V> MutableMap<String, V>.append(name: String, value: V) {
		this[name] = value
	}
}

@MockDsl
sealed interface CookieBuilder {
	
	fun append(
		name: String,
		value: String,
		maxAge: Int = 0,
		expires: GMTDate? = null,
		domain: String? = null,
		path: String? = null,
		secure: Boolean = false,
		httpOnly: Boolean = false,
		extensions: Map<String, String?> = emptyMap()
	)
}

@MockDsl
private class CookieBuilderImpl : CookieBuilder {
	
	val cookies = mutableMapOf<String, CookieConfig>()
	
	override fun append(
		name: String,
		value: String,
		maxAge: Int,
		expires: GMTDate?,
		domain: String?,
		path: String?,
		secure: Boolean,
		httpOnly: Boolean,
		extensions: Map<String, String?>
	) {
		this.cookies[name] = CookieConfig(
			value = value,
			maxAge = maxAge,
			expires = expires,
			domain = domain,
			path = path,
			secure = secure,
			httpOnly = httpOnly,
			extensions = extensions
		)
	}
}

internal class CookieConfig(
	val value: Any,
	val maxAge: Int,
	val expires: GMTDate?,
	val domain: String?,
	val path: String?,
	val secure: Boolean,
	val httpOnly: Boolean,
	val extensions: Map<String, String?>
)

@MockDsl
sealed interface TimeoutConfig {
	var requestTimeoutMillis: Long?
	var connectTimeoutMillis: Long?
	var socketTimeoutMillis: Long?
}

@MockDsl
private class TimeoutConfigImpl(
	override var requestTimeoutMillis: Long? = null,
	override var connectTimeoutMillis: Long? = null,
	override var socketTimeoutMillis: Long? = null
) : TimeoutConfig

data class MockBody(
	val json: String,
	val format: SerializationFormat,
)
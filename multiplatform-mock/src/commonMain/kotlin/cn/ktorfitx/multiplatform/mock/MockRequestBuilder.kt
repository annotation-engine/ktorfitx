package cn.ktorfitx.multiplatform.mock

import cn.ktorfitx.multiplatform.annotation.MockDsl
import cn.ktorfitx.multiplatform.annotation.SerializationFormat
import io.ktor.client.request.forms.*
import io.ktor.http.*
import io.ktor.util.date.*
import kotlinx.io.Source
import kotlinx.serialization.StringFormat
import kotlinx.serialization.encodeToString

@MockDsl
class MockRequestBuilder internal constructor(
	val stringFormat: StringFormat?
) {
	
	internal var urlString: String? = null
		private set
	
	internal var timeout: TimeoutBuilder? = null
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
	
	var body: BodyConfig? = null
	
	fun url(urlString: String) {
		this.urlString = urlString
	}
	
	fun timeout(block: TimeoutBuilder.() -> Unit) {
		this.timeout = TimeoutBuilder().apply(block)
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
	
	fun parts(block: PartBuilder.() -> Unit) {
		this._parts += PartBuilder().apply(block).build()
	}
	
	fun fields(block: MutableMap<String, Any?>.() -> Unit) {
		this._fields += mutableMapOf<String, Any?>().apply(block)
	}
	
	fun paths(block: MutableMap<String, Any>.() -> Unit) {
		this._paths += mutableMapOf<String, Any>().apply(block)
	}
	
	fun cookies(block: CookieBuilder.() -> Unit) {
		this._cookies += CookieBuilder().apply(block).build()
	}
	
	fun attributes(block: MutableMap<String, Any>.() -> Unit) {
		this._attributes += mutableMapOf<String, Any>().apply(block)
	}
	
	inline fun <reified T : Any> body(body: T, format: SerializationFormat) {
		this.body = BodyConfig(stringFormat?.encodeToString(body) ?: body.toString(), format)
	}
	
	fun <V> MutableMap<String, V>.append(name: String, value: V) {
		this[name] = value
	}
}

@MockDsl
class CookieBuilder internal constructor() {
	
	private val cookies = mutableMapOf<String, CookieConfig>()
	
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
	
	internal fun build(): Map<String, CookieConfig> = cookies
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
class TimeoutBuilder internal constructor(
	var requestTimeoutMillis: Long? = null,
	var connectTimeoutMillis: Long? = null,
	var socketTimeoutMillis: Long? = null
)

class BodyConfig(
	val json: String,
	val format: SerializationFormat,
)

@MockDsl
class PartBuilder internal constructor() {
	
	internal val parts = mutableListOf<FormPart<*>>()
	
	fun append(key: String, value: String, headers: Headers = Headers.Empty) {
		this.parts += FormPart(key, value, headers)
	}
	
	fun append(key: String, value: Number, headers: Headers = Headers.Empty) {
		this.parts += FormPart(key, value, headers)
	}
	
	fun append(key: String, value: ByteArray, headers: Headers = Headers.Empty) {
		this.parts += FormPart(key, value, headers)
	}
	
	fun append(key: String, value: InputProvider, headers: Headers = Headers.Empty) {
		this.parts += FormPart(key, value, headers)
	}
	
	fun append(key: String, value: Source, headers: Headers = Headers.Empty) {
		this.parts += FormPart(key, value, headers)
	}
	
	fun append(key: String, values: Iterable<String>, headers: Headers = Headers.Empty) {
		require(key.endsWith("[]")) {
			"Array parameter must be suffixed with square brackets ie `$key[]`"
		}
		values.forEach { value ->
			this.parts += FormPart(key, value, headers)
		}
	}
	
	fun append(key: String, values: Array<String>, headers: Headers = Headers.Empty) {
		this.append(key, values.asIterable(), headers)
	}
	
	fun append(key: String, value: ChannelProvider, headers: Headers = Headers.Empty) {
		this.parts += FormPart(key, value, headers)
	}
	
	fun <T : Any> append(part: FormPart<T>) {
		this.parts += part
	}
	
	internal fun build(): List<FormPart<*>> = parts
}
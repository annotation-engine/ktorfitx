package cn.ktorfitx.multiplatform.mock

import cn.ktorfitx.multiplatform.mock.config.LogConfig
import io.ktor.client.plugins.logging.LogLevel.*
import io.ktor.http.*
import kotlinx.serialization.KSerializer
import kotlinx.serialization.StringFormat
import kotlin.time.Duration.Companion.milliseconds

class MockLogging internal constructor(
	val log: LogConfig,
	private val mockRequest: MockRequestBuilder,
	urlString: String,
) {
	
	val urlString: String by lazy {
		formatUrl(urlString, mockRequest.paths, mockRequest.queries)
	}
	
	fun request(method: HttpMethod) {
		if (log.level != NONE) {
			buildString {
				if (log.level.info) {
					appendLine("REQUEST: $urlString")
					appendLine("METHOD: ${method.value}")
					val timeout = mockRequest.timeout
					if (timeout != null) {
						appendLine("TIMEOUT:")
						if (timeout.requestTimeoutMillis != null) {
							appendLine("-> Request Timeout: ${timeout.requestTimeoutMillis}ms")
						}
						if (timeout.connectTimeoutMillis != null) {
							appendLine("-> Connect Timeout: ${timeout.connectTimeoutMillis}ms")
						}
						if (timeout.socketTimeoutMillis != null) {
							appendLine("-> Socket Timeout: ${timeout.socketTimeoutMillis}ms")
						}
					}
				}
				if (log.level.headers) {
					if (mockRequest.headers.isNotEmpty()) {
						appendLine("HEADERS: COUNT=${mockRequest.headers.size}")
						mockRequest.headers.forEach { (name, value) ->
							appendLine("-> $name: $value")
						}
					}
					if (mockRequest.attributes.isNotEmpty()) {
						appendLine("ATTRIBUTES: COUNT=${mockRequest.attributes.size}")
						mockRequest.attributes.forEach { (name, value) ->
							appendLine("-> $name: $value")
						}
					}
					if (mockRequest.cookies.isNotEmpty()) {
						appendLine("COOKIES: COUNT=${mockRequest.cookies.size}")
						mockRequest.cookies.forEach { (name, value) ->
							val value = buildString {
								append("-> $name: $value")
								if (value.maxAge > 0) {
									append(", max-age=$value")
								}
								if (value.expires != null) {
									append(", expires=${value.expires}")
								}
								if (value.domain != null) {
									append(", domain=${value.domain}")
								}
								if (value.path != null) {
									append(", path=${value.path}")
								}
								if (value.secure) {
									append(", secure=true")
								}
								if (value.httpOnly) {
									append(", httpOnly=true")
								}
								if (value.extensions.isNotEmpty()) {
									append(", extensions=${value.extensions}")
								}
							}
							appendLine("-> $name: $value")
						}
					}
				}
				if (log.level.body) {
					if (mockRequest.parts.isNotEmpty()) {
						appendLine("FORMS: COUNT=${mockRequest.parts.size}")
						mockRequest.parts.forEach { (name, value) ->
							appendLine("-> $name: $value")
						}
					}
					if (mockRequest.fields.isNotEmpty()) {
						appendLine("FIELDS: COUNT=${mockRequest.fields.size}")
						mockRequest.fields.forEach { (name, value) ->
							appendLine("-> $name: $value")
						}
					}
					if (mockRequest.body != null) {
						val body = mockRequest.body!!
						appendLine("BODY START: LENGTH=${body.json.length} FORMAT=${body.format}")
						appendLine(mockRequest.body!!)
						appendLine("BODY END")
					}
				}
			}.let(log.logger)
		}
	}
	
	internal fun <R> response(
		result: R,
		serializer: KSerializer<R>,
		format: StringFormat?,
		delay: Long
	) {
		if (log.level != NONE) {
			buildString {
				if (log.level.info) {
					appendLine("RESPONSE: $urlString")
					appendLine("DELAY TIME: ${delay.milliseconds}")
				}
				if (log.level.body) {
					val text = format?.encodeToString(serializer, result) ?: result.toString()
					appendLine("BODY START: LENGTH=${text.length}")
					appendLine(text)
					appendLine("BODY END")
				}
			}.also(log.logger)
		}
	}
	
	private fun formatUrl(
		urlString: String,
		paths: Map<String, Any>,
		queries: Map<String, Any?>
	): String = buildString {
		val url = paths.entries.fold(urlString) { acc, path ->
			acc.replace("{${path.key}}", path.value.toString())
		}
		append(url)
		queries.entries.forEachIndexed { index, query ->
			append(if (index == 0) '?' else '&')
			append(query.key)
			append('=')
			append(query.value)
		}
	}
}
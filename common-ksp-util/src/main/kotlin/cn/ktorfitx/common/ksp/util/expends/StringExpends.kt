package cn.ktorfitx.common.ksp.util.expends

private val lowerCamelCaseRegex by lazy { "^[a-z][a-zA-Z0-9]*$".toRegex() }

/**
 * 判断是否是小驼峰命名
 */
fun String.isLowerCamelCase(): Boolean {
	return lowerCamelCaseRegex.matches(this)
}

/**
 * 改为小驼峰命名
 */
fun String.toLowerCamelCase(): String {
	if (this.isBlank()) return this
	
	return if ('_' in this) {
		this.split('_')
			.filter { it.isNotBlank() }
			.joinToString("") { part ->
				part.lowercase().replaceFirstToUppercase()
			}
			.replaceFirstToLowercase()
	} else {
		this.lowercase()
	}
}

private val httpMethodRegex by lazy { "^[A-Z0-9-]+$".toRegex() }

fun String.isValidHttpMethod(): Boolean {
	return httpMethodRegex.matches(this)
}

/**
 * 替换首字母为小写
 */
fun String.replaceFirstToLowercase(): String {
	return this.replaceFirstChar { it.lowercaseChar() }
}

/**
 * 替换首字母为大写
 */
fun String.replaceFirstToUppercase(): String {
	return this.replaceFirstChar { it.uppercaseChar() }
}

private const val HTTP = "http://"
private const val HTTPS = "https://"
private const val WS = "ws://"
private const val WSS = "wss://"

private const val SCHEME_SEPARATOR = "://"

/**
 * http or https
 */
fun String.isHttpOrHttps(): Boolean {
	return this.startsWith(HTTP) || this.startsWith(HTTPS)
}

/**
 * ws or wss
 */
fun String.isWSOrWSS(): Boolean {
	return this.startsWith(WS) || this.startsWith(WSS)
}

/**
 * 包含 ://
 */
fun String.containsSchemeSeparator(): Boolean {
	return SCHEME_SEPARATOR in this
}

private val headerCaseRegex by lazy { "([a-z])([A-Z])".toRegex() }

fun String.camelToHeaderCase(): String {
	return this.replace(headerCaseRegex) {
		"${it.groupValues[1]}-${it.groupValues[2]}"
	}.replaceFirstChar { it.uppercaseChar() }
}

fun String.isValidRegex(
	options: Set<RegexOption> = emptySet(),
): Boolean {
	return try {
		if (options.isEmpty()) {
			this.toRegex()
		} else if (options.size == 1) {
			this.toRegex(options.first())
		} else {
			this.toRegex(options)
		}
		true
	} catch (_: Exception) {
		false
	}
}

private val headerRegex by lazy { "^\\s*([^:\\s]+)\\s*:\\s*(.*?)\\s*$".toRegex() }

/**
 * 解析 Header
 */
fun String.parseHeader(): Pair<String, String>? {
	val result = headerRegex.matchEntire(this.trim()) ?: return null
	val (key, value) = result.destructured
	return if (key.isNotEmpty()) key to value else null
}
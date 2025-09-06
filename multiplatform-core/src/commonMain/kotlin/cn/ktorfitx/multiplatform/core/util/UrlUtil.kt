package cn.ktorfitx.multiplatform.core.util

object UrlUtil {
	
	private const val SCHEME_SEPARATOR = "://"
	
	fun parseDynamicUrl(
		url: String,
		apiUrl: String?,
		paths: Map<String, Any>?
	): String {
		val initialUrl = when {
			apiUrl == null || SCHEME_SEPARATOR in url -> url
			else -> "$apiUrl/$url"
		}
		if (paths == null) return initialUrl
		return paths.toList().fold(initialUrl) { acc, it ->
			acc.replace("{${it.first}}", it.second.toString())
		}
	}
	
	fun parseUrl(
		url: String,
		vararg args: Pair<String, Any>
	): String {
		if (args.isEmpty()) return url
		TODO()
	}
}
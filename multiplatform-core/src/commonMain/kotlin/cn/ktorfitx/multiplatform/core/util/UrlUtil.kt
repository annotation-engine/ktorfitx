package cn.ktorfitx.multiplatform.core.util

object UrlUtil {
	
	private const val SCHEME_SEPARATOR = "://"
	
	fun parseDynamicUrl(
		url: String,
		apiUrl: String?,
		vararg args: Pair<String, Any>
	): String {
		val jointUrl = when {
			apiUrl == null || SCHEME_SEPARATOR in url -> url
			else -> "${apiUrl.trim('/')}/${url.trim('/')}"
		}
		if (args.isEmpty()) return jointUrl
		return args.fold(jointUrl) { acc, (key, value) ->
			acc.replace("{$key}", value.toString())
		}
	}
}
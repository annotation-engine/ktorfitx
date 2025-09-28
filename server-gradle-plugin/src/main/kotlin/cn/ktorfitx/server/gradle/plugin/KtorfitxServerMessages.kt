package cn.ktorfitx.server.gradle.plugin

internal typealias MessageConfig = Message.() -> Unit

internal val languageLocal = ThreadLocal<KtorfitxLanguage>()

internal operator fun MessageConfig.invoke(vararg args: Any?): String {
	val config = Message().apply(this)
	val message = when (languageLocal.get()) {
		KtorfitxLanguage.CHINESE -> config.chinese ?: config.english
		KtorfitxLanguage.ENGLISH -> config.english
	}?.invoke() ?: ""
	if (args.isEmpty()) return message
	return args.foldIndexed(message) { index, acc, arg ->
		val value = when (arg) {
			is String -> arg
			else -> arg.toString()
		}
		acc.replace("{${index + 1}}", value)
	}
}

internal class Message internal constructor() {
	
	internal var english: (() -> String)? = null
	internal var chinese: (() -> String)? = null
	
	fun english(message: () -> String) {
		this.english = message
	}
	
	fun chinese(message: () -> String) {
		this.chinese = message
	}
}

private fun message(config: MessageConfig): MessageConfig = config

internal val MISSING_DEPENDENCIES = message {
	english { "Please add implementation(\"{1}:{2}\") to build.gradle.kts" }
	chinese { "请在 build.gradle.kts 中添加 implementation(\"{1}:{2}\")" }
}

internal val MISSING_GRADLE_PLUGIN = message {
	english { "Please add id(\"{1}\") to build.gradle.kts" }
	chinese { "请在 build.gradle.kts 中添加 id(\"{1}\")" }
}

internal val VERSION_NOT_MATCH = message {
	english { "The version of dependency \"{1}\" is \"{2}\", but the required version is \"{3}\"." }
	chinese { "依赖项 \"{1}\" 的版本是 \"{2}\"，但是需要 \"{3}\" 版本" }
}
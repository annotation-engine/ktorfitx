package cn.ktorfitx.common.ksp.util.message

import com.google.devtools.ksp.symbol.KSName

typealias MessageConfig = Message.() -> Unit

fun MessageConfig.getString(vararg args: Any?): String {
	val config = Message().apply(this)
	val message = when (Language.get()) {
		Language.CHINESE -> config.chinese ?: config.english
		Language.ENGLISH -> config.english
	}?.invoke() ?: ""
	if (args.isEmpty()) return message
	return args.foldIndexed(message) { index, acc, arg ->
		val value = when (arg) {
			is String -> arg
			is KSName -> arg.asString()
			else -> arg.toString()
		}
		acc.replace("{${index + 1}}", value)
	}
}

class Message internal constructor() {
	
	internal var english: (() -> String)? = null
	internal var chinese: (() -> String)? = null
	
	fun english(message: () -> String) {
		this.english = message
	}
	
	fun chinese(message: () -> String) {
		this.chinese = message
	}
}

fun message(config: MessageConfig): MessageConfig = config
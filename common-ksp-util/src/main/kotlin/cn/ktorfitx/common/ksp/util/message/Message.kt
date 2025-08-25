package cn.ktorfitx.common.ksp.util.message

import com.google.devtools.ksp.symbol.KSName

interface Message {
	
	val english: () -> String
	
	val chinese: () -> String
	
	companion object Companion {
		
		val language = ThreadLocal<String>()
	}
}

fun Message.format(vararg args: Any?): String {
	val hint = when (Message.language.get()) {
		"CHINESE" -> this.chinese()
		else -> this.english()
	}
	return args.foldIndexed(hint) { index, acc, arg ->
		val value = when (arg) {
			is String -> arg
			is KSName -> arg.asString()
			else -> arg.toString()
		}
		acc.replace("{${index + 1}}", value)
	}
}
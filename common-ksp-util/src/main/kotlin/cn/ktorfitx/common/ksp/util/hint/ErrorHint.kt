package cn.ktorfitx.common.ksp.util.hint

import com.google.devtools.ksp.symbol.KSName

interface ErrorHint {
	
	val english: () -> String
	
	val chinese: () -> String
	
	companion object {
		
		val language = ThreadLocal<String>()
	}
}

fun ErrorHint.format(vararg args: Any?): String {
	val hint = when (ErrorHint.language.get()) {
		"CHINESE" -> this.chinese()
		"ENGLISH" -> this.english()
		else -> return ""
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
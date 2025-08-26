package cn.ktorfitx.common.ksp.util.message

import com.google.devtools.ksp.symbol.KSName

interface Message {
	
	val english: () -> String
	
	val chinese: () -> String
}

private val languageLocal = ThreadLocal<Language>()

fun setLanguage(language: String) {
	languageLocal.set(
		when (language) {
			"CHINESE" -> Language.CHINESE
			else -> Language.ENGLISH
		}
	)
}

private enum class Language {
	CHINESE,
	ENGLISH
}

fun Message.format(vararg args: Any?): String {
	val hint = when (languageLocal.get()) {
		Language.CHINESE -> this.chinese()
		Language.ENGLISH -> this.english()
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
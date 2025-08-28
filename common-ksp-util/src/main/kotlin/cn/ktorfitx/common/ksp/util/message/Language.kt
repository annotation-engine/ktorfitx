package cn.ktorfitx.common.ksp.util.message

import cn.ktorfitx.common.ksp.util.exception.KtorfitxConfigErrorException

enum class Language {
	CHINESE,
	ENGLISH;
	
	companion object {
		private val current = ThreadLocal<Language>()
		
		fun get(): Language = current.get()
		
		fun set(value: String) {
			this.current.set(
				when (value) {
					"CHINESE" -> CHINESE
					"ENGLISH" -> ENGLISH
					else -> throw KtorfitxConfigErrorException("Invalid language: $value")
				}
			)
		}
	}
}
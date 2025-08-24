package cn.ktorfitx.common.ksp.util.hint

import kotlinx.serialization.json.JsonElement

enum class CommonErrorHint : ErrorHint {
	UNKNOWN,
	ERROR_LOCATION;
	
	companion object : ErrorHintCompanion() {
		
		override val languagePathMap: Map<String, String> by lazy {
			mapOf(
				"CHINESE" to "common-ksp-util/src/main/resources/hint/zh.json",
				"ENGLISH" to "common-ksp-util/src/main/resources/hint/zh.json"
			)
		}
	}
	
	override val jsonElement: JsonElement?
		get() = getJsonElement()
}
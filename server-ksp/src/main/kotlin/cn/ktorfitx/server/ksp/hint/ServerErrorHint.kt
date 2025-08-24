package cn.ktorfitx.server.ksp.hint

import cn.ktorfitx.common.ksp.util.hint.ErrorHint
import cn.ktorfitx.common.ksp.util.hint.ErrorHintCompanion
import kotlinx.serialization.json.JsonElement

enum class ServerErrorHint : ErrorHint {
	TOP_LEVEL_OR_OBJECT_ONLY,
	MUST_USE_ONE_OF_ANNOTATIONS,
	ONLY_USE_ONE_OF_ANNOTATIONS;
	
	internal companion object : ErrorHintCompanion() {
		
		override val languagePathMap: Map<String, String> by lazy {
			mapOf(
				"CHINESE" to "server-ksp/src/main/resources/hint/zh.json",
				"ENGLISH" to "server-ksp/src/main/resources/hint/zh.json"
			)
		}
	}
	
	override val jsonElement: JsonElement?
		get() = getJsonElement()
}
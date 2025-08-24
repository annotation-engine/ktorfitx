package cn.ktorfitx.common.ksp.util.hint

import cn.ktorfitx.common.ksp.util.expends.formatHint
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.io.File

interface ErrorHint {
	
	val jsonElement: JsonElement?
	
	fun format(vararg args: Any?): String {
		val hint = jsonElement?.jsonObject[this.toString()]
			?.jsonPrimitive?.content ?: return ""
		return hint.formatHint(*args)
	}
}

abstract class ErrorHintCompanion {
	
	private val jsonElement = ThreadLocal<JsonElement>()
	
	abstract val languagePathMap: Map<String, String>
	
	fun load(language: String) {
		val path = languagePathMap[language] ?: return
		val json = File(path).readText()
		jsonElement.set(Json.parseToJsonElement(json))
	}
	
	fun getJsonElement(): JsonElement? = jsonElement.get()
	
	fun release() {
		jsonElement.set(null)
	}
}
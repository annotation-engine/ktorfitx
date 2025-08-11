package cn.ktorfitx.multiplatform.ksp.model

internal class PartModel(
	val name: String,
	val varName: String,
	val headers: Map<String, String>?,
	val partKind: PartKind
)

internal enum class PartKind {
	DIRECT,
	FORM_PART,
	KEY_VALUE
}
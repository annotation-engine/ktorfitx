package cn.ktorfitx.multiplatform.ksp.model

internal class PartsModel(
	val varName: String,
	val partsKind: PartsKind,
	val valueKind: PartsValueKind?
)

internal enum class PartsKind {
	MAP,
	LIST_PAIR,
	LIST_FORM_PART
}

internal enum class PartsValueKind {
	FORM_PART,
	KEY_VALUE
}
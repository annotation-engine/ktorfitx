package cn.ktorfitx.multiplatform.ksp.model

internal class AttributesModel(
	val varName: String,
	val isNullable: Boolean,
	val attributesKind: AttributesKind
)

internal enum class AttributesKind {
	MAP,
	LIST
}
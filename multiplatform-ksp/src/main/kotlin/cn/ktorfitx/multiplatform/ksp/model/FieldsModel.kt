package cn.ktorfitx.multiplatform.ksp.model

internal class FieldsModel(
	val varName: String,
	val fieldsKind: FieldsKind,
	val isNullable: Boolean,
	val valueIsString: Boolean,
	val valueIsNullable: Boolean
)

internal enum class FieldsKind {
	LIST,
	MAP
}
package cn.ktorfitx.common.ksp.util.hint

internal enum class CommonErrorHint(
	override val chinese: () -> String,
	override val english: () -> String
) : ErrorHint {
	UNKNOWN(
		chinese = { "未知" },
		english = { "Unknown." }
	),
	ERROR_LOCATION(
		chinese = { "错误位置：" },
		english = { "Error location: " }
	)
}
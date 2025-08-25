package cn.ktorfitx.common.ksp.util.message

internal enum class CommonMessage(
	override val chinese: () -> String,
	override val english: () -> String
) : Message {
	UNKNOWN(
		chinese = { "未知" },
		english = { "Unknown." }
	),
	ERROR_LOCATION(
		chinese = { "错误位置：" },
		english = { "Error location: " }
	)
}
package cn.ktorfitx.server.ksp.hint

import cn.ktorfitx.common.ksp.util.hint.ErrorHint

enum class ServerErrorHint(
	override val chinese: () -> String,
	override val english: () -> String,
) : ErrorHint {
	TOP_LEVEL_OR_OBJECT_ONLY(
		chinese = { "{1} 函数只允许声明在 文件顶层 或 object 类中" },
		english = { "The {1} function can only be declared at the top level of a file or within an object class." }
	),
	MUST_USE_ONE_OF_ANNOTATIONS(
		chinese = { "{1} 函数的 {2} 参数必须使用 {3} 注解中的一个" },
		english = { "The {2} parameter of the {1} function must use one of the annotations in {3}." },
	),
	ONLY_USE_ONE_OF_ANNOTATIONS(
		chinese = { "{1} 函数的 {2} 参数只允许使用 {3} 注解中的一个" },
		english = { "The {2} parameter of the {1} function is restricted to use only one of the annotations in {3}." }
	)
}
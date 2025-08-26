package cn.ktorfitx.multiplatform.ksp.message

import cn.ktorfitx.common.ksp.util.message.Message

internal enum class MultiplatformMessage(
	override val chinese: () -> String,
	override val english: () -> String
) : Message {
	INTERFACE_NOT_ALLOW_GENERICS(
		chinese = { "{1} 接口不允许包含泛型" },
		english = { "The {1} interface does not allow generics." }
	),
	INTERFACE_MUST_BE_DECLARED_PUBLIC_OR_INTERNAL_ACCESS_PERMISSION(
		chinese = { "{1} 接口必须声明为 public 或 internal 访问权限" },
		english = { "The {1} interface must be declared with public or internal access permission." }
	),
	INTERFACE_MUST_BE_INTERFACE_BECAUSE_MARKED_API(
		chinese = { "{1} 必须是 interface，因为您标记了 @Api 注解" },
		english = { "xx must be an interface because you have marked the @Api annotation." }
	),
	INTERFACE_NOT_SUPPORT_SEALED_MODIFIER(
		chinese = { "{1} 接口不支持 \"sealed\" 修饰符" },
		english = { "The {1} interface does not support the \"sealed\" modifier." }
	),
	INTERFACE_MUST_BE_PLACED_FILE_TOP_LEVEL(
		chinese = { "{1} 接口必须在文件顶层" },
		english = { "The {1} interface must be placed at the top level of the file." }
	),
	ANNOTATION_NOT_ALLOW_USE_PROTOCOL_FROM_STRINGS(
		chinese = { "{1} 接口上的 @Api 注解中不允许出现包含 \"://\" 的协议形式字符串，例如：\"http://\" 或 \"https://\"" },
		english = { "The @Api annotation on the {1} interface does not allow the use of protocol-form strings containing \"://\", such as \"http://\" or \"https://\"." }
	),
	ANNOTATION_URL_PARAMETER_FORMAT_INCORRECT(
		chinese = { "{1} 接口上的 @Api 注解的 url 参数格式错误" },
		english = { "The format of the url parameter in the @Api annotation on the {1} interface is incorrect." }
	),
	ANNOTATION_SCOPES_PARAMETER_NOT_ALLOW_NULLABLE(
		chinese = { "{1} 接口上的 @ApiScope 注解的 scopes 参数不允许为空" },
		english = { "The scopes parameter of the @ApiScope annotation on the {1} interface is not allowed to be empty." }
	),
	ANNOTATION_SCOPES_NOT_ALLOWED_USE_SAME_CLASS_NAME_K_CLASS(
		chinese = { "{1} 接口上的 @ApiScope 注解的 scopes 参数不允许使用相同类名的 KClass<*>" },
		english = { "The scopes parameter of the @ApiScope annotation on the {1} interface is not allowed to use the KClass<*> of the same class name." }
	),
	FUNCTION_LACKS_SUSPEND_MODIFIER(
		chinese = { "{1} 函数缺少 \"suspend\" 修饰符" },
		english = { "The {1} function lacks the \"suspend\" modifier." }
	),
	FUNCTION_HAS_BEEN_WEBSOCKET_SO_RETURN_TYPE_MUST_BE_UNIT(
		chinese = { "{1} 函数已标注 @WebSocket 注解，因此返回类型必须为 Unit" },
		english = { "The {1} function has been annotated with the @WebSocket annotation, so the return type must be Unit." }
	),
	FUNCTION_NOT_ALLOW_RETURN_TYPE_RESULT_SET_NULLABLE(
		chinese = { "{1} 函数不允许为 Result 返回类型设置为可空" },
		english = { "The {1} function does not allow the return type of Result to be set as nullable." }
	),
	FUNCTION_NOT_ALLOW_RETURN_TYPE_UNIT_USE_NULLABLE(
		chinese = { "{1} 函数不允许使用 Unit? 返回类型" },
		english = { "The {1} function does not allow the use of the Unit? return type." }
	),
	FUNCTION_NOT_ALLOW_USE_RETURN_TYPE_NOTHING(
		chinese = { "{1} 函数不允许使用 {2} 返回类型" },
		english = { "The {1} function does not allow the use of the {2} return type." }
	),
	FUNCTION_ONLY_ALLOW_USE_ONE_REQUEST_TYPE_ANNOTATION(
		chinese = { "{1} 函数只允许使用一种请求类型注解，而您同时使用了 {2} 注解" },
		english = { "The {1} function only allows the use of one type of request annotation, but you have used the {2} annotation{3} simultaneously." },
	),
	FUNCTION_NOT_USE_ROUTE_ANNOTATION(
		chinese = { "{1} 函数未添加任何路由注解" },
		english = { "The {1} function does not add any route annotations." }
	),
	FUNCTION_NOT_ALLOW_USE_PATH_PARAMETER(
		chinese = { "{1} 函数不支持使用 @Path 参数" },
		english = { "The {1} function does not support the use of @Path parameters." }
	)
}
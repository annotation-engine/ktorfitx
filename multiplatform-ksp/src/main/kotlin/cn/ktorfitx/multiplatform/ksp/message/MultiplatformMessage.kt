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
	ANNOTATION_SCOPES_PARAMETER_NOT_ALLOW_NULLABLE_TYPE(
		chinese = { "{1} 接口上的 @ApiScope 注解的 scopes 参数不允许为空" },
		english = { "The scopes parameter of the @ApiScope annotation on the {1} interface is not allowed to be empty." }
	),
	ANNOTATION_SCOPES_NOT_ALLOWED_USE_SAME_CLASS_NAME_K_CLASS(
		chinese = { "{1} 接口上的 @ApiScope 注解的 scopes 参数不允许使用相同类名的 KClass<*>" },
		english = { "The scopes parameter of the @ApiScope annotation on the {1} interface is not allowed to use the KClass<*> of the same class name." }
	),
	ANNOTATION_NOT_SET_URL_OR_ADDED_DYNAMIC_URL(
		chinese = { "{1} 函数上的 @{2} 注解未设置 url 参数，或在参数上添加 @DynamicUrl 注解" },
		english = { "The @{2} annotation on the {1} function is not set with a URL parameter, or the @DynamicUrl annotation is not added to the parameters." }
	),
	ANNOTATION_URL_ONLY_SUPPORTED_WS_AND_WSS_PROTOCOLS(
		chinese = { "{1} 函数上的 @{2} 注解中的 url 参数仅支持 \"ws://\" 和 \"wss://\" 协议" },
		english = { "The url parameter in the @{2} annotation on the {1} function is only supported for the \"ws://\" and \"wss://\" protocols." }
	),
	ANNOTATION_URL_ONLY_SUPPORTED_HTTP_AND_HTTPS_PROTOCOLS(
		chinese = { "{1} 函数上的 @{2} 注解中的 url 参数仅支持 \"http://\" 和 \"https://\" 协议" },
		english = { "The url parameter in the @{2} annotation on the {1} function is only supported for the \"http://\" and \"https://\" protocols." }
	),
	FUNCTION_NOW_ALLOW_SETTING_URL_WHEN_MARKED_DYNAMIC_URL(
		chinese = { "{1} 函数上的 {2} 注解不允许设置 url 参数，因为函数已经标记了 @DynamicUrl 注解" },
		english = { "The {2} annotation on the {1} function does not allow setting URL parameter because the function has already been marked with the @DynamicUrl annotation." }
	),
	ANNOTATION_URL_FORMAT_INCORRECT(
		chinese = { "{1} 函数上的 @{2} 注解上的 url 参数格式错误" },
		english = { "The format of the url parameter on the @{2} annotation of the {1} function is incorrect." }
	),
	FUNCTION_LACKS_SUSPEND_MODIFIER(
		chinese = { "{1} 函数缺少 \"suspend\" 修饰符" },
		english = { "The {1} function lacks the \"suspend\" modifier." }
	),
	FUNCTION_HAS_BEEN_WEBSOCKET_SO_RETURN_TYPE_MUST_BE_UNIT(
		chinese = { "{1} 函数已标注 @WebSocket 注解，因此返回类型必须为 Unit" },
		english = { "The {1} function has been annotated with the @WebSocket annotation, so the return type must be Unit." }
	),
	FUNCTION_NOT_ALLOW_RETURN_TYPE_RESULT_SET_NULLABLE_TYPE(
		chinese = { "{1} 函数不允许为 Result 返回类型设置为可空" },
		english = { "The {1} function does not allow the return type of Result to be set as nullable." }
	),
	FUNCTION_NOT_ALLOW_RETURN_TYPE_UNIT_USE_NULLABLE_TYPE(
		chinese = { "{1} 函数不允许使用 Unit? 返回类型" },
		english = { "The {1} function does not allow the use of the Unit? return type." }
	),
	FUNCTION_NOT_ALLOW_USE_RETURN_TYPE_NOTHING(
		chinese = { "{1} 函数不允许使用 Nothing{2} 返回类型" },
		english = { "The {1} function does not allow the use of the Nothing{2} return type." }
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
	),
	FUNCTION_USE_INCOMPATIBLE_ANNOTATIONS(
		chinese = { "{1} 函数使用了不兼容的注解 {2}" },
		english = { "The {1} function uses incompatible annotations {2}." }
	),
	FUNCTION_NOT_ALLOW_USE_MULTIPLE_BODY_ANNOTATIONS(
		chinese = { "{1} 函数不允许使用多个 @Body 注解" },
		english = { "The {1} function does not allow the use of multiple @Body annotations." }
	),
	PARAMETER_MUST_BE_DECLARED_SPECIFIC_TYPE_BECAUSE_MARKED_BODY(
		chinese = { "{1} 函数的 {2} 参数必须声明为具体类型，因为您标记了 @Body 注解" },
		english = { "The {2} parameter of the {1} function must be declared as a specific type because you have marked the @Body annotation." }
	),
	PARAMETER_ONLY_ALLOW_USE_SUPPORTED_BY_FIELD(
		chinese = { "{1} 函数的 {2} 参数只允许使用 Map<String, *> 或 List<Pair<String, *>> 类型或是它们的具体化子类型或是派生类型" },
		english = { "The {2} parameter of the {1} function can only be of the type Map<String, *> or List<Pair<String, *>> or their specificized subtypes or derived types." }
	),
	PARAMETER_ONLY_ALLOW_USE_SUPPORTED_BY_PART(
		chinese = { "{1} 函数的 {2} 参数只允许使用 Map<String, Any> 或 List<Pair<String, Any>> 或 List<FormPart<*>> 类型或是它的具体化子类型或派生类型" },
		english = { "The {2} parameter of the {1} function can only be of the types Map<String, Any>, List<Pair<String, Any>> or List<FormPart<*>>, or its specificized subtypes or derived types." }
	),
	PARAMETER_HEADERS_FORMAT_INCORRECT(
		chinese = { "xx 函数的 xx 参数的 @Part 注解上的 headers 参数格式有误，需要以 <key>:<value> 格式" },
		english = { "The format of the headers parameter on the @Part annotation of the {1} function's {2} parameter is incorrect. It needs to be in the format of <key>:<value>." }
	),
	PARAMETER_NOT_ALLOW_USE_NULLABLE_TYPE(
		chinese = { "{1} 函数的 {2} 参数不允许使用可空类型" },
		english = { "The {2} parameter of the {1} function is not allowed to use nullable types." }
	)
}
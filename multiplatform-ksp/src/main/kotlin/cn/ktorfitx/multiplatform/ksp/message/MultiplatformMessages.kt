package cn.ktorfitx.multiplatform.ksp.message

import cn.ktorfitx.common.ksp.util.message.message

internal val MESSAGE_INTERFACE_NOT_ALLOW_GENERICS = message {
	english { "{1} 接口不允许包含泛型" }
	chinese { "The {1} interface does not allow generics." }
}

internal val MESSAGE_INTERFACE_MUST_BE_DECLARED_PUBLIC_OR_INTERNAL_ACCESS_PERMISSION = message {
	english { "The {1} interface must be declared with public or internal access permission." }
	chinese { "{1} 接口必须声明为 public 或 internal 访问权限" }
}

internal val MESSAGE_INTERFACE_MUST_BE_INTERFACE_BECAUSE_MARKED_API = message {
	english { "The {1} must be an interface because you have marked the @Api annotation." }
	chinese { "{1} 必须是 interface，因为您标记了 @Api 注解" }
}

internal val MESSAGE_INTERFACE_NOT_SUPPORT_SEALED_MODIFIER = message {
	english { "The {1} interface does not support the \"sealed\" modifier." }
	chinese { "{1} 接口不支持 \"sealed\" 修饰符" }
}

internal val MESSAGE_INTERFACE_MUST_BE_PLACED_FILE_TOP_LEVEL = message {
	english { "The {1} interface must be placed at the top level of the file." }
	chinese { "{1} 接口必须在文件顶层" }
}

internal val MESSAGE_ANNOTATION_NOT_ALLOW_USE_PROTOCOL_FROM_STRINGS = message {
	english { "The @Api annotation on the {1} interface does not allow the use of protocol-form strings containing \"://\", such as \"http://\" or \"https://\"." }
	chinese { "{1} 接口上的 @Api 注解中不允许出现包含 \"://\" 的协议形式字符串，例如：\"http://\" 或 \"https://\"" }
}

internal val MESSAGE_ANNOTATION_URL_PARAMETER_FORMAT_INCORRECT = message {
	english { "The format of the url parameter in the @Api annotation on the {1} interface is incorrect." }
	chinese { "{1} 接口上的 @Api 注解的 url 参数格式错误" }
}

internal val MESSAGE_ANNOTATION_SCOPES_PARAMETER_NOT_ALLOW_NULLABLE_TYPE = message {
	english { "The scopes parameter of the @ApiScope annotation on the {1} interface is not allowed to be empty." }
	chinese { "{1} 接口上的 @ApiScope 注解的 scopes 参数不允许为空" }
}

internal val MESSAGE_ANNOTATION_SCOPES_NOT_ALLOWED_USE_SAME_CLASS_NAME_K_CLASS = message {
	english { "The scopes parameter of the @ApiScope annotation on the {1} interface is not allowed to use the KClass<*> of the same class name." }
	chinese { "{1} 接口上的 @ApiScope 注解的 scopes 参数不允许使用相同类名的 KClass<*>" }
}

internal val MESSAGE_ANNOTATION_NOT_SET_URL_OR_ADDED_DYNAMIC_URL = message {
	english { "The @{2} annotation on the {1} function is not set with a URL parameter, or the @DynamicUrl annotation is not added to the parameters." }
	chinese { "{1} 函数上的 @{2} 注解未设置 url 参数，或在参数上添加 @DynamicUrl 注解" }
}

internal val MESSAGE_ANNOTATION_URL_ONLY_SUPPORTED_WS_AND_WSS_PROTOCOLS = message {
	english { "The url parameter in the @{2} annotation on the {1} function is only supported for the \"ws://\" and \"wss://\" protocols." }
	chinese { "{1} 函数上的 @{2} 注解中的 url 参数仅支持 \"ws://\" 和 \"wss://\" 协议" }
}

internal val MESSAGE_ANNOTATION_URL_ONLY_SUPPORTED_HTTP_AND_HTTPS_PROTOCOLS = message {
	english { "The url parameter in the @{2} annotation on the {1} function is only supported for the \"http://\" and \"https://\" protocols." }
	chinese { "{1} 函数上的 @{2} 注解中的 url 参数仅支持 \"http://\" 和 \"https://\" 协议" }
}

internal val MESSAGE_FUNCTION_NOW_ALLOW_SETTING_URL_WHEN_MARKED_DYNAMIC_URL = message {
	english { "The {2} annotation on the {1} function does not allow setting URL parameter because the function has already been marked with the @DynamicUrl annotation." }
	chinese { "{1} 函数上的 {2} 注解不允许设置 url 参数，因为函数已经标记了 @DynamicUrl 注解" }
}

internal val MESSAGE_ANNOTATION_URL_FORMAT_INCORRECT = message {
	english { "The format of the url parameter on the @{2} annotation of the {1} function is incorrect." }
	chinese { "{1} 函数上的 @{2} 注解上的 url 参数格式错误" }
}

internal val MESSAGE_FUNCTION_LACKS_SUSPEND_MODIFIER = message {
	english { "The {1} function lacks the \"suspend\" modifier." }
	chinese { "{1} 函数缺少 \"suspend\" 修饰符" }
}

internal val MESSAGE_FUNCTION_HAS_BEEN_WEBSOCKET_SO_RETURN_TYPE_MUST_BE_UNIT = message {
	english { "The {1} function has been annotated with the @WebSocket annotation, so the return type must be Unit." }
	chinese { "{1} 函数已标注 @WebSocket 注解，因此返回类型必须为 Unit" }
}

internal val MESSAGE_FUNCTION_NOT_ALLOW_RETURN_TYPE_RESULT_SET_NULLABLE_TYPE = message {
	english { "The {1} function does not allow the return type of Result to be set as nullable." }
	chinese { "{1} 函数不允许为 Result 返回类型设置为可空" }
}

internal val MESSAGE_FUNCTION_NOT_ALLOW_RETURN_TYPE_UNIT_USE_NULLABLE_TYPE = message {
	english { "The {1} function does not allow the use of the Unit? return type." }
	chinese { "{1} 函数不允许使用 Unit? 返回类型" }
}

internal val MESSAGE_FUNCTION_NOT_ALLOW_USE_RETURN_TYPE_NOTHING = message {
	english { "The {1} function does not allow the use of the Nothing{2} return type." }
	chinese { "{1} 函数不允许使用 Nothing{2} 返回类型" }
}

internal val MESSAGE_FUNCTION_ONLY_ALLOW_USE_ONE_REQUEST_TYPE_ANNOTATION = message {
	english { "The {1} function only allows the use of one type of request annotation, but you have used the {2} annotation{3} simultaneously." }
	chinese { "{1} 函数只允许使用一种请求类型注解，而您同时使用了 {2} 注解" }
}

internal val MESSAGE_FUNCTION_NOT_USE_ROUTE_ANNOTATION = message {
	english { "The {1} function does not add any route annotations." }
	chinese { "{1} 函数未添加任何路由注解" }
}

internal val MESSAGE_FUNCTION_NOT_ALLOW_USE_PATH_PARAMETER = message {
	english { "The {1} function does not support the use of @Path parameters." }
	chinese { "{1} 函数不支持使用 @Path 参数" }
}

internal val MESSAGE_FUNCTION_USE_INCOMPATIBLE_ANNOTATIONS = message {
	english { "The {1} function uses incompatible annotations {2}." }
	chinese { "{1} 函数使用了不兼容的注解 {2}" }
}

internal val MESSAGE_FUNCTION_NOT_ALLOW_USE_MULTIPLE_BODY_ANNOTATIONS = message {
	english { "The {1} function does not allow the use of multiple @Body annotations." }
	chinese { "{1} 函数不允许使用多个 @Body 注解" }
}

internal val MESSAGE_PARAMETER_MUST_BE_DECLARED_SPECIFIC_TYPE_BECAUSE_MARKED_BODY = message {
	english { "The {2} parameter of the {1} function must be declared as a specific type because you have marked the @Body annotation." }
	chinese { "{1} 函数的 {2} 参数必须声明为具体类型，因为您标记了 @Body 注解" }
}

internal val MESSAGE_PARAMETER_ONLY_ALLOW_USE_SUPPORTED_BY_FIELD = message {
	english { "The {2} parameter of the {1} function can only be of the type Map<String, *> or List<Pair<String, *>> or their specificized subtypes or derived types." }
	chinese { "{1} 函数的 {2} 参数只允许使用 Map<String, *> 或 List<Pair<String, *>> 类型或是它们的具体化子类型或是派生类型" }
}

internal val MESSAGE_PARAMETER_ONLY_ALLOW_USE_SUPPORTED_BY_PART = message {
	english { "The {2} parameter of the {1} function can only be of the types Map<String, Any>, List<Pair<String, Any>> or List<FormPart<*>>, or its specificized subtypes or derived types." }
	chinese { "{1} 函数的 {2} 参数只允许使用 Map<String, Any> 或 List<Pair<String, Any>> 或 List<FormPart<*>> 类型或是它的具体化子类型或派生类型" }
}

internal val MESSAGE_PARAMETER_HEADERS_FORMAT_INCORRECT = message {
	english { "The format of the headers parameter on the @Part annotation of the {1} function's {2} parameter is incorrect. It needs to be in the format of <key>:<value>." }
	chinese { "xx 函数的 xx 参数的 @Part 注解上的 headers 参数格式有误，需要以 <key>:<value> 格式" }
}

internal val MESSAGE_PARAMETER_NOT_ALLOW_USE_NULLABLE_TYPE = message {
	english { "The {2} parameter of the {1} function is not allowed to use nullable types." }
	chinese { "{1} 函数的 {2} 参数不允许使用可空类型" }
}
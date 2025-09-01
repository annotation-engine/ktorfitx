package cn.ktorfitx.multiplatform.ksp.message

import cn.ktorfitx.common.ksp.util.message.message

internal val MESSAGE_CLASS_MUST_USE_OBJECT_TYPE = message {
	english { "The {1} class must be of the object type." }
	chinese { "{1} 类必须是 object 类型的" }
}

internal val MESSAGE_CLASS_NOT_ALLOW_USE_PRIVATE_ACCESS_MODIFIER = message {
	english { "The {1} class does not allow the use of the \"private\" access modifier." }
	chinese { "{1} 类不允许使用 \"private\" 访问修饰符" }
}

internal val MESSAGE_CLASS_MUST_IMPLEMENT_MOCK_PROVIDER_INTERFACE = message {
	english { "Class {1} must implement the MockProvider<T> interface." }
	chinese { "{1} 类必须实现 MockProvider<T> 接口" }
}

internal val MESSAGE_INTERFACE_NOT_ALLOW_GENERICS = message {
	english { "{1} 接口不允许包含泛型" }
	chinese { "The {1} interface does not allow generics." }
}

internal val MESSAGE_INTERFACE_MUST_BE_DECLARED_PUBLIC_OR_INTERNAL_ACCESS_PERMISSION = message {
	english { "The {1} interface must be declared with \"public\" or \"internal\" access modifier." }
	chinese { "{1} 接口必须声明 \"public\" 或 \"internal\" 访问修饰符" }
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

internal val MESSAGE_ANNOTATION_URL_FORMAT_INCORRECT = message {
	english { "The format of the url parameter on the @{2} annotation of the {1} function is incorrect." }
	chinese { "{1} 函数上的 @{2} 注解上的 url 参数格式错误" }
}

internal val MESSAGE_FUNCTION_NOW_ALLOW_SETTING_URL_WHEN_MARKED_DYNAMIC_URL = message {
	english { "The {2} annotation on the {1} function does not allow setting URL parameter because the function has already been marked with the @DynamicUrl annotation." }
	chinese { "{1} 函数上的 {2} 注解不允许设置 url 参数，因为函数已经标记了 @DynamicUrl 注解" }
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

internal val MESSAGE_FUNCTION_USE_MOCK_PROVIDER_IMPLEMENTATION_CLASS_THAT_IS_INCOMPATIBLE_WITH_RETURN_TYPE = message {
	english { "The @Mock annotation on the {1} function uses a MockProvider<R> implementation class that is incompatible with the return type. The generic type R should be set to {2}." }
	chinese { "{1} 函数上的 @Mock 注解中使用了与返回类型不兼容的 MockProvider<R> 实现类，泛型 R 应该为 {2}" }
}

internal val MESSAGE_FUNCTION_MUST_USE_HTTP_STATEMENT_RETURN_TYPE = message {
	english { "The {1} function must use HttpStatement as its return type." }
	chinese { "{1} 函数必须使用 HttpStatement 作为返回类型" }
}

internal val MESSAGE_FUNCTION_NOT_ALLOW_SIMULTANEOUS_USE_PREPARE_AND_MOCK_ANNOTATIONS = message {
	english { "The {1} function does not allow the simultaneous use of @Prepare and @Mock annotations." }
	chinese { "{1} 函数不允许同时使用 @Prepare 和 @Mock 注解" }
}

internal val MESSAGE_FUNCTION_NOT_ALLOW_SIMULTANEOUS_USE_PREPARE_AND_WEBSOCKET_ANNOTATIONS = message {
	english { "The {1} function does not allow the simultaneous use of @Prepare and @WebSocket annotations." }
	chinese { "{1} 函数不允许同时使用 @Prepare 和 @WebSocket 注解" }
}

internal val MESSAGE_FUNCTION_NOT_ALLOW_SIMULTANEOUS_USE_MOCK_AND_WEBSOCKET_ANNOTATIONS = message {
	english { "The {1} function does not allow the simultaneous use of @Mock and @WebSocket annotations." }
	chinese { "{1} 函数不允许同时使用 @Mock 和 @WebSocket 注解" }
}

internal val MESSAGE_FUNCTION_HEADERS_FORMAT_IS_INCORRECT = message {
	english { "The @Headers annotation parameter format on the {1} function is incorrect. The correct format should be: \"<key>:<value>\"" }
	chinese { "{1} 函数上的 @Headers 注解参数格式错误，正确格式为：\"<key>:<value>\"" }
}

internal val MESSAGE_FUNCTION_MUST_USE_MOCK_PROVIDER_DERIVED_CLASS = message {
	english { "In the @Mock annotation on the {1} function, the KClass of the derived class of MockProvider must be used as the value." }
	chinese { "{1} 函数上的 @Mock 注解中必须使用 MockProvider 的派生类的 KClass 作为值" }
}

internal val MESSAGE_FUNCTION_NOT_ALLOWED_TO_CONTAIN_GENERICS = message {
	english { "The {1} function does not allow the use of generics." }
	chinese { "{1} 函数不允许包含泛型" }
}

internal val MESSAGE_FUNCTION_ONLY_ACCEPTS_ONE_PARAMETER_AND_TYPE_IS_SUPPORTED_BY_WEB_SOCKET = message {
	english { "The {1} function only accepts one parameter, and its type is either the alias of \"WebSocketSessionHandler\" or the type of \"suspend DefaultClientWebSocketSession.() -> Unit\"." }
	chinese { "{1} 函数只允许一个参数，且类型为 \"WebSocketSessionHandler\" 别名 或使用 \"suspend DefaultClientWebSocketSession.() -> Unit\" 类型" }
}

internal val MESSAGE_FUNCTION_FAILED_PARSE_FOLLOWING_PATH_PARAMETER = message {
	english { "The {1} function failed to parse the following {2} Path parameter." }
	chinese { "{1} 函数未解析以下 {2} Path 参数" }
}

internal val MESSAGE_FUNCTION_NOT_ALLOW_USE_ONE_PARAMETER_MARKED_DYNAMIC_URL_ANNOTATION = message {
	english { "The {1} function only allows the use of one parameter marked with the @DynamicUrl annotation to dynamically set the URL parameter." }
	chinese { "{1} 函数只允许使用一个标注了 @DynamicUrl 注解的参数来动态设置 url 参数" }
}

internal val MESSAGE_PARAMETER_MUST_BE_DECLARED_SPECIFIC_TYPE_BECAUSE_MARKED_BODY = message {
	english { "The {2} parameter of the {1} function must be declared as a specific type because you have marked the @Body annotation." }
	chinese { "{1} 函数的 {2} 参数必须声明为具体类型，因为您标记了 @Body 注解" }
}

internal val MESSAGE_PARAMETER_ONLY_ALLOW_USE_SUPPORTED_BY_FIELD = message {
	english { "The {2} parameter of the {1} function can only be of the type Map<String, *> or List<Pair<String, *>> or their specificized subtypes or derived types." }
	chinese { "{1} 函数的 {2} 参数只允许使用 Map<String, *> 或 List<Pair<String, *>> 类型或是它们的具体化子类型或是派生类型" }
}

internal val MESSAGE_PARAMETER_ONLY_ALLOW_USE_SUPPORTED_BY_PARTS = message {
	english { "The {2} parameter of the {1} function can only be of the types Map<String, Any>, List<Pair<String, Any>> or List<FormPart<*>>, or its specificized subtypes or derived types." }
	chinese { "{1} 函数的 {2} 参数只允许使用 Map<String, Any> 或 List<Pair<String, Any>> 或 List<FormPart<*>> 类型或是它的具体化子类型或派生类型" }
}

internal val MESSAGE_PARAMETER_ONLY_ALLOW_UES_SUPPORTED_BY_QUERIES = message {
	english { "The {2} parameter of the {1} function can only be of the types Map<String, *> or List<Pair<String, *>>, or its specificized subtypes or derived types." }
	chinese { "{1} 函数的 {2} 参数只允许使用 Map<String, *> 或 List<Pair<String, *>> 类型或是它的具体化子类型或派生类型" }
}

internal val MESSAGE_PARAMETER_ONLY_ALLOW_UES_SUPPORTED_BY_ATTRIBUTES = message {
	english { "The {2} parameter of the {1} function can only be of the types Map<String, Any> or List<Pair<String, Any>>, or its specificized subtypes or derived types." }
	chinese { "{1} 函数的 {2} 参数只允许使用 Map<String, Any> 或 List<Pair<String, Any>> 类型或是它的具体化子类型或派生类型" }
}

internal val MESSAGE_PARAMETER_NOT_ALLOW_USE_NULLABLE_TYPE = message {
	english { "The {2} parameter of the {1} function is not allowed to use nullable types." }
	chinese { "{1} 函数的 {2} 参数不允许使用可空类型" }
}

internal val MESSAGE_PARAMETER_MUST_USE_STRING_TYPE = message {
	english { "The {2} parameter of the {1} function can only be of the String type." }
	chinese { "{1} 函数的 {2} 参数只允许为 String 类型" }
}

internal val MESSAGE_PARAMETER_PART_FORMAT_IS_INCORRECT = message {
	english { "The @Part annotation parameter on the {2} parameter of the {1} function has an incorrect format. The correct format should be: \"<key>:<value>\"." }
	chinese { "{1} 函数的 {2} 参数上的 @Part 注解参数的格式错误，正确格式为：\"<key>:<value>\"" }
}

internal val MESSAGE_PARAMETER_COOKIE_FORMAT_IS_INCORRECT = message {
	english { "The @Cookie annotation parameter on the {2} parameter of the {1} function has a format error. The correct format should be: \"<key>:<value>\"." }
	chinese { "{1} 函数的 {2} 参数上的 @Cookie 注解参数的格式错误，正确格式为：\"<key>:<value>\"" }
}

internal val MESSAGE_PARAMETER_DELAY_MUST_BE_GREATER_THAN_OR_EQUAL_TO_ZERO = message {
	english { "The delay parameter of the @Mock annotation on the {1} function must be greater than or equal to 0." }
	chinese { "{1} 函数上的 @Mock 注解的 delay 参数必须大于等于 0" }
}

internal val MESSAGE_PARAMETER_NOT_USE_ANY_FUNCTIONAL_ANNOTATIONS = message {
	english { "The {2} parameter on the {1} function does not use any functional annotations." }
	chinese { "{1} 函数上的 {2} 参数未使用任何功能注解" }
}

internal val MESSAGE_PARAMETER_NOT_ALLOW_USE_MORE_THAN_ONE_FUNCTIONALITY_ANNOTATION_AT_SAME_TIME = message {
	english { "The {2} parameter on the {1} function is not allowed to use more than one {3} functionality annotation at the same time." }
	chinese { "{1} 函数上的 {2} 参数不允许同时使用 {3} 多个功能注解" }
}

internal val MESSAGE_PARAMETER_NOT_FOLLOW_LOWERCASE_CAMEL_CASE_NAMING_CONVENTION = message {
	english { "The {2} parameter on the {1} function does not follow the lowercase camel case naming convention. It is recommended to modify it to \"{3}\"." }
	chinese { "{1} 函数上的 {2} 参数不符合小驼峰命名规则，建议修改为 \"{3}\"" }
}

internal val MESSAGE_PARAMETER_REDUNDANTLY_PARSED_AS_THE_PATH_PARAMETER = message {
	english { "The {2} parameter of the {1} function is redundantly parsed as the path parameter." }
	chinese { "{1} 函数的 {2} 参数重复解析 Path 参数" }
}

internal val MESSAGE_PARAMETER_WAS_NOT_FOUND_IN_THE_URL = message {
	english { "The {2} parameter of the {1} function was not found in the url." }
	chinese { "{1} 函数的 {2} 参数未在 url 中找到" }
}

internal val MESSAGE_PARAMETER_ONLY_USE_STRING = message {
	english { "The {2} parameter of the {1} function is restricted to accepting only String type values." }
	chinese { "{1} 函数的 {2} 参数只允许使用 String 类型" }
}

internal val FILE_COMMENT = message {
	english {
		"""
		This file was generated by the "cn.ktorfitx:multiplatform-ksp" module during the compilation process based on the annotations.
		All manual modifications will be overwritten during the next build.
		If you need to modify the behavior, please modify the corresponding annotations or source code definitions instead of this file itself.
		
		Generation time: {1}
		""".trimIndent()
	}
	chinese {
		"""
		该文件是由 "cn.ktorfitx:multiplatform-ksp" 模块在编译期间根据注解生成的代码，
		所有手动修改将会在下次构建时被覆盖，
		若需修改行为，请修改对应的注解或源代码定义，而不是此文件本身。
		
		生成时间：{1}
		""".trimIndent()
	}
}
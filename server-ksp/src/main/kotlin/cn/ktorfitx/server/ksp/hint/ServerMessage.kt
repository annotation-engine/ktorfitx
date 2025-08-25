package cn.ktorfitx.server.ksp.hint

import cn.ktorfitx.common.ksp.util.message.Message

internal enum class ServerMessage(
	override val chinese: () -> String,
	override val english: () -> String,
) : Message {
	FUNCTION_TOP_LEVEL_OR_OBJECT_ONLY(
		chinese = { "{1} 函数只允许声明在 文件顶层 或 object 类中" },
		english = { "The {1} function can only be declared at the top level of a file or within an object class." }
	),
	FUNCTION_NOT_ALLOWED_TO_CONTAIN_GENERICS(
		chinese = { "{1} 函数不允许包含泛型" },
		english = { "The {1} function does not allow the use of generics." }
	),
	FUNCTION_NOT_ALLOWED_NULLABLE_RETURN_TYPE(
		chinese = { "{1} 函数返回类型不允许为可空类型" },
		english = { "The return type of the {1} function is not allowed to be a nullable type." }
	),
	FUNCTION_RETURN_TYPE_MUST_BE_DEFINITE_CLASS(
		chinese = { "{1} 函数返回类型必须是明确的类" },
		english = { "The return type of the {1} function must be a definite class." }
	),
	FUNCTION_FAILED_PARSE_FOLLOWING_PATH_PARAMETER(
		chinese = { "{1} 函数未解析以下 {2} Path 参数" },
		english = { "{1} x function failed to parse the following {2} Path parameter." }
	),
	FUNCTION_NOT_ALLOW_USE_BODY_FIELD_PART_ANNOTATION(
		chinese = { "{1} 函数不允许同时使用 @Body 注解、@Field 注解 以及 @PartForm、@PartFile、@PartBinary、@PartBinaryChannel 注解" },
		english = { "The {1} function does not allow the simultaneous use of the @Body annotation, the @Field annotation, and the @PartForm, @PartFile, @PartBinary, and @PartBinaryChannel annotations." }
	),
	FUNCTION_PARAMETER_NOT_ALLOW_USE_MULTIPLE_BODY(
		chinese = { "{1} 函数参数不允许同时使用多个 @Body 注解" },
		english = { "The {1} function parameter does not allow the use of multiple @Body." },
	),
	FUNCTION_NOT_ALLOW_USE_UNIT_AND_NOTHING(
		chinese = { "{1} 函数不允许使用 Unit 和 Nothing 返回类型" },
		english = { "The {1} function does not allow the use of Unit and Nothing as return types." }
	),
	FUNCTION_IS_WEBSOCKET_TYPE_NOT_ALLOW_USE_UNIT(
		chinese = { "{1} 函数标注了 {2} 注解，因此返回类型只允许是 Unit 类型" },
		english = { "The {1} function is annotated with the {2} annotation, so the return type can only be of the Unit type." }
	),
	FUNCTION_NOT_ALLOW_ADDING_MULTIPLE_REQUEST_TYPES_SIMULTANEOUSLY(
		chinese = { "{1} 函数不允许同时添加多个请求类型" },
		english = { "The {1} function does not allow adding multiple request types at the same time." },
	),
	FUNCTION_ONLY_ALLOW_CREATION_OF_EXTENSION_METHODS_FOR_DEFAULT_WEB_SOCKET_SERVER_SESSION(
		chinese = { "{1} 函数仅允许为 DefaultWebSocketServerSession 类型创建扩展方法" },
		english = { "The {1} function only allows the creation of extension methods for the DefaultWebSocketServerSession type." }
	),
	FUNCTION_ONLY_ALLOW_CREATION_OF_EXTENSION_METHODS_FOR_WEB_SOCKET_SERVER_SESSION(
		chinese = { "{1} 函数仅允许为 WebSocketServerSession 类型创建扩展方法" },
		english = { "The {1} function only allows the creation of extension methods for the WebSocketServerSession type." }
	),
	FUNCTION_ONLY_ALLOW_CREATION_OF_EXTENSION_METHODS_FOR_ROUTING_CONTEXT(
		chinese = { "{1} 函数仅允许为 RoutingContext 类型创建扩展方法" },
		english = { "The {1} function only allows the creation of extension methods for the RoutingContext type." }
	),
	PARAMETER_WAS_NOT_FOUND_IN_THE_URL(
		chinese = { "{1} 函数的 {2} 参数未在 url 中找到" },
		english = { "The {2} parameter of the {1} function was not found in the url." }
	),
	PARAMETER_REDUNDANTLY_PARSED_AS_THE_PATH_PARAMETER(
		chinese = { "{1} 函数的 {2} 参数重复解析 path 参数" },
		english = { "The {2} parameter of the {1} function is redundantly parsed as the path parameter." }
	),
	PARAMETER_NOT_ALLOWED_NULLABLE(
		chinese = { "{1} 函数的 {2} 参数不允许可空" },
		english = { "The {2} parameter of the {1} function is not allowed to be null." }
	),
	PARAMETER_MUST_USE_ONE_OF_ANNOTATIONS(
		chinese = { "{1} 函数的 {2} 参数必须使用 {3} 注解中的一个" },
		english = { "The {2} parameter of the {1} function must use one of the annotations in {3}." },
	),
	PARAMETER_ONLY_USE_ONE_OF_ANNOTATIONS(
		chinese = { "{1} 函数的 {2} 参数只允许使用 {3} 注解中的一个" },
		english = { "The {2} parameter of the {1} function is restricted to use only one of the annotations in {3}." }
	),
	PARAMETER_NULLABLE_ONLY_STRING(
		chinese = { "{1} 函数的 {2} 参数只允许 String 为可空类型" },
		english = { "The {2} parameter of the {1} function is restricted to be of a nullable String type." }
	),
	PARAMETER_RETRIEVED_TWICE_WITH_PART_PARAMETER(
		chinese = { "{1} 函数的 {2} 参数重复获取了 {3} 参数" },
		english = { "The {2} parameter of the {1} function is  twice with the parameter {3}." }
	),
	PARAMETER_ONLY_USE_STRING(
		chinese = { "{1} 函数的 {2} 参数只允许使用 String 类型" },
		english = { "The {2} parameter of the {1} function is restricted to accepting only String type values." }
	),
	PARAMETER_ONLY_USE_STRING_OR_FORM_ITEM(
		chinese = { "{1} 函数的 {2} 参数只允许使用 String 和 PartData.FormItem 类型" },
		english = { "The {2} parameter of the {1} function can only accept values of type String or PartData.FormItem." }
	),
	PARAMETER_ONLY_USE_BYTE_ARRAY_OR_FILE_ITEM(
		chinese = { "{1} 函数的 {2} 参数只允许使用 ByteArray 和 PartData.FileItem 类型" },
		english = { "The {2} parameter of the {1} function can only accept types of ByteArray and PartData.FileItem." }
	),
	PARAMETER_ONLY_USE_BYTE_ARRAY_OR_BINARY_ITEM(
		chinese = { "{1} 函数的 {2} 参数只允许使用 ByteArray 和 PartData.BinaryItem 类型" },
		english = { "The {2} parameter of the {1} function can only be of the types ByteArray and PartData.BinaryItem." }
	),
	PARAMETER_ONLY_USE_BINARY_CHANNEL_ITEM(
		chinese = { "{1} 函数的 {2} 参数只允许使用 PartData.BinaryChannelItem 类型" },
		english = { "The {2} parameter of the {1} function can only be of the PartData.BinaryChannelItem type." }
	),
	ANNOTATION_NOT_ALLOW_USE_SAME_PATH_PARAMETER(
		chinese = { "{1} 函数的 {2} 注解中不允许使用相同的 Path 参数 {3}" },
		english = { "The {2} annotation of the {1} function does not allow the use of the same {3} parameter for the Path." }
	),
	ANNOTATION_NOT_ALLOW_USE_REGEX_WHEN_WEBSOCKET_HAS_BEEN_MARKED(
		chinese = { "{1} 函数不允许在标记了 {2} 注解的情况下使用 @Regex 注解" },
		english = { "The {1} function does not allow the use of the @Regex annotation when the {2} annotation has been marked." }
	),
	ANNOTATION_NOT_ALLOW_USE_TIMEOUT_WHEN_WEBSOCKET_HAS_BEEN_MARKED(
		chinese = { "{1} 函数不允许在标记了 {2} 注解的情况下使用 @Timeout 注解" },
		english = { "The {1} function does not allow the use of the @Timeout annotation when the {2} annotation has been marked." }
	),
	ANNOTATION_VALUE_PARAMETER_MUST_BE_GREATER_THAN_ZERO(
		chinese = { "{1} 函数上的 {2} 注解的 value 参数必须大于 0" },
		english = { "The value parameter of the {2} annotation on the {1} function must be greater than 0." }
	),
	ANNOTATION_PATH_PARAMETER_NOT_VALID_REGULAR_EXPRESSION(
		chinese = { "{1} 函数上的 {2} 注解的 path 参数不是一个合法的正则表达式" },
		english = { "The path parameter of the {2} annotation on the {1} function is not a valid regular expression." }
	)
}
package cn.ktorfitx.common.ksp.util.message

internal val MESSAGE_UNKNOWN = message {
	english { "Unknown." }
	chinese { "未知" }
}

internal val MESSAGE_ERROR_LOCATION = message {
	english { "Error location: " }
	chinese { "错误位置：" }
}

internal val MESSAGE_ANNOTATION_NOT_ALLOW_USE_GENERIC = message {
	english { "{1} annotation does not allow the use of generics." }
	chinese { "{1} 注解不允许使用泛型" }
}

internal val MESSAGE_ANNOTATION_MUST_INCLUDE_STRING_PARAMETER = message {
	english { "@{1} annotation must include the \"val {2}: String\" parameter." }
	chinese { "@{1} 注解必须添加 \"val {2}: String\" 参数" }
}

internal val MESSAGE_ANNOTATION_MUST_BE_ANNOTATED_TARGET_FUNCTION = message {
	english { "The {1} annotation must be annotated with @Target(AnnotationTarget.FUNCTION)." }
	chinese { "{1} 注解必须标注 @Target(AnnotationTarget.FUNCTION) 注解" }
}

internal val MESSAGE_ANNOTATION_MUST_BE_ANNOTATED_RETENTION_SOURCE = message {
	english { "The {1} annotation must be annotated with @Target(AnnotationRetention.SOURCE)." }
	chinese { "{1} 注解必须标注 @Retention(AnnotationRetention.SOURCE) 注解" }
}

internal val MESSAGE_ANNOTATION_HTTP_METHOD_USE_INVALID_HTTP_METHOD_NAME = message {
	english { "The @HttpMethod annotation marked on @{1} is using an invalid HTTP method name. Only names consisting of \"A-Z 0-9 -\" are allowed." }
	chinese { "@{1} 注解上标注的 @HttpMethod 注解中使用了不合法的 Http Method 名称，只允许包含：\"A-Z 0-9 -\"" }
}

internal val MESSAGE_ANNOTATION_DUPLICATES_PROVIDED_SYSTEM_HTTP_METHOD_ANNOTATION = message {
	english { "@{1} annotation duplicates the provided Http Method annotation by the system. Please directly use the @{2} annotation." }
	chinese { "@{1} 注解重复定义系统提供的 Http Method 注解，请直接使用 @{2} 注解" }
}
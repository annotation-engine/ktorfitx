package cn.ktorfitx.multiplatform.annotation

@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.SOURCE)
annotation class Part(
	val name: String = "",
	val headers: Array<String> = []
)
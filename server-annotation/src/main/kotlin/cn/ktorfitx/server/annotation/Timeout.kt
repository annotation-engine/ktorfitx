package cn.ktorfitx.server.annotation

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.SOURCE)
annotation class Timeout(
	val millis: Long
)
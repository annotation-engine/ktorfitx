package cn.ktorfitx.server.annotation

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class Api(
	val path: String
)
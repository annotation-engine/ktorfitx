package cn.ktorfitx.server.annotation

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class Controller(
	val path: String
)
package cn.ktorfitx.server.annotation

import kotlin.time.DurationUnit

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.SOURCE)
annotation class Timeout(
	val value: Long,
	val unit: DurationUnit = DurationUnit.MILLISECONDS
)
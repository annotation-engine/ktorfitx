package cn.ktorfitx.multiplatform.sample.http

import kotlinx.serialization.Serializable

@Serializable
data class ApiResult<T : Any>(
	val code: Int,
	val msg: String,
	val data: T?
)
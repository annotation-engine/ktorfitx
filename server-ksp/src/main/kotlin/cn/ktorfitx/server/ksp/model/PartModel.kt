package cn.ktorfitx.server.ksp.model

import com.squareup.kotlinpoet.ClassName

internal class PartModel(
	val name: String,
	val varName: String,
	val annotation: ClassName,
	val isNullable: Boolean,
	val isPartData: Boolean,
	val isList: Boolean
)
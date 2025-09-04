package cn.ktorfitx.common.ksp.util.builders

import com.squareup.kotlinpoet.FileSpec

private val fileSpecBuilderLocal = ThreadLocal<FileSpec.Builder>()

var fileSpecBuilder: FileSpec.Builder
	set(value) = fileSpecBuilderLocal.set(value)
	get() = fileSpecBuilderLocal.get()!!
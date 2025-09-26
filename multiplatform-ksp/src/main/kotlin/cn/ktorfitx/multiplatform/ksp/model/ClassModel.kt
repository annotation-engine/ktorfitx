package cn.ktorfitx.multiplatform.ksp.model

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.KModifier

internal class ClassModel(
	val className: ClassName,
	val superinterface: ClassName,
	val kModifier: KModifier,
	val apiUrl: String?,
	val funModels: List<FunModel>,
)
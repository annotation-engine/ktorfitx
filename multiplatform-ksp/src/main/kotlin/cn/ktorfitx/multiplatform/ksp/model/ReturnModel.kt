package cn.ktorfitx.multiplatform.ksp.model

import com.squareup.kotlinpoet.TypeName

internal class ReturnModel(
	val typeName: TypeName,
	val serializedTypeName: TypeName,
	val returnKind: ReturnKind
)

internal enum class ReturnKind {
	Unit,
	Result,
	Any
}
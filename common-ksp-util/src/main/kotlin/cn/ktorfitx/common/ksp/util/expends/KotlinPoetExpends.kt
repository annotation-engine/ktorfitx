@file:Suppress("NOTHING_TO_INLINE")

package cn.ktorfitx.common.ksp.util.expends

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.ParameterizedTypeName
import com.squareup.kotlinpoet.TypeName

/**
 * 获取 TypeName 的 rawType，ClassName 是自己，ParameterizedTypeName 是 this.rawType
 */
inline val TypeName.rawType: ClassName
	get() = when (this) {
		is ClassName -> this
		is ParameterizedTypeName -> this.rawType
		else -> error("Only supports ParameterizedTypeName and ClassName types.")
	}

inline fun TypeName.asNotNullable(): TypeName =
	if (this.isNullable) this.copy(nullable = false) else this

inline fun TypeName.asNullable(): TypeName =
	if (this.isNullable) this else this.copy(nullable = true)

inline fun TypeName.equals(other: TypeName, ignoreNullable: Boolean): Boolean =
	if (ignoreNullable) {
		this.asNotNullable() == other.asNotNullable()
	} else {
		this == other
	}
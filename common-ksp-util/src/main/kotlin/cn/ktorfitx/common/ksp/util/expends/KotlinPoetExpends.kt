package cn.ktorfitx.common.ksp.util.expends

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.ParameterizedTypeName
import com.squareup.kotlinpoet.TypeName

/**
 * 获取 TypeName 的 rawType，ClassName 是自己，ParameterizedTypeName 是 this.rawType
 */
val TypeName.rawType: ClassName
	get() = when (this) {
		is ParameterizedTypeName -> {
			this.rawType.copy(this.isNullable, emptyList(), emptyMap())
		}
		
		is ClassName -> this
		else -> error("Only supports ParameterizedTypeName and ClassName types.")
	}

fun TypeName.asNotNullable(): TypeName {
	return if (this.isNullable) this.copy(nullable = false) else this
}

fun TypeName.asNullable(): TypeName {
	return if (this.isNullable) this else this.copy(nullable = true)
}

fun TypeName.equals(other: TypeName, ignoreNullable: Boolean): Boolean {
	if (ignoreNullable) {
		return this.asNotNullable() == other.asNotNullable()
	}
	return this == other
}
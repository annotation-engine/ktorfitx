package cn.ktorfitx.common.ksp.util.check

import com.google.devtools.ksp.symbol.KSNode
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

/**
 * 编译器检查
 */
@Suppress("NOTHING_TO_INLINE")
@OptIn(ExperimentalContracts::class)
inline fun <T : KSNode> T.compileCheck(
	value: Boolean,
	noinline errorMessage: () -> String,
) {
	contract {
		returns() implies value
	}
	if (!value) {
		ktorfitxCompilationError(errorMessage)
	}
}
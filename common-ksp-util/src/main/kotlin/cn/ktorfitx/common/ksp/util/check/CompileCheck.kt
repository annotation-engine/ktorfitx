package cn.ktorfitx.common.ksp.util.check

import cn.ktorfitx.common.ksp.util.exception.KtorfitxCompilationErrorException
import cn.ktorfitx.common.ksp.util.exception.KtorfitxConfigErrorException
import cn.ktorfitx.common.ksp.util.message.MESSAGE_ERROR_LOCATION
import cn.ktorfitx.common.ksp.util.message.MESSAGE_UNKNOWN
import cn.ktorfitx.common.ksp.util.message.getString
import com.google.devtools.ksp.symbol.FileLocation
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

/**
 * 编译错误
 */
fun <T : KSNode> T.ktorfitxCompilationError(
	message: () -> String
): Nothing {
	val message = message()
	val location = this.location as? FileLocation
	val errorLocation = if (location != null) "${location.filePath}:${location.lineNumber}" else MESSAGE_UNKNOWN.getString()
	throw KtorfitxCompilationErrorException("$message\n${MESSAGE_ERROR_LOCATION.getString()}$errorLocation")
}

fun ktorfitxConfigError(message: String): Nothing {
	throw KtorfitxConfigErrorException(message)
}
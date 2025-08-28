package cn.ktorfitx.common.ksp.util.check

import cn.ktorfitx.common.ksp.util.exception.KtorfitxCompilationErrorException
import cn.ktorfitx.common.ksp.util.exception.KtorfitxConfigErrorException
import cn.ktorfitx.common.ksp.util.message.MESSAGE_ERROR_LOCATION
import cn.ktorfitx.common.ksp.util.message.MESSAGE_UNKNOWN
import cn.ktorfitx.common.ksp.util.message.getString
import com.google.devtools.ksp.symbol.FileLocation
import com.google.devtools.ksp.symbol.KSNode
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

@Suppress("NOTHING_TO_INLINE")
@OptIn(ExperimentalContracts::class)
@Throws(KtorfitxCompilationErrorException::class)
inline fun ktorfitxCheck(
	value: Boolean,
	node: KSNode,
	noinline lazyMessage: () -> String,
) {
	contract {
		callsInPlace(lazyMessage, InvocationKind.AT_MOST_ONCE)
		returns() implies value
	}
	if (!value) {
		val message = lazyMessage()
		ktorfitxCompilationError(node, message)
	}
}

@Suppress("NOTHING_TO_INLINE")
@OptIn(ExperimentalContracts::class)
@Throws(KtorfitxCompilationErrorException::class)
inline fun <T : Any> ktorfitxCheckNotNull(
	value: T?,
	node: KSNode,
	noinline lazyMessage: () -> String,
): T {
	contract {
		callsInPlace(lazyMessage, InvocationKind.AT_MOST_ONCE)
		returns() implies (value != null)
	}
	if (value == null) {
		val message = lazyMessage()
		ktorfitxCompilationError(node, message)
	}
	return value
}

@Throws(KtorfitxCompilationErrorException::class)
fun ktorfitxCompilationError(
	node: KSNode,
	message: String,
): Nothing {
	val location = node.location as? FileLocation
	val errorLocation = if (location != null) "${location.filePath}:${location.lineNumber}" else MESSAGE_UNKNOWN.getString()
	throw KtorfitxCompilationErrorException("$message\n${MESSAGE_ERROR_LOCATION.getString()}$errorLocation")
}

@Throws(KtorfitxConfigErrorException::class)
fun ktorfitxConfigError(message: String): Nothing {
	throw KtorfitxConfigErrorException(message)
}
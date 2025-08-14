package cn.ktorfitx.common.ksp.util.check

import cn.ktorfitx.common.ksp.util.exception.KtorfitxCompilationException
import cn.ktorfitx.common.ksp.util.exception.KtorfitxConfigException
import com.google.devtools.ksp.symbol.FileLocation
import com.google.devtools.ksp.symbol.KSNode

/**
 * 编译错误
 */
fun <T : KSNode> T.ktorfitxCompilationError(
	message: () -> String
): Nothing {
	val message = message()
	val location = this.location as? FileLocation
	val errorLocation = if (location != null) "${location.filePath}:${location.lineNumber}" else "未知"
	throw KtorfitxCompilationException("$message\n错误位于：$errorLocation")
}

fun ktorfitxConfigError(message: String): Nothing {
	throw KtorfitxConfigException(message)
}
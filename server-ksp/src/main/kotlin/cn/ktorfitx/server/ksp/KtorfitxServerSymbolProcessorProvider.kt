package cn.ktorfitx.server.ksp

import cn.ktorfitx.common.ksp.util.check.ktorfitxConfigError
import cn.ktorfitx.common.ksp.util.log.kspLoggerLocal
import cn.ktorfitx.common.ksp.util.message.Message
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider

internal class KtorfitxServerSymbolProcessorProvider : SymbolProcessorProvider {
	
	override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
		kspLoggerLocal.set(environment.logger)
		val packageName = environment.options["ktorfitx.generate.packageName"] ?: ktorfitxConfigError("ktorfitx.generate.packageName 为空或未设置")
		val fileName = environment.options["ktorfitx.generate.fileName"] ?: ktorfitxConfigError("ktorfitx.generate.fileName 为空或未设置")
		val funName = environment.options["ktorfitx.generate.funName"] ?: ktorfitxConfigError("ktorfitx.generate.funName 为空或未设置")
		val language = environment.options["ktorfitx.language"] ?: ktorfitxConfigError("ktorfitx.language 为空或未设置")
		Message.language.set(language)
		return KtorfitxServerSymbolProcessor(environment.codeGenerator, packageName, fileName, funName)
	}
}
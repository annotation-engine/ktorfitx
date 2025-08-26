package cn.ktorfitx.server.ksp

import cn.ktorfitx.common.ksp.util.check.ktorfitxConfigError
import cn.ktorfitx.common.ksp.util.log.kspLoggerLocal
import cn.ktorfitx.common.ksp.util.message.setLanguage
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider

internal class KtorfitxServerSymbolProcessorProvider : SymbolProcessorProvider {
	
	override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
		kspLoggerLocal.set(environment.logger)
		if (environment.options["ktorfitx.server.gradle.plugin.enabled"] != "true") {
			ktorfitxConfigError("Please add \"cn.ktorfitx.server\" Gradle Plugin!")
		}
		val packageName = environment.options["ktorfitx.generate.packageName"]!!
		val fileName = environment.options["ktorfitx.generate.fileName"]!!
		val funName = environment.options["ktorfitx.generate.funName"]!!
		val language = environment.options["ktorfitx.language"]!!
		setLanguage(language)
		return KtorfitxServerSymbolProcessor(environment.codeGenerator, packageName, fileName, funName)
	}
}
package cn.ktorfitx.server.ksp

import cn.ktorfitx.common.ksp.util.check.ktorfitxConfigError
import cn.ktorfitx.common.ksp.util.log.kspLogger
import cn.ktorfitx.common.ksp.util.message.Language
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider

internal class KtorfitxServerSymbolProcessorProvider : SymbolProcessorProvider {
	
	private companion object {
		
		private const val OPTION_SERVER_GRADLE_PLUGIN_ENABLED = "ktorfitx.server.gradle.plugin.enabled"
		private const val OPTION_GENERATE_PACKAGE_NAME = "ktorfitx.generate.packageName"
		private const val OPTION_GENERATE_FILE_NAME = "ktorfitx.generate.fileName"
		private const val OPTION_GENERATE_FUN_NAME = "ktorfitx.generate.funName"
		private const val OPTION_LANGUAGE = "ktorfitx.language"
	}
	
	override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
		kspLogger = environment.logger
		kspLogger.warn("1" + Thread.currentThread().name)
		if (!environment.options[OPTION_SERVER_GRADLE_PLUGIN_ENABLED].toBoolean()) {
			ktorfitxConfigError("Please add the \"cn.ktorfitx.server\" Gradle Plugin!")
		}
		val packageName = environment.options[OPTION_GENERATE_PACKAGE_NAME]!!
		val fileName = environment.options[OPTION_GENERATE_FILE_NAME]!!
		val funName = environment.options[OPTION_GENERATE_FUN_NAME]!!
		val language = environment.options[OPTION_LANGUAGE]!!
		Language.set(language)
		return KtorfitxServerSymbolProcessor(environment.codeGenerator, packageName, fileName, funName)
	}
}
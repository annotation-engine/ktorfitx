package cn.ktorfitx.server.ksp

import cn.ktorfitx.common.ksp.util.check.ktorfitxConfigError
import cn.ktorfitx.common.ksp.util.log.kspLogger
import cn.ktorfitx.common.ksp.util.message.Language
import cn.ktorfitx.common.ksp.util.message.invoke
import cn.ktorfitx.server.ksp.message.MESSAGE_MISSING_GRADLE_PLUGIN
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider

internal class KtorfitxServerSymbolProcessorProvider : SymbolProcessorProvider {
	
	private companion object {
		
		private const val OPTION_TYPE = "ktorfitx.type"
		private const val OPTION_GENERATE_PACKAGE_NAME = "ktorfitx.generate.packageName"
		private const val OPTION_GENERATE_FILE_NAME = "ktorfitx.generate.fileName"
		private const val OPTION_GENERATE_FUN_NAME = "ktorfitx.generate.funName"
		private const val OPTION_LANGUAGE = "ktorfitx.language"
		
		private const val TYPE_KTOR_SERVER = "KTOR_SERVER"
	}
	
	override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
		if (environment.options[OPTION_TYPE] != TYPE_KTOR_SERVER) {
			ktorfitxConfigError(MESSAGE_MISSING_GRADLE_PLUGIN())
		}
		kspLogger = environment.logger
		val packageName = environment.options[OPTION_GENERATE_PACKAGE_NAME]!!
		val fileName = environment.options[OPTION_GENERATE_FILE_NAME]!!
		val funName = environment.options[OPTION_GENERATE_FUN_NAME]!!
		val language = environment.options[OPTION_LANGUAGE]!!
		Language.set(language)
		return KtorfitxServerSymbolProcessor(environment.codeGenerator, packageName, fileName, funName)
	}
}
package cn.ktorfitx.multiplatform.ksp

import cn.ktorfitx.common.ksp.util.check.ktorfitxConfigError
import cn.ktorfitx.common.ksp.util.log.kspLogger
import cn.ktorfitx.common.ksp.util.message.Language
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider

internal class KtorfitxMultiplatformSymbolProcessorProvider : SymbolProcessorProvider {
	
	private companion object {
		
		private const val OPTION_MULTIPLATFORM_GRADLE_PLUGIN_ENABLED = "ktorfitx.multiplatform.gradle.plugin.enabled"
		private const val OPTION_LANGUAGE = "ktorfitx.language"
	}
	
	override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
		kspLogger = environment.logger
		if (!environment.options[OPTION_MULTIPLATFORM_GRADLE_PLUGIN_ENABLED].toBoolean()) {
			ktorfitxConfigError("Please add the \"cn.ktorfitx.multiplatform\" Gradle plugin")
		}
		val language = environment.options[OPTION_LANGUAGE]!!
		Language.set(language)
		return KtorfitxMultiplatformSymbolProcessor(
			codeGenerator = environment.codeGenerator,
			isCommon = environment.platforms.size > 1
		)
	}
}
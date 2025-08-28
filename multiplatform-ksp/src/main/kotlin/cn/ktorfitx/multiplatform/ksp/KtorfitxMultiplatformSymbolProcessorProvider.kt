package cn.ktorfitx.multiplatform.ksp

import cn.ktorfitx.common.ksp.util.check.ktorfitxConfigError
import cn.ktorfitx.common.ksp.util.log.kspLoggerLocal
import cn.ktorfitx.common.ksp.util.message.Language
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider

internal class KtorfitxMultiplatformSymbolProcessorProvider : SymbolProcessorProvider {
	
	override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
		kspLoggerLocal.set(environment.logger)
		if (environment.options["ktorfitx.multiplatform.gradle.plugin.enabled"] != "true") {
			ktorfitxConfigError("Please add \"cn.ktorfitx.multiplatform\" Gradle Plugin!")
		}
		val language = environment.options["ktorfitx.language"]!!
		Language.set(language)
		return KtorfitxMultiplatformSymbolProcessor(environment.codeGenerator)
	}
}
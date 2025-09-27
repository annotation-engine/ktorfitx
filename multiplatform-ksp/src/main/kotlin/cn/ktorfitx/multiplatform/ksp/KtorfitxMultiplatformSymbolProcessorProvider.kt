package cn.ktorfitx.multiplatform.ksp

import cn.ktorfitx.common.ksp.util.check.ktorfitxConfigError
import cn.ktorfitx.common.ksp.util.log.kspLogger
import cn.ktorfitx.common.ksp.util.message.Language
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

internal class KtorfitxMultiplatformSymbolProcessorProvider : SymbolProcessorProvider {
	
	private companion object {
		
		private const val OPTION_MULTIPLATFORM_GRADLE_PLUGIN_ENABLED = "ktorfitx.multiplatform.gradle.plugin.enabled"
		private const val OPTION_LANGUAGE = "ktorfitx.language"
		private const val OPTION_SOURCE_SETS_VARIANTS = "ktorfitx.sourceSets.variants"
		private const val OPTION_PROJECT_PATH = "ktorfitx.project.path"
	}
	
	override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
		if (!environment.options[OPTION_MULTIPLATFORM_GRADLE_PLUGIN_ENABLED].toBoolean()) {
			ktorfitxConfigError("Please add the \"cn.ktorfitx.multiplatform\" Gradle plugin.")
		}
		kspLogger = environment.logger
		Language.set(environment.options[OPTION_LANGUAGE]!!)
		val sourceSetsVariants = Json.parseToJsonElement(environment.options[OPTION_SOURCE_SETS_VARIANTS]!!)
			.jsonObject.mapValues { it.value.jsonPrimitive.content }
		return KtorfitxMultiplatformSymbolProcessor(
			codeGenerator = environment.codeGenerator,
			projectPath = environment.options[OPTION_PROJECT_PATH]!!,
			sourceSetsVariants = sourceSetsVariants,
			isCommon = environment.platforms.size > 1
		)
	}
}
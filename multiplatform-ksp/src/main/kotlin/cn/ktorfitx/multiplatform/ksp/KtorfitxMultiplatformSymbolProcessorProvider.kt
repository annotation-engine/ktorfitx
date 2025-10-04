package cn.ktorfitx.multiplatform.ksp

import cn.ktorfitx.common.ksp.util.check.ktorfitxConfigError
import cn.ktorfitx.common.ksp.util.log.kspLogger
import cn.ktorfitx.common.ksp.util.message.Language
import cn.ktorfitx.common.ksp.util.message.invoke
import cn.ktorfitx.multiplatform.ksp.message.MESSAGE_MISSING_GRADLE_PLUGIN
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider
import com.google.devtools.ksp.symbol.KSAnnotated

internal class KtorfitxMultiplatformSymbolProcessorProvider : SymbolProcessorProvider {
	
	private companion object {
		
		private const val OPTION_TYPE = "ktorfitx.type"
		private const val OPTION_LANGUAGE = "ktorfitx.language"
		
		private const val OPTION_SOURCE_SETS_ALL_NON_SHARED_NAMES = "ktorfitx.sourceSets.allNonSharedNames"
		private const val OPTION_SOURCE_SETS_CURRENT_SHARED_NAMES = "ktorfitx.sourceSets.currentSharedNames"
		private const val OPTION_PROJECT_PATH = "ktorfitx.project.path"
		
		private const val TYPE_KOTLIN_MULTIPLATFORM = "KOTLIN_MULTIPLATFORM"
		private const val TYPE_ANDROID = "ANDROID"
	}
	
	override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
		val type = environment.options[OPTION_TYPE]?.takeIf {
			it == TYPE_ANDROID || it == TYPE_KOTLIN_MULTIPLATFORM
		} ?: ktorfitxConfigError(MESSAGE_MISSING_GRADLE_PLUGIN())
		
		if (environment.platforms.size > 1) {
			return EmptySymbolProcessor
		}
		
		kspLogger = environment.logger
		Language.set(environment.options[OPTION_LANGUAGE]!!)
		
		val projectPath = environment.options[OPTION_PROJECT_PATH]!!
		
		val sourceSetModel = if (type == TYPE_KOTLIN_MULTIPLATFORM) {
			val currentSharedSets = environment.options[OPTION_SOURCE_SETS_CURRENT_SHARED_NAMES]!!.split(",")
			val allNonSharedSourceSets = environment.options[OPTION_SOURCE_SETS_ALL_NON_SHARED_NAMES]!!.split(",")
			MultiplatformSourceSetModel(currentSharedSets, allNonSharedSourceSets)
		} else AndroidOnlySourceSetModel
		
		return KtorfitxMultiplatformSymbolProcessor(
			codeGenerator = environment.codeGenerator,
			sourceSetModel = sourceSetModel,
			projectPath = projectPath
		)
	}
	
	private object EmptySymbolProcessor : SymbolProcessor {
		override fun process(resolver: Resolver): List<KSAnnotated> = emptyList()
	}
}

internal sealed interface SourceSetModel

internal class MultiplatformSourceSetModel(
	val currentSharedSourceSets: List<String>,
	val allNonSharedSourceSets: List<String>
) : SourceSetModel

internal object AndroidOnlySourceSetModel : SourceSetModel
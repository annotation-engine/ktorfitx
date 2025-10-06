package cn.ktorfitx.multiplatform.ksp

import cn.ktorfitx.common.ksp.util.check.ktorfitxConfigError
import cn.ktorfitx.common.ksp.util.log.kspLogger
import cn.ktorfitx.common.ksp.util.message.Language
import cn.ktorfitx.common.ksp.util.message.invoke
import cn.ktorfitx.multiplatform.ksp.message.MESSAGE_MISSING_GRADLE_PLUGIN
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider

internal class KtorfitxMultiplatformSymbolProcessorProvider : SymbolProcessorProvider {
	
	private companion object {
		
		private const val OPTION_TYPE = "ktorfitx.type"
		private const val OPTION_LANGUAGE = "ktorfitx.language"
		
		private const val OPTION_MIDDLE_SOURCE_SETS = "ktorfitx.middleSourceSets"
		private const val OPTION_PROJECT_PATH = "ktorfitx.project.path"
		
		private const val TYPE_KOTLIN_MULTIPLATFORM = "KOTLIN_MULTIPLATFORM"
		private const val TYPE_ANDROID = "ANDROID"
	}
	
	override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
		val type = environment.options[OPTION_TYPE]?.takeIf {
			it == TYPE_ANDROID || it == TYPE_KOTLIN_MULTIPLATFORM
		} ?: ktorfitxConfigError(MESSAGE_MISSING_GRADLE_PLUGIN())
		
		kspLogger = environment.logger
		Language.set(environment.options[OPTION_LANGUAGE]!!)
		
		val projectPath = environment.options[OPTION_PROJECT_PATH]!!
		
		val sourceSetModel = if (type == TYPE_KOTLIN_MULTIPLATFORM) {
			val middleSourceSets = environment.options[OPTION_MIDDLE_SOURCE_SETS]!!.split(",").filter { it != "" }
			MultiplatformSourceSetModel(
				isCommonMain = environment.platforms.size > 1,
				middleSourceSets = middleSourceSets
			)
		} else AndroidOnlySourceSetModel
		
		return KtorfitxMultiplatformSymbolProcessor(
			codeGenerator = environment.codeGenerator,
			sourceSetModel = sourceSetModel,
			projectPath = projectPath
		)
	}
}

internal sealed interface SourceSetModel

internal class MultiplatformSourceSetModel(
	val isCommonMain: Boolean,
	val middleSourceSets: List<String>
) : SourceSetModel

internal object AndroidOnlySourceSetModel : SourceSetModel
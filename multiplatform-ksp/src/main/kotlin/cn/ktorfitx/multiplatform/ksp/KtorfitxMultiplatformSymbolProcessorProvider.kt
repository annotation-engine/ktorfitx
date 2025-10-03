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
import kotlinx.serialization.json.Json
import java.io.File

internal class KtorfitxMultiplatformSymbolProcessorProvider : SymbolProcessorProvider {
	
	private companion object {
		
		private const val OPTION_TYPE = "ktorfitx.type"
		private const val OPTION_LANGUAGE = "ktorfitx.language"
		private const val OPTION_SOURCE_SETS_NON_SHARED_NAMES = "ktorfitx.sourceSets.nonSharedNames"
		private const val OPTION_PROJECT_PATH = "ktorfitx.project.path"
		
		private const val PATH_BUILD_KTORFITX = "build/ktorfitx"
		
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
		
		val sourceSetModel = if (type == TYPE_KOTLIN_MULTIPLATFORM) {
			val projectPath = environment.options[OPTION_PROJECT_PATH]!!
			val sharedSourceSets = getSharedSourceSets(projectPath)
			val nonSharedSourceSets = Json.decodeFromString<Set<String>>(environment.options[OPTION_SOURCE_SETS_NON_SHARED_NAMES]!!)
			MultiplatformSourceSetModel(projectPath, sharedSourceSets, nonSharedSourceSets)
		} else AndroidOnlySourceSetModel
		
		return KtorfitxMultiplatformSymbolProcessor(
			codeGenerator = environment.codeGenerator,
			sourceSetModel = sourceSetModel
		)
	}
	
	private object EmptySymbolProcessor : SymbolProcessor {
		override fun process(resolver: Resolver): List<KSAnnotated> = emptyList()
	}
	
	private fun getSharedSourceSets(projectPath: String): Set<String> {
		val file = File("$projectPath/$PATH_BUILD_KTORFITX".replace('/', File.separatorChar), "sharedSourceSets.json")
		if (!file.exists()) return emptySet()
		return try {
			Json.decodeFromString<Set<String>>(file.readText())
		} catch (_: Exception) {
			emptySet()
		}
	}
}

internal sealed interface SourceSetModel

internal class MultiplatformSourceSetModel(
	val projectPath: String,
	val sharedSourceSets: Set<String>,
	val nonSharedSourceSets: Set<String>
) : SourceSetModel

internal object AndroidOnlySourceSetModel : SourceSetModel
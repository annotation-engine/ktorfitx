package cn.ktorfitx.multiplatform.gradle.plugin

import cn.ktorfitx.multiplatform.gradle.plugin.KtorfitxMultiplatformMode.DEVELOPMENT
import cn.ktorfitx.multiplatform.gradle.plugin.KtorfitxMultiplatformMode.RELEASE
import com.google.devtools.ksp.gradle.KspExtension
import kotlinx.serialization.json.Json
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.Dependency
import org.gradle.api.artifacts.dsl.DependencyHandler
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.project
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinDependencyHandler
import org.jetbrains.kotlin.konan.file.File

@Suppress("unused")
class KtorfitxMultiplatformPlugin : Plugin<Project> {
	
	private companion object {
		
		private const val VERSION = "3.3.0-3.1.1"
		private const val KTOR_VERSION = "3.3.0"
		
		private const val GROUP_NAME = "cn.ktorfitx"
		
		private const val OPTION_IS_MULTIPLATFORM = "ktorfitx.isMultiplatform"
		private const val OPTION_LANGUAGE = "ktorfitx.language"
		private const val OPTION_SOURCE_SETS_VARIANTS = "ktorfitx.sourceSets.variants"
		private const val OPTION_PROJECT_PATH = "ktorfitx.project.path"
	}
	
	override fun apply(target: Project) = with(target) {
		val extension = target.extensions.create("ktorfitx", KtorfitxMultiplatformExtension::class.java)
		afterEvaluate {
			val language = extension.language.get()
			languageLocal.set(language)
			
			if (!pluginManager.hasPlugin("org.jetbrains.kotlin.multiplatform")) {
				error(MISSING_GRADLE_PLUGIN("org.jetbrains.kotlin.multiplatform"))
			}
			if (!pluginManager.hasPlugin("com.google.devtools.ksp")) {
				error(MISSING_GRADLE_PLUGIN("com.google.devtools.ksp"))
			}
			
			val hasKtorClientCore = configurations.any {
				val dependency = it.dependencies.find { it.group == "io.ktor" && it.name.startsWith("ktor-client-core") }
				if (dependency != null) {
					if (dependency.version != KTOR_VERSION) {
						VERSION_NOT_MATCH("${dependency.group}:${dependency.name}", dependency.version, KTOR_VERSION)
					}
					true
				} else false
			}
			if (!hasKtorClientCore) {
				error(MISSING_DEPENDENCIES("io.ktor:ktor-client-core", KTOR_VERSION))
			}
			
			val mode = extension.mode.get()
			val websocketsEnabled = extension.websockets.enabled.get()
			val mockEnabled = extension.mock.enabled.get()
			val kspExtension = extensions.getByType<KspExtension>().apply {
				this[OPTION_LANGUAGE] = language.name
				this[OPTION_IS_MULTIPLATFORM] = true
				this[OPTION_PROJECT_PATH] = projectDir.absolutePath
			}
			extensions.getByType<KotlinMultiplatformExtension>().apply {
				sourceSets.commonMain {
					dependencies {
						implementation("multiplatform-annotation", mode)
						implementation("multiplatform-core", mode)
						if (websocketsEnabled) {
							implementation("multiplatform-websockets", mode)
						}
						if (mockEnabled) {
							implementation("multiplatform-mock", mode)
						}
					}
				}
				val sourceSetsVariants = mutableMapOf<String, String>()
				sourceSets.configureEach {
					val sourceSetsVariant = when {
						this.name.startsWith("common") -> "metadata"
						this.name.startsWith("android") -> "android"
						else -> this.name.removeSuffix("Test").removeSuffix("Main")
					}
					sourceSetsVariants[this.name] = sourceSetsVariant
					this.kotlin.srcDir("build/generated/ksp/$sourceSetsVariant/${this.name}/kotlin".replace('/', File.separatorChar))
					kspExtension[OPTION_SOURCE_SETS_VARIANTS] = Json.encodeToString(sourceSetsVariants)
				}
			}
			dependencies {
				configurations.matching { it.name.startsWith("ksp") && it.name != "ksp" }.configureEach {
					add(this.name, "multiplatform-ksp", mode)
				}
			}
			tasks.named { name -> name.startsWith("ksp") }.configureEach {
				if (name != "kspCommonMainKotlinMetadata") {
					dependsOn("kspCommonMainKotlinMetadata")
				}
			}
			val isAndroid = gradle.startParameter.taskNames.any { "assemble" in it }
			if (isAndroid) {
				val isDebug = gradle.startParameter.taskNames.any { "assembleDebug" in it }
				listOf(
					"generateResourceAccessorsForAndroidDebug",
					"generateResourceAccessorsForAndroidMain",
					"generateActualResourceCollectorsForAndroidMain",
					"generateResourceAccessorsForAndroidRelease"
				).forEach { name ->
					if (name in tasks.names) {
						tasks.named(name).configure {
							dependsOn(if (isDebug) "kspDebugKotlinAndroid" else "kspReleaseKotlinAndroid")
						}
					}
				}
			}
		}
	}
	
	private inline operator fun <reified T : Any> KspExtension.set(key: String, value: T) {
		this.arg(key, value.toString())
	}
	
	private fun KotlinDependencyHandler.implementation(path: String, mode: KtorfitxMultiplatformMode): Dependency? {
		return when (mode) {
			DEVELOPMENT -> this.implementation(project(":$path"))
			RELEASE -> this.implementation("$GROUP_NAME:$path:$VERSION")
		}
	}
	
	private fun DependencyHandler.add(configurationName: String, path: String, mode: KtorfitxMultiplatformMode): Dependency? {
		return when (mode) {
			DEVELOPMENT -> this.add(configurationName, project(":$path"))
			RELEASE -> this.add(configurationName, "$GROUP_NAME:$path:$VERSION")
		}
	}
}
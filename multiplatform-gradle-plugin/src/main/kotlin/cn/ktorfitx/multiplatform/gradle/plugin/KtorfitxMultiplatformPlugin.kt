package cn.ktorfitx.multiplatform.gradle.plugin

import cn.ktorfitx.multiplatform.gradle.plugin.KtorfitxMultiplatformMode.DEVELOPMENT
import cn.ktorfitx.multiplatform.gradle.plugin.KtorfitxMultiplatformMode.RELEASE
import com.google.devtools.ksp.gradle.KspExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.Dependency
import org.gradle.api.artifacts.dsl.DependencyHandler
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.project
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinDependencyHandler

@Suppress("unused")
class KtorfitxMultiplatformPlugin : Plugin<Project> {
	
	private companion object {
		
		private const val VERSION = "3.3.0-3.2.0"
		private const val GROUP_NAME = "cn.ktorfitx"
		
		private const val OPTION_MULTIPLATFORM_GRADLE_PLUGIN_ENABLED = "ktorfitx.multiplatform.gradle.plugin.enabled"
		private const val OPTION_LANGUAGE = "ktorfitx.language"
	}
	
	override fun apply(target: Project) = with(target) {
		val extension = target.extensions.create("ktorfitx", KtorfitxMultiplatformExtension::class.java)
		if (!pluginManager.hasPlugin("org.jetbrains.kotlin.multiplatform")) {
			pluginManager.apply("org.jetbrains.kotlin.multiplatform")
		}
		if (!pluginManager.hasPlugin("com.google.devtools.ksp")) {
			pluginManager.apply("com.google.devtools.ksp")
		}
		afterEvaluate {
			val mode = extension.mode.get()
			val websocketsEnabled = extension.websockets.enabled.get()
			val mockEnabled = extension.mock.enabled.get()
			extensions.configure(KspExtension::class) {
				arg(OPTION_LANGUAGE, extension.language.get().name)
				arg(OPTION_MULTIPLATFORM_GRADLE_PLUGIN_ENABLED, "true")
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
					kotlin.srcDir("build/generated/ksp/metadata/commonMain/kotlin")
				}
			}
			dependencies {
				configurations.matching { it.name.startsWith("ksp") }.configureEach {
					add(this@configureEach.name, "multiplatform-ksp", mode)
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
			tasks.named { name -> name.startsWith("ksp") }.configureEach {
				if (name != "kspCommonMainKotlinMetadata") {
					dependsOn("kspCommonMainKotlinMetadata")
				}
			}
		}
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
package cn.ktorfitx.multiplatform.gradle.plugin

import cn.ktorfitx.multiplatform.gradle.plugin.KtorfitxMultiplatformMode.DEVELOPMENT
import cn.ktorfitx.multiplatform.gradle.plugin.KtorfitxMultiplatformMode.RELEASE
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.Dependency
import org.gradle.api.artifacts.dsl.DependencyHandler
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinDependencyHandler
import org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask

@Suppress("unused")
class KtorfitxMultiplatformPlugin : Plugin<Project> {
	
	private companion object {
		private const val VERSION = "3.2.3-3.1.0-Beta3"
	}
	
	override fun apply(target: Project) {
		val extension = target.extensions.create("ktorfitx", KtorfitxMultiplatformExtension::class.java)
		target.pluginManager.apply("org.jetbrains.kotlin.multiplatform")
		target.pluginManager.apply("com.google.devtools.ksp")
		target.afterEvaluate {
			val mode = extension.mode.get()
			when (mode) {
				DEVELOPMENT -> onDevelopmentMode(extension)
				RELEASE -> onReleaseMode(extension)
			}
			val taskName = "kspCommonMainKotlinMetadata"
			target.tasks.withType<KotlinCompilationTask<*>>().configureEach {
				if (this.name != taskName) {
					dependsOn(taskName)
				}
			}
		}
	}
	
	private fun Project.onDevelopmentMode(extension: KtorfitxMultiplatformExtension) {
		extensions.getByType<KotlinMultiplatformExtension>().apply {
			sourceSets.commonMain {
				dependencies {
					implementation(project(":multiplatform-annotation"))
					implementation(project(":multiplatform-core"))
					if (extension.websockets.enabled.get()) {
						implementation(project(":multiplatform-websockets"))
					}
					if (extension.mock.enabled.get()) {
						implementation(project(":multiplatform-mock"))
					}
				}
				kotlin.srcDir(extension.ksp.kspCommonMainGeneratedDir.get())
			}
		}
		dependencies {
			kspCommonMainMetadata(project(":multiplatform-ksp"))
		}
	}
	
	private fun Project.onReleaseMode(extension: KtorfitxMultiplatformExtension) {
		extensions.getByType<KotlinMultiplatformExtension>().apply {
			sourceSets.commonMain {
				dependencies {
					implementation("cn.ktorfitx", "multiplatform-annotation")
					implementation("cn.ktorfitx", "multiplatform-core")
					if (extension.websockets.enabled.get()) {
						implementation("cn.ktorfitx", "multiplatform-websockets")
					}
					if (extension.mock.enabled.get()) {
						implementation("cn.ktorfitx", "multiplatform-mock")
					}
				}
				kotlin.srcDir(extension.ksp.kspCommonMainGeneratedDir.get())
			}
		}
		dependencies {
			kspCommonMainMetadata("cn.ktorfitx", "multiplatform-ksp")
		}
	}
	
	private fun KotlinDependencyHandler.implementation(group: String, name: String): Dependency? =
		implementation("$group:$name:$VERSION")
	
	private fun DependencyHandler.kspCommonMainMetadata(group: String, name: String): Dependency? =
		add("kspCommonMainMetadata", "$group:$name:$VERSION")
	
	private fun DependencyHandler.kspCommonMainMetadata(project: Project): Dependency? =
		add("kspCommonMainMetadata", project)
}
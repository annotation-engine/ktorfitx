package cn.ktorfitx.multiplatform.gradle.plugin

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
		
		private const val VERSION = "3.2.3-3.0.4"
	}
	
	override fun apply(target: Project) {
		val extension = target.extensions.create("ktorfitx", KtorfitxMultiplatformExtension::class.java)
		target.pluginManager.apply("org.jetbrains.kotlin.multiplatform")
		target.pluginManager.apply("com.google.devtools.ksp")
		target.afterEvaluate {
			extensions.getByType<KotlinMultiplatformExtension>().apply {
				sourceSets.commonMain {
					dependencies {
						implementation(group = "cn.ktorfitx", name = "multiplatform-annotation")
						implementation(group = "cn.ktorfitx", name = "multiplatform-core")
						if (extension.websockets.enabled.get()) {
							implementation(group = "cn.ktorfitx", name = "multiplatform-websockets")
						}
						if (extension.mock.enabled.get()) {
							implementation(group = "cn.ktorfitx", name = "multiplatform-mock")
						}
					}
					kotlin.srcDir(extension.ksp.kspGeneratedSrcDir.get())
				}
			}
			
			if (extension.ksp.kspMetadataGenerationTask.get()) {
				tasks.withType<KotlinCompilationTask<*>>().all {
					"kspCommonMainKotlinMetadata".also {
						if (name != it) dependsOn(it)
					}
				}
			}
		}
		target.dependencies {
			kspCommonMainMetadata(group = "cn.ktorfitx", name = "multiplatform-ksp")
		}
	}
	
	private fun KotlinDependencyHandler.implementation(group: String, name: String): Dependency? =
		implementation("$group:$name:$VERSION")
	
	private fun DependencyHandler.kspCommonMainMetadata(group: String, name: String): Dependency? =
		add("kspCommonMainMetadata", "$group:$name:$VERSION")
}
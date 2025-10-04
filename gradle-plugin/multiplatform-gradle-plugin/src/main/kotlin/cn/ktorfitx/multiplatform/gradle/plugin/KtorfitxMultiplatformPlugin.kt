package cn.ktorfitx.multiplatform.gradle.plugin

import com.google.devtools.ksp.gradle.KspAATask
import com.google.devtools.ksp.gradle.KspExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.Dependency
import org.gradle.api.artifacts.dsl.DependencyHandler
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.project
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinDependencyHandler

@Suppress("unused")
class KtorfitxMultiplatformPlugin : Plugin<Project> {
	
	private companion object {
		
		private const val VERSION = "3.3.0-3.2.0-RC2"
		private const val KTOR_VERSION = "3.3.0"
		
		private const val GROUP_NAME = "cn.ktorfitx"
		
		private const val OPTION_TYPE = "ktorfitx.type"
		private const val OPTION_LANGUAGE = "ktorfitx.language"
		private const val OPTION_SOURCE_SETS_ALL_NON_SHARED_NAMES = "ktorfitx.sourceSets.allNonSharedNames"
		private const val OPTION_SOURCE_SETS_CURRENT_SHARED_NAMES = "ktorfitx.sourceSets.currentSharedNames"
		private const val OPTION_PROJECT_PATH = "ktorfitx.project.path"
		
		private const val PATH_BUILD_KTORFITX = "build/ktorfitx"
		
		private const val TYPE_KOTLIN_MULTIPLATFORM = "KOTLIN_MULTIPLATFORM"
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
			
			val isDevelopmentMode = extension.isDevelopmentMode
			val websocketsEnabled = extension.websockets.enabled.get()
			val mockEnabled = extension.mock.enabled.get()
			val kspExtension = extensions.getByType<KspExtension>().apply {
				this[OPTION_LANGUAGE] = language.name
				this[OPTION_TYPE] = TYPE_KOTLIN_MULTIPLATFORM
				this[OPTION_PROJECT_PATH] = projectDir.absolutePath
			}
			extensions.getByType<KotlinMultiplatformExtension>().apply {
				sourceSets.commonMain {
					dependencies {
						checkDependency("io.ktor", "ktor-client-core")
						implementation("multiplatform-annotation", isDevelopmentMode)
						implementation("multiplatform-core", isDevelopmentMode)
						if (websocketsEnabled) {
							checkDependency("io.ktor", "ktor-client-websockets")
							implementation("multiplatform-websockets", isDevelopmentMode)
						}
						if (mockEnabled) {
							implementation("multiplatform-mock", isDevelopmentMode)
						}
					}
				}
				
				val allNonSharedNames = sourceSets.mapNotNull { sourceSet ->
					sourceSet.name.takeIf { "Test" !in it && it != "commonMain" }
				}
				kspExtension[OPTION_SOURCE_SETS_ALL_NON_SHARED_NAMES] = allNonSharedNames.joinToString(",")
				
				sourceSets.configureEach {
					if ("Test" in name) return@configureEach
					if (name in allNonSharedNames) return@configureEach
					this.kotlin.srcDir("build/generated/ksp/metadata/$name/kotlin")
				}
			}
			dependencies {
				configurations.matching {
					it.name.startsWith("ksp") &&
							it.name != "ksp" &&
							"Test" !in it.name
				}.configureEach {
					add(this.name, "multiplatform-ksp", isDevelopmentMode)
				}
			}
			tasks.withType<KspAATask>().configureEach {
				doFirst {
					val currentSharedSourceSets = getCurrentSharedSourceSets(this.name)
					kspConfig.processorOptions[OPTION_SOURCE_SETS_CURRENT_SHARED_NAMES] = currentSharedSourceSets.joinToString(",")
				}
				if (name != "kspCommonMainKotlinMetadata") {
					dependsOn("kspCommonMainKotlinMetadata")
				}
			}
			val isAndroid = gradle.startParameter.taskNames.any { "assemble" in it }
			if (isAndroid) {
				val isRelease = gradle.startParameter.taskNames.any { "assembleRelease" in it }
				if (isRelease) {
					listOf(
						"generateResourceAccessorsForAndroidRelease",
						"generateResourceAccessorsForAndroidMain"
					).forEach { name ->
						if (name in tasks.names) {
							tasks.named(name).configure {
								this.dependsOn("kspReleaseKotlinAndroid")
							}
						}
					}
				} else {
					listOf(
						"generateResourceAccessorsForAndroidDebug",
						"generateResourceAccessorsForAndroidMain"
					).forEach { name ->
						if (name in tasks.names) {
							tasks.named(name).configure {
								this.dependsOn("kspDebugKotlinAndroid")
							}
						}
					}
				}
			}
		}
	}
	
	private val sourceSetRelationMap = mapOf(
		"macosArm64" to "macos",
		"macosX64" to "macos",
		"iosArm64" to "ios",
		"iosSimulatorArm64" to "ios",
		"iosX64" to "ios",
		"tvosArm64" to "tvos",
		"tvosSimulatorArm64" to "tvos",
		"tvosX64" to "tvos",
		"watchosArm32" to "watchos",
		"watchosArm64" to "watchos",
		"watchosSimulatorArm64" to "watchos",
		"watchosSimulatorDeviceArm64" to "watchos",
		"watchosX64" to "watchos",
		"linuxArm32HfpMain" to "linux",
		"linuxArm64" to "linux",
		"linuxX64" to "linux",
		"mingwX64" to "mingw",
		"androidNativeArm32" to "androidNative",
		"androidNativeArm64" to "androidNative",
		"androidNativeX86" to "androidNative",
		"androidNativeX64" to "androidNative",
		"js" to "web",
		"wasmJs" to "web",
		"macos" to "apple",
		"ios" to "apple",
		"tvos" to "apple",
		"watchos" to "apple",
		"apple" to "native",
		"linux" to "native",
		"mingw" to "native",
		"androidNative" to "native",
		"native" to "common",
		"android" to "common",
		"jvm" to "common",
		"desktop" to "common",
		"web" to "common"
	)
	
	private fun getCurrentSharedSourceSets(taskName: String): List<String> {
		if ("Test" in taskName) return emptyList()
		val seed = when {
			"Android" in taskName -> "android"
			"CommonMain" in taskName -> "common"
			else -> taskName.removePrefix("kspKotlin").replaceFirstChar { it.lowercaseChar() }
		}
		return generateSequence(seed) { sourceSetRelationMap[it] }
			.drop(1)
			.map { "${it}Main" }
			.toList()
	}
	
	private fun Project.checkDependency(group: String, name: String) {
		val contains = this.configurations.any {
			val dependency = it.dependencies.find { it.group == group && it.name.startsWith(name) }
			if (dependency != null) {
				if (dependency.version != KTOR_VERSION) {
					error(VERSION_NOT_MATCH(dependency.group, dependency.name, dependency.version, KTOR_VERSION))
				}
				true
			} else false
		}
		if (!contains) {
			error(MISSING_DEPENDENCIES(group, name, KTOR_VERSION))
		}
	}
	
	private operator fun KspExtension.set(key: String, value: String) {
		this.arg(key, value)
	}
	
	private operator fun MapProperty<String, String>.set(key: String, value: String) {
		this.put(key, value)
	}
	
	private fun KotlinDependencyHandler.implementation(path: String, isDevelopmentMode: Property<Boolean>): Dependency? {
		return if (isDevelopmentMode.get()) {
			this.implementation(project(":$path"))
		} else {
			this.implementation("$GROUP_NAME:$path:$VERSION")
		}
	}
	
	private fun DependencyHandler.add(configurationName: String, path: String, isDevelopmentMode: Property<Boolean>): Dependency? {
		return if (isDevelopmentMode.get()) {
			this.add(configurationName, project(":$path"))
		} else {
			this.add(configurationName, "$GROUP_NAME:$path:$VERSION")
		}
	}
}
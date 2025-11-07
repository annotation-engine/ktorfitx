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
		
		private const val VERSION = "3.3.2-3.2.6"
		private const val KTOR_VERSION = "3.3.2"
		
		private const val GROUP_NAME = "cn.ktorfitx"
		
		private const val OPTION_TYPE = "ktorfitx.type"
		private const val OPTION_LANGUAGE = "ktorfitx.language"
		private const val OPTION_MIDDLE_SOURCE_SETS = "ktorfitx.middleSourceSets"
		private const val OPTION_PROJECT_PATH = "ktorfitx.project.path"
		
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
			extensions.getByType<KspExtension>().apply {
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
					val middleSourceSets = getMiddleSourceSets(this.name)
					kspConfig.processorOptions[OPTION_MIDDLE_SOURCE_SETS] = middleSourceSets.joinToString(",")
				}
				if (name != "kspCommonMainKotlinMetadata") {
					dependsOn("kspCommonMainKotlinMetadata")
				}
			}
		}
	}
	
	private val sourceSetRelationMap = mapOf(
		"macosArm64Main" to "macosMain",
		"macosX64Main" to "macosMain",
		"iosArm64Main" to "iosMain",
		"iosSimulatorArm64Main" to "iosMain",
		"iosX64Main" to "iosMain",
		"tvosArm64Main" to "tvosMain",
		"tvosSimulatorArm64Main" to "tvosMain",
		"tvosX64Main" to "tvosMain",
		"watchosArm32Main" to "watchosMain",
		"watchosArm64Main" to "watchosMain",
		"watchosSimulatorArm64Main" to "watchosMain",
		"watchosSimulatorDeviceArm64Main" to "watchosMain",
		"watchosX64Main" to "watchosMain",
		"linuxArm32HfpMain" to "linuxMain",
		"linuxArm64Main" to "linuxMain",
		"linuxX64Main" to "linuxMain",
		"mingwX64Main" to "mingwMain",
		"androidNativeArm32Main" to "androidNativeMain",
		"androidNativeArm64Main" to "androidNativeMain",
		"androidNativeX86Main" to "androidNativeMain",
		"androidNativeX64Main" to "androidNativeMain",
		"jsMain" to "webMain",
		"wasmJsMain" to "webMain",
		"macosMain" to "appleMain",
		"iosMain" to "appleMain",
		"tvosMain" to "appleMain",
		"watchosMain" to "appleMain",
		"appleMain" to "nativeMain",
		"linuxMain" to "nativeMain",
		"mingwMain" to "nativeMain",
		"androidNativeMain" to "nativeMain",
		"nativeMain" to "commonMain",
		"androidMain" to "commonMain",
		"jvmMain" to "commonMain",
		"desktopMain" to "commonMain",
		"webMain" to "commonMain"
	)
	
	private fun getMiddleSourceSets(taskName: String): List<String> {
		if ("Test" in taskName) return emptyList()
		val seed = when {
			"Android" in taskName -> "androidMain"
			"CommonMain" in taskName -> "commonMain"
			else -> taskName.removePrefix("kspKotlin").replaceFirstChar { it.lowercaseChar() } + "Main"
		}
		return generateSequence(seed) { sourceSetRelationMap[it] }
			.toList()
			.drop(1)
			.dropLast(1)
	}
	
	private fun Project.checkDependency(group: String, name: String) {
		val contains = this.configurations.any { configuration ->
			val dependency = configuration.dependencies.find { it.group == group && it.name.startsWith(name) }
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
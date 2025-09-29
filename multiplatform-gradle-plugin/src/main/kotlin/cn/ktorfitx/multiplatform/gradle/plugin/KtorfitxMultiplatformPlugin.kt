package cn.ktorfitx.multiplatform.gradle.plugin

import cn.ktorfitx.multiplatform.gradle.plugin.KtorfitxMultiplatformMode.DEVELOPMENT
import cn.ktorfitx.multiplatform.gradle.plugin.KtorfitxMultiplatformMode.RELEASE
import com.google.devtools.ksp.gradle.KspAATask
import com.google.devtools.ksp.gradle.KspExtension
import com.google.devtools.ksp.gradle.KspTask
import kotlinx.serialization.json.Json
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.Dependency
import org.gradle.api.artifacts.dsl.DependencyHandler
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.project
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinDependencyHandler
import java.io.File

@Suppress("unused")
class KtorfitxMultiplatformPlugin : Plugin<Project> {
	
	private companion object {
		
		private const val VERSION = "3.3.0-3.2.0-Beta1"
		private const val KTOR_VERSION = "3.3.0"
		
		private const val GROUP_NAME = "cn.ktorfitx"
		
		private const val OPTION_IS_MULTIPLATFORM = "ktorfitx.isMultiplatform"
		private const val OPTION_LANGUAGE = "ktorfitx.language"
		private const val OPTION_SOURCE_SETS_NON_SHARED_NAMES = "ktorfitx.sourceSets.nonSharedNames"
		private const val OPTION_PROJECT_PATH = "ktorfitx.project.path"
		
		private const val PATH_BUILD_KTORFITX = "/build/ktorfitx"
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
				
				val nonSharedNames = sourceSets.mapNotNull {
					it.name.takeIf { "Test" !in it && it != "commonMain" }
				}
				kspExtension[OPTION_SOURCE_SETS_NON_SHARED_NAMES] = Json.encodeToString(nonSharedNames)
				
				sourceSets.configureEach {
					if ("Test" in name) return@configureEach
					if (name in nonSharedNames) return@configureEach
					val srcPath = "build/generated/ksp/metadata/$name/kotlin".replace('/', File.separatorChar)
					this.kotlin.srcDir(srcPath)
				}
			}
			dependencies {
				configurations.matching {
					it.name.startsWith("ksp") &&
							it.name != "ksp" &&
							"Test" !in it.name
				}.configureEach {
					add(this.name, "multiplatform-ksp", mode)
				}
			}
			tasks.withType<KspTask>().configureEach {
				this.doFirst {
					writeSharedSourceSetsNames(projectDir.absolutePath, this.name)
				}
			}
			tasks.withType<KspAATask>().configureEach {
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
	
	private fun writeSharedSourceSetsNames(projectPath: String, taskName: String) {
		if ("Test" in taskName) {
			write(projectPath, emptySet())
			return
		}
		var currentSourceSet = when {
			taskName in listOf("kspDebugKotlinAndroid", "kspReleaseKotlinAndroid") -> "androidMain"
			else -> taskName.removePrefix("kspKotlin").replaceFirstChar { it.lowercaseChar() }
		}
		val sharedSourceSets = mutableSetOf<String>()
		while (currentSourceSet in sourceSetRelationMap) {
			currentSourceSet = sourceSetRelationMap[currentSourceSet]!!
			sharedSourceSets += "${currentSourceSet}Main"
		}
		write(projectPath, sharedSourceSets)
	}
	
	private fun write(projectPath: String, sharedSourceSets: Set<String>) {
		val parent = File("$projectPath$PATH_BUILD_KTORFITX".replace('/', File.separatorChar))
		if (!parent.exists()) {
			parent.mkdirs()
		}
		val file = File(parent, "sharedSourceSets.json")
		if (!file.exists()) {
			file.createNewFile()
		}
		val json = Json.encodeToString(sharedSourceSets)
		file.writeText(json)
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
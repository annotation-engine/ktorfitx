package cn.ktorfitx.android.gradle.plugin

import cn.ktorfitx.android.gradle.plugin.KtorfitxAndroidMode.DEVELOPMENT
import cn.ktorfitx.android.gradle.plugin.KtorfitxAndroidMode.RELEASE
import com.google.devtools.ksp.gradle.KspExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.Dependency
import org.gradle.api.provider.Property
import org.gradle.kotlin.dsl.*

@Suppress("unused")
class KtorfitxAndroidPlugin : Plugin<Project> {
	
	private companion object {
		private const val VERSION = "3.3.0-3.1.1"
		private const val KTOR_VERSION = "3.3.0"
		
		private const val GROUP_NAME = "cn.ktorfitx"
		
		private const val OPTION_IS_MULTIPLATFORM = "ktorfitx.isMultiplatform"
		private const val OPTION_LANGUAGE = "ktorfitx.language"
		private const val OPTION_PROJECT_PATH = "ktorfitx.project.path"
	}
	
	override fun apply(target: Project) = with(target) {
		val extension = extensions.create<KtorfitxAndroidExtension>("ktorfitx")
		afterEvaluate {
			val language = extension.language.get()
			languageLocal.set(language)
			
			if (!pluginManager.hasPlugin("com.android.application") && !pluginManager.hasPlugin("com.android.library")) {
				error(MISSING_ANDROID_GRADLE_PLUGIN())
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
			extensions.getByType<KspExtension>().apply {
				this[OPTION_LANGUAGE] = language.name
				this[OPTION_IS_MULTIPLATFORM] = false
				this[OPTION_PROJECT_PATH] = projectDir.absolutePath
			}
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
		dependencies {
			ksp("multiplatform-ksp", extension.mode)
		}
	}
	
	private inline operator fun <reified T : Any> KspExtension.set(key: String, value: T) {
		this.arg(key, value.toString())
	}
	
	private fun DependencyHandlerScope.implementation(path: String, mode: KtorfitxAndroidMode): Dependency? {
		return when (mode) {
			DEVELOPMENT -> this.add("implementation", project(":$path"))
			RELEASE -> this.add("implementation", "$GROUP_NAME:$path:$VERSION")
		}
	}
	
	private fun DependencyHandlerScope.ksp(path: String, mode: Property<KtorfitxAndroidMode>) {
		this.addProvider("ksp", mode.map {
			when (it) {
				DEVELOPMENT -> project(":$path")
				RELEASE -> "$GROUP_NAME:$path:$VERSION"
			}
		})
	}
}
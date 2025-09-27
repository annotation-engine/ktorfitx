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
		private const val GROUP_NAME = "cn.ktorfitx"
		
		private const val OPTION_IS_MULTIPLATFORM = "ktorfitx.isMultiplatform"
		private const val OPTION_LANGUAGE = "ktorfitx.language"
		private const val OPTION_PROJECT_PATH = "ktorfitx.project.path"
	}
	
	override fun apply(target: Project) = with(target) {
		val extension = extensions.create<KtorfitxAndroidExtension>("ktorfitx")
		val isApplication = when {
			pluginManager.hasPlugin("com.android.application") -> true
			pluginManager.hasPlugin("com.android.library") -> false
			else -> error("Please add the \"com.android.application\" or \"com.android.library\" Gradle Plugin.")
		}
		if (!pluginManager.hasPlugin("com.google.devtools.ksp")) {
			error("Please add Gradle Plugin \"com.google.devtools.ksp\".")
		}
		afterEvaluate {
			val mode = extension.mode.get()
			val websocketsEnabled = extension.websockets.enabled.get()
			val mockEnabled = extension.mock.enabled.get()
			val kspExtension = extensions.getByType<KspExtension>()
			kspExtension[OPTION_LANGUAGE] = extension.language.get().name
			kspExtension[OPTION_IS_MULTIPLATFORM] = false
			kspExtension[OPTION_PROJECT_PATH] = this.projectDir.absolutePath
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
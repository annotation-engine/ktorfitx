package cn.ktorfitx.android.gradle.plugin

import com.google.devtools.ksp.gradle.KspExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.Dependency
import org.gradle.api.provider.Property
import org.gradle.kotlin.dsl.*

@Suppress("unused")
class KtorfitxAndroidPlugin : Plugin<Project> {
	
	private companion object {
		private const val VERSION = "3.3.0-3.2.1"
		private const val KTOR_VERSION = "3.3.0"
		
		private const val GROUP_NAME = "cn.ktorfitx"
		
		private const val OPTION_TYPE = "ktorfitx.type"
		private const val OPTION_LANGUAGE = "ktorfitx.language"
		private const val OPTION_PROJECT_PATH = "ktorfitx.project.path"
		
		private const val TYPE_ANDROID = "ANDROID"
	}
	
	override fun apply(target: Project) = with(target) {
		val extension = extensions.create<KtorfitxAndroidExtension>("ktorfitx")
		val isDevelopmentMode = extension.isDevelopmentMode
		dependencies {
			ksp("multiplatform-ksp", isDevelopmentMode)
		}
		afterEvaluate {
			val language = extension.language.get()
			languageLocal.set(language)
			
			if (!pluginManager.hasPlugin("com.android.application") && !pluginManager.hasPlugin("com.android.library")) {
				error(MISSING_ANDROID_GRADLE_PLUGIN())
			}
			if (!pluginManager.hasPlugin("com.google.devtools.ksp")) {
				error(MISSING_GRADLE_PLUGIN("com.google.devtools.ksp"))
			}
			
			val websocketsEnabled = extension.websockets.enabled.get()
			val mockEnabled = extension.mock.enabled.get()
			extensions.getByType<KspExtension>().apply {
				this[OPTION_LANGUAGE] = language.name
				this[OPTION_TYPE] = TYPE_ANDROID
				this[OPTION_PROJECT_PATH] = projectDir.absolutePath
			}
			
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
	
	private inline operator fun <reified T : Any> KspExtension.set(key: String, value: T) {
		this.arg(key, value.toString())
	}
	
	private fun DependencyHandlerScope.implementation(path: String, isDevelopmentMode: Property<Boolean>): Dependency? {
		return if (isDevelopmentMode.get()) {
			this.add("implementation", project(":$path"))
		} else {
			this.add("implementation", "$GROUP_NAME:$path:$VERSION")
		}
	}
	
	private fun DependencyHandlerScope.ksp(path: String, isDevelopmentMode: Property<Boolean>) {
		this.addProvider("ksp", isDevelopmentMode.map {
			if (it) project(":$path") else "$GROUP_NAME:$path:$VERSION"
		})
	}
}
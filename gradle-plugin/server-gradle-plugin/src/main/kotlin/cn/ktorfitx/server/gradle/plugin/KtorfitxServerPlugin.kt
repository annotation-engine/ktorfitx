package cn.ktorfitx.server.gradle.plugin

import com.google.devtools.ksp.gradle.KspExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.Dependency
import org.gradle.api.provider.Property
import org.gradle.kotlin.dsl.DependencyHandlerScope
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.project

@Suppress("unused")
class KtorfitxServerPlugin : Plugin<Project> {
	
	private companion object {
		
		private const val VERSION = "3.3.1-3.2.4"
		private const val KTOR_VERSION = "3.3.1"
		private const val KSP_VERSION = "2.2.20-2.0.3"
		
		private const val GROUP_NAME = "cn.ktorfitx"
		
		private const val OPTION_TYPE = "ktorfitx.type"
		private const val OPTION_GENERATE_PACKAGE_NAME = "ktorfitx.generate.packageName"
		private const val OPTION_GENERATE_FILE_NAME = "ktorfitx.generate.fileName"
		private const val OPTION_GENERATE_FUN_NAME = "ktorfitx.generate.funName"
		private const val OPTION_LANGUAGE = "ktorfitx.language"
		
		private const val TYPE_KTOR_SERVER = "KTOR_SERVER"
	}
	
	override fun apply(target: Project) = with(target) {
		val extension = extensions.create("ktorfitx", KtorfitxServerExtension::class.java)
		val isDevelopmentMode = extension.isDevelopmentMode
		dependencies {
			ksp("server-ksp", isDevelopmentMode)
		}
		afterEvaluate {
			val language = extension.language.get()
			languageLocal.set(language)
			
			if (!pluginManager.hasPlugin("com.google.devtools.ksp")) {
				error(MISSING_GRADLE_PLUGIN("com.google.devtools.ksp"))
			}
			
			extensions.getByType<KspExtension>().apply {
				this[OPTION_GENERATE_PACKAGE_NAME] = extension.generate.packageName.getOrElse("$group.generated")
				this[OPTION_GENERATE_FILE_NAME] = extension.generate.fileName.get().removeSuffix(".kt")
				this[OPTION_GENERATE_FUN_NAME] = extension.generate.funName.get()
				this[OPTION_LANGUAGE] = language.name
				this[OPTION_TYPE] = TYPE_KTOR_SERVER
			}
			
			val authEnabled = extension.auth.enabled.get()
			val websocketsEnabled = extension.websockets.enabled.get()
			dependencies {
				checkDependency("io.ktor", "ktor-server-core")
				implementation("server-core", isDevelopmentMode)
				implementation("server-annotation", isDevelopmentMode)
				if (authEnabled) {
					checkDependency("io.ktor", "ktor-server-auth")
					implementation("server-auth", isDevelopmentMode)
				}
				if (websocketsEnabled) {
					checkDependency("io.ktor", "ktor-server-websockets")
					implementation("server-websockets", isDevelopmentMode)
				}
			}
		}
	}
	
	private fun Project.checkDependency(group: String, name: String) {
		val contains = this.configurations.any { configuration ->
			val dependency = configuration.dependencies.find { it.group == group && it.name.startsWith(name) }
			if (dependency != null) {
				if (dependency.version != KTOR_VERSION) {
					error(VERSION_NOT_MATCH("${dependency.group}:${dependency.name}", dependency.version, KTOR_VERSION))
				}
				true
			} else false
		}
		if (!contains) {
			error(MISSING_DEPENDENCIES(group, name, KTOR_VERSION))
		}
	}
	
	private operator fun <T : Any> KspExtension.set(key: String, value: T) {
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
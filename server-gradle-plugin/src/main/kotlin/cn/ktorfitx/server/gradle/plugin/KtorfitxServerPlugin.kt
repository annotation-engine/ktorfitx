package cn.ktorfitx.server.gradle.plugin

import com.google.devtools.ksp.gradle.KspExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.Dependency
import org.gradle.api.artifacts.ProjectDependency
import org.gradle.kotlin.dsl.DependencyHandlerScope
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.project

@Suppress("unused")
class KtorfitxServerPlugin : Plugin<Project> {
	
	private companion object {
		
		private const val VERSION = "3.2.3-3.1.0"
		
		private const val OPTION_SERVER_GRADLE_PLUGIN_ENABLED = "ktorfitx.server.gradle.plugin.enabled"
		private const val OPTION_GENERATE_PACKAGE_NAME = "ktorfitx.generate.packageName"
		private const val OPTION_GENERATE_FILE_NAME = "ktorfitx.generate.fileName"
		private const val OPTION_GENERATE_FUN_NAME = "ktorfitx.generate.funName"
		private const val OPTION_LANGUAGE = "ktorfitx.language"
	}
	
	override fun apply(target: Project) {
		target.pluginManager.apply("com.google.devtools.ksp")
		val extension = target.extensions.create("ktorfitx", KtorfitxServerExtension::class.java)
		target.dependencies {
			addServerKspProvider(extension)
		}
		target.afterEvaluate {
			this.extensions.configure(KspExtension::class) {
				this.arg(OPTION_GENERATE_PACKAGE_NAME, extension.generate.packageName.getOrElse("$group.generated"))
				this.arg(OPTION_GENERATE_FILE_NAME, extension.generate.fileName.get().removeSuffix(".kt"))
				this.arg(OPTION_GENERATE_FUN_NAME, extension.generate.funName.get())
				this.arg(OPTION_LANGUAGE, extension.language.get().name)
				this.arg(OPTION_SERVER_GRADLE_PLUGIN_ENABLED, true.toString())
			}
			when (extension.mode.get()) {
				KtorfitxServerMode.RELEASE -> onReleaseMode(extension)
				KtorfitxServerMode.DEVELOPMENT -> onDevelopmentMode(extension)
			}
		}
	}
	
	private fun Project.onReleaseMode(
		extension: KtorfitxServerExtension,
	) {
		dependencies {
			implementation("cn.ktorfitx", "server-core")
			implementation("cn.ktorfitx", "server-annotation")
			if (extension.auth.enabled.get()) {
				implementation("cn.ktorfitx", "server-auth")
			}
			if (extension.websockets.enabled.get()) {
				implementation("cn.ktorfitx", "server-websockets")
			}
		}
	}
	
	private fun Project.onDevelopmentMode(
		extension: KtorfitxServerExtension,
	) {
		dependencies {
			implementation(project(":server-core"))
			implementation(project(":server-annotation"))
			if (extension.auth.enabled.get()) {
				implementation(project(":server-auth"))
			}
			if (extension.websockets.enabled.get()) {
				implementation(project(":server-websockets"))
			}
		}
	}
	
	private fun DependencyHandlerScope.implementation(group: String, name: String): Dependency? =
		add("implementation", "$group:$name:$VERSION")
	
	private fun DependencyHandlerScope.implementation(project: ProjectDependency): Dependency? =
		add("implementation", project)
	
	private fun DependencyHandlerScope.addServerKspProvider(extension: KtorfitxServerExtension) {
		addProvider("ksp", extension.mode.map {
			when (it) {
				KtorfitxServerMode.DEVELOPMENT -> project(":server-ksp")
				KtorfitxServerMode.RELEASE -> "cn.ktorfitx:server-ksp:$VERSION"
			}
		})
	}
}
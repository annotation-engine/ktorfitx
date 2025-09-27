package cn.ktorfitx.server.gradle.plugin

import cn.ktorfitx.server.gradle.plugin.KtorfitxServerMode.DEVELOPMENT
import cn.ktorfitx.server.gradle.plugin.KtorfitxServerMode.RELEASE
import com.google.devtools.ksp.gradle.KspExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.Dependency
import org.gradle.api.provider.Property
import org.gradle.kotlin.dsl.DependencyHandlerScope
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.project

@Suppress("unused")
class KtorfitxServerPlugin : Plugin<Project> {
	
	private companion object {
		
		private const val VERSION = "3.3.0-3.1.1"
		private const val GROUP_NAME = "cn.ktorfitx"
		
		private const val OPTION_SERVER_GRADLE_PLUGIN_ENABLED = "ktorfitx.server.gradle.plugin.enabled"
		private const val OPTION_GENERATE_PACKAGE_NAME = "ktorfitx.generate.packageName"
		private const val OPTION_GENERATE_FILE_NAME = "ktorfitx.generate.fileName"
		private const val OPTION_GENERATE_FUN_NAME = "ktorfitx.generate.funName"
		private const val OPTION_LANGUAGE = "ktorfitx.language"
	}
	
	override fun apply(target: Project) = with(target) {
		if (!pluginManager.hasPlugin("com.google.devtools.ksp")) {
			error("Please add the \"com.google.devtools.ksp\" Gradle Plugin.")
		}
		val extension = extensions.create("ktorfitx", KtorfitxServerExtension::class.java)
		dependencies {
			ksp("server-ksp", extension.mode)
		}
		afterEvaluate {
			this.extensions.configure(KspExtension::class) {
				this.arg(OPTION_GENERATE_PACKAGE_NAME, extension.generate.packageName.getOrElse("$group.generated"))
				this.arg(OPTION_GENERATE_FILE_NAME, extension.generate.fileName.get().removeSuffix(".kt"))
				this.arg(OPTION_GENERATE_FUN_NAME, extension.generate.funName.get())
				this.arg(OPTION_LANGUAGE, extension.language.get().name)
				this.arg(OPTION_SERVER_GRADLE_PLUGIN_ENABLED, true.toString())
			}
			val mode = extension.mode.get()
			val authEnabled = extension.auth.enabled.get()
			val websocketsEnabled = extension.websockets.enabled.get()
			dependencies {
				implementation("server-core", mode)
				implementation("server-annotation", mode)
				if (authEnabled) {
					implementation("server-auth", mode)
				}
				if (websocketsEnabled) {
					implementation("server-websockets", mode)
				}
			}
		}
	}
	
	private operator fun <T : Any> KspExtension.set(key: String, value: T) {
		this.arg(key, value.toString())
	}
	
	private fun DependencyHandlerScope.implementation(path: String, mode: KtorfitxServerMode): Dependency? {
		return when (mode) {
			DEVELOPMENT -> this.add("implementation", project(":$path"))
			RELEASE -> this.add("implementation", "$GROUP_NAME:$path:$VERSION")
		}
	}
	
	private fun DependencyHandlerScope.ksp(path: String, mode: Property<KtorfitxServerMode>) {
		this.addProvider("ksp", mode.map {
			when (it) {
				DEVELOPMENT -> project(":$path")
				RELEASE -> "$GROUP_NAME:$path:$VERSION"
			}
		})
	}
}
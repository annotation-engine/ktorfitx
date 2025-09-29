package cn.ktorfitx.server.gradle.plugin

import cn.ktorfitx.server.gradle.plugin.KtorfitxServerMode.DEVELOPMENT
import cn.ktorfitx.server.gradle.plugin.KtorfitxServerMode.RELEASE
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
		
		private const val VERSION = "3.3.0-3.2.0-Beta1"
		private const val KTOR_VERSION = "3.3.0"
		private const val KSP_VERSION = "2.2.20-2.0.3"
		
		private const val GROUP_NAME = "cn.ktorfitx"
		
		private const val OPTION_IS_SERVER = "ktorfitx.isServer"
		private const val OPTION_GENERATE_PACKAGE_NAME = "ktorfitx.generate.packageName"
		private const val OPTION_GENERATE_FILE_NAME = "ktorfitx.generate.fileName"
		private const val OPTION_GENERATE_FUN_NAME = "ktorfitx.generate.funName"
		private const val OPTION_LANGUAGE = "ktorfitx.language"
	}
	
	override fun apply(target: Project) = with(target) {
		val extension = extensions.create("ktorfitx", KtorfitxServerExtension::class.java)
		dependencies {
			ksp("server-ksp", extension.mode)
		}
		afterEvaluate {
			val language = extension.language.get()
			languageLocal.set(language)
			
			if (!pluginManager.hasPlugin("com.google.devtools.ksp")) {
				error(MISSING_GRADLE_PLUGIN("com.google.devtools.ksp"))
			}
			
			val hasKtorServerCore = configurations.any {
				val dependency = it.dependencies.find { it.group == "io.ktor" && it.name.startsWith("ktor-server-core") }
				if (dependency != null) {
					if (dependency.version != KTOR_VERSION) {
						VERSION_NOT_MATCH("${dependency.group}:${dependency.name}", dependency.version, KTOR_VERSION)
					}
					true
				} else false
			}
			if (!hasKtorServerCore) {
				error(MISSING_DEPENDENCIES("io.ktor:ktor-server-core", KTOR_VERSION))
			}
			
			extensions.getByType<KspExtension>().apply {
				this[OPTION_GENERATE_PACKAGE_NAME] = extension.generate.packageName.getOrElse("$group.generated")
				this[OPTION_GENERATE_FILE_NAME] = extension.generate.fileName.get().removeSuffix(".kt")
				this[OPTION_GENERATE_FUN_NAME] = extension.generate.funName.get()
				this[OPTION_LANGUAGE] = language.name
				this[OPTION_IS_SERVER] = true
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
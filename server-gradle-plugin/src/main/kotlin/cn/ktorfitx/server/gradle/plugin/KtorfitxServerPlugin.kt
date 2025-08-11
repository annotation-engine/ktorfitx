package cn.ktorfitx.server.gradle.plugin

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.Dependency
import org.gradle.api.artifacts.ProjectDependency
import org.gradle.kotlin.dsl.DependencyHandlerScope
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.project

@Suppress("unused")
class KtorfitxServerPlugin : Plugin<Project> {
	
	private companion object {
		private const val VERSION = "3.2.3-3.1.0-Beta1"
	}
	
	override fun apply(target: Project) {
		val extension = target.extensions.create("ktorfitx", KtorfitxServerExtension::class.java)
		target.pluginManager.apply("com.google.devtools.ksp")
		target.afterEvaluate {
			dependencies {
				when (extension.mode.get()) {
					KtorfitxServerMode.RELEASE -> {
						implementation("cn.ktorfitx", "server-core")
						implementation("cn.ktorfitx", "server-annotation")
						if (extension.auth.enabled.get()) {
							implementation("cn.ktorfitx", "server-auth")
						}
						if (extension.websockets.enabled.get()) {
							implementation("cn.ktorfitx", "server-websockets")
						}
						ksp("cn.ktorfitx", "server-ksp")
					}
					
					KtorfitxServerMode.DEVELOPMENT -> {
						implementation(project(":server-core"))
						implementation(project(":server-annotation"))
						if (extension.auth.enabled.get()) {
							implementation(project(":server-auth"))
						}
						if (extension.websockets.enabled.get()) {
							implementation(project(":server-websockets"))
						}
						ksp(project(":server-ksp"))
					}
				}
			}
		}
		target.dependencies {
			add("ksp", project(":server-ksp"))
		}
	}
	
	private fun DependencyHandlerScope.implementation(group: String, name: String): Dependency? =
		add("implementation", "$group:$name:$VERSION")
	
	private fun DependencyHandlerScope.implementation(project: ProjectDependency): Dependency? =
		add("implementation", project)
	
	private fun DependencyHandlerScope.ksp(group: String, name: String): Dependency? =
		add("ksp", "$group:$name:$VERSION")
	
	private fun DependencyHandlerScope.ksp(project: ProjectDependency): Dependency? =
		add("ksp", project)
}
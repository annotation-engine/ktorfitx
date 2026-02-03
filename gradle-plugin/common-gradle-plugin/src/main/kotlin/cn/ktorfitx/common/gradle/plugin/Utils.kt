package cn.ktorfitx.common.gradle.plugin

import com.google.devtools.ksp.gradle.KspExtension
import org.gradle.api.Project
import org.gradle.api.artifacts.Dependency
import org.gradle.api.artifacts.dsl.DependencyHandler
import org.gradle.api.plugins.PluginManager
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.kotlin.dsl.DependencyHandlerScope
import org.gradle.kotlin.dsl.project

operator fun KspExtension.set(key: String, value: String) {
    this.arg(key, value)
}

operator fun MapProperty<String, String>.set(key: String, value: String) {
    this.put(key, value)
}

fun DependencyHandlerScope.implementation(path: String, isDevelopmentMode: Property<Boolean>): Dependency? {
    return if (isDevelopmentMode.get()) {
        this.add("implementation", project(":$path"))
    } else {
        this.add("implementation", "${KtorfitxConstrants.GROUP_NAME}:$path:${KtorfitxVersions.KTORFITX}")
    }
}

fun DependencyHandlerScope.ksp(path: String, isDevelopmentMode: Property<Boolean>) {
    this.addProvider("ksp", isDevelopmentMode.map {
        if (it) project(":$path") else "${KtorfitxConstrants.GROUP_NAME}:$path:${KtorfitxVersions.KTORFITX}"
    })
}

fun DependencyHandler.add(configurationName: String, path: String, isDevelopmentMode: Property<Boolean>): Dependency? {
    return if (isDevelopmentMode.get()) {
        this.add(configurationName, project(":$path"))
    } else {
        this.add(configurationName, "${KtorfitxConstrants.GROUP_NAME}:$path:${KtorfitxVersions.KTORFITX}")
    }
}

fun Project.checkDependency(group: String, name: String) {
    val contains = this.configurations.any { configuration ->
        val dependency = configuration.dependencies.find { it.group == group && it.name.startsWith(name) }
        if (dependency != null) {
            if (dependency.version != KtorfitxVersions.KTOR) {
                error(
                    VERSION_NOT_MATCH(
                        "${dependency.group}:${dependency.name}",
                        dependency.version,
                        KtorfitxVersions.KTOR
                    )
                )
            }
            true
        } else false
    }
    if (!contains) {
        error(MISSING_DEPENDENCIES(group, name, KtorfitxVersions.KTOR))
    }
}

fun PluginManager.checkPlugin(id: String) {
    if (!this.hasPlugin(id)) {
        error(MISSING_GRADLE_PLUGIN(id))
    }
}
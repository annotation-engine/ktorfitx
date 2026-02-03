package cn.ktorfitx.android.gradle.plugin

import cn.ktorfitx.common.gradle.plugin.*
import com.google.devtools.ksp.gradle.KspExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getByType

@Suppress("unused")
class KtorfitxAndroidPlugin : Plugin<Project> {

    private companion object {

        private const val TYPE_ANDROID = "ANDROID"

        private const val OPTION_TYPE = "ktorfitx.type"
        private const val OPTION_LANGUAGE = "ktorfitx.language"
        private const val OPTION_PROJECT_PATH = "ktorfitx.project.path"
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

            pluginManager.checkPlugin("com.android.application")
            pluginManager.checkPlugin("com.google.devtools.ksp")

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
}
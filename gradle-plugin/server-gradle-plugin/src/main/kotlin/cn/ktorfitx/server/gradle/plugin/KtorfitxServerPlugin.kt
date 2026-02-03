package cn.ktorfitx.server.gradle.plugin

import cn.ktorfitx.common.gradle.plugin.*
import com.google.devtools.ksp.gradle.KspExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getByType

@Suppress("unused")
class KtorfitxServerPlugin : Plugin<Project> {

    private companion object {

        private const val TYPE_KTOR_SERVER = "KTOR_SERVER"

        private const val OPTION_TYPE = "ktorfitx.type"
        private const val OPTION_GENERATE_PACKAGE_NAME = "ktorfitx.generate.packageName"
        private const val OPTION_GENERATE_FILE_NAME = "ktorfitx.generate.fileName"
        private const val OPTION_GENERATE_FUN_NAME = "ktorfitx.generate.funName"
        private const val OPTION_LANGUAGE = "ktorfitx.language"
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

            pluginManager.checkPlugin("com.google.devtools.ksp")

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
}
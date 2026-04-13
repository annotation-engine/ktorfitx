import cn.ktorfitx.build.gradle.supportPlatforms
import cn.ktorfitx.common.gradle.plugin.KtorfitxLanguage
import com.google.devtools.ksp.gradle.KspAATask
import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion
import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpackConfig

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.kotlin.multiplatform.library)
    alias(libs.plugins.jetbrains.compose)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    id("cn.ktorfitx.multiplatform")
}

val ktorfitxSampleVersion = property("ktorfitx.sample.version").toString()

kotlin {
    jvmToolchain(21)

    supportPlatforms(
        android = {
            androidLibrary {
                namespace = "cn.ktorfitx.multiplatform.sample"
                compileSdk {
                    version = release(libs.versions.android.compileSdk.get().toInt()) {
                        minorApiLevel = libs.versions.android.compileSdkMinor.get().toInt()
                    }
                }

                androidResources {
                    enable = true
                }
            }
        },
        desktop = {
            jvm("desktop") {
                compilerOptions {
                    jvmTarget = JvmTarget.JVM_21
                }
            }
        },
        ios = {
            listOf(
                iosX64(),
                iosArm64(),
                iosSimulatorArm64()
            ).forEach { target ->
                target.binaries.framework {
                    baseName = "SampleApp"
                    isStatic = true
                }
            }
        },
        js = {
            js(IR) {
                outputModuleName = "sampleApp"
                browser {
                    commonWebpackConfig {
                        outputFileName = "sampleApp.js"
                    }
                }
                useEsModules()
                binaries.executable()
            }
        },
        wasmJs = {
            @OptIn(ExperimentalWasmDsl::class)
            wasmJs {
                outputModuleName = "sampleApp"
                browser {
                    val rootDirPath = project.rootDir.path
                    val projectDirPath = project.projectDir.path
                    commonWebpackConfig {
                        outputFileName = "sampleApp.js"
                        devServer = (devServer ?: KotlinWebpackConfig.DevServer()).apply {
                            static(rootDirPath)
                            static(projectDirPath)
                        }
                    }
                }
                useEsModules()
                binaries.executable()
            }
        }
    )

    compilerOptions {
        languageVersion = KotlinVersion.KOTLIN_2_3
        apiVersion = KotlinVersion.KOTLIN_2_3
    }

    sourceSets {
        commonMain.dependencies {
            implementation(libs.bundles.multiplatform.sample)
        }

        supportPlatforms(
            android = {
                androidMain.dependencies {
                    implementation(libs.androidx.activity.compose)
                }
            },
            desktop = {
                val desktopMain by getting
                desktopMain.dependencies {
                    implementation(compose.desktop.currentOs)
                }
            }
        )
    }
}

tasks.withType<KspAATask>().configureEach {
    group = "ksp"
}

compose.resources {
    packageOfResClass = "cn.ktorfitx.multiplatform.sample.generated.resources"
    publicResClass = false
}

compose.desktop {
    application {
        mainClass = "MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb, TargetFormat.Exe)
            packageName = "cn.ktorfitx.multiplatform.sample"
            packageVersion = ktorfitxSampleVersion
        }
    }
}

ktorfitx {
    isDevelopmentMode = true
    language = KtorfitxLanguage.CHINESE
    websockets {
        enabled = true
    }
    mock {
        enabled = true
    }
}
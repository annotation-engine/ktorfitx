import cn.ktorfitx.build.gradle.supportPlatforms
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.kotlin.multiplatform.library)
    alias(libs.plugins.maven.publish)
}

val ktorfitxVersion = property("ktorfitx.version").toString()
val ktorfitxAutomaticRelease = property("ktorfitx.automaticRelease").toString().toBoolean()

group = "cn.ktorfitx.multiplatform.websockets"
version = ktorfitxVersion

kotlin {
    jvmToolchain(21)

    supportPlatforms(
        android = {
            androidLibrary {
                namespace = "cn.ktorfitx.multiplatform.websockets"
                compileSdk {
                    version = release(libs.versions.android.compileSdk.get().toInt()) {
                        minorApiLevel = libs.versions.android.compileSdkMinor.get().toInt()
                    }
                }
            }
        },
        desktop = {
            jvm("desktop")
        },
        ios = {
            iosX64()
            iosArm64()
            iosSimulatorArm64()
        },
        macos = {
            macosArm64()
        },
        watchos = {
            watchosArm32()
            watchosArm64()
            watchosSimulatorArm64()
            watchosDeviceArm64()
        },
        tvos = {
            tvosArm64()
            tvosSimulatorArm64()
        },
        linux = {
            linuxX64()
            linuxArm64()
        },
        mingw = {
            mingwX64()
        },
        js = {
            js(IR) {
                browser()
            }
        },
        wasmJs = {
            @Suppress("OPT_IN_USAGE")
            wasmJs {
                browser()
            }
        }
    )

    compilerOptions {
        languageVersion = KotlinVersion.KOTLIN_2_3
        apiVersion = KotlinVersion.KOTLIN_2_3
    }

    sourceSets {
        commonMain {
            dependencies {
                implementation(libs.bundles.multiplatform.websockets)
            }
        }
    }
}

mavenPublishing {
    publishToMavenCentral(automaticRelease = ktorfitxAutomaticRelease)
    signAllPublications()

    coordinates("cn.ktorfitx", "multiplatform-websockets", ktorfitxVersion)

    pom {
        name.set("multiplatform-websockets")
        description.set("Ktorfitx 是一款专为 Ktor 设计的代码生成框架，致力于减少样板代码，为 Ktor Client 和 Ktor Server 提供代码生成服务，支持 Kotlin Multiplatform")
        inceptionYear.set("2025")
        url.set("https://github.com/annotation-engine/ktorfitx")
        licenses {
            license {
                name.set("The Apache License, Version 2.0")
                url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                distribution.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
            }
        }
        developers {
            developer {
                id.set("JarvisLi")
                name.set("JarvisLi")
                url.set("https://github.com/annotation-engine/ktorfitx")
            }
        }

        scm {
            url.set("https://github.com/annotation-engine/ktorfitx")
            connection.set("scm:git:git://github.com/annotation-engine/ktorfitx.git")
            developerConnection.set("scm:git:ssh://git@github.com:annotation-engine/ktorfitx.git")
        }
    }
}
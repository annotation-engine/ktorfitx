import cn.ktorfitx.build.gradle.Platform
import cn.ktorfitx.build.gradle.configurePlatformFeatures
import cn.ktorfitx.build.gradle.toPlatforms
import cn.ktorfitx.multiplatform.gradle.plugin.KtorfitxLanguage
import com.android.build.api.dsl.androidLibrary
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
val ktorfitxPlatforms = property("ktorfitx.platforms").toString().toPlatforms()

kotlin {
	jvmToolchain(21)
	
	configurePlatformFeatures(ktorfitxPlatforms) {
		if (androidEnabled) {
			@Suppress("UnstableApiUsage")
			androidLibrary {
				namespace = "cn.ktorfitx.multiplatform.sample"
				compileSdk = libs.versions.android.compileSdk.get().toInt()
				
				androidResources {
					enable = true
				}
			}
		}
		if (desktopEnabled) {
			jvm("desktop") {
				compilerOptions {
					jvmTarget = JvmTarget.JVM_21
				}
			}
		}
		if (iosEnabled) {
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
		}
		if (jsEnabled) {
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
		}
		if (wasmJsEnabled) {
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
	}
	
	compilerOptions {
		languageVersion = KotlinVersion.KOTLIN_2_3
		apiVersion = KotlinVersion.KOTLIN_2_3
	}
	
	sourceSets {
		commonMain.dependencies {
			implementation(libs.bundles.multiplatform.sample)
		}
		
		if (Platform.ANDROID in ktorfitxPlatforms) {
			androidMain.dependencies {
				implementation(libs.androidx.activity.compose)
			}
		}
		
		if (Platform.DESKTOP in ktorfitxPlatforms) {
			val desktopMain by getting
			desktopMain.dependencies {
				implementation(compose.desktop.currentOs)
			}
		}
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
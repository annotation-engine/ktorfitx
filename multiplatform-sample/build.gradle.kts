import cn.ktorfitx.build.gradle.Platform
import cn.ktorfitx.build.gradle.configurePlatformFeatures
import cn.ktorfitx.build.gradle.toPlatforms
import cn.ktorfitx.multiplatform.gradle.plugin.KtorfitxLanguage
import com.google.devtools.ksp.gradle.KspAATask
import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion
import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpackConfig

plugins {
	alias(libs.plugins.kotlin.multiplatform)
	alias(libs.plugins.android.application)
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
			androidTarget {
				compilerOptions {
					jvmTarget = JvmTarget.JVM_21
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
		languageVersion = KotlinVersion.KOTLIN_2_2
		apiVersion = KotlinVersion.KOTLIN_2_2
	}
	
	sourceSets {
		commonMain.dependencies {
			implementation(libs.bundles.multiplatform.sample)
			implementation(compose.runtime)
			implementation(compose.foundation)
			implementation(compose.material3)
			implementation(compose.ui)
			implementation(compose.components.resources)
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

android {
	namespace = "cn.ktorfitx.multiplatform.sample"
	compileSdk = libs.versions.android.compileSdk.get().toInt()
	buildToolsVersion = "36.1.0"
	compileSdkVersion = "android-36.1"
	
	sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
	sourceSets["main"].res.srcDirs("src/androidMain/res")
	sourceSets["main"].resources.srcDirs("src/commonMain/resources")
	
	defaultConfig {
		applicationId = "cn.ktorfitx.multiplatform.sample"
		minSdk = libs.versions.android.minSdk.get().toInt()
		targetSdk = libs.versions.android.targetSdk.get().toInt()
		versionCode = 2
		versionName = ktorfitxSampleVersion
	}
	packaging {
		resources {
			excludes += "/META-INF/{AL2.0,LGPL2.1}"
		}
	}
	buildTypes {
		release {
			isMinifyEnabled = false
		}
	}
	compileOptions {
		sourceCompatibility = JavaVersion.VERSION_21
		targetCompatibility = JavaVersion.VERSION_21
	}
	buildFeatures {
		compose = true
	}
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
import cn.ktorfitx.build.gradle.configurePlatformFeatures
import cn.ktorfitx.build.gradle.toPlatforms
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion

plugins {
	alias(libs.plugins.kotlin.multiplatform)
	alias(libs.plugins.android.library)
	alias(libs.plugins.maven.publish)
}

val ktorfitxVersion = property("ktorfitx.version").toString()
val ktorfitxAutomaticRelease = property("ktorfitx.automaticRelease").toString().toBoolean()
val ktorfitxPlatforms = property("ktorfitx.platforms").toString().toPlatforms()

group = "cn.ktorfitx.multiplatform.mock"
version = ktorfitxVersion

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
					baseName = "KtorfitxMock"
					isStatic = true
				}
			}
		}
		if (macosEnabled) {
			listOf(
				macosX64(),
				macosArm64()
			).forEach { target ->
				target.binaries.framework {
					baseName = "KtorfitxMock"
					isStatic = true
				}
			}
		}
		if (watchosEnabled) {
			listOf(
				watchosX64(),
				watchosArm32(),
				watchosArm64(),
				watchosSimulatorArm64(),
				watchosDeviceArm64()
			).forEach { target ->
				target.binaries.framework {
					baseName = "KtorfitxMock"
					isStatic = true
				}
			}
		}
		if (tvosEnabled) {
			listOf(
				tvosX64(),
				tvosArm64(),
				tvosSimulatorArm64()
			).forEach { target ->
				target.binaries.framework {
					baseName = "KtorfitxMock"
					isStatic = true
				}
			}
		}
		if (linuxEnabled) {
			listOf(
				linuxX64(),
				linuxArm64()
			).forEach { target ->
				target.binaries.executable()
			}
		}
		if (mingwEnabled) {
			mingwX64().binaries.executable()
		}
		if (jsEnabled) {
			js(IR) {
				outputModuleName = "ktorfitxMock"
				browser()
				useEsModules()
				binaries.executable()
			}
		}
		if (wasmJsEnabled) {
			@OptIn(ExperimentalWasmDsl::class)
			wasmJs {
				outputModuleName = "ktorfitxMock"
				browser()
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
		commonMain {
			dependencies {
				implementation(projects.multiplatformAnnotation)
				implementation(projects.multiplatformCore)
				implementation(libs.bundles.multiplatform.mock)
			}
		}
	}
}

android {
	namespace = "cn.ktorfitx.multiplatform.mock"
	compileSdk = libs.versions.android.compileSdk.get().toInt()
	buildToolsVersion = "36.1.0"
	compileSdkVersion = "android-36.1"
	
	sourceSets["main"].apply {
		manifest.srcFile("src/androidMain/AndroidManifest.xml")
		res.srcDirs("src/androidMain/res")
		resources.srcDirs("src/commonMain/resources")
	}
	
	defaultConfig {
		minSdk = libs.versions.android.minSdk.get().toInt()
	}
	packaging {
		resources {
			excludes += "/META-INF/{AL2.0,LGPL2.1}"
			merges += "/META-INF/DEPENDENCIES"
		}
	}
	buildTypes {
		getByName("release") {
			isMinifyEnabled = false
		}
	}
	compileOptions {
		sourceCompatibility = JavaVersion.VERSION_21
		targetCompatibility = JavaVersion.VERSION_21
	}
}

mavenPublishing {
	publishToMavenCentral(automaticRelease = ktorfitxAutomaticRelease)
	signAllPublications()
	
	coordinates("cn.ktorfitx", "multiplatform-mock", ktorfitxVersion)
	
	pom {
		name.set("multiplatform-mock")
		description.set("Ktorfitx 基于 KSP2 的代码生成框架，在 Kotlin Multiplatform 中是 RESTful API 框架，在 Ktor Server 中是 路由以及参数解析框架")
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
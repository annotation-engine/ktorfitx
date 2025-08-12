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
val ktorfitxPlatforms = property("ktorfitx.platforms").toString().split(",")

group = "cn.ktorfitx.multiplatform.websockets"
version = ktorfitxVersion

kotlin {
	jvmToolchain(21)

	if ("android" in ktorfitxPlatforms) {
		androidTarget {
			compilerOptions {
				jvmTarget = JvmTarget.JVM_21
			}
		}
	}

	if ("desktop" in ktorfitxPlatforms) {
		jvm("desktop") {
			compilerOptions {
				jvmTarget = JvmTarget.JVM_21
			}
		}
	}

	if ("ios" in ktorfitxPlatforms) {
		listOf(
			iosX64(),
			iosArm64(),
			iosSimulatorArm64()
		).forEach { target ->
			target.binaries.framework {
				baseName = "KtorfitxWebSockets"
				isStatic = true
			}
		}
	}

	if ("macos" in ktorfitxPlatforms) {
		listOf(
			macosX64(),
			macosArm64(),
		).forEach { target ->
			target.binaries.framework {
				baseName = "KtorfitxWebSockets"
				isStatic = true
			}
		}
	}

	if ("watchos" in ktorfitxPlatforms) {
		listOf(
			watchosX64(),
			watchosArm32(),
			watchosArm64(),
			watchosSimulatorArm64(),
			watchosDeviceArm64(),
		).forEach { target ->
			target.binaries.framework {
				baseName = "KtorfitxWebSockets"
				isStatic = true
			}
		}
	}

	if ("tvos" in ktorfitxPlatforms) {
		listOf(
			tvosX64(),
			tvosArm64(),
			tvosSimulatorArm64()
		).forEach { target ->
			target.binaries.framework {
				baseName = "KtorfitxWebSockets"
				isStatic = true
			}
		}
	}

	if ("linux" in ktorfitxPlatforms) {
		listOf(
			linuxArm64(),
			linuxX64()
		).forEach { target ->
			target.binaries.executable()
		}
	}

	if ("mingw" in ktorfitxPlatforms) {
		mingwX64().binaries.executable()
	}

	if ("js" in ktorfitxPlatforms) {
		js(IR) {
			outputModuleName = "ktorfitxWebSockets"
			nodejs()
			binaries.executable()
		}
	}

	if ("wasmJs" in ktorfitxPlatforms) {
		@OptIn(ExperimentalWasmDsl::class)
		wasmJs {
			outputModuleName = "ktorfitxWebSockets"
			nodejs()
			binaries.executable()
		}
	}
	
	compilerOptions {
		languageVersion = KotlinVersion.KOTLIN_2_2
		apiVersion = KotlinVersion.KOTLIN_2_2
	}
	
	sourceSets {
		commonMain {
			dependencies {
				implementation(libs.bundles.multiplatform.websockets)
			}
		}
	}
}

android {
	namespace = "cn.ktorfitx.multiplatform.websockets"
	compileSdk = libs.versions.android.compileSdk.get().toInt()

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
	
	coordinates("cn.ktorfitx", "multiplatform-websockets", ktorfitxVersion)
	
	pom {
		name.set("multiplatform-websockets")
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
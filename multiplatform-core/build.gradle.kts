import cn.ktorfitx.build.gradle.configurePlatformFeatures
import cn.ktorfitx.build.gradle.toPlatforms
import com.google.devtools.ksp.gradle.KspAATask
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion

plugins {
	alias(libs.plugins.kotlin.multiplatform)
	alias(libs.plugins.android.library)
	alias(libs.plugins.maven.publish)
	alias(libs.plugins.ksp)
}

val ktorfitxVersion = property("ktorfitx.version").toString()
val ktorfitxAutomaticRelease = property("ktorfitx.automaticRelease").toString().toBoolean()
val ktorfitxPlatforms = property("ktorfitx.platforms").toString().toPlatforms()

group = "cn.ktorfitx.multiplatform.core"
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
		if (androidNativeEnabled) {
			androidNativeX86()
			androidNativeX64()
			androidNativeArm32()
			androidNativeArm64()
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
					baseName = "KtorfitxCore"
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
					baseName = "KtorfitxCore"
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
					baseName = "KtorfitxCore"
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
					baseName = "KtorfitxCore"
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
				outputModuleName = "ktorfitxCore"
				browser()
				useEsModules()
				binaries.executable()
			}
		}
		if (wasmJsEnabled) {
			@OptIn(ExperimentalWasmDsl::class)
			wasmJs {
				outputModuleName = "ktorfitxCore"
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
				implementation(libs.bundles.multiplatform.core)
			}
		}
	}
}

tasks.withType<KspAATask>().configureEach {
	group = "ksp"
}

android {
	namespace = "cn.ktorfitx.multiplatform.core"
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
		release {
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
	
	coordinates("cn.ktorfitx", "multiplatform-core", ktorfitxVersion)
	
	pom {
		name.set("multiplatform-core")
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
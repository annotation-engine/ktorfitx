rootProject.name = "ktorfitx"
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
	repositories {
		google()
		gradlePluginPortal()
		mavenCentral()
		
		maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
		maven("https://maven.aliyun.com/repository/public")
		maven("https://maven.aliyun.com/repository/gradle-plugin")
	}
	includeBuild("android-gradle-plugin")
	includeBuild("multiplatform-gradle-plugin")
	includeBuild("server-gradle-plugin")
}

plugins {
	id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

dependencyResolutionManagement {
	@Suppress("UnstableApiUsage")
	repositories {
		google()
		mavenCentral()
		
		maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
		maven("https://maven.aliyun.com/repository/public")
		maven("https://maven.aliyun.com/repository/gradle-plugin")
	}
}

include("multiplatform-annotation")
include("multiplatform-core")
include("multiplatform-mock")
include("multiplatform-websockets")
include("multiplatform-ksp")
include("multiplatform-sample")

include("android-sample")

include("server-core")
include("server-annotation")
include("server-auth")
include("server-websockets")
include("server-ksp")
include("server-sample")

include("common-ksp-util")
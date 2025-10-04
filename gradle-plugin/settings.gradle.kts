rootProject.name = "gradle-plugin"
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
	repositories {
		google()
		gradlePluginPortal()
		mavenCentral()
		
		maven("https://maven.aliyun.com/repository/public")
		maven("https://maven.aliyun.com/repository/gradle-plugin")
	}
}

plugins {
	id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

dependencyResolutionManagement {
	@Suppress("UnstableApiUsage")
	repositories {
		google()
		mavenCentral()
		
		maven("https://maven.aliyun.com/repository/public")
		maven("https://maven.aliyun.com/repository/gradle-plugin")
	}
	versionCatalogs {
		create("libs", Action<VersionCatalogBuilder> {
			from(files("../gradle//libs.versions.toml"))
		})
	}
}

include("android-gradle-plugin")
include("multiplatform-gradle-plugin")
include("server-gradle-plugin")
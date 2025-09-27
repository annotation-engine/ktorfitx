rootProject.name = "multiplatform-gradle-plugin"
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

dependencyResolutionManagement {
	@Suppress("UnstableApiUsage")
	repositories {
		google()
		mavenCentral()
		
		maven("https://maven.aliyun.com/repository/public")
		maven("https://maven.aliyun.com/repository/gradle-plugin")
	}
}
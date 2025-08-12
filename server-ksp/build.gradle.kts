import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion

plugins {
	alias(libs.plugins.kotlin.jvm)
	alias(libs.plugins.maven.publish)
}

val ktorfitxVersion = property("ktorfitx.version").toString()
val ktorfitxAutomaticRelease = property("ktorfitx.automaticRelease").toString().toBoolean()

group = "cn.ktorfitx.server.ksp"
version = ktorfitxVersion

java {
	sourceCompatibility = JavaVersion.VERSION_21
	targetCompatibility = JavaVersion.VERSION_21
}

kotlin {
	jvmToolchain(21)
	
	compilerOptions {
		jvmTarget = JvmTarget.JVM_21
		apiVersion = KotlinVersion.KOTLIN_2_2
		languageVersion = KotlinVersion.KOTLIN_2_2
	}
}

sourceSets {
	main {
		kotlin.srcDirs("build/generated/ksp/main/kotlin")
		java.srcDirs("build/generated/ksp/main/kotlin")
	}
}

dependencies {
	implementation(projects.commonKspUtil)
	implementation(libs.bundles.server.ksp)
}

mavenPublishing {
	publishToMavenCentral(automaticRelease = ktorfitxAutomaticRelease)
	signAllPublications()
	
	coordinates("cn.ktorfitx", "server-ksp", ktorfitxVersion)
	
	pom {
		name.set("server-ksp")
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
				id.set("li-jia-wei")
				name.set("li-jia-wei")
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
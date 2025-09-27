plugins {
	`java-gradle-plugin`
	`kotlin-dsl`
	alias(libs.plugins.maven.publish)
}

val ktorfitxVersion = property("ktorfitx.version").toString()
val ktorfitxAutomaticRelease = property("ktorfitx.automaticRelease").toString().toBoolean()

group = "cn.ktorfitx.multiplatform.gradle.plugin"
version = ktorfitxVersion

gradlePlugin {
	plugins {
		create("KtorfitxMultiplatformGradlePlugin", Action<PluginDeclaration> {
			id = "cn.ktorfitx.multiplatform"
			displayName = "Ktorfitx Multiplatform Gradle Plugin"
			implementationClass = "cn.ktorfitx.multiplatform.gradle.plugin.KtorfitxMultiplatformPlugin"
		})
	}
}

dependencies {
	implementation(libs.bundles.gradle.plugin)
}

mavenPublishing {
	publishToMavenCentral(automaticRelease = ktorfitxAutomaticRelease)
	signAllPublications()
	
	coordinates("cn.ktorfitx", "multiplatform-gradle-plugin", ktorfitxVersion)
	
	pom {
		name.set("multiplatform-gradle-plugin")
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
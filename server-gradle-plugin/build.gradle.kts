plugins {
	`java-gradle-plugin`
	`kotlin-dsl`
	alias(libs.plugins.maven.publish)
}

val ktorfitxVersion = property("ktorfitx.version").toString()
val ktorfitxAutomaticRelease = property("ktorfitx.automaticRelease").toString().toBoolean()

group = "cn.ktorfitx.server.gradle.plugin"
version = ktorfitxVersion

gradlePlugin {
	plugins {
		create("KtorfitxServerGradlePlugin", Action<PluginDeclaration> {
			id = "cn.ktorfitx.server"
			displayName = "Ktorfitx Server Gradle Plugin"
			implementationClass = "cn.ktorfitx.server.gradle.plugin.KtorfitxServerPlugin"
		})
	}
}

dependencies {
	implementation(libs.ksp)
}

mavenPublishing {
	publishToMavenCentral(automaticRelease = ktorfitxAutomaticRelease)
	signAllPublications()
	
	coordinates("cn.ktorfitx", "server-gradle-plugin", ktorfitxVersion)
	
	pom {
		name.set("server-gradle-plugin")
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
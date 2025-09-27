import cn.ktorfitx.android.gradle.plugin.KtorfitxAndroidMode
import cn.ktorfitx.android.gradle.plugin.KtorfitxLanguage

plugins {
	alias(libs.plugins.android.application)
	alias(libs.plugins.kotlin.android)
	alias(libs.plugins.compose.compiler)
	alias(libs.plugins.ksp)
	id("cn.ktorfitx.android")
}

val ktorfitxSampleVersion = property("ktorfitx.sample.version").toString()

android {
	namespace = "cn.ktorfitx.android.sample"
	compileSdk = libs.versions.android.compileSdk.get().toInt()
	buildToolsVersion = "36.1.0"
	compileSdkVersion = "android-36.1"
	
	defaultConfig {
		applicationId = "cn.ktorfitx.android.sample"
		minSdk = libs.versions.android.minSdk.get().toInt()
		targetSdk = libs.versions.android.targetSdk.get().toInt()
		versionCode = 2
		versionName = ktorfitxSampleVersion
		
		testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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

dependencies {
	implementation(libs.bundles.android.sample)
}

ktorfitx {
	mode = KtorfitxAndroidMode.DEVELOPMENT
	language = KtorfitxLanguage.CHINESE
	websockets {
		enabled = true
	}
	mock {
		enabled = true
	}
}
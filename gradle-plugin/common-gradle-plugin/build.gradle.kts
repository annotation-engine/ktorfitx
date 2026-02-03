plugins {
    `kotlin-dsl`
    `java-gradle-plugin`
}

val ktorfitxVersion = property("ktorfitx.version").toString()
group = "cn.ktorfitx.common.gradle.plugin"
version = ktorfitxVersion

kotlin {
    jvmToolchain(21)
}

dependencies {
    implementation(libs.bundles.common.gradle.plugin)
}
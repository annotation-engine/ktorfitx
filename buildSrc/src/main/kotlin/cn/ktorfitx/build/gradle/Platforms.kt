package cn.ktorfitx.build.gradle

import org.gradle.api.Project

fun Project.supportPlatforms(
    android: (() -> Unit)? = null,
    ios: (() -> Unit)? = null,
    desktop: (() -> Unit)? = null,
    macos: (() -> Unit)? = null,
    watchos: (() -> Unit)? = null,
    tvos: (() -> Unit)? = null,
    linux: (() -> Unit)? = null,
    mingw: (() -> Unit)? = null,
    js: (() -> Unit)? = null,
    wasmJs: (() -> Unit)? = null
) {
    val platforms = property("ktorfitx.platforms").toString().toPlatforms()
    if (Platform.ANDROID in platforms) android?.invoke()
    if (Platform.IOS in platforms) ios?.invoke()
    if (Platform.DESKTOP in platforms) desktop?.invoke()
    if (Platform.MACOS in platforms) macos?.invoke()
    if (Platform.WATCHOS in platforms) watchos?.invoke()
    if (Platform.TVOS in platforms) tvos?.invoke()
    if (Platform.LINUX in platforms) linux?.invoke()
    if (Platform.MINGW in platforms) mingw?.invoke()
    if (Platform.JS in platforms) js?.invoke()
    if (Platform.WASM_JS in platforms) wasmJs?.invoke()
}

private fun String.toPlatforms(): List<Platform> {
    return this.split(",")
        .filter { it.isNotBlank() }
        .map { Platform.valueOf(it.toConstantCase()) }
}

private fun String.toConstantCase(): String =
    this.fold(StringBuilder()) { acc, c ->
        if (c.isUpperCase()) acc.append("_")
        acc.append(c.uppercaseChar())
    }.toString()

private enum class Platform {
    ANDROID,
    IOS,
    DESKTOP,
    MACOS,
    WATCHOS,
    TVOS,
    LINUX,
    MINGW,
    JS,
    WASM_JS
}
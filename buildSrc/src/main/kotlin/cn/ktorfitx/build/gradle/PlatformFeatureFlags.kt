package cn.ktorfitx.build.gradle

fun configurePlatformFeatures(
	platforms: List<Platform>,
	targets: PlatformFeatureFlags.() -> Unit
) = PlatformFeatureFlags(platforms).apply(targets)

fun String.toPlatforms(): List<Platform> {
	return this.split(",")
		.filter { it.isNotBlank() }
		.map { Platform.valueOf(it.toConstantCase()) }
}

private fun String.toConstantCase(): String =
	this.fold(StringBuilder()) { acc, c ->
		if (c.isUpperCase()) acc.append("_")
		acc.append(c.uppercaseChar())
	}.toString()

enum class Platform {
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

class PlatformFeatureFlags internal constructor(
	private val platforms: List<Platform>
) {
	val androidEnabled get() = Platform.ANDROID in platforms
	val iosEnabled get() = Platform.IOS in platforms
	val desktopEnabled get() = Platform.DESKTOP in platforms
	val macosEnabled get() = Platform.MACOS in platforms
	val watchosEnabled get() = Platform.WATCHOS in platforms
	val tvosEnabled get() = Platform.TVOS in platforms
	val linuxEnabled get() = Platform.LINUX in platforms
	val mingwEnabled get() = Platform.MINGW in platforms
	val jsEnabled get() = Platform.JS in platforms
	val wasmJsEnabled get() = Platform.WASM_JS in platforms
}
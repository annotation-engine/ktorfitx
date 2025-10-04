package cn.ktorfitx.server.gradle.plugin

import org.gradle.api.model.ObjectFactory
import org.gradle.kotlin.dsl.newInstance
import org.gradle.kotlin.dsl.property
import javax.inject.Inject

open class KtorfitxServerExtension @Inject constructor(
	objects: ObjectFactory
) {
	
	val isDevelopmentMode = objects.property<Boolean>().convention(false)
	
	val websockets = objects.newInstance<WebsocketsConfig>()
	
	val auth = objects.newInstance<MockConfig>()
	
	val language = objects.property<KtorfitxLanguage>().convention(KtorfitxLanguage.ENGLISH)
	
	val generate = objects.newInstance<GenerateConfig>()
	
	fun websockets(action: WebsocketsConfig.() -> Unit) {
		websockets.action()
	}
	
	fun auth(action: MockConfig.() -> Unit) {
		auth.action()
	}
	
	fun generate(action: GenerateConfig.() -> Unit) {
		generate.action()
	}
}

enum class KtorfitxLanguage {
	CHINESE,
	ENGLISH
}

open class WebsocketsConfig @Inject constructor(objects: ObjectFactory) {
	val enabled = objects.property<Boolean>().convention(false)
}

open class MockConfig @Inject constructor(objects: ObjectFactory) {
	val enabled = objects.property<Boolean>().convention(false)
}

open class GenerateConfig @Inject constructor(objects: ObjectFactory) {
	val packageName = objects.property<String>()
	val fileName = objects.property<String>().convention("GenerateRoutes")
	val funName = objects.property<String>().convention("generateRoutes")
}
package cn.ktorfitx.android.gradle.plugin

import org.gradle.api.model.ObjectFactory
import org.gradle.kotlin.dsl.newInstance
import org.gradle.kotlin.dsl.property
import javax.inject.Inject

open class KtorfitxAndroidExtension @Inject constructor(
	objects: ObjectFactory
) {
	
	val mode = objects.property<KtorfitxAndroidMode>().convention(KtorfitxAndroidMode.RELEASE)
	val websockets = objects.newInstance<WebsocketsConfig>()
	val mock = objects.newInstance<MockConfig>()
	val language = objects.property<KtorfitxLanguage>().convention(KtorfitxLanguage.ENGLISH)
	
	fun websockets(action: WebsocketsConfig.() -> Unit) {
		websockets.action()
	}
	
	fun mock(action: MockConfig.() -> Unit) {
		mock.action()
	}
}

enum class KtorfitxAndroidMode {
	DEVELOPMENT,
	RELEASE
}

open class WebsocketsConfig @Inject constructor(objects: ObjectFactory) {
	val enabled = objects.property<Boolean>().convention(false)
}

open class MockConfig @Inject constructor(objects: ObjectFactory) {
	val enabled = objects.property<Boolean>().convention(false)
}

enum class KtorfitxLanguage {
	CHINESE,
	ENGLISH
}
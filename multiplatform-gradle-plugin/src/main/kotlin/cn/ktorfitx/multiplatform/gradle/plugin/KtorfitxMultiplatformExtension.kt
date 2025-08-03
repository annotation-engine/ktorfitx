package cn.ktorfitx.multiplatform.gradle.plugin

import org.gradle.api.model.ObjectFactory
import org.gradle.kotlin.dsl.newInstance
import org.gradle.kotlin.dsl.property
import javax.inject.Inject

open class KtorfitxMultiplatformExtension @Inject constructor(
	objects: ObjectFactory
) {
	
	val websockets = objects.newInstance<WebsocketsConfig>()
	val mock = objects.newInstance<MockConfig>()
	val ksp = objects.newInstance<KSPConfig>()
	
	fun websockets(action: WebsocketsConfig.() -> Unit) {
		websockets.action()
	}
	
	fun mock(action: MockConfig.() -> Unit) {
		mock.action()
	}
	
	fun ksp(action: KSPConfig.() -> Unit) {
		ksp.action()
	}
}

open class WebsocketsConfig @Inject constructor(objects: ObjectFactory) {
	val enabled = objects.property<Boolean>().convention(false)
}

open class MockConfig @Inject constructor(objects: ObjectFactory) {
	val enabled = objects.property<Boolean>().convention(false)
}

open class KSPConfig @Inject constructor(objects: ObjectFactory) {
	val kspCommonMainGeneratedDir = objects.property<String>().convention(
		"build/generated/ksp/metadata/commonMain/kotlin"
	)
}
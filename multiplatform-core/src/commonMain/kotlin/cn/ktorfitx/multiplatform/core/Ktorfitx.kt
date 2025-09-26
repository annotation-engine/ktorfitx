package cn.ktorfitx.multiplatform.core

import cn.ktorfitx.multiplatform.core.config.KtorfitxConfig
import kotlin.jvm.JvmInline

@JvmInline
value class Ktorfitx internal constructor(
	val config: KtorfitxConfig
)

/**
 * ktorfitx
 */
fun ktorfitx(
	config: KtorfitxConfig.() -> Unit,
): Ktorfitx = KtorfitxConfig()
	.apply(config)
	.build()
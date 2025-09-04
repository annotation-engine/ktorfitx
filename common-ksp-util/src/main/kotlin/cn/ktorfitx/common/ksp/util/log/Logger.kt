package cn.ktorfitx.common.ksp.util.log

import com.google.devtools.ksp.processing.KSPLogger

private val kspLoggerLocal = ThreadLocal<KSPLogger>()

var kspLogger: KSPLogger
	set(value) = kspLoggerLocal.set(value)
	get() = kspLoggerLocal.get()!!
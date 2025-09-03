package cn.ktorfitx.common.ksp.util.resolver

import com.google.devtools.ksp.processing.Resolver

internal val resolverLocal = ThreadLocal<Resolver>()

var safeResolver: Resolver
	set(value) = resolverLocal.set(value)
	get() = resolverLocal.get() ?: error("The resolver is not set!")
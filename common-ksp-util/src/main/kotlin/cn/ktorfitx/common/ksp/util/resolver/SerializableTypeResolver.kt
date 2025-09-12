package cn.ktorfitx.common.ksp.util.resolver

import cn.ktorfitx.common.ksp.util.constants.TypeNames
import cn.ktorfitx.common.ksp.util.expends.hasAnnotation
import com.google.devtools.ksp.getClassDeclarationByName
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.ParameterizedTypeName
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.ksp.toClassName

private val serializableCache = mutableMapOf<String, Boolean>()

fun TypeName.isSerializableType(
	resolver: Resolver = safeResolver
): Boolean {
	val className = when (this) {
		is ClassName -> this
		is ParameterizedTypeName -> this.rawType
		else -> return false
	}
	val canonicalName = className.canonicalName
	serializableCache[canonicalName]?.let { return it }
	
	val declaration = resolver.getClassDeclarationByName(canonicalName) ?: let {
		serializableCache[canonicalName] = false
		return false
	}
	
	val hasSerializable = declaration.hasAnnotation(TypeNames.Serializable)
	if (hasSerializable) {
		serializableCache[canonicalName] = true
		return true
	}
	
	val hasSerializer = declaration.hasKotlinxSerializer()
	if (!hasSerializer) {
		serializableCache[canonicalName] = false
		return false
	}
	if (this !is ParameterizedTypeName) {
		serializableCache[canonicalName] = true
		return true
	}
	
	this.typeArguments.forEach {
		if (!it.isSerializableType(resolver)) {
			return false
		}
	}
	
	serializableCache[canonicalName] = true
	return true
}

private fun KSClassDeclaration.hasKotlinxSerializer(): Boolean {
	val className = this.toClassName()
	if (className in TypeNames.kotlinxSerializerTypeNames) return true
	this.superTypes.forEach {
		val declaration = it.resolve().declaration as? KSClassDeclaration ?: return@forEach
		if (declaration.hasKotlinxSerializer()) return true
	}
	return false
}
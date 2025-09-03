package cn.ktorfitx.common.ksp.util.resolver

import cn.ktorfitx.common.ksp.util.constants.TypeNames
import cn.ktorfitx.common.ksp.util.expends.hasAnnotation
import cn.ktorfitx.common.ksp.util.expends.rawType
import com.google.devtools.ksp.getClassDeclarationByName
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSTypeAlias
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.ParameterizedTypeName
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.ksp.toClassName
import com.squareup.kotlinpoet.ksp.toTypeName

fun TypeName.isSerializableType(
	resolver: Resolver = safeResolver
): Boolean {
	if (this !is ClassName && this !is ParameterizedTypeName) return false
	val declaration = resolver.getClassDeclarationByName(this.rawType.canonicalName) ?: return false
	val hasSerializer = declaration.hasKotlinxSerializer()
	if (hasSerializer && this is ClassName) return true
	val hasSerializable = declaration.hasAnnotation(TypeNames.Serializable)
	if (declaration.classKind == ClassKind.OBJECT && (hasSerializer || hasSerializable)) return true
	if (!hasSerializer && !hasSerializable) return false
	val primaryConstructor = declaration.primaryConstructor
	if (!hasSerializer && primaryConstructor == null) return false
	val properties = declaration.getAllProperties()
	primaryConstructor?.parameters?.forEach { parameter ->
		if (!parameter.isVar && !parameter.isVal) return false
		val property = properties.first { it.simpleName == parameter.name }
		
		if (property.hasAnnotation(TypeNames.Transient) || property.hasAnnotation(TypeNames.Contextual)) {
			return@forEach
		}
		val type = parameter.type.resolve()
		val declaration = type.declaration
		if (declaration is KSClassDeclaration || declaration is KSTypeAlias) {
			if (!type.toTypeName().isSerializableType(resolver)) {
				return false
			}
		}
	}
	if (this is ParameterizedTypeName) {
		this.typeArguments.forEach {
			if (!it.isSerializableType(resolver)) {
				return false
			}
		}
	}
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
package cn.ktorfitx.common.ksp.util.expends

import cn.ktorfitx.common.ksp.util.constants.TypeNames
import com.google.devtools.ksp.getAllSuperTypes
import com.google.devtools.ksp.symbol.*
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.ParameterizedTypeName
import com.squareup.kotlinpoet.WildcardTypeName
import com.squareup.kotlinpoet.ksp.toClassName
import com.squareup.kotlinpoet.ksp.toTypeName

/**
 * 是否包含注解
 */
fun KSAnnotated.hasAnnotation(annotationClassName: ClassName): Boolean {
	return this.annotations.any {
		it.shortName.getShortName() == annotationClassName.simpleName &&
			it.annotationType.resolve().declaration.qualifiedName?.asString() == annotationClassName.canonicalName
	}
}

/**
 * 获取 KSAnnotation
 */
fun KSAnnotated.getKSAnnotationByType(annotationClassName: ClassName): KSAnnotation? {
	return this.annotations.filter {
		it.shortName.getShortName() == annotationClassName.simpleName &&
			it.annotationType.resolve().declaration.qualifiedName?.asString() == annotationClassName.canonicalName
	}.firstOrNull()
}

/**
 * 通过 KSAnnotation 获取参数的 KSClassDeclaration
 */
fun KSAnnotation.getArgumentKSClassDeclaration(propertyName: String): KSClassDeclaration {
	val value = this.arguments.find { it.name?.asString() == propertyName }?.value
	check(value is KSType)
	return value.declaration as KSClassDeclaration
}

/**
 * 获取注解上的数据
 */
@Suppress("UNCHECKED_CAST")
fun <V : Any> KSAnnotation.getValueOrNull(propertyName: String): V? {
	val value = this.arguments.find { it.name?.asString() == propertyName }?.value
	check(value !is KSType && value !is ArrayList<*>)
	return value as? V
}

@Suppress("UNCHECKED_CAST")
fun <V : Any> KSAnnotation.getValue(propertyName: String): V {
	val value = this.arguments.first { it.name?.asString() == propertyName }.value
	check(value !is KSType && value !is ArrayList<*>)
	return value as V
}

/**
 * 获取注解上的数组数据
 */
inline fun <reified T : Any> KSAnnotation.getValues(propertyName: String): Array<T> {
	val values = this.arguments.first { it.name?.asString() == propertyName }.value as ArrayList<*>
	return values.map { it as T }.toTypedArray()
}

inline fun <reified T : Any> KSAnnotation.getValuesOrNull(propertyName: String): Array<T>? {
	val values = this.arguments.find { it.name?.asString() == propertyName }?.value
	return if (values is ArrayList<*>) {
		values.map { it as T }.toTypedArray()
	} else null
}

fun KSAnnotation.getClassName(propertyName: String): ClassName {
	val value = this.arguments.first { it.name!!.asString() == propertyName }.value
	return when (value) {
		is KSClassDeclaration -> value.toClassName()
		is KSType -> (value.declaration as KSClassDeclaration).toClassName()
		else -> error("$propertyName is not a KSClassDeclaration or KSType.")
	}
}

fun KSAnnotation.getClassNameOrNull(propertyName: String): ClassName? {
	val value = this.arguments.find { it.name?.asString() == propertyName }?.value ?: return null
	return when (value) {
		is KSClassDeclaration -> value.toClassName()
		is KSType -> (value.declaration as? KSClassDeclaration)?.toClassName()
		else -> null
	}
}

fun KSAnnotation.getClassNamesOrNull(propertyName: String): Array<ClassName>? {
	val values = this.arguments.find { it.name?.asString() == propertyName }?.value
	return if (values is ArrayList<*>) {
		values.mapNotNull {
			when (it) {
				is KSClassDeclaration -> it.toClassName()
				is KSType -> (it.declaration as KSClassDeclaration).toClassName()
				else -> null
			}
		}.toTypedArray()
	} else null
}

@Suppress("UNCHECKED_CAST", "unused")
fun <T> Any.safeAs(): T = this as T

fun KSFunctionDeclaration.isExtension(className: ClassName): Boolean {
	return this.extensionReceiver?.resolve()?.declaration?.qualifiedName?.asString() == className.canonicalName
}

fun KSDeclaration.isGeneric(): Boolean {
	return this.typeParameters.isNotEmpty()
}

fun KSType.isMapOfStringToAny(
	valueNullable: Boolean = true
): Boolean {
	val declaration = this.declaration as? KSClassDeclaration ?: return false
	val typeName = (declaration.getAllSuperTypes().find {
		if (it.declaration !is KSClassDeclaration) return@find false
		it.declaration.qualifiedName?.asString() == TypeNames.Map.canonicalName
	}?.toTypeName() ?: this.toTypeName()) as? ParameterizedTypeName ?: return false
	val keyTypeName = typeName.typeArguments.first()
	if (keyTypeName != TypeNames.String) return false
	val valueTypeName = typeName.typeArguments[1]
	return valueNullable || (valueTypeName !is WildcardTypeName && !valueTypeName.isNullable)
}

fun KSType.isListOfStringPair(
	valueNullable: Boolean = true
): Boolean {
	val declaration = this.declaration as? KSClassDeclaration ?: return false
	val typeName = (declaration.getAllSuperTypes().find {
		if (it.declaration !is KSClassDeclaration) return@find false
		it.declaration.qualifiedName?.asString() == TypeNames.List.canonicalName
	}?.toTypeName() ?: this.toTypeName()) as? ParameterizedTypeName ?: return false
	val pairTypeName = (typeName.typeArguments.first() as? ParameterizedTypeName)
		?.takeIf { it.rawType == TypeNames.Pair } ?: return false
	val keyTypeName = pairTypeName.typeArguments.first()
	if (keyTypeName != TypeNames.String) return false
	val valueTypeName = pairTypeName.typeArguments[1]
	return valueNullable || (valueTypeName !is WildcardTypeName && !valueTypeName.isNullable)
}
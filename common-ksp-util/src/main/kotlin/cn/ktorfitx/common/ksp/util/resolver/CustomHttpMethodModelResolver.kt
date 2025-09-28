package cn.ktorfitx.common.ksp.util.resolver

import cn.ktorfitx.common.ksp.util.check.ktorfitxCheck
import cn.ktorfitx.common.ksp.util.constants.TypeNames
import cn.ktorfitx.common.ksp.util.expends.*
import cn.ktorfitx.common.ksp.util.message.*
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.validate
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.ksp.toClassName
import com.squareup.kotlinpoet.ksp.toTypeName

fun <R : Any> Resolver.getCustomHttpMethodModels(
	httpMethod: ClassName,
	defaultHttpMethods: List<ClassName>,
	parameterName: String,
	transform: (method: String, className: ClassName) -> R
): List<R> = this.getSymbolsWithAnnotation(httpMethod.canonicalName)
	.filterIsInstance<KSClassDeclaration>()
	.filter { it.validate() }
	.map {
		ktorfitxCheck(!it.isGeneric(), it) {
			MESSAGE_ANNOTATION_NOT_ALLOW_USE_GENERIC(it.simpleName)
		}
		fun validProperty(): Boolean {
			val properties = it.getAllProperties().toList()
			if (properties.size != 1) return false
			val property = properties.single()
			val typeName = property.type.toTypeName()
			if (typeName != TypeNames.String) return false
			val simpleName = property.simpleName.asString()
			return simpleName == parameterName
		}
		ktorfitxCheck(validProperty(), it) {
			MESSAGE_ANNOTATION_MUST_INCLUDE_STRING_PARAMETER(it.simpleName, parameterName)
		}
		fun validTarget(): Boolean {
			val annotation = it.getKSAnnotationByType(TypeNames.Target) ?: return false
			val classNames = annotation.getClassNamesOrNull("allowedTargets") ?: return false
			if (classNames.size != 1) return false
			val className = classNames.single()
			return className == TypeNames.AnnotationTargetFunction
		}
		ktorfitxCheck(validTarget(), it) {
			MESSAGE_ANNOTATION_MUST_BE_ANNOTATED_TARGET_FUNCTION(it.simpleName)
		}
		fun validRetention(): Boolean {
			val annotation = it.getKSAnnotationByType(TypeNames.Retention) ?: return false
			val className = annotation.getClassNameOrNull("value") ?: return false
			return className == TypeNames.AnnotationRetentionSource
		}
		ktorfitxCheck(validRetention(), it) {
			MESSAGE_ANNOTATION_MUST_BE_ANNOTATED_RETENTION_SOURCE(it.simpleName)
		}
		val httpMethod = it.getKSAnnotationByType(httpMethod)!!
		val method = httpMethod.getValueOrNull<String>("method")
			?.takeIf { method -> method.isNotBlank() }
			?: it.simpleName.asString()
		ktorfitxCheck(method.isValidHttpMethod(), httpMethod) {
			MESSAGE_ANNOTATION_HTTP_METHOD_USE_INVALID_HTTP_METHOD_NAME(it.simpleName, httpMethod)
		}
		ktorfitxCheck(defaultHttpMethods.all { it.simpleName != method }, httpMethod) {
			MESSAGE_ANNOTATION_DUPLICATES_PROVIDED_SYSTEM_HTTP_METHOD_ANNOTATION(it.simpleName, method)
		}
		transform(method, it.toClassName())
	}
	.toList()
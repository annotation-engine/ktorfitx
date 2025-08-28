package cn.ktorfitx.common.ksp.util.expends

import cn.ktorfitx.common.ksp.util.check.compileCheck
import cn.ktorfitx.common.ksp.util.constants.TypeNames
import cn.ktorfitx.common.ksp.util.message.*
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.validate
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.ksp.toClassName
import com.squareup.kotlinpoet.ksp.toTypeName

fun <R : Any> Resolver.getCustomHttpMethodModels(
	httpMethod: ClassName,
	httpMethods: List<ClassName>,
	parameterName: String,
	transform: (method: String, className: ClassName) -> R
): List<R> = this.getSymbolsWithAnnotation(httpMethod.canonicalName)
	.filterIsInstance<KSClassDeclaration>()
	.filter { it.validate() }
	.map {
		it.compileCheck(!it.isGeneric()) {
			MESSAGE_ANNOTATION_NOT_ALLOW_USE_GENERIC.getString(it.simpleName)
		}
		fun validProperty(): Boolean {
			val properties = it.getAllProperties().toList()
			if (properties.size != 1) return false
			val property = properties.first()
			val typeName = property.type.toTypeName()
			if (typeName != TypeNames.String) return false
			val simpleName = property.simpleName.asString()
			return simpleName == parameterName
		}
		it.compileCheck(validProperty()) {
			MESSAGE_ANNOTATION_MUST_INCLUDE_STRING_PARAMETER.getString(it.simpleName, parameterName)
		}
		fun validTarget(): Boolean {
			val annotation = it.getKSAnnotationByType(TypeNames.Target) ?: return false
			val classNames = annotation.getClassNamesOrNull("allowedTargets") ?: return false
			if (classNames.size != 1) return false
			val className = classNames.first()
			return className == TypeNames.AnnotationTargetFunction
		}
		it.compileCheck(validTarget()) {
			MESSAGE_ANNOTATION_MUST_BE_ANNOTATED_TARGET_FUNCTION.getString(it.simpleName)
		}
		fun validRetention(): Boolean {
			val annotation = it.getKSAnnotationByType(TypeNames.Retention) ?: return false
			val className = annotation.getClassNameOrNull("value") ?: return false
			return className == TypeNames.AnnotationRetentionSource
		}
		it.compileCheck(validRetention()) {
			MESSAGE_ANNOTATION_MUST_BE_ANNOTATED_RETENTION_SOURCE.getString(it.simpleName)
		}
		val httpMethod = it.getKSAnnotationByType(httpMethod)!!
		val method = httpMethod.getValueOrNull<String>("method")
			?.takeIf { method -> method.isNotBlank() }
			?: it.simpleName.asString()
		httpMethod.compileCheck(method.isValidHttpMethod()) {
			MESSAGE_ANNOTATION_HTTP_METHOD_USE_INVALID_HTTP_METHOD_NAME.getString(it.simpleName, httpMethod)
		}
		httpMethod.compileCheck(httpMethods.all { it.simpleName != method }) {
			MESSAGE_ANNOTATION_DUPLICATES_PROVIDED_SYSTEM_HTTP_METHOD_ANNOTATION.getString(it.simpleName, method)
		}
		transform(method, it.toClassName())
	}
	.toList()
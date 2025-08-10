package cn.ktorfitx.common.ksp.util.expends

import cn.ktorfitx.common.ksp.util.check.compileCheck
import cn.ktorfitx.common.ksp.util.constants.TypeNames
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.ClassKind
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
		val classKind = it.classKind
		it.compileCheck(classKind == ClassKind.ANNOTATION_CLASS) {
			"@HttpMethod 只允许标注在 annotation class 上，而您的是 ${classKind.code}"
		}
		it.compileCheck(!it.isGeneric()) {
			"${it.simpleName.asString()} 注解不允许使用泛型"
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
			"@${it.simpleName.asString()} 注解必须添加 val $parameterName: String 参数"
		}
		fun validTarget(): Boolean {
			val annotation = it.getKSAnnotationByType(TypeNames.Target) ?: return false
			val classNames = annotation.getClassNamesOrNull("allowedTargets") ?: return false
			if (classNames.size != 1) return false
			val className = classNames.first()
			return className == TypeNames.AnnotationTargetFunction
		}
		it.compileCheck(validTarget()) {
			"${it.simpleName.asString()} 注解必须标注 @Target(AnnotationTarget.FUNCTION)"
		}
		fun validRetention(): Boolean {
			val annotation = it.getKSAnnotationByType(TypeNames.Retention) ?: return false
			val className = annotation.getClassNameOrNull("value") ?: return false
			return className == TypeNames.AnnotationRetentionSource
		}
		it.compileCheck(validRetention()) {
			"${it.simpleName.asString()} 注解必须标注 @Retention(AnnotationRetention.SOURCE)"
		}
		val httpMethod = it.getKSAnnotationByType(httpMethod)!!
		val method = httpMethod.getValueOrNull<String>("method")
			?.takeIf { method -> method.isNotBlank() }
			?: it.simpleName.asString()
		httpMethod.compileCheck(method.isValidHttpMethod()) {
			"${it.simpleName.asString()} 注解上的 $httpMethod 中使用了不合法的 Http Method 名称，只允许包含 A-Z 0-9 '-'"
		}
		httpMethod.compileCheck(httpMethods.all { it.simpleName != method }) {
			"${it.simpleName.asString()} 函数不允许使用 ${httpMethods.joinToString { it.simpleName }} 作为自定义 Http Method 名称"
		}
		transform(method, it.toClassName())
	}
	.toList()
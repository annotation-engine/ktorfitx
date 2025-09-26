package cn.ktorfitx.multiplatform.ksp

import cn.ktorfitx.common.ksp.util.check.ktorfitxCheck
import cn.ktorfitx.common.ksp.util.message.getString
import cn.ktorfitx.common.ksp.util.resolver.getCustomHttpMethodModels
import cn.ktorfitx.common.ksp.util.resolver.safeResolver
import cn.ktorfitx.multiplatform.ksp.constants.TypeNames
import cn.ktorfitx.multiplatform.ksp.kotlinpoet.ApiKotlinPoet
import cn.ktorfitx.multiplatform.ksp.message.MESSAGE_INTERFACE_MUST_BE_INTERFACE_BECAUSE_MARKED_API
import cn.ktorfitx.multiplatform.ksp.message.MESSAGE_INTERFACE_MUST_BE_PLACED_FILE_TOP_LEVEL
import cn.ktorfitx.multiplatform.ksp.message.MESSAGE_INTERFACE_NOT_SUPPORT_SEALED_MODIFIER
import cn.ktorfitx.multiplatform.ksp.model.CustomHttpMethodModel
import cn.ktorfitx.multiplatform.ksp.visitor.ApiVisitor
import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.Modifier
import com.google.devtools.ksp.validate

internal class KtorfitxMultiplatformSymbolProcessor(
	private val codeGenerator: CodeGenerator,
	private val isCommon: Boolean
) : SymbolProcessor {
	
	override fun process(resolver: Resolver): List<KSAnnotated> {
		safeResolver = resolver
		val customHttpMethods = resolver.getCustomHttpMethodModels(
			httpMethod = TypeNames.HttpMethod,
			defaultHttpMethods = TypeNames.httpMethods,
			parameterName = "url",
			transform = ::CustomHttpMethodModel
		)
		resolver.generateApiImpls(customHttpMethods)
		return emptyList()
	}
	
	private fun Resolver.generateApiImpls(
		customHttpMethodModels: List<CustomHttpMethodModel>
	) {
		this.getSymbolsWithAnnotation(TypeNames.Api.canonicalName)
			.filterIsInstance<KSClassDeclaration>()
			.filter { it.validate() }
			.filter {
				if (isCommon) return@filter true
				val filePath = it.containingFile?.filePath ?: return@filter false
				"src/commonMain/kotlin" !in filePath
			}
			.forEach {
				ktorfitxCheck(it.classKind == ClassKind.INTERFACE, it) {
					MESSAGE_INTERFACE_MUST_BE_INTERFACE_BECAUSE_MARKED_API.getString(it.simpleName)
				}
				ktorfitxCheck(Modifier.SEALED !in it.modifiers, it) {
					MESSAGE_INTERFACE_NOT_SUPPORT_SEALED_MODIFIER.getString(it.simpleName)
				}
				ktorfitxCheck(it.parentDeclaration == null, it) {
					MESSAGE_INTERFACE_MUST_BE_PLACED_FILE_TOP_LEVEL.getString(it.simpleName)
				}
				val classModel = it.accept(ApiVisitor, customHttpMethodModels)
				val fileSpec = ApiKotlinPoet.getFileSpec(classModel)
				val className = classModel.className
				codeGenerator.createNewFile(
					dependencies = Dependencies.ALL_FILES,
					packageName = className.packageName,
					fileName = className.simpleName
				).bufferedWriter().use(fileSpec::writeTo)
			}
	}
}
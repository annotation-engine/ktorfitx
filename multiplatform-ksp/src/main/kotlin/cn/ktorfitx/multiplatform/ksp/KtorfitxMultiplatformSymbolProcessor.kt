package cn.ktorfitx.multiplatform.ksp

import cn.ktorfitx.common.ksp.util.check.compileCheck
import cn.ktorfitx.common.ksp.util.expends.getCustomHttpMethodModels
import cn.ktorfitx.common.ksp.util.message.format
import cn.ktorfitx.multiplatform.ksp.constants.TypeNames
import cn.ktorfitx.multiplatform.ksp.kotlinpoet.ApiKotlinPoet
import cn.ktorfitx.multiplatform.ksp.message.MultiplatformMessage
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
) : SymbolProcessor {
	
	override fun process(resolver: Resolver): List<KSAnnotated> {
		val customHttpMethods = resolver.getCustomHttpMethodModels(
			httpMethod = TypeNames.HttpMethod,
			httpMethods = TypeNames.httpMethods,
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
			.forEach {
				it.compileCheck(it.classKind == ClassKind.INTERFACE) {
					MultiplatformMessage.INTERFACE_MUST_BE_INTERFACE_BECAUSE_MARKED_API.format(it.simpleName)
				}
				it.compileCheck(Modifier.SEALED !in it.modifiers) {
					MultiplatformMessage.INTERFACE_NOT_SUPPORT_SEALED_MODIFIER.format(it.simpleName)
				}
				it.compileCheck(it.parentDeclaration == null) {
					MultiplatformMessage.INTERFACE_MUST_BE_PLACED_FILE_TOP_LEVEL.format(it.simpleName)
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
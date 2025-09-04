package cn.ktorfitx.server.ksp

import cn.ktorfitx.common.ksp.util.check.ktorfitxCheck
import cn.ktorfitx.common.ksp.util.message.getString
import cn.ktorfitx.common.ksp.util.resolver.getCustomHttpMethodModels
import cn.ktorfitx.common.ksp.util.resolver.safeResolver
import cn.ktorfitx.server.ksp.constants.TypeNames
import cn.ktorfitx.server.ksp.kotlinpoet.RouteKotlinPoet
import cn.ktorfitx.server.ksp.message.MESSAGE_FUNCTION_TOP_LEVEL_OR_OBJECT_ONLY
import cn.ktorfitx.server.ksp.model.CustomHttpMethodModel
import cn.ktorfitx.server.ksp.model.FunModel
import cn.ktorfitx.server.ksp.visitor.RouteVisitor
import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.*

internal class KtorfitxServerSymbolProcessor(
	private val codeGenerator: CodeGenerator,
	private val packageName: String,
	private val fileName: String,
	private val funName: String
) : SymbolProcessor {
	
	private var isProcessed = false
	
	override fun process(resolver: Resolver): List<KSAnnotated> {
		if (isProcessed) return emptyList()
		isProcessed = true
		safeResolver = resolver
		val customHttpMethodModels = resolver.getCustomHttpMethodModels(
			httpMethod = TypeNames.HttpMethod,
			defaultHttpMethods = TypeNames.httpMethodAnnotationTypes,
			parameterName = "path",
			transform = ::CustomHttpMethodModel
		)
		val funModels = resolver.getFunModels(customHttpMethodModels)
		generateRouteFile(funModels)
		return emptyList()
	}
	
	private fun Resolver.getFunModels(
		customHttpMethodModels: List<CustomHttpMethodModel>
	): List<FunModel> {
		return (TypeNames.routeAnnotationTypes + customHttpMethodModels.map { it.className })
			.flatMap { this.getSymbolsWithAnnotation(it.canonicalName) }
			.filterIsInstance<KSFunctionDeclaration>()
			.map {
				val parent = it.parent
				ktorfitxCheck(parent != null && (parent is KSFile || (parent is KSClassDeclaration && parent.classKind == ClassKind.OBJECT)), it) {
					MESSAGE_FUNCTION_TOP_LEVEL_OR_OBJECT_ONLY.getString(it.simpleName)
				}
				val visitor = RouteVisitor()
				it.accept(visitor, customHttpMethodModels)
			}
	}
	
	private fun generateRouteFile(
		funModels: List<FunModel>,
	) {
		val fileSpec = RouteKotlinPoet()
			.getFileSpec(funModels, packageName, fileName, funName)
		codeGenerator.createNewFile(
			dependencies = Dependencies.ALL_FILES,
			packageName = packageName,
			fileName = fileName
		).bufferedWriter().use(fileSpec::writeTo)
	}
}
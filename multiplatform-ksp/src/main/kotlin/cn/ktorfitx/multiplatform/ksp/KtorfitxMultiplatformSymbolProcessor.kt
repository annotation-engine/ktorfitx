package cn.ktorfitx.multiplatform.ksp

import cn.ktorfitx.common.ksp.util.check.ktorfitxCheck
import cn.ktorfitx.common.ksp.util.io.deleteDirectory
import cn.ktorfitx.common.ksp.util.message.invoke
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
import com.squareup.kotlinpoet.FileSpec
import java.io.File

internal class KtorfitxMultiplatformSymbolProcessor(
	private val codeGenerator: CodeGenerator,
	private val sourceSetModel: SourceSetModel,
	private val projectPath: String
) : SymbolProcessor {
	
	override fun process(resolver: Resolver): List<KSAnnotated> {
		safeResolver = resolver
		val customHttpMethods = resolver.getCustomHttpMethodModels(
			httpMethod = TypeNames.HttpMethod,
			defaultHttpMethods = TypeNames.httpMethods,
			parameterName = "url",
			transform = ::CustomHttpMethodModel
		)
		resolver.getSymbolsWithAnnotation(TypeNames.Api.canonicalName)
			.filterIsInstance<KSClassDeclaration>()
			.filter { it.validate() }
			.filter {
				if (sourceSetModel !is MultiplatformSourceSetModel) return@filter true
				if (sourceSetModel.isCommonMain) return@filter true
				val sourceSet = it.getSourceSet() ?: return@filter false
				sourceSet != "commonMain"
			}
			.toList()
			.deleteMiddleSourceSetsDirs()
			.forEach { it.dispose(customHttpMethods) }
		
		return emptyList()
	}
	
	private fun List<KSClassDeclaration>.deleteMiddleSourceSetsDirs(): List<KSClassDeclaration> {
		if (this.isEmpty()) return emptyList()
		if (sourceSetModel is MultiplatformSourceSetModel && !sourceSetModel.isCommonMain) {
			sourceSetModel.middleSourceSets.forEach { sourceSets ->
				val parent = "$projectPath/build/generated/ksp/metadata/$sourceSets"
				val parentDir = File(parent)
				parentDir.deleteDirectory()
			}
		}
		return this
	}
	
	private fun KSClassDeclaration.dispose(customHttpMethodModels: List<CustomHttpMethodModel>) {
		ktorfitxCheck(this.classKind == ClassKind.INTERFACE, this) {
			MESSAGE_INTERFACE_MUST_BE_INTERFACE_BECAUSE_MARKED_API(this.simpleName)
		}
		ktorfitxCheck(Modifier.SEALED !in this.modifiers, this) {
			MESSAGE_INTERFACE_NOT_SUPPORT_SEALED_MODIFIER(this.simpleName)
		}
		ktorfitxCheck(this.parentDeclaration == null, this) {
			MESSAGE_INTERFACE_MUST_BE_PLACED_FILE_TOP_LEVEL(this.simpleName)
		}
		val classModel = this.accept(ApiVisitor, customHttpMethodModels)
		val fileSpec = ApiKotlinPoet.getFileSpec(classModel)
		val className = classModel.className
		val packageName = className.packageName
		val fileName = className.simpleName
		
		createNewFile(packageName, fileName, fileSpec)
	}
	
	private fun KSClassDeclaration.createNewFile(
		packageName: String,
		fileName: String,
		fileSpec: FileSpec
	) {
		val codeGeneratorCreateNewFile = {
			codeGenerator.createNewFile(
				dependencies = Dependencies.ALL_FILES,
				packageName = packageName,
				fileName = fileName
			).bufferedWriter().use(fileSpec::writeTo)
		}
		if (sourceSetModel is AndroidOnlySourceSetModel) {
			codeGeneratorCreateNewFile()
			return
		}
		sourceSetModel as MultiplatformSourceSetModel
		val sourceSet = getSourceSet()
		if (sourceSetModel.isCommonMain || sourceSet !in sourceSetModel.middleSourceSets) {
			codeGeneratorCreateNewFile()
			return
		}
		val parent = "$projectPath/build/generated/ksp/metadata/$sourceSet/kotlin/" +
				packageName.replace('.', '/')
		val parentDir = File(parent)
		if (parentDir.exists() && !parentDir.isDirectory) {
			parentDir.delete()
		}
		if (!parentDir.exists()) {
			parentDir.mkdirs()
		}
		val file = File(parentDir, "$fileName.kt")
		if (file.exists()) {
			file.delete()
		}
		file.createNewFile()
		file.bufferedWriter()
			.use(fileSpec::writeTo)
	}
	
	private fun KSClassDeclaration.getSourceSet(): String? {
		val filePath = this.containingFile?.filePath ?: return null
		sourceSetModel as MultiplatformSourceSetModel
		return filePath.removePrefix("$projectPath/src/")
			.split('/')
			.firstOrNull()
	}
}
package cn.ktorfitx.multiplatform.ksp

import cn.ktorfitx.common.ksp.util.check.ktorfitxCheck
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
import java.io.File
import java.io.OutputStream

internal class KtorfitxMultiplatformSymbolProcessor(
	private val codeGenerator: CodeGenerator,
	private val sourceSetModel: SourceSetModel
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
			.deleteSharedSourceSetsDirs()
			.forEach { it.dispose(customHttpMethods) }
		
		return emptyList()
	}
	
	private fun Sequence<KSClassDeclaration>.deleteSharedSourceSetsDirs(): Sequence<KSClassDeclaration> {
		if (sourceSetModel is MultiplatformSourceSetModel) {
			sourceSetModel.sharedSourceSets.forEach {
				val parent = "${sourceSetModel.projectPath}/build/generated/ksp/metadata/$it".replace('/', File.separatorChar)
				val parentDir = File(parent)
				deleteDirectory(parentDir)
			}
		}
		return this
	}
	
	private fun deleteDirectory(file: File) {
		if (!file.exists()) return
		
		if (file.isDirectory) {
			file.listFiles()?.forEach { child ->
				deleteDirectory(child)
			}
		}
		file.delete()
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
		
		createNewFile(
			packageName = className.packageName,
			fileName = className.simpleName
		)?.bufferedWriter()?.use(fileSpec::writeTo)
	}
	
	private fun KSClassDeclaration.createNewFile(packageName: String, fileName: String): OutputStream? {
		val getCodeGeneratorCreateNewFile = {
			codeGenerator.createNewFile(
				dependencies = Dependencies.ALL_FILES,
				packageName = packageName,
				fileName = fileName
			)
		}
		if (sourceSetModel is AndroidOnlySourceSetModel) {
			return getCodeGeneratorCreateNewFile()
		}
		sourceSetModel as MultiplatformSourceSetModel
		val sourceSet = getSourceSet()
		if (sourceSet in sourceSetModel.nonSharedSourceSets) {
			return getCodeGeneratorCreateNewFile()
		}
		val parent = "${sourceSetModel.projectPath}/build/generated/ksp/metadata/$sourceSet/kotlin/".replace('/', File.separatorChar) +
				packageName.replace('.', File.separatorChar)
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
		return file.outputStream()
	}
	
	private fun KSClassDeclaration.getSourceSet(): String? {
		val filePath = this.containingFile?.filePath ?: return null
		sourceSetModel as MultiplatformSourceSetModel
		return filePath.removePrefix("${sourceSetModel.projectPath}/src/".replace('/', File.separatorChar))
			.split(File.separatorChar)
			.firstOrNull()
	}
}
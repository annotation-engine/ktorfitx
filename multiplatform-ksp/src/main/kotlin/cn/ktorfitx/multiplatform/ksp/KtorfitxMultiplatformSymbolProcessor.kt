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
import java.io.File
import java.io.OutputStream

internal class KtorfitxMultiplatformSymbolProcessor(
	private val codeGenerator: CodeGenerator,
	private val projectPath: String,
	private val model: KtorfitxModel
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
				if (model is OnlyAndroidModel) return@filter true
				model as KotlinMultiplatformModel
				if (model.isCommon) return@filter true
				val sourceSet = it.getSourceSet() ?: return@filter false
				!sourceSet.startsWith("common")
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
				createNewFile(
					sourceSet = it.getSourceSet() ?: return@forEach,
					packageName = className.packageName,
					fileName = className.simpleName
				)?.bufferedWriter()?.use(fileSpec::writeTo)
			}
	}
	
	private fun KSClassDeclaration.getSourceSet(): String? {
		val filePath = this.containingFile?.filePath ?: return null
		return filePath.removePrefix("$projectPath/src/".replace('/', File.separatorChar))
			.split(File.separatorChar)
			.firstOrNull()
	}
	
	private fun createNewFile(sourceSet: String, packageName: String, fileName: String): OutputStream? {
		return if (model is OnlyAndroidModel || isUseCodeGeneratorCreate(sourceSet)) {
			codeGenerator.createNewFile(
				dependencies = Dependencies.ALL_FILES,
				packageName = packageName,
				fileName = fileName
			)
		} else {
			model as KotlinMultiplatformModel
			val variants = model.sourceSetsVariants[sourceSet] ?: error("Can't find source set $sourceSet")
			val parent = "$projectPath/build/generated/ksp/$variants/$sourceSet/kotlin/".replace('/', File.separatorChar) +
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
			file.outputStream()
		}
	}
	
	private val targetKeywords = arrayOf("common", "X64", "Arm", "js", "wasmJs", "android", "desktop")
	
	private fun isUseCodeGeneratorCreate(sourceName: String): Boolean {
		return targetKeywords.any { it in sourceName }
	}
}
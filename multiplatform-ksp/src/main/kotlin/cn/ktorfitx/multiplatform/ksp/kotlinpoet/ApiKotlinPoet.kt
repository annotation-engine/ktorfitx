package cn.ktorfitx.multiplatform.ksp.kotlinpoet

import cn.ktorfitx.common.ksp.util.builders.*
import cn.ktorfitx.common.ksp.util.expends.asNullable
import cn.ktorfitx.common.ksp.util.expends.replaceFirstToLowercase
import cn.ktorfitx.common.ksp.util.expends.replaceFirstToUppercase
import cn.ktorfitx.common.ksp.util.message.getString
import cn.ktorfitx.multiplatform.ksp.constants.PackageNames
import cn.ktorfitx.multiplatform.ksp.constants.TypeNames
import cn.ktorfitx.multiplatform.ksp.kotlinpoet.block.HttpCodeBlockBuilder
import cn.ktorfitx.multiplatform.ksp.kotlinpoet.block.WebSocketCodeBuilder
import cn.ktorfitx.multiplatform.ksp.message.FILE_COMMENT
import cn.ktorfitx.multiplatform.ksp.model.*
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

internal object ApiKotlinPoet {
	
	/**
	 * 文件
	 */
	fun getFileSpec(classModel: ClassModel): FileSpec {
		return buildFileSpec(classModel.className) {
			fileSpecBuilderLocal.set(this)
			addFileComment(FILE_COMMENT.getString(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))))
			indent("\t")
			addType(getTypeSpec(classModel))
			addProperties(getExpendPropertySpecs(classModel))
			fileSpecBuilderLocal.remove()
		}
	}
	
	/**
	 * 实现类
	 */
	private fun getTypeSpec(classModel: ClassModel): TypeSpec {
		val primaryConstructorFunSpec = buildConstructorFunSpec {
			addModifiers(KModifier.PRIVATE)
			addParameter("config", TypeNames.KtorfitxConfig)
		}
		return buildClassTypeSpec(classModel.className) {
			addModifiers(KModifier.PRIVATE)
			addSuperinterface(classModel.superinterface)
			primaryConstructor(primaryConstructorFunSpec)
			val ktorfitxConfigPropertySpec = buildPropertySpec("config", TypeNames.KtorfitxConfig, KModifier.PRIVATE) {
				initializer("config")
				mutable(false)
			}
			addProperty(ktorfitxConfigPropertySpec)
			addType(getCompanionObjectBuilder(classModel))
			addFunctions(getFunSpecs(classModel))
		}
	}
	
	/**
	 * 伴生对象
	 */
	private fun getCompanionObjectBuilder(classModel: ClassModel): TypeSpec {
		val typeName = classModel.superinterface.asNullable()
		fileSpecBuilder.addImport(PackageNames.KTOR_UTILS_IO_LOCKS, "synchronized")
		return buildCompanionObjectTypeSpec {
			addModifiers(classModel.kModifier)
			val optInSpec = buildAnnotationSpec(TypeNames.OptIn) {
				addMember("%T::class", TypeNames.InternalAPI)
			}
			addAnnotation(optInSpec)
			if (classModel.apiUrl != null) {
				val apiUrlPropertySpec = buildPropertySpec("API_URL", TypeNames.String, KModifier.CONST, KModifier.PRIVATE) {
					initializer("%S", classModel.apiUrl)
				}
				addProperty(apiUrlPropertySpec)
			}
			classModel.apiScopeModels.forEach { model ->
				val simpleName = model.className.simpleNames.joinToString("") { it.replaceFirstToUppercase() }
				val varName = simpleName.replaceFirstToLowercase()
				val instanceVarName = "${varName}Instance"
				val instancePropertySpec = buildPropertySpec(instanceVarName, typeName, KModifier.PRIVATE) {
					initializer("null")
					mutable(true)
				}
				addProperty(instancePropertySpec)
				val lockVarName = "${varName}SynchronizedObject"
				val mutexPropertySpec = buildPropertySpec(lockVarName, TypeNames.SynchronizedObject, KModifier.PRIVATE) {
					initializer("%T()", TypeNames.SynchronizedObject)
					mutable(false)
				}
				addProperty(mutexPropertySpec)
				val funSpec = buildFunSpec("getInstanceBy$simpleName") {
					addModifiers(classModel.kModifier)
					returns(classModel.superinterface)
					addParameter(
						"ktorfitx",
						TypeNames.Ktorfitx.parameterizedBy(model.className)
					)
					val codeBlock = buildCodeBlock {
						beginControlFlow("return %N ?: synchronized(%N)", instanceVarName, lockVarName)
						addStatement("%N ?: %T(ktorfitx.config).also { %N = it }", instanceVarName, classModel.className, instanceVarName)
						endControlFlow()
					}
					addCode(codeBlock)
				}
				addFunction(funSpec)
			}
		}
	}
	
	/**
	 * 扩展函数
	 */
	private fun getExpendPropertySpecs(classModel: ClassModel): List<PropertySpec> {
		val expendPropertyName = classModel.superinterface.simpleName.replaceFirstChar { it.lowercase() }
		return classModel.apiScopeModels.map { model ->
			val simpleName = model.className.simpleNames.joinToString(".")
			val jvmNameAnnotationSpec = buildAnnotationSpec(JvmName::class) {
				addMember("%S", "${expendPropertyName}By$simpleName")
			}
			val getterFunSpec = buildGetterFunSpec {
				addAnnotation(jvmNameAnnotationSpec)
				addStatement("return %T.%N(this)", classModel.className, "getInstanceBy$simpleName")
			}
			buildPropertySpec(expendPropertyName, classModel.superinterface, classModel.kModifier) {
				receiver(TypeNames.Ktorfitx.parameterizedBy(model.className))
				getter(getterFunSpec)
			}
		}
	}
	
	/**
	 * 实现方法
	 */
	private fun getFunSpecs(classModel: ClassModel): List<FunSpec> {
		return classModel.funModels.map {
			buildFunSpec(it.funName) {
				addModifiers(KModifier.SUSPEND, KModifier.OVERRIDE)
				addParameters(getParameterSpecs(it.parameterModels))
				addCode(getCodeBlock(classModel, it))
				returns(it.returnModel.typeName)
			}
		}.toList()
	}
	
	private fun getParameterSpecs(models: List<ParameterModel>): List<ParameterSpec> {
		return models.map { buildParameterSpec(it.varName, it.typeName) }
	}
	
	private fun getCodeBlock(classModel: ClassModel, funModel: FunModel): CodeBlock {
		return buildCodeBlock {
			val tokenVarName = if (funModel.hasBearerAuth) getTokenVarName(funModel.parameterModels) else null
			if (tokenVarName != null) {
				addStatement("val %N = this.config.token?.invoke()", tokenVarName)
			}
			when (funModel.routeModel) {
				is HttpRequestModel -> {
					with(HttpCodeBlockBuilder(classModel, funModel, funModel.routeModel, tokenVarName)) {
						buildCodeBlock()
					}
				}
				
				is WebSocketModel -> {
					with(WebSocketCodeBuilder(classModel, funModel, funModel.routeModel, tokenVarName)) {
						buildCodeBlock()
					}
				}
			}
		}
	}
	
	private fun getTokenVarName(
		parameterModels: List<ParameterModel>
	): String {
		var i = 0
		val raw = "token"
		var varName = raw
		val varNames = parameterModels.map { it.varName }
		while (varName in varNames) {
			varName = raw + i++
		}
		return varName
	}
}
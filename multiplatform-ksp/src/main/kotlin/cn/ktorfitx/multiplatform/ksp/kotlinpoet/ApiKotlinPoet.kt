package cn.ktorfitx.multiplatform.ksp.kotlinpoet

import cn.ktorfitx.common.ksp.util.builders.*
import cn.ktorfitx.common.ksp.util.expends.asNullable
import cn.ktorfitx.common.ksp.util.message.invoke
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
			fileSpecBuilder = this
			addFileComment(FILE_COMMENT(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))))
			indent("\t")
			addType(getTypeSpec(classModel))
			addProperty(getExpendPropertySpec(classModel))
		}
	}
	
	/**
	 * 实现类
	 */
	private fun getTypeSpec(classModel: ClassModel): TypeSpec {
		val primaryConstructorFunSpec = buildConstructorFunSpec {
			addModifiers(KModifier.PRIVATE)
			if (classModel.funModels.isNotEmpty()) {
				addParameter("config", TypeNames.KtorfitxConfig)
			}
		}
		return buildClassTypeSpec(classModel.className) {
			addModifiers(KModifier.PRIVATE)
			addSuperinterface(classModel.superinterface)
			primaryConstructor(primaryConstructorFunSpec)
			if (classModel.funModels.isNotEmpty()) {
				val ktorfitxConfigPropertySpec = buildPropertySpec("config", TypeNames.KtorfitxConfig, KModifier.PRIVATE) {
					initializer("config")
					mutable(false)
				}
				addProperty(ktorfitxConfigPropertySpec)
			}
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
			if (classModel.apiUrl != null && classModel.funModels.isNotEmpty()) {
				val apiUrlPropertySpec = buildPropertySpec("API_URL", TypeNames.String, KModifier.CONST, KModifier.PRIVATE) {
					initializer("%S", classModel.apiUrl)
				}
				addProperty(apiUrlPropertySpec)
			}
			
			val instancePropertySpec = buildPropertySpec("instances", TypeNames.MutableMap.parameterizedBy(TypeNames.Ktorfitx, typeName), KModifier.PRIVATE) {
				initializer("mutableMapOf()")
				mutable(false)
			}
			addProperty(instancePropertySpec)
			val mutexPropertySpec = buildPropertySpec("lock", TypeNames.SynchronizedObject, KModifier.PRIVATE) {
				initializer("%T()", TypeNames.SynchronizedObject)
				mutable(false)
			}
			addProperty(mutexPropertySpec)
			val funSpec = buildFunSpec("getInstance") {
				addModifiers(classModel.kModifier)
				returns(classModel.superinterface)
				if (classModel.funModels.isNotEmpty()) {
					addParameter(
						"ktorfitx",
						TypeNames.Ktorfitx
					)
				}
				val codeBlock = buildCodeBlock {
					beginControlFlow("return instances[ktorfitx] ?: synchronized(lock)")
					if (classModel.funModels.isEmpty()) {
						addStatement("instances[ktorfitx] ?: %T().also { instances[ktorfitx] = it }", classModel.className)
					} else {
						addStatement("instances[ktorfitx] ?: %T(ktorfitx.config).also { instances[ktorfitx] = it }", classModel.className)
					}
					endControlFlow()
				}
				addCode(codeBlock)
			}
			addFunction(funSpec)
		}
	}
	
	/**
	 * 扩展函数
	 */
	private fun getExpendPropertySpec(classModel: ClassModel): PropertySpec {
		val expendPropertyName = classModel.superinterface.simpleName.replaceFirstChar { it.lowercase() }
		
		val getterFunSpec = buildGetterFunSpec {
			if (classModel.funModels.isEmpty()) {
				addStatement("return %T.%N()", classModel.className, "getInstance")
			} else {
				addStatement("return %T.%N(this)", classModel.className, "getInstance")
			}
		}
		return buildPropertySpec(expendPropertyName, classModel.superinterface, classModel.kModifier) {
			receiver(TypeNames.Ktorfitx)
			getter(getterFunSpec)
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
package cn.ktorfitx.common.ksp.util.builders

import com.squareup.kotlinpoet.*
import kotlin.reflect.KClass
import kotlin.reflect.typeOf

/**
 * buildFileSpec
 */
fun buildFileSpec(
	className: ClassName,
	block: FileSpec.Builder.() -> Unit = {},
): FileSpec = FileSpec.builder(className).apply(block).build()

/**
 * buildFileSpec
 */
fun buildFileSpec(
	packageName: String,
	fileName: String,
	block: FileSpec.Builder.() -> Unit = {},
): FileSpec = FileSpec.builder(packageName, fileName).apply(block).build()

/**
 * buildClassTypeSpec
 */
fun buildClassTypeSpec(
	className: ClassName,
	block: TypeSpec.Builder.() -> Unit = {},
): TypeSpec = TypeSpec.classBuilder(className).apply(block).build()

/**
 * buildCompanionObjectTypeSpec
 */
fun buildCompanionObjectTypeSpec(
	block: TypeSpec.Builder.() -> Unit = {},
): TypeSpec = TypeSpec.companionObjectBuilder().apply(block).build()

/**
 * buildFunSpec
 */
fun buildFunSpec(
	name: String,
	block: FunSpec.Builder.() -> Unit = {},
): FunSpec = FunSpec.builder(name).apply(block).build()

/**
 * buildConstructorFunSpec
 */
fun buildConstructorFunSpec(
	block: FunSpec.Builder.() -> Unit = {},
): FunSpec = FunSpec.constructorBuilder().apply(block).build()

/**
 * buildGetterFunSpec
 */
fun buildGetterFunSpec(
	block: FunSpec.Builder.() -> Unit = {},
): FunSpec = FunSpec.getterBuilder().apply(block).build()

/**
 * buildPropertySpec
 */
fun buildPropertySpec(
	name: String,
	type: TypeName,
	vararg modifiers: KModifier,
	block: PropertySpec.Builder.() -> Unit = {},
): PropertySpec = PropertySpec.builder(name, type, *modifiers).apply(block).build()

/**
 * buildParameterSpec
 */
fun buildParameterSpec(
	name: String,
	type: TypeName,
	block: ParameterSpec.Builder.() -> Unit = {},
): ParameterSpec = ParameterSpec.builder(name, type).apply(block).build()

/**
 * buildAnnotationSpec
 */
fun buildAnnotationSpec(
	type: KClass<out Annotation>,
	block: AnnotationSpec.Builder.() -> Unit = {},
): AnnotationSpec = AnnotationSpec.builder(type).apply(block).build()

/**
 * buildAnnotationSpec
 */
fun buildAnnotationSpec(
	className: ClassName,
	block: AnnotationSpec.Builder.() -> Unit = {},
): AnnotationSpec = AnnotationSpec.builder(className).apply(block).build()

/**
 * Map<String, V> to CodeBlock
 */
inline fun <reified V : String?> Map<String, V>.toCodeBlock(
	explicitTypeIfEmpty: Boolean = false
): CodeBlock = buildCodeBlock {
	if (this@toCodeBlock.isEmpty()) {
		if (explicitTypeIfEmpty) {
			val valueTypeName = typeOf<V>().asTypeName()
			add("emptyMap<String, %T>()", valueTypeName)
		} else {
			add("emptyMap()")
		}
	} else {
		val format = buildString {
			append("mapOf(")
			append(this@toCodeBlock.map { "%S to %S" }.joinToString())
			append(")")
		}
		val args = this@toCodeBlock.flatMap { listOf(it.key, it.value) }
		add(format, *args.toTypedArray())
	}
}
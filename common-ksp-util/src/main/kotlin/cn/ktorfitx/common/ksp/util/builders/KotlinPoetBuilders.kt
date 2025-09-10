package cn.ktorfitx.common.ksp.util.builders

import com.squareup.kotlinpoet.*
import kotlin.reflect.KClass
import kotlin.reflect.typeOf

fun buildFileSpec(
	className: ClassName,
	block: FileSpec.Builder.() -> Unit = {},
): FileSpec = FileSpec.builder(className).apply(block).build()

fun buildFileSpec(
	packageName: String,
	fileName: String,
	block: FileSpec.Builder.() -> Unit = {},
): FileSpec = FileSpec.builder(packageName, fileName).apply(block).build()

fun buildClassTypeSpec(
	className: ClassName,
	block: TypeSpec.Builder.() -> Unit = {},
): TypeSpec = TypeSpec.classBuilder(className).apply(block).build()

fun buildCompanionObjectTypeSpec(
	block: TypeSpec.Builder.() -> Unit = {},
): TypeSpec = TypeSpec.companionObjectBuilder().apply(block).build()

fun buildFunSpec(
	name: String,
	block: FunSpec.Builder.() -> Unit = {},
): FunSpec = FunSpec.builder(name).apply(block).build()

fun buildConstructorFunSpec(
	block: FunSpec.Builder.() -> Unit = {},
): FunSpec = FunSpec.constructorBuilder().apply(block).build()

fun buildGetterFunSpec(
	block: FunSpec.Builder.() -> Unit = {},
): FunSpec = FunSpec.getterBuilder().apply(block).build()

fun buildPropertySpec(
	name: String,
	type: TypeName,
	vararg modifiers: KModifier,
	block: PropertySpec.Builder.() -> Unit = {},
): PropertySpec = PropertySpec.builder(name, type, *modifiers).apply(block).build()

fun buildParameterSpec(
	name: String,
	type: TypeName,
	block: ParameterSpec.Builder.() -> Unit = {},
): ParameterSpec = ParameterSpec.builder(name, type).apply(block).build()

fun buildAnnotationSpec(
	type: KClass<out Annotation>,
	block: AnnotationSpec.Builder.() -> Unit = {},
): AnnotationSpec = AnnotationSpec.builder(type).apply(block).build()

fun buildAnnotationSpec(
	className: ClassName,
	block: AnnotationSpec.Builder.() -> Unit = {},
): AnnotationSpec = AnnotationSpec.builder(className).apply(block).build()

inline fun <reified V : String?> Map<String, V>.toMapCode(
	explicitTypeIfEmpty: Boolean = false
): CodeBlock = buildCodeBlock {
	if (this@toMapCode.isEmpty()) {
		if (explicitTypeIfEmpty) {
			val valueTypeName = typeOf<V>().asTypeName()
			add("emptyMap<String, %T>()", valueTypeName)
		} else {
			add("emptyMap()")
		}
	} else {
		val format = buildString {
			append("mapOf(")
			append(this@toMapCode.map { "%S to %S" }.joinToString())
			append(")")
		}
		val args = this@toMapCode.flatMap { listOf(it.key, it.value) }
		add(format, *args.toTypedArray())
	}
}
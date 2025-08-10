package cn.ktorfitx.common.ksp.util.constants

import com.squareup.kotlinpoet.ClassName

internal object TypeNames {
	
	val Target = ClassName("kotlin.annotation", "Target")
	
	val Retention = ClassName("kotlin.annotation", "Retention")
	
	val AnnotationRetentionSource = ClassName("kotlin.annotation", "AnnotationRetention", "SOURCE")
	
	val AnnotationTargetFunction = ClassName("kotlin.annotation", "AnnotationTarget", "FUNCTION")
	
	val String = ClassName("kotlin", "String")
	
	val List = ClassName("kotlin.collections", "List")
	
	val Map = ClassName("kotlin.collections", "Map")
	
	val Pair = ClassName("kotlin", "Pair")
}
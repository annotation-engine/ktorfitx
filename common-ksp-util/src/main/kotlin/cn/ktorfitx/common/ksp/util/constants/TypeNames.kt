package cn.ktorfitx.common.ksp.util.constants

import com.squareup.kotlinpoet.*

internal object TypeNames {
	
	val Target = ClassName("kotlin.annotation", "Target")
	
	val Retention = ClassName("kotlin.annotation", "Retention")
	
	val AnnotationRetentionSource = ClassName("kotlin.annotation", "AnnotationRetention", "SOURCE")
	
	val AnnotationTargetFunction = ClassName("kotlin.annotation", "AnnotationTarget", "FUNCTION")
	
	val String = STRING
	
	val List = LIST
	
	val Map = MAP
	
	val Pair = ClassName("kotlin", "Pair")
	
	val Serializable = ClassName("kotlinx.serialization", "Serializable")
	
	val Transient = ClassName("kotlinx.serialization", "Transient")
	
	val Contextual = ClassName("kotlinx.serialization", "Contextual")
	
	val kotlinxSerializerTypeNames = listOf(
		Pair,
		ClassName("kotlin.collections", "Map", "Entry"),
		ClassName("kotlin", "Triple"),
		CHAR,
		CHAR_ARRAY,
		BYTE,
		BYTE_ARRAY,
		U_BYTE,
		U_BYTE_ARRAY,
		SHORT,
		SHORT_ARRAY,
		U_SHORT,
		U_SHORT_ARRAY,
		INT,
		INT_ARRAY,
		U_INT,
		U_INT_ARRAY,
		LONG,
		LONG_ARRAY,
		U_LONG,
		U_LONG_ARRAY,
		FLOAT,
		FLOAT_ARRAY,
		DOUBLE,
		DOUBLE_ARRAY,
		BOOLEAN,
		BOOLEAN_ARRAY,
		UNIT,
		STRING,
		ARRAY,
		LIST,
		SET,
		MAP,
		ClassName("kotlin.time", "Duration"),
		ClassName("kotlin.uuid", "Uuid"),
		NOTHING
	)
}
package cn.ktorfitx.server.core

import io.ktor.http.content.*
import kotlinx.io.readByteArray
import kotlin.reflect.KClass

suspend fun MultiPartData.extractParameters(): MultiPartParameters {
	val partDataList = mutableListOf<Pair<String, PartData>>()
	this.forEachPart {
		if (it.name == null) {
			it.dispose()
			return@forEachPart
		}
		partDataList += it.name!! to it
	}
	return MultiPartParameters(partDataList)
}

class MultiPartParameters internal constructor(
	val partDataList: List<Pair<String, PartData>>
) {
	
	inline fun <reified T : PartData> getPartData(name: String): T {
		return partDataList.asSequence()
			.mapNotNull { if (it.first == name) it.second else null }
			.filterIsInstance<T>()
			.firstOrNull() ?: error("Not Found ${T::class.simpleName}: $name.")
	}
	
	inline fun <reified T : PartData> getPartDataOrNull(name: String): T? {
		return partDataList.asSequence()
			.mapNotNull { if (it.first == name) it.second else null }
			.filterIsInstance<T>()
			.firstOrNull()
	}
	
	inline fun <reified T : PartData> getPartDataList(name: String): List<T> {
		return partDataList.asSequence()
			.mapNotNull { if (it.first == name) it.second else null }
			.filterIsInstance<T>()
			.toList()
	}
	
	inline fun <reified T : PartData> getPartDataListOrNull(name: String): List<T>? {
		return partDataList.asSequence()
			.mapNotNull { if (it.first == name) it.second else null }
			.filterIsInstance<T>()
			.toList()
			.ifEmpty { null }
	}
	
	suspend inline fun <reified T : Any> getValue(name: String): T = getValue(name, T::class)
	
	suspend inline fun <reified T : Any> getValueOrNull(name: String): T? = getValueOrNull(name, T::class)
	
	suspend inline fun <reified T : Any> getValues(name: String): List<T> = getValues(name, T::class)
	
	suspend inline fun <reified T : Any> getValuesOrNull(name: String): List<T>? = getValuesOrNull(name, T::class)
	
	suspend fun <T : Any> getValue(name: String, kClass: KClass<T>): T {
		@Suppress("UNCHECKED_CAST")
		return when (kClass) {
			String::class -> getPartData<PartData.FormItem>(name).use { it.value }
			ByteArray::class -> getPartData<PartData.BinaryItem>(name).use { it.provider().readByteArray() }
			else -> error("Unsupported type: ${kClass.simpleName}")
		} as T
	}
	
	suspend fun <T : Any> getValueOrNull(name: String, kClass: KClass<T>): T? {
		@Suppress("UNCHECKED_CAST")
		return when (kClass) {
			String::class -> getPartData<PartData.FormItem>(name).use { it.value }
			ByteArray::class -> getPartData<PartData.BinaryItem>(name).use { it.provider().readByteArray() }
			else -> error("Unsupported type: ${kClass.simpleName}")
		} as? T
	}
	
	suspend fun <T : Any> getValues(name: String, kClass: KClass<T>): List<T> {
		@Suppress("UNCHECKED_CAST")
		return when (kClass) {
			String::class -> getPartDataList<PartData.FormItem>(name).uses { it.value }
			ByteArray::class -> getPartDataList<PartData.BinaryItem>(name).uses { it.provider().readByteArray() }
			else -> error("Unsupported type: ${kClass.simpleName}")
		} as List<T>
	}
	
	suspend fun <T : Any> getValuesOrNull(name: String, kClass: KClass<T>): List<T>? {
		@Suppress("UNCHECKED_CAST")
		return when (kClass) {
			String::class -> getPartDataListOrNull<PartData.FormItem>(name)?.uses { it.value }
			ByteArray::class -> getPartDataListOrNull<PartData.BinaryItem>(name)?.uses { it.provider().readByteArray() }
			else -> error("Unsupported type: ${kClass.simpleName}")
		} as? List<T>
	}
	
	fun disposeAll() {
		partDataList.forEach {
			it.second.dispose()
		}
	}
	
	suspend fun <T : PartData, R> T.use(block: suspend (T) -> R): R {
		return block(this).also { this.dispose() }
	}
	
	suspend fun <T : PartData, R> List<T>.uses(block: suspend (T) -> R): List<R> {
		return this.map { partData ->
			block(partData).also { partData.dispose() }
		}
	}
}
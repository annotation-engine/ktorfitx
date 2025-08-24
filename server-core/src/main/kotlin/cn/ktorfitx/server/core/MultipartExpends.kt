package cn.ktorfitx.server.core

import io.ktor.http.content.*
import io.ktor.utils.io.*
import kotlinx.io.readByteArray

suspend fun MultiPartData.getMultipartParameters(): MultipartParameters {
	val partDataMap = mutableMapOf<String, PartData>()
	this.forEachPart {
		if (it.name == null) {
			it.dispose()
			return@forEachPart
		}
		partDataMap[it.name!!] = it
	}
	return MultipartParameters(partDataMap)
}

class MultipartParameters internal constructor(
	private val partDataMap: Map<String, PartData>
) {
	
	private val nonDisposeNames = mutableSetOf(*partDataMap.keys.toTypedArray())
	
	fun getForm(name: String): PartData.FormItem {
		return partDataMap[name] as? PartData.FormItem ?: error("Not Found PartItem: $name.")
	}
	
	suspend fun getFormValue(name: String): String {
		val formItem = getForm(name)
		return formItem.use { it.value }
	}
	
	fun getFormOrNull(name: String): PartData.FormItem? {
		return partDataMap[name] as? PartData.FormItem
	}
	
	suspend fun getFormValueOrNull(name: String): String? {
		val formItem = getFormOrNull(name) ?: return null
		return formItem.use { it.value }
	}
	
	fun getFile(name: String): PartData.FileItem {
		return partDataMap[name] as? PartData.FileItem ?: error("Not Found FileItem: $name.")
	}
	
	suspend fun getFileByteArray(name: String): ByteArray {
		val fileItem = getFile(name)
		return fileItem.use { it.provider().readBuffer().readByteArray() }
	}
	
	fun getFileOrNull(name: String): PartData.FileItem? {
		return partDataMap[name] as? PartData.FileItem
	}
	
	suspend fun getFileByteArrayOrNull(name: String): ByteArray? {
		val fileItem = getFileOrNull(name) ?: return null
		return fileItem.use { it.provider().readBuffer().readByteArray() }
	}
	
	fun getBinary(name: String): PartData.BinaryItem {
		return partDataMap[name] as? PartData.BinaryItem ?: error("Not Found BinaryItem: $name.")
	}
	
	suspend fun getBinaryByteArray(name: String): ByteArray {
		val binaryItem = getBinary(name)
		return binaryItem.use { it.provider().readByteArray() }
	}
	
	fun getBinaryOrNull(name: String): PartData.BinaryItem? {
		return partDataMap[name] as? PartData.BinaryItem
	}
	
	suspend fun getBinaryByteArrayOrNull(name: String): ByteArray? {
		val binaryItem = getBinaryOrNull(name) ?: return null
		return binaryItem.use { it.provider().readByteArray() }
	}
	
	fun getBinaryChannel(name: String): PartData.BinaryChannelItem {
		return partDataMap[name] as? PartData.BinaryChannelItem ?: error("Not Found BinaryChannelItem: $name.")
	}
	
	fun getBinaryChannelOrNull(name: String): PartData.BinaryChannelItem? {
		return partDataMap[name] as? PartData.BinaryChannelItem
	}
	
	fun disposeAll() {
		nonDisposeNames.forEach {
			partDataMap[it]!!.dispose()
		}
		nonDisposeNames.clear()
	}
	
	private suspend fun <T : PartData, R> T.use(block: suspend (T) -> R): R {
		return block(this).also {
			this.dispose()
			nonDisposeNames -= this.name!!
		}
	}
}
package cn.ktorfitx.common.ksp.util.io

import java.io.File

fun File.deleteDirectory() {
	if (!this.exists()) return
	if (this.isDirectory) {
		this.listFiles()?.forEach { child ->
			child.deleteDirectory()
		}
	}
	this.delete()
}
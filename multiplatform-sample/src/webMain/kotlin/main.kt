package com.example.demo

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import cn.ktorfitx.multiplatform.sample.App

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
	ComposeViewport {
		App()
	}
}
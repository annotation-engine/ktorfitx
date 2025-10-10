# KtorfitX 3.3.1-3.2.4

[![Maven](https://img.shields.io/badge/Maven-Central-download.svg)](https://central.sonatype.com/search?q=cn.ktorfitx:multiplatform-core)
[![PRs Welcome](https://img.shields.io/badge/PRs-welcome-brightgreen.svg)](http://annotation-engine.github.io/ktorfitx-document/index_md.html)
[![License](https://img.shields.io/badge/Apache-2.0-brightgreen.svg)](https://github.com/annotation-engine/ktorfitx/blob/master/LICENSE-2.0)

## 项目简介

Kotlin Multiplatform 平台是为了实现类似 RESTful 风格的网络请求接口定义，使用代码生成实现类

Ktor Server 是为了自动生成路由层代码，自动管理路由代码，并可以通过注解获取各种类型参数

## 官方文档

http://annotation-engine.github.io/ktorfitx-document/start.html

建设中...

## 版本说明

Kotlin `2.2.20`

Ktor `3.3.1`

KSP `2.2.20-2.0.4`

## 支持平台

### Kotlin Multiplatform

支持的源集：

- commonMain, nativeMain, appleMain
- androidMain
- androidNativeMain, androidNativeArm32Main, androidNativeArm64Main, androidNativeX86Main, androidNativeX64Main
- desktopMain
- iosMain, iosArm64Main, iosSimulatorArm64Main, iosX64Main
- watchosMain, watchosArm32Main, watchosArm64Main, watchosSimulatorArm64Main, watchosSimulatorDeviceArm64Main,
  watchosX64Main
- tvosMain, tvosArm64Main, tvosSimulatorArm64Main, tvosX64Main
- linuxMain, linuxArm32HfpMain, linuxArm64Main, linuxX64Main
- mingwMain, mingwX64Main
- macosMain, macosArm64Main, macosX64Main
- webMain, jsMain, wasmJsMain

### Ktor Server

依赖说明：

请使用和 ktorfitx 相同版本的 ktor 版本，以保证他们的最佳兼容性

### 全部依赖

- Kotlin Multiplatform
    - cn.ktorfitx:multiplatform-core
    - cn.ktorfitx:multiplatform-annotation
    - cn.ktorfitx:multiplatform-websockets
    - cn.ktorfitx:multiplatform-mock
    - cn.ktorfitx:multiplatform-ksp
    - cn.ktorfitx:multiplatform-gradle-plugin
    - cn.ktorfitx:android-gradle-plugin (Android Only)

- Ktor Server
    - cn.ktorfitx:server-core
    - cn.ktorfitx:server-annotation
    - cn.ktorfitx:server-websockets
    - cn.ktorfitx:server-auth
    - cn.ktorfitx:server-ksp
    - cn.ktorfitx:server-gradle-plugin

- Common
    - cn.ktorfitx:common-ksp-util

## 注解介绍

### Kotlin Multiplatform

#### 注解

- `@HttpMethod` 自定义 HttpMethod

#### 接口

- `@Api` 定义接口

#### 方法

- `@GET` GET 请求
- `@POST` POST 请求
- `@PUT` PUT 请求
- `@DELETE` DELETE 请求
- `@PATCH` PATCH 请求
- `@OPTIONS` OPTIONS 请求
- `@HEAD` HEAD 请求
- `@BearerAuth` 启用授权
- `@Headers` 多个请求头
- `@Mock` 定义 Mock
- `@WebSocket` WebSocket
- `@Timeout` 超时时间
- `@Prepare` 预创建

#### 参数

- `@Body` 请求体
- `@Query` 请求参数
- `@Field` x-www-form-urlencoded 字段
- `@Part` form-data 字段
- `@Header` 动态请求头
- `@Path` path 参数
- `@Cookie` cookie 参数
- `@Attribute` attribute 参数
- `@DynamicUrl` 动态 url 参数
- `@Queries` 动态请求参数
- `@Fields` 动态 x-www-form-urlencoded 字段
- `@Parts` 动态 form-data 字段
- `@Attributes` 动态 attribute 参数

### Ktor Server

#### 注解

- `@HttpMethod` 自定义 HttpMethod

#### 类

- `@Controller` 控制器

#### 方法

- `@GET` GET 请求
- `@POST` POST 请求
- `@PUT` PUT 请求
- `@DELETE` DELETE 请求
- `@PATCH` PATCH 请求
- `@OPTIONS` OPTIONS 请求
- `@HEAD` HEAD 请求
- `@Authentication` 路由授权
- `@WebSocket` WebSocket
- `@WebSocketRaw` WebSocketRaw
- `@Regex` 正则匹配 path
- `@Timeout` 超时时间

#### 参数

- `@Query` 查询参数
- `@Body` 请求体参数
- `@Field` x-www-form-urlencoded 字段
- `@PartForm` form-data 参数
- `@PartFile` form-data 文件
- `@PartBinary` form-data 二进制参数
- `@PartBinaryChannel` form-data 数据流
- `@Path` path 参数，支持正则表达式
- `@Header` 请求头参数
- `@Attribute` attribute 参数
- `@Cookie` cookie 参数

## Gradle 配置

- 在模块级 build.gradle.kts 中配置

### Android

- 请在 Android 模块中的 build.gradle.kts 配置一下内容，请按照实际情况编写
- 注意：此处不包含 ktor 的依赖，请自行添加

```kotlin
plugins {
	// 省略其他...
	// 在这里使用 Gradle 插件
	id("cn.ktorfitx.android") version "<latest>"
}

ktorfitx {
	// 将所有提示文本改为中文，默认：ENGLISH，支持：ENGLISH, CHINESE
	language = KtorfitxLanguage.CHINESE
	
	websockets {
		enabled = true  // 启用 WebSockets 功能，默认关闭
	}
	
	mock {
		enabled = true  // 启用 Mock 功能，默认关闭
	}
}
```

### Kotlin Multiplatform

- 请在 Kotlin Multiplatform 模块中的 build.gradle.kts 配置一下内容，请按照实际情况编写
- 注意：此处不包含 ktor 的依赖，请自行添加

```kotlin
plugins {
	// 省略其他...
	// 在这里使用 Gradle 插件
	id("cn.ktorfitx.multiplatform") version "<latest>"
}

ktorfitx {
	// 将所有提示文本改为中文，默认：ENGLISH，支持：ENGLISH, CHINESE
	language = KtorfitxLanguage.CHINESE
	
	websockets {
		enabled = true  // 启用 WebSockets 功能，默认关闭
	}
	
	mock {
		enabled = true  // 启用 Mock 功能，默认关闭
	}
}
```

### Ktor Server

```kotlin
plugins {
	// 省略其他...
	// 在这里使用 Gradle 插件
	id("cn.ktorfitx.server") version "<latest>"
}

ktorfitx {
	// 将所有提示文本改为中文，默认：ENGLISH，支持：ENGLISH, CHINESE
	language = KtorfitxLanguage.CHINESE
	
	websockets {
		enabled = true  // 启用 WebSockets 功能，默认关闭
	}
	
	auth {
		enabled = true  // 启用授权功能，默认关闭
	}
	
	generate {
		this.packageName = "<package name>" // 生成文件目录，默认：<package>.generated
		this.funName = "<function name>"    // 生成方法名，默认：generateRoutes
		this.fileName = "<filename>"        // 生成文件名，默认：GenerateRoutes，可以不加 .kt 后缀
	}
}
```

## 编译期错误检查

支持编译期错误检查，当您使用的方式不正确时，Ktorfitx 会在编译期提供错误检查，以帮助用户更快的定位错误
目前支持英文和中文两种语言，如果需要更多语言，请联系我

## 异常处理及返回类型

当返回值是 `Result<T>` 时，会自动处理异常，反之则需要自行处理异常逻辑
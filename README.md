# KtorfitX 3.2.3-3.1.0-Beta6

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

Kotlin `2.2.10`

Ktor `3.2.3`

KSP `2.2.10-2.0.2`

## 支持平台

### Kotlin Multiplatform

- android
- ios: x86, arm64, simulatorArm64
- watchos: x64, arm32, arm64, simulatorArm64, deviceArm64
- tvos: x64, arm64, simulatorArm64
- linux: x64, arm64
- window: mingwX64
- web: js, wasmJs

### Ktor Server

## 依赖说明

请使用和 ktorfitx 相同版本的 ktor 版本，以保证他们的最佳兼容性

### 全部依赖

- Kotlin Multiplatform
    - cn.ktorfitx:multiplatform-core
    - cn.ktorfitx:multiplatform-annotation
    - cn.ktorfitx:multiplatform-websockets
    - cn.ktorfitx:multiplatform-mock
    - cn.ktorfitx:multiplatform-ksp
    - cn.ktorfitx:multiplatform-gradle-plugin

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
- `@ApiScope` 接口作用域，用于控制扩展方法的泛型

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
- `@Regex` 正则匹配 path
- `@Timeout` 超时时间

#### 参数

- `@Attribute` attribute 参数
- `@Body` 请求体参数
- `@Cookie` cookie 参数
- `@Field` x-www-form-urlencoded 字段
- `@Header` 请求头参数
- `@PartForm` form-data 参数
- `@PartFile` form-data 文件
- `@PartBinary` form-data 二进制参数
- `@PartBinaryChannel` form-data 数据流
- `@Path` path 参数，支持正则表达式
- `@Query` 查询参数

## 迁移 从 2.x 迁移至 3.x

- 修改了依赖包名

请将依赖包改为 `cn.ktorfitx` 下的 GroupId，旧的 `cn.vividcode.multiplatform` GroupId 现在已弃用

- ktorfitx-api 模块拆分为 multiplatform-core 和 multiplatform-mock 模块

- 支持服务端

添加 Ktor Server 端支持，标记注解，符号处理器会自动生成对应的路由解析函数，包含参数解析授权等行为

## Gradle 配置

- 在模块级 build.gradle.kts 中配置

### Kotlin Multiplatform

- 请在 Kotlin Multiplatform 模块中的 build.gradle.kts 配置一下内容，请按照实际情况编写

```kotlin
plugins {
	// 省略其他...
	// 在这里使用 Gradle 插件
	id("cn.ktorfitx.multiplatform") version "<latest>"
}

ktorfitx {
	websockets {
		enabled = true  // 启用 WebSockets 功能，默认关闭
	}
	
	mock {
		enabled = true  // 启用 Mock 功能，默认关闭
	}
	
	ksp {
		kspCommonMainGeneratedDir = "<custom>"  // 自定义生成目录，默认："build/generated/ksp/metadata/commonMain/kotlin"
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
	websockets {
		enabled = true  // 启用 WebSockets 功能，默认关闭
	}
	
	auth {
		enabled = true  // 启用授权功能，默认关闭
	}
	
	generate {
		this.packageName = "<package name>" // 生成文件目录，默认：当前模块包 + .generated
		this.funName = "<function name>"    // 生成方法名，默认：generateRoutes
		this.fileName = "<filename>"        // 生成文件名，默认：GenerateRoutes，可以不加 .kt 后缀
	}
}
```

## 编译期错误检查

支持编译期错误检查，当您使用的方式不正确时，Ktorfitx 将会在编译期提供错误检查，
以帮助用户更快的定位错误

## 异常处理及返回类型

当返回值是 `Result<T>` 时，会自动处理异常，反之则需要自行处理异常逻辑
package cn.ktorfitx.multiplatform.sample.http.api

import cn.ktorfitx.multiplatform.annotation.Api
import cn.ktorfitx.multiplatform.annotation.ApiScope
import cn.ktorfitx.multiplatform.core.scope.DefaultApiScope
import cn.ktorfitx.multiplatform.sample.http.TestApiScope

@Api("apiScope")
@ApiScope(TestApiScope::class, DefaultApiScope::class)
interface ApiScopeApi
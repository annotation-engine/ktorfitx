package cn.ktorfitx.multiplatform.sample.http.mock

import cn.ktorfitx.multiplatform.mock.MockProvider
import cn.ktorfitx.multiplatform.sample.http.ApiResult

data object ApiResultMockProvider : MockProvider<ApiResult<Unit>> {
	
	override fun provide(): ApiResult<Unit> {
		throw IllegalStateException()
	}
}
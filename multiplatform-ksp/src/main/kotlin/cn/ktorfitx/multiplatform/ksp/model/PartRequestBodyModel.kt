package cn.ktorfitx.multiplatform.ksp.model

internal class PartRequestBodyModel(
	val partModels: List<PartModel>,
	val partsModels: List<PartsModel>
) : RequestBodyModel
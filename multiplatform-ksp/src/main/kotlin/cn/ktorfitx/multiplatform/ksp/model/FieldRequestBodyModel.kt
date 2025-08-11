package cn.ktorfitx.multiplatform.ksp.model

internal class FieldRequestBodyModel(
	val fieldModels: List<FieldModel>,
	val fieldsModels: List<FieldsModel>
) : RequestBodyModel
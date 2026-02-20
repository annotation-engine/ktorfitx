tasks.register("publishAllGradlePlugins") {
	group = "publishing"
	
	val projects = arrayOf(
        "common-gradle-plugin",
		"android-gradle-plugin",
		"multiplatform-gradle-plugin",
		"server-gradle-plugin",
	)
	val tasks = projects.map { ":$it:publishAllPublicationsToMavenCentralRepository" }.toTypedArray()
	dependsOn(*tasks)
}
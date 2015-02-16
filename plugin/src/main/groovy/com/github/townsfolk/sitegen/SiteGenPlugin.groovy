package com.github.townsfolk.sitegen

import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * @author elberry
 * Created: Sat Feb 14 14:46:07 PST 2015
 */
abstract class SiteGenPlugin implements Plugin<Project> {

	protected Project project

	@Override
	void apply(Project project) {
		this.project = project
		project.task("dist", distTask)
		project.task("init", initTask)
		project.task("render", renderTask)
	}

	abstract Closure getDistTask()

	abstract Closure getInitTask()

	abstract Closure getRenderTask()

	protected File createDirectory(String relativePath) {
		File dir = new File(relativePath)
		dir.exists() ?: dir.mkdirs() ?: {
			throw new GradleException("'$relativePath' directory didn't exist, and could not be created.")
		}
		return dir
	}

	protected File createFile(File directory, String relativePath) {
		File file = new File(directory, relativePath)
		file.exists() ?: (file.parentFile.exists() || file.parentFile.mkdirs()) && file.createNewFile() ?: {
			throw new GradleException("'${file}' didn't exist, and could not be created.")
		}
		return file
	}

	/**
	 * Creates a new layout file under the layouts directory.
	 * @param relativePath A file name, or path/file.
	 * @return Created File
	 */
	protected File createLayout(String relativePath) {
		createFile(layoutsDir, relativePath)
	}

	/**
	 * Creates a new page or file under the site directory.
	 * @param relativePath A File name, or path/file.
	 * @return Created File
	 */
	protected File createPage(String relativePath) {
		createFile(siteDir, relativePath)
	}

	protected File getLayoutsDir() {
		createDirectory("layouts")
	}

	protected File getRenderedDir() {
		createDirectory("${project.buildDir}/rendered")
	}

	protected File getSiteDir() {
		createDirectory("site")
	}
}
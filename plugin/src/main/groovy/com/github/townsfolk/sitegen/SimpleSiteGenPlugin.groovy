package com.github.townsfolk.sitegen

import groovy.text.SimpleTemplateEngine
import groovy.text.Template
import org.gradle.api.Project

/**
 * @author elberry
 * Created: Sat Feb 14 14:46:26 PST 2015
 */
class SimpleSiteGenPlugin extends SiteGenPlugin {

	@Override
	void apply(Project project) {
		super.apply(project)
		project.apply(plugin: 'groovy')
		project.dependencies {
			compile localGroovy()
		}
		project.sourceSets {
			main {
				groovy {
					srcDirs = ['layouts', 'site']
				}
			}
		}
	}

	@Override
	Closure getDistTask() {
		return {

		}
	}

	@Override
	Closure getInitTask() {
		return {

			outputs.files layoutsDir, siteDir

			doLast {
				project.logger.debug("Initializing Project")

				File mainLayout = createLayout("main.gsp")
				mainLayout.text ?: (mainLayout.text = '''
				<html>
					<head>
					  <title><sitemesh:write property='title'/></title>
					  <sitemesh:write property='head'/>
					</head>
					<body>
					  <sitemesh:write property='body'/>
					</body>
				</html>'''.stripIndent())

				File indexPage = createPage("index.gsp")
				indexPage.text ?: (indexPage.text = '''
				<html>
					<head>
					  <title>${title}</title>
					  <meta name='description' content='${description}'>
					</head>
					<body>
					  ${content}
					</body>
				</html>'''.stripIndent())

				File mainModel = createPage("model.groovy")
				mainModel.text ?: (mainModel.text = '''
				// Model scripts must return a map
				[
					title: "Hello World",
					description: "A simple page",
					content: "<p>Hello <strong>world</strong>!</p>"
				]'''.stripIndent())
			}
		}
	}

	@Override
	Closure getRenderTask() {
		return {

			inputs.files layoutsDir, siteDir
			//outputs.dir renderedDir

			doLast {
				def shell = new GroovyShell()
				def model = [:]
				project.logger.debug("Collecting site data model.")
				siteDir.eachFileRecurse { File file ->
					if (file.name.endsWith("model.groovy")) {
						project.logger.debug("Evaluating model file: ${file}")
						def _model = shell.evaluate(file)
						model.putAll(_model) // merge model with root
					}
				}

				SimpleTemplateEngine engine = new SimpleTemplateEngine()
				siteDir.eachFileRecurse { File file ->
					if (file.name.endsWith(".gsp")) {
						Template template = engine.createTemplate(file.newReader())
						def renderedText = template.make(model)
						println "Processing file: $file"
						println renderedText

						String cleanName = file.name - ".gsp"
						cleanName += ".html"
						String cleanPath = file.parentFile.path - siteDir.path

						File renderedFile = new File(cleanPath, cleanName)
						println "Rendered file: $renderedFile"

						renderedFile = new File(renderedDir, renderedFile.path)
						renderedFile.text = renderedText
					}
				}
			}
		}
	}
}
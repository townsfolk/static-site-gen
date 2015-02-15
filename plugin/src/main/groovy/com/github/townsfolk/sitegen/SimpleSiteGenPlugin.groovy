package com.github.townsfolk.sitegen

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
			outputs.dir renderedDir

			doLast {
			}
		}
	}
}
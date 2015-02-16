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
				mainLayout.text ?: (mainLayout.text = '''|<html>
				|   <head>
				|      <title><sitemesh:write property='title'/></title>
				|      <sitemesh:write property='head'/>
				|   </head>
				|   <body>
				|      <sitemesh:write property='body'/>
				|   </body>
				|</html>'''.stripMargin())

				File indexPage = createPage("index.html")
				indexPage.text ?: (indexPage.text = '''|<html>
				|   <head>
				|      <title>${title}</title>
				|      <meta name='description' content='${description}'>
				|   </head>
				|   <body>
				|      ${content}
				|   </body>
				|</html>'''.stripMargin())

				File mainModel = createPage("model.groovy")
				mainModel.text ?: (mainModel.text = '''|// Model scripts must return a map
				|[
				|	title: "Hello World",
				|	description: "A simple page",
				|	content: \'''
				|		<p>Hello <strong>world</strong>!</p>
				|		<a href="./products">${products.title}</a>
				|	\'''
				|]'''.stripMargin())

				File productsPage = createPage("/products/index.html")
				productsPage.text ?: (productsPage.text = '''|<html>
				|   <head>
				|      <title>${products.title}</title>
				|   </head>
				|   <body>
				|      <h1>${products.header}</h1>
				|      <ul>
				|         <% products.values.each { product -> %>
				|         <li><a href="./${product.name.toLowerCase()}/">${product.name}</a></li>
				|         <% } %>
				|      </ul>
				|   </body>
				|</html>'''.stripMargin())

				File productPageTemplate = createPage("/products/_products.html")
				productPageTemplate.text ?: (productPageTemplate.text = '''|<html>
				|   <head>
				|      <title>${productsItem.name}</title>
				|   </head>
				|   <body>
				|      <h1>${productsItem.name}</h1>
				|      <p>${productsItem.description}</p>
				|   </body>
				|</html>'''.stripMargin())

				File productsModel = createPage("/products/model.groovy")
				productsModel.text ?: (productsModel.text = '''|// Model scripts must return a map
				|[	products: [
				|	title: "Products",
				|	header: "Products List:",
				|	filenameResolver: { item -> "${item.name.toLowerCase()}/index.html" },
				|	values: [ // list item templates use values to render each file
				|		[name: "Painting", description: "Painting Description"],
				|		[name: "Lighting", description: "Lighting Description"],
				|		[name: "Plumbing", description: "Plumbing Description"]
				|	]
				|]]'''.stripMargin())


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

				project.logger.debug("Rendering templates.")
				SimpleTemplateEngine engine = new SimpleTemplateEngine()
				siteDir.eachFileRecurse { File file ->
					if (!file.directory && !file.name.endsWith("model.groovy")) {
						if (file.name.startsWith("_")) { // render item list template
							int dotIndex = file.name.lastIndexOf(".")
							String modelName = file.name.substring(1, dotIndex)
							println "Rendering list item template. Model Name: ${modelName}"
							def modelValues = model[modelName].values
							println "Rendering list item template: ${modelName}, item count: ${modelValues.size()}"
							Template template = engine.createTemplate(file.newReader())
							Closure filenameResolver = model[modelName].filenameResolver
							modelValues.each { item ->
								def _model = [:]
								_model.putAll(model)
								_model["${modelName}Item"] = item
								def renderedText = template.make(_model)

								String _filename = filenameResolver(item)
								File _file = new File(file.parentFile, _filename)
								println "Rendering file: ${_file}"
								println renderedText

								String cleanPath = _file.parentFile.path - siteDir.path

								File renderedFile = new File(cleanPath, _file.name)
								println "Rendered file: $renderedFile"

								renderedFile = createFile(renderedDir, renderedFile.path)
								renderedFile.text = renderedText
							}
						} else { // render single template
							Template template = engine.createTemplate(file.newReader())
							def renderedText = template.make(model)
							println "Rendering file: $file"
							println renderedText

							String cleanPath = file.parentFile.path - siteDir.path

							File renderedFile = new File(cleanPath, file.name)
							println "Rendered file: $renderedFile"

							renderedFile = createFile(renderedDir, renderedFile.path)
							renderedFile.text = renderedText
						}
					}
				}
			}
		}
	}
}
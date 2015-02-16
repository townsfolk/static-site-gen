// Model scripts must return a map
[	products: [
	title: "Products",
	header: "Products List:",
  filenameResolver: { item -> "${item.name.toLowerCase()}/index.html" },
	values: [ // list item templates use values to render each file
		[name: "Painting", description: "Painting Description"],
		[name: "Lighting", description: "Lighting Description"],
		[name: "Plumbing", description: "Plumbing Description"]
	]
]]
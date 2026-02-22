---
title: Components for Go Templates
date: 2026-01-17
reading: Coldheart Canyon (by Clive Barker)
---

A few years ago, I [wrote about an approach](/posts/2023/atomic-design/ ) to implementing an [atomic design library](https://bradfrost.com/blog/post/atomic-web-design/) using Go templates. Since then, I've had the opportunity the revise the approach somewhat, finding a better balance between expressiveness and ease of use. It all comes down to the ability to define a visual component in Go's template engine, that is, a reusable and parameterized HTML fragment. For example, a hero banner parameterized by a title, a subtitle, a description, possibly some action buttons and a breadcrumb, that sort of thing. 

We can already write components in Go's templating language:

    {{define "Banner"}}
    <div class="banner">
      <div class="title">{{.Title}}</div>
      <div class="subtitle">{{.Subtitle}}</div>
      <div class="description">{{.Description}}</div>
      <div class="button-bar">
        <button onclick="{{.ActionOnClick}}">{{.ActionLabel}}</button>
      </div>
    </div>
    {{end}}

This is vastly simplified, but you get the gist. The point being, there's a lot of structural stuff here, and if you build a website from templates, then most pages may get this kind of banner, and therefore would benefit from simply using the component (subtemplate) above, for example:

    <div class="screen">
      {{template "Banner" .BannerArgs}}
      <div class="content">
        ...
      </div>
    </div>

This assumes that the Go program that "executes" this template passes a struct value to the template with a field `BannerArgs` containing the struct with the banner arguments, namely something like:

    struct {
      Title string
      Subtitle string
      Description string
      ActionOnClick string
      ActionLabel string
    }

The above works fine, and there are many variations available. For instance, you can have the banner arguments in the top level struct value passed to the template, and use `{{template "Banner" .}}`, etc. 

There are times, however, where the above solution is inelegant. In particular, the fact that all arguments to the banner need to come from the Go program executing the template means that even choices that are internal to the template need to be surfaced to the level of the Go program. That's because Go's templating engine doesn't allow you to create a struct value _on the fly_ within a template to pass to a component. Again, not a problem _per se_ — just inelegant. But there is a solution.

The solution is two-fold: implement components in such a way that they use a _map_ from strings to values instead of a struct, and provide a way to construct such a map _on the fly_ within a template. 

Implementing components to use a map actually requires zero changes, since accessing fields of a struct and indexing a map uses the same syntax in a Go template, as long as the map indices don't use special characters. In those situations, instead of `{{.Foo}}`, we can use `{{index . "Foo"}}`

Creating a map _on the fly_ within a template can be done with a _template function_. I'm calling this function `props` because it mimics how a React component gets instantiated by passing values for its props (using the form `name={value}`):

    func templateProps(props ...any) (map[string]interface{}, error) {
    	if len(props) % 2 != 0 {
    		return nil, errors.New("invalid props call")
    	}
    	dict := make(map[string]any, len(props) / 2)
    	for i := 0; i < len(props); i += 2 {
    		key, ok :=props[i].(string)
    		if !ok {
    			return nil, errors.New("props keys must be strings")
    		}
    		dict[key] = props[i+1]
    	}
    	return dict, nil
    }

Once this function is made it available to the template (see below), we can invoke it when instantiating a component as follows:

    {{template "Banner" props
        "Title" "My Fantastic New Page" 
        "Subtitle" "It'll blow your mind" 
        "Description" "(Insert witty description here)" 
        "ActionOnClick" "navigate()" 
        "ActionLabel" "Go to overview"}}
    
Note the alternation between prop name and prop value. This invokes component `Banner` with a map that sends `Title` to `My Fantastic New Page`, `Subtitle` to `It'll blow your mind`, etc. Basically, every alternation of prop name and prop value after the `props` function call gets used to create the map passed to the component.

To use this function, you need register it with the template before executing it. Personally, I tend to put all my project components in a single template file `components.gohtml`, and then use a function `createTemplate` that takes a source template file, adds the components library file, and registers the `templateProps` function:
    
    func createTemplate(page string) (*template.Template, error) {
    	tmpl := template.New("components.gohtml").Funcs(template.FuncMap{
    		"props": templateProps,
    	})
    	return tmpl.ParseFiles("templates/components.gohtml", page)
    }
    
I use this function in the most straightforward way to create a template before executing it:

	tmpl, err := createTemplate("templates/test.gohtml")
	if err != nil {
		log.Println(err)
		http.Error(w, err.Error(), http.StatusInternalServerError)
		return
	}
	args := &struct {
        SomeTemplateArg string
	}{
        "Hello world"
	}
	err = tmpl.Execute(w, args)
	if err != nil {
		log.Println(err)
		return
	}

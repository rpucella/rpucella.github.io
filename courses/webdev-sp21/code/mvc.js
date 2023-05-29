function elt(id) {
    // useful shortcut
    return document.getElementById(id)
}


class Model {
    constructor() {
        this.current = null
        this.pictures = []
	// list of functions to call when picture changes
        this.changedPictureSubscribers = []
	// list of functions to call when picture is added
        this.addedPictureSubscribers = []
    }

    // helper methods
    
    getPicture() {
        return this.pictures[this.current]
    }

    // subscription methods
    
    subAddedPicture(f) {
        this.addedPictureSubscribers.push(f)
    }
    
    subChangedPicture(f) {
        this.changedPictureSubscribers.push(f)
    }

    // actions
    
    changePicture(v) {
        this.current = v
	// go through every function in the list changedPictureSubscribers
	// and call each function with the index of the current picture
        this.changedPictureSubscribers.forEach((f) => f(v))
    }

    addPicture(pict) {
        this.pictures.push(pict)
        const idx = this.pictures.length - 1
        this.addedPictureSubscribers.forEach((f) => f(idx, pict))
        this.changePicture(idx)
    }
}


class SelectionController {
    constructor(m) {
        this.model = m
        this.eltSelect = elt('picture-select')
        this.eltSelect.addEventListener('change', () => this.selectionChange())
        m.subAddedPicture((idx, pict) => this.handleAddedPicture(idx, pict))
        m.subChangedPicture((v) => this.handleChangedPicture(v))
    }
    
    selectionChange() {
	const value = this.eltSelect.value
	this.model.changePicture(+value)
    }

    handleChangedPicture(v) {
        this.eltSelect.value = v
    }

    handleAddedPicture(index, pict) { 
	const newOption = document.createElement('option')
	const newValue = index
	newOption.setAttribute('value', index)
	newOption.innerText = pict.name
	this.eltSelect.appendChild(newOption)
    }
    
}


class NewPictureController {
    constructor(m) {
        this.model = m
        this.eltButton = elt('create-button')
        this.eltButton.addEventListener('click', () => this.newPicture())
        this.eltNewName = elt('new-name')
        this.eltNewUrl = elt('new-url')
    }
    
    getPictureInfo() {
	const name = this.eltNewName.value
	const url = this.eltNewUrl.value
	if (!name || !url) {
	    return false
	}
	return { name: name, url: url, added: Date() }
    }

    clearInputs() {
	this.eltNewName.value = ''
	this.eltNewUrl.value = ''
    }    

    newPicture() {
        const pict = this.getPictureInfo()
        if (pict) {
            this.model.addPicture(pict)
	    this.clearInputs()
        }
    }
}


class ImageView {
    constructor(m) {
	this.model = m
	this.eltName = elt('name')
	this.eltUrl = elt('url')
	m.subChangedPicture(() => this.showPicture())
    }

    showPicture() {
	const pict = this.model.getPicture()
	this.eltName.innerText = pict.name
	this.eltUrl.setAttribute('src', pict.url)
    }
}


class TimeAddedView {
    constructor(m) {
        this.model = m
        this.eltAdded = elt('added')
	this.eltUrl = elt('url')
        m.subChangedPicture(() => this.handleChangedPicture())
        this.hideAdded()
        this.eltUrl.addEventListener('mouseover', () => this.showAdded())
        this.eltUrl.addEventListener('mouseout', () => this.hideAdded())
    }

    changeColorAdded(color) {
	this.eltAdded.style.color = color
    }

    hideAdded() { 
	this.changeColorAdded('white')
    }
    
    showAdded() {
	this.changeColorAdded('black')
    }

    handleChangedPicture() {
        const pict = this.model.getPicture()
	this.eltAdded.innerText = 'Added: ' + pict.added
    }
}


class ThumbnailView {
    constructor(m) {
	this.model = m
	this.eltThumbnails = elt('thumbnails')
	m.subAddedPicture((idx, pict) => this.handleAddedPicture(idx, pict))
    }

    handleAddedPicture(idx, pict) {
	const thumb = document.createElement('img')
	thumb.style.height = '1in'
	thumb.style.width = '1in'
	thumb.style.objectFit = 'contain'
	thumb.setAttribute('src', pict.url)
	this.eltThumbnails.appendChild(thumb)
    }
}


function init() {
    const model = new Model()
    const selectC = new SelectionController(model)
    const newPicC = new NewPictureController(model)
    const imageV = new ImageView(model)
    const addedV = new TimeAddedView(model)
    const ThumbnailV = new ThumbnailView(model)
    // initialization
    model.addPicture({
        name: 'Cat',
        url: 'cat.png',
        added: 'built-in'
    })
    model.addPicture({
        name: 'Dog',
        url: 'dog.jpg',
        added: 'built-in'
    })
    model.changePicture(0)
}

// put all the initialization code in a function called
// when the document finishes loading
// (you're sure all elements have been created before your code kicks in)

window.addEventListener('load', init)

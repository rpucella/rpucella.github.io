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
    this.changedPictureSubscribers.forEach((f) => f(v, this.pictures[v]))
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
    this.eltSelect.addEventListener('change', () => this.handleSelectionChange())
    m.subAddedPicture((idx, pict) => this.handleAddedPicture(idx, pict))
    m.subChangedPicture((v) => this.handleChangedPicture(v))
  }

  handleSelectionChange() {
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


class ImageView {
  constructor(m) {
    this.model = m
    this.eltName = elt('name')
    this.eltUrl = elt('url')
    m.subChangedPicture((idx, pict) => this.handleChangedPicture(idx, pict))
  }

  handleChangedPicture(idx, pict) {
    this.eltName.innerText = pict.name
    this.eltUrl.setAttribute('src', pict.url)
  }
}


class NewPictureController {
    constructor(m) {
        this.model = m
        this.eltButton = elt('create-button')
        this.eltButton.addEventListener('click', () => this.handleClick())
        this.eltNewName = elt('new-name')
        this.eltNewUrl = elt('new-url')
    }

    _getPictureInfo() {
	const name = this.eltNewName.value
	const url = this.eltNewUrl.value
	if (!name || !url) {
	    return false
	}
        return { name: name, url: url }
    }

    _clearInputs() {
	this.eltNewName.value = ''
	this.eltNewUrl.value = ''
    }

    handleClick() {
        const pict = this._getPictureInfo()
        if (pict) {
            this.model.addPicture(pict)
	    this._clearInputs()
        }
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
    thumb.setAttribute('src', pict.url)
    thumb.classList.add('thumbnail')
    this.eltThumbnails.appendChild(thumb)
  }
}


function init() {
  const model = new Model()
  const selectC = new SelectionController(model)
  const newPicC = new NewPictureController(model)
  const imageV = new ImageView(model)
  const ThumbnailV = new ThumbnailView(model)
  // initialization
  model.addPicture({
    name: 'Cat',
    url: 'images/cat.png'
  })
  model.addPicture({
    name: 'Dog',
    url: 'images/dog.jpg'
  })
  model.changePicture(0)
}

// put all the initialization code in a function called
// when the document finishes loading
// (you're sure all elements have been created before your code kicks in)

window.addEventListener('load', init)
// alternatively:
// window.onload = init


const useState = React.useState
const e = React.createElement

window.onload = () => {
    const domContainer = document.querySelector('#app')
    const root = ReactDOM.createRoot(domContainer)
    root.render(e(App, {}))
}

const initialPics = [
  {
    name: 'Cat',
    url: 'images/cat.png'
  },
  {
    name: 'Dog',
    url: 'images/dog.jpg'
  }
]


function App() {
  // State
  const [pics, setPics] = useState(initialPics)
  const [current, setCurrent] = useState(0)
  // Helper function to add a new picture to the state
  const addPic = (name, url) => {
    const newPics = [...pics, {name: name, url:url}]
    setPics(newPics)
    setCurrent(newPics.length - 1)
  }
  return e('div', {},
	   e('h1', {className: 'title'}, 'React Demo'),
	   e('div', {className: 'layout'},
	     e('div', {className: 'left-column'},
	       e(Selection, {pics, current, setCurrent}),
	       e(NewPicture, {addPic})),
	     e('div', {className: 'right-column'},
	       e(Image, {pic: pics[current]}),
               e(Thumbnails, {pics, current, setCurrent}))))
}


function Selection({pics, current, setCurrent}) {
  const handleChange = (evt) => {
    // The + is to convert the value (which is a string)
    // to a number.
    setCurrent(+evt.target.value)
  }
  const mkOption = (pic, i) => {
    return e('option', {'value': i, 'key': i}, pic.name)
  }
  return e('div', {className: 'boxed'},
	   e('div', {className: 'control'},
	     e('label', {}, 'Select picture:'),
	     e('select', {value: current, onChange: handleChange},
	       pics.map(mkOption))))
}


function Image({pic}) {
  return e('div', {className: 'image-view'},
	   e('img', {src: pic.url}),
	   e('h2', {}, pic.name))
}


function NewPicture({addPic}) {
  const [name, setName] = useState('')
  const [url, setUrl] = useState('')
  const handleClick = (evt) => {
    if (name && url) {
      addPic(name, url)
      setName('')
      setUrl('')
    }
  }
  const handleChangeName = (evt) => {
    setName(evt.target.value)

  }
  const handleChangeUrl = (evt) => {
    setUrl(evt.target.value)
  }
  return e('div', {className: 'boxed'},
	   e('div', {className: 'control'},
	     e('label', {}, 'Name:'),
	     e('input', {type: 'text', value: name,
			 onChange: handleChangeName})),
	   e('div', {className: 'control'},
	     e('label', {}, 'URL:'),
	     e('input', {type: 'text', value: url,
			 onChange: handleChangeUrl})),
	   e('button', {onClick: handleClick}, 'Add picture'))
}



function Thumbnails({pics, current, setCurrent}) {
  const mkThumbnail = (pic, i) => {
    return e(Thumbnail, {key: i, index: i, pic, current, setCurrent})
  }
  return e('div', {className: 'thumbnails'}, pics.map(mkThumbnail))
}


function Thumbnail({pic, index, current, setCurrent}) {
  console.log(index, pic.name, current)
  const className = index === current ? 'highlight thumbnail' : 'thumbnail'
  const handleClick = () => {
    setCurrent(index)
  }
  return e('img', {src: pic.url, className, onClick: handleClick})
}

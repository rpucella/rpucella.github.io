import './App.css'
import {useState} from 'react'
import Selection from './Selection'
import NewPicture from './NewPicture'
import Image from './Image'
import Thumbnails from './Thumbnails'

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
  console.log('Rendering App')
  const addPic = (name, url) => {
    const newPics = [...pics, {name: name, url:url}]
    setPics(newPics)
    setCurrent(newPics.length - 1)
  }
  return (
    <div>
      <h1 className="title">React Demo</h1>
      <div className="layout">
        <div className="left-column">
          <Selection pics={pics} current={current} setCurrent={setCurrent} />
          <NewPicture addPic={addPic} />
        </div>
        <div className="right-column">
          <Image pic={pics[current]} />
          <Thumbnails pics={pics} current={current} setCurrent={setCurrent} />
        </div>
        </div>
    </div>
  )
}

export default App

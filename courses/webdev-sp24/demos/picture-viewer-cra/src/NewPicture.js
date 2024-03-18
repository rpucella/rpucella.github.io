import {useState} from 'react'

function NewPicture({addPic}) {
  const [name, setName] = useState('')
  const [url, setUrl] = useState('')
  console.log('Rendering NewPicture')
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
  return (
    <div className="boxed">
      <div className="control">
        <label>Name:</label>
        <input type="text" value={name} onChange={handleChangeName} />
      </div>
      <div className="control">
        <label>URL:</label>
        <input type="text" value={url} onChange={handleChangeUrl} />
      </div>
      <button onClick={handleClick}>Add picture</button>
    </div>
  )
}

export default NewPicture

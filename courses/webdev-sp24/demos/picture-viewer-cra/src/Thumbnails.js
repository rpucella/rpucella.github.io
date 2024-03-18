
function Thumbnails({pics, current, setCurrent}) {
  console.log('Rendering Thumbnails')
  const mkThumbnail = (pic, i) => {
      return <Thumbnail key={i} index={i} pic={pic} current={current} setCurrent={setCurrent} />
  }
  return (
      <div className="thumbnails">
        {pics.map(mkThumbnail)}
      </div>
  )
}

function Thumbnail({pic, index, current, setCurrent}) {
  console.log('Rendering Thumbnail')
  const className = index === current ? 'highlight thumbnail' : 'thumbnail'
  const handleClick = () => {
    setCurrent(index)
  }
  return (
    <img src={pic.url} className={className} onClick={handleClick} />
  )
}

export default Thumbnails


function Image({pic}) {
  console.log('Rendering Image')
  return (
    <div className="image-view">
      <img src={pic.url} />
      <h2>{pic.name}</h2>
    </div>
  )
}

export default Image

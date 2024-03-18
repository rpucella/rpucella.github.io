
function Selection({pics, current, setCurrent}) {
  console.log('Rendering Selection')
  const handleChange = (evt) => {
    // The + is to convert the value (which is a string)
    // to a number.
    setCurrent(+evt.target.value)
  }
  const mkOption = (pic, i) => {
    return <option value={i} key={i}>{pic.name}</option>
  }
  return (
    <div className="boxed">
      <div className="control">
        <label>Select picture:</label>
        <select value={current} onChange={handleChange}>
          { pics.map(mkOption) }
        </select>
      </div>
    </div>
  )
}

export default Selection

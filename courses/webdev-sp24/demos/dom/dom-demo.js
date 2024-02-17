
function createBox(colorClass) {
    const boxes = document.getElementById('boxes')
    const box = document.createElement('div')
    box.classList.add('basic-box')
    if (colorClass) {
        box.classList.add(colorClass)
    }
    box.onmouseover = showText
    box.onmouseout = hideText
    box.onclick = (evt) => {
        hideText(evt)
        evt.target.remove()
    }
    boxes.appendChild(box)
}

function createRandomBox() {
    const v = Math.random()
    let color;
    if (v < 0.33) {
        color = 'red'
    } else if (v < 0.66) {
        color = 'blue'
    } else {
        color = 'green'
    }
    createBox(color)
}

function clearBoxes() {
    const boxes = document.getElementById('boxes')
    for (let i = boxes.childNodes.length - 1; i >= 0; i--) {
        boxes.childNodes[i].remove()
    }
}

function showText(evt) {
    // evt.target is the node the event is trigger on
    const p = document.getElementById('text')
    let color = ''
    if (evt.target.classList.contains('red')) {
        color = 'red'
    } else if (evt.target.classList.contains('blue')) {
        color = 'blue'
    } else if (evt.target.classList.contains('green')) {
        color = 'green'
    } else {
        color = 'of unknown color'
    }
    p.innerText = `This box is ${color}\n\nClick to remove it`
    p.classList.add('show')
}

function hideText(evt) {
    const p = document.getElementById('text')
    p.classList.remove('show')
}

window.onload = () => {
    console.log('Initializing')
    const cs = document.getElementById('color-selector')
   // cs.onchange = (evt) => {
   //     console.log(evt.target.value)
   // }
    const cr = document.getElementById('create')
    cr.onclick = () => {
        if (cs.value === 'random') {
            createRandomBox()
        } else {
            createBox(cs.value)
        }
    }
}

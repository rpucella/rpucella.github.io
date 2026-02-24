
window.addEventListener('load', run)

const CONTENT = []

const VIEW = {
    startx: 0,
    starty: 0,
    zoom: 1
}

function run() {
    const canvas = document.getElementById('canvas')
    canvas.width = canvas.clientWidth
    canvas.height = canvas.clientHeight
    canvas.addEventListener('click', canvasClickHandler)
    document.getElementById('btn-up').addEventListener('click', btnUpHandler)
    document.getElementById('btn-down').addEventListener('click', btnDownHandler)
    document.getElementById('btn-left').addEventListener('click', btnLeftHandler)
    document.getElementById('btn-right').addEventListener('click', btnRightHandler)
    document.getElementById('btn-in').addEventListener('click', btnInHandler)
    document.getElementById('btn-out').addEventListener('click', btnOutHandler)
}

function getCanvas() {
    return document.getElementById('canvas')
}

function getCanvasSize() {
    const canvas = getCanvas()
    return [canvas.width, canvas.height]
}

function getCanvasOffset() {
    const canvas = getCanvas()
    return [canvas.offsetLeft, canvas.offsetTop]
}

function center(view, {x, y}) {
    // return start x, y that puts virtual x, y in the center of the canvas.
    const [width, height] = getCanvasSize()
    const start_x = x - (width / 2) / view.zoom
    const start_y = y - (height / 2) / view.zoom
    return {x: start_x, y: start_y}
}

function toCanvas(view, {x, y}) {
    // Convert a virtual x, y to physical position within the canvas.
    const nX = (x - view.startx) * view.zoom
    const nY = (y - view.starty) * view.zoom
    return {x: nX, y: nY}
}

function fromCanvas(view, {x, y}) {
    // Convert an x, y WITHIN the canvas to virtual coordinates.
    const nX = (x / view.zoom) + view.startx
    const nY = (y / view.zoom) + view.starty
    return {x: nX, y: nY}
}

function render(content, view) {
    const canvas = getCanvas()
    const ctx = canvas.getContext('2d')
    ctx.clearRect(0, 0, canvas.width, canvas.height)
    const size = 30 * view.zoom
    ctx.fillStyle = 'red'
    for (const r of content) {
        p = toCanvas(view, r)
        ctx.fillRect(p.x - size / 2, p.y - size / 2, size, size)
    }
    document.getElementById('pos-x').innerText = `X ${Math.round(view.startx)}`
    document.getElementById('pos-y').innerText = `Y ${Math.round(view.starty)}`
    document.getElementById('zoom').innerText = `Zoom ${Math.round(view.zoom * 100)}%`
}


// Event handlers.

function canvasClickHandler(evt) {
    const [eX, eY] = getCanvasOffset()
    const vp = fromCanvas(VIEW, {x: evt.clientX - eX, y: evt.clientY - eY})
    CONTENT.push(vp)
    render(CONTENT, VIEW)
}

function btnUpHandler(evt) {
    VIEW.starty = VIEW.starty + (50 / VIEW.zoom)
    render(CONTENT, VIEW)
}

function btnDownHandler(evt) {
    VIEW.starty = VIEW.starty - (50 / VIEW.zoom)
    render(CONTENT, VIEW)
}

function btnLeftHandler(evt) {
    VIEW.startx = VIEW.startx + (50 / VIEW.zoom)
    render(CONTENT, VIEW)
}

function btnRightHandler(evt) {
    VIEW.startx = VIEW.startx - (50 / VIEW.zoom)
    render(CONTENT, VIEW)
}

function btnInHandler(evt) {
    const [width, height] = getCanvasSize()
    const old_c = fromCanvas(VIEW, {x: width / 2, y: height / 2})
    VIEW.zoom = VIEW.zoom * 1.25
    const new_start = center(VIEW, old_c)
    VIEW.startx = new_start.x
    VIEW.starty = new_start.y
    render(CONTENT, VIEW)
}

function btnOutHandler(evt) {
    const [width, height] = getCanvasSize()
    const old_c = fromCanvas(VIEW, {x: width / 2, y: height / 2})
    VIEW.zoom = VIEW.zoom / 1.25
    const new_start = center(VIEW, old_c)
    VIEW.startx = new_start.x
    VIEW.starty = new_start.y
    render(CONTENT, VIEW)
}

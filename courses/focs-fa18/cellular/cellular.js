

window.addEventListener("load",run);

var WIDTH = 100;
var HEIGHT = 100;

function appendElement(target, tag) {
    var el = document.createElementNS("http://www.w3.org/2000/svg",tag);
    target.appendChild(el);
    return el;
}

function e (id) {
    return document.getElementById(id);
}


function initializeGrid () {
    var grid = new Array(WIDTH * HEIGHT);
    for (var i=0; i<HEIGHT*WIDTH; i++) { 
	    grid[i] = false;
    }
    _grid = grid;
}

function initializePattern (p) {
    if (_grid) { 
        for (var i=0; i<HEIGHT*WIDTH; i++) { 
	        _grid[i] = false;
        }
        pattern = JSON.parse(p);
        pattern.forEach(function(p) { 
	        _grid[p[1]*WIDTH+p[0]] = true;
	    });
    }
}

function showGrid () {
    for (var i=0; i<HEIGHT*WIDTH; i++) { 
	    if (_grid[i]) { 
	        _screen[i].setAttributeNS(null,"class","show-cell");
	    } else { 
	        _screen[i].setAttributeNS(null,"class","hide-cell");
	    }
    }
}


function liveNeighbors (pos) {

    var j = Math.floor(pos / WIDTH);
    var i = pos - j*WIDTH;
    var g = _grid;
    
    return (i > 0 && j > 0 && g[pos-WIDTH-1] ? 1 : 0)
	    + ( j > 0 && g[pos-WIDTH] ? 1 : 0)
	    + ( i < WIDTH-1 && j > 0 && g[pos-WIDTH+1] ? 1 : 0)
	    + ( i > 0 && g[pos-1] ? 1 : 0)
	    + ( i < WIDTH-1 && g[pos+1] ? 1 : 0)
	    + ( i > 0 && j < HEIGHT-1 && g[pos+WIDTH-1] ? 1 : 0)
	    + ( j < HEIGHT-1 && g[pos+WIDTH] ? 1 : 0)
	    + ( i < WIDTH-1 && j < HEIGHT-1 & g[pos+WIDTH+1] ? 1 : 0);
}


function nextGeneration () {

    var g = _grid;
    
    var ng = new Array(WIDTH*HEIGHT);
    var n = 0, p=0;
    for (var p=0; p<WIDTH*HEIGHT; p++) {
	    n = liveNeighbors(p);
	    if (g[p]) {
	        if (n >= 2 && n <= 3) {
		        ng[p] = true;
	        } else {
		        ng[p] = false;
	        }
	    } else {
	        if (n == 3 ) {
		        ng[p] = true;
	        } else {
		        ng[p] = false;
	        }
	    }
    }
    return ng;
}


function run () {

    var grid = e("grid");
    
    var screen = new Array(WIDTH*HEIGHT);
    for (var i=0; i<WIDTH; i++) { 
	    for (var j=0; j<HEIGHT; j++) { 
	        var el = appendElement(grid,"rect");
	        screen[j*WIDTH+i] = el;
	        el.setAttributeNS(null,"class","hide-cell");
	        el.setAttributeNS(null,"x",i*_size);
	        el.setAttributeNS(null,"y",j*_size);
	        el.setAttributeNS(null,"width",_size);
	        el.setAttributeNS(null,"height",_size);
	        el.setAttributeNS(null,"stroke","white");
	    }
    }

    _screen = screen;
    initializeGrid();
    
    e("go").addEventListener("click",go);
	e("stop").addEventListener("click",stop);
    e("sel").addEventListener("change",change);
    e("grid").addEventListener("click",processGridClick);
}


var processGridClick = function(evt) {
    if (!_intervalId) { 
	    if (evt.target && evt.target.nodeName == "rect") { 
		    var i = evt.target.getAttributeNS(null,"x") / _size;
		    var j = evt.target.getAttributeNS(null,"y") / _size;
		    ///console.log("Cell = ",i,j);
		    if (_grid[j*WIDTH+i]) { 
		        _grid[j*WIDTH+i] = false;
		        _screen[j*WIDTH+i].setAttributeNS(null,"class","hide-cell");
		    } else { 
		        _grid[j*WIDTH+i] = true;
		        _screen[j*WIDTH+i].setAttributeNS(null,"class","show-cell");
		    }
	    }
    }
};



var _intervalId = null;
var _grid = null;
var _screen = null;
var _size = 6;


var stop = function() {
    if (_intervalId) { 
	    e("stop").setAttribute("disabled",true);
        e("go").removeAttribute("disabled");
        e("sel").removeAttribute("disabled");
	    window.clearInterval(_intervalId);
        _intervalId = null;
    }
}

var work = function() {
	_grid = nextGeneration();
	showGrid();
};

var go = function() {
    
    if (!_intervalId) {
        e("go").setAttribute("disabled",true);
        e("sel").setAttribute("disabled",true);
	    e("stop").removeAttribute("disabled");
	    _intervalId = window.setInterval(work,200);
    }
};


function change (evt) { 

    ///console.log(this.value);
    initializePattern(this.value);
    showGrid()
    
}


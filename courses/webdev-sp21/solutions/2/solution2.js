
// SIMPLE
// ======

function roundN (n, i) {
  if (i < 0) {
    return n
  }
  let current = n
  for (let j = 0; j < i; j++) {
    current = current / 10
  }
  current = Math.round(current)
  for (let j = 0; j < i; j++) {
    current = current * 10
  }
  return current
}

function positive (lst) {
  return lst.filter((x) => (x >= 0))
}

function positiveStr (s) {
  return positive(s.split(';')).join(';')
}

function mapStr (s, sep, f) {
  if (s) { 
    return s.split(sep).map(f).join(sep)
  }
  else {
    return ''
  }
}

function range (n, m) {
  const result = []
  while (n < m) {
    result.push(n)
    n++
  }
  return result
}


// OBJECTS
// =======


const sample = [
  {a: 1,
   b: 2,
   c: 3},
  {a: 10,
   b: 20,
   c: 30},
  {a: 99,
   b: 66,
   c: 33},
  {a: 1,
   b: 20,
   c: 33},
  {a: 10,
   b: 66,
   c: 3},
  {a: 99,
   b: 2,
   c: 30}
]

const sample_obj = {
  a: 33,
  b: 66,
  c: 99,
  x: ['this', 'is', 'a', 'string'],
  y: [1, 2, 3],
  z: []
}

function distinct (objs, field) {
  const vals = new Set()
  objs.forEach((r) => vals.add(r[field]))
  return Array.from(vals)
}

function sort (objs, field) {
  const result = objs.slice()
  result.sort((a, b) => (a[field] > b[field]) ? 1 : ((a[field] < b[field]) ? -1 : 0))
  return result
}

function sum (objs, field1, field2) {
  return objs.map((r) => ( { ...r, sum: r[field1] + r[field2]} ))
}

function group (objs, field) {
  const result = {}
  for (let r of objs) {
    if (!result[r[field]]) {
      result[r[field]] = []
    }
    result[r[field]].push(r)
  }
  return result
}

function expand (obj, field) {
  const result = []
  for (let v of obj[field]) {
    const new_obj = {...obj}
    new_obj[field] = v
    result.push(new_obj)
  }
  return result
}


// classes
// =======


class Empty {
  
  isEmpty() {
    return true
  }

  height() {
    return 0
  }

  size() {
    return 0
  }

  fringe() {
    return []
  }

  preorder(f) {
    return
  }
  
  map(f) {
    return new Empty()
  }

  trim() {
    return new Empty()
  }

  toJSON() {
    return {}
  }
}


class Node {
  
  constructor(value, left, right) {
    this.value = value
    this.left = left
    this.right = right
  }

  isEmpty() {
    return false
  }

  height() {
    return 1 + Math.max(this.left.height(), this.right.height())
  }

  size() {
    return 1 + this.left.size() + this.right.size()
  }

  fringe() {
    if (this.left.isEmpty() && this.right.isEmpty()) {
      return [this.value]
    }
    else {
      return this.left.fringe().concat(this.right.fringe())
    }
  }

  preorder(f) {
    f(this.value)
    this.left.preorder(f)
    this.right.preorder(f)
  }
  
  map(f) {
    return new Node(f(this.value), this.left.map(f), this.right.map(f))
  }

  trim() {
    if (this.left.isEmpty() && this.right.isEmpty()) {
      return new Empty()
    }
    return new Node(this.value, this.left.trim(), this.right.trim())
  }

  toJSON() {
    const result = {
      value: this.value,
    }
    if (!this.left.isEmpty()) {
      result.left = this.left.toJSON()
    }
    if (!this.right.isEmpty()) {
      result.right = this.right.toJSON()
    }
    return result
  }
}

const leaf = (v) => new Node(v, new Empty(), new Empty())
const node = (v, l, r) => new Node(v, l, r)
const sample_tree = node(10,
			 node(20, node(40, leaf(80), leaf(90)),
                              node(50, leaf(100), leaf(110))),
			 node(30, leaf(60), leaf(70)))


function fromJSON(json) {
  if (!json) {
    return new Empty()
  }
  return new Node(json.value,
                  fromJSON(json.left),
                  fromJSON(json.right))
}

function fromArray(arr) {
  // root is the element that is not a child of any other node
  const map = {}
  const parents = {}
  for (let n of arr) {
    map[n.id] = n
    if (n.left) {
      parents[n.left] = n.id
    }
    if (n.right) {
      parents[n.right] = n.id
    }
  }
  let root = null
  const children = Object.keys(parents)
  for (let n of arr) {
    if (!children.includes(n.id)) {
      if (root) {
        throw('Two possible roots: ', root, n.id)
      }
      root = n.id
    }
  }
  if (!root) {
    throw('No root identified')
  }

  const mkTree = (id) => {
    const n = map[id]
    const result = {
      value: n.value
    }
    if (n.left) {
      result.left = mkTree(n.left)
    }
    if (n.right) {
      result.right = mkTree(n.right)
    }
    return result
  }
  return fromJSON(mkTree(root))
}

const sample_arr_1 = [
  {id: 'a', value: 1, left: 'b', right: 'c'},
  {id: 'b', value: 2, left: 'd', right: 'e'},
  {id: 'c', value: 3, left: 'f', right: 'g'},
  {id: 'd', value: 4, left: 'h'},
  {id: 'e', value: 5, right: 'i'},
  {id: 'f', value: 6},
  {id: 'g', value: 7},
  {id: 'h', value: 8},
  {id: 'i', value: 9}
]

const sample_arr_2 = [
  {id: 'john', value: 'L'},
  {id: 'paul', value: 'M'},
  {id: 'george', value: 'H', left: 'john', right: 'paul'},
  {id: 'ringo', value: 'S', left: 'george', right: 'george'},
]

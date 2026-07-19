
function boxLayoutInit() {

  function dim(opacity) {
    document.body.style.opacity = `${opacity}`
  }
  
  function refreshAll() {
    document.querySelectorAll(".box-layout").forEach((elt) => {
      const bb = innerSize(elt)
      console.log(bb)
      refreshVt(elt, bb.innerWidth, bb.innerHeight)
    })
  }

  function refresh(elt, width, height) {
    height = restrict(height, getBoundsDir(elt, "height"))
    width = restrict(width, getBoundsDir(elt, "width"))
    elt.style.width = `${width}px`
    elt.style.height = `${height}px`
    if (elt.classList.contains("box-vt")) {
      const style = getComputedStyle(elt)
      height = height - parseFloat(style.paddingTop) - parseFloat(style.paddingBottom)
      width = width - parseFloat(style.paddingLeft) - parseFloat(style.paddingRight)
      refreshVt(elt, width, height)
    } else if (elt.classList.contains("box-hz")) {
      const style = getComputedStyle(elt)
      height = height - parseFloat(style.paddingTop) - parseFloat(style.paddingBottom)
      width = width - parseFloat(style.paddingLeft) - parseFloat(style.paddingRight)
      refreshHz(elt, width, height)
    } 
  }

  function getBoundsDir(elt, dir) {
    const bounds = {Min: 0}
    const v = elt.getAttribute(`data-${dir}`)
    const vmn = elt.getAttribute(`data-min-${dir}`)
    const vmx = elt.getAttribute(`data-max-${dir}`)
    if (vmn) {
      bounds.Min = +vmn
    }
    if (vmx) {
      bounds.Max = +vmx
    }
    if (v) {
      bounds.Min = +v
      bounds.Max = +v
    }
    return bounds
  }      

  function getBounds(elt) {
    let width = getBoundsDir(elt, "width")
    let height = getBoundsDir(elt, "height")
    return {height, width}
  }

  function refreshVt(elt, width, height) {
    console.log(width)
    const numChildren = elt.children.length
    bounds = []
    for (let i = 0; i < numChildren; i++) {
      bounds.push(getBoundsDir(elt.children[i], "height"))
    }
    const heights = calculatePartition(bounds, height)
    for (let idx = 0; idx < numChildren; idx++) {
      const w = restrict(width, getBoundsDir(elt.children[idx], "width"))
      refresh(elt.children[idx], w, heights[idx])
    }
  }

  function refreshHz(elt, width, height) {
    const numChildren = elt.children.length
    bounds = []
    for (let i = 0; i < numChildren; i++) {
      bounds.push(getBoundsDir(elt.children[i], "width"))
    }
    console.log(bounds)
    const widths = calculatePartition(bounds, width)
    console.log(width, widths)
    for (let idx = 0; idx < numChildren; idx++) {
      const h = restrict(height, getBoundsDir(elt.children[idx], "height"))
      refresh(elt.children[idx], widths[idx], h)
    }
  }

  function restrict(v, bounds) {
    if (v < bounds.Min) {
      return bounds.Min
    } else if (bounds.Max !== undefined && v > bounds.Max) {
      return bounds.Max
    }
    return v
  }

  function innerSize(elt) {
    const style = getComputedStyle(elt);
    const innerWidth =
          elt.clientWidth -
          parseFloat(style.paddingLeft) -
          parseFloat(style.paddingRight)
    const innerHeight =
          elt.clientHeight -
          parseFloat(style.paddingTop) -
          parseFloat(style.paddingBottom)
    return {innerWidth, innerHeight, height: elt.offsetHeight, width: elt.offsetWidth}
  }

  function addDim(a, b) {
    result = {
      Min: a.Min + b.Min
    }
    if (a.Max !== undefined && b.Max !== undefined) {
      result.Max = a.Max + b.Max
    }
    return result
  }

  function calculatePartition(bnds, length) {
    const result = new Array(bnds.length);
    let bTotal = { Min: 0, Max: 0 };

    for (let i = 0; i < bnds.length; i++) {
      const bb = bnds[i];
      bTotal = addDim(bTotal, bb);
      result[i] = bb.Min;
    }

    if (length < bTotal.Min) {
      while (true) {
        let current = 0;
        let countAboveMin = 0;
        let delta = bTotal.Min;

        for (let i = 0; i < bnds.length; i++) {
          const bb = bnds[i];
          current += result[i];
          if (result[i] > bb.Min) {
            delta = Math.min(delta, result[i] - bb.Min);
            countAboveMin++;
          }
        }

        if (countAboveMin === 0) {
          return result;
        }

        const excess = current - length;

        if (excess < bnds.length) {
          return result;
        }

        let toSubtract = delta;
        if (excess < countAboveMin * delta) {
          toSubtract = Math.ceil(excess / countAboveMin);
        }

        for (let i = 0; i < bnds.length; i++) {
          const bb = bnds[i];
          if (result[i] > bb.Min) {
            result[i] -= toSubtract;
          }
        }
      }
    }

    if (length > bTotal.Min) {
      while (true) {
        let current = 0;
        let countBelowMax = 0;
        let delta = -1;

        for (let i = 0; i < bnds.length; i++) {
          const bb = bnds[i];
          current += result[i];

          if (bb.Max === undefined) {
            countBelowMax++;
          } else if (result[i] < bb.Max) {
            if (delta < 0) {
              delta = bb.Max - result[i];
            } else {
              delta = Math.min(delta, bb.Max - result[i]);
            }
            countBelowMax++;
          }
        }

        if (countBelowMax === 0) {
          return result;
        }

        const excess = length - current;

        if (excess < bnds.length) {
          return result;
        }

        let toAdd = delta;

        // delta === -1 → no upper bounds
        if (delta === -1 || excess < countBelowMax * delta) {
          toAdd = Math.floor(excess / countBelowMax);
        }

        for (let i = 0; i < bnds.length; i++) {
          const bb = bnds[i];
          if (bb.Max === undefined || result[i] < bb.Max) {
            result[i] += toAdd;
          }
        }
      }
    }
    return result;
  }

  // Wait 0.1s without a resize before firing off a resize message.
  const delayResize = 200 
  let timeoutId = null
  window.addEventListener("resize", (evt) => {
    dim(0.1)
    if (timeoutId) {
      // We already have a timer going, so reset it.
      window.clearTimeout(timeoutId)
      timeoutId = null
    }
    timeoutId = window.setTimeout(() => {
      dim(1)
      refreshAll()
      // Do it a second time because otherwise overflow property messes up the layout.
      refreshAll()
    }, delayResize)
  })
  
  // This is the actual initialization.
  refreshAll()
  
}

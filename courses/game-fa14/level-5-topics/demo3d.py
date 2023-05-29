#
# 3D Wireframe demo
#
# There are some problems with this code. I think we're hitting
# some floating point precision problems, and that leads to weird
# artifacts when numbers get too small (mostly when dividing by very
# small numbers). 
#

from graphics import *
from math import sin,cos,sqrt,atan2,pi,acos,asin


# homogenized 3d coordinates translation matrix

def m_translate (dx,dy,dz):
    return [[1,0,0,dx],
	    [0,1,0,dy],
	    [0,0,1,dz],
	    [0,0,0,1]]


# homogenized 3d coordinates rotation matrices
# all rotations obey the right-hand rule
# (rotations are counterclockwise around an axis when that
#  axis is pointing towards you)

def m_rotateX (theta):
    c = cos(theta)
    s = sin(theta)
    return [[1,0,0,0],
	    [0,c,-s,0],
	    [0,s,c,0],
	    [0,0,0,1]]

def m_rotateY (theta):
    c = cos(theta)
    s = sin(theta)
    return [[c,0,s,0],
	    [0,1,0,0],
	    [-s,0,c,0],
	    [0,0,0,1]]

def m_rotateZ (theta):
    c = cos(theta);
    s = sin(theta);
    return [[c,-s,0,0],
	    [s,c,0,0],
	    [0,0,1,0],
	    [0,0,0,1]]


# homogenized 3d coordinates scaling matrix

def m_scale (s):
    return [[s,0,0,0],
	    [0,s,0,0],
	    [0,0,s,0],
	    [0,0,0,1]]


# matrix multiplications (binary and sequence)

def multiply_2 (m1,m2):
    result = [[0,0,0,0],[0,0,0,0],[0,0,0,0],[0,0,0,0]];
    for i in range(4):
	for j in range(4):
	    for k in range(4):
		result[i][j] += m1[i][k] * m2[k][j]
    return result

def multiply (mats):
    if len(mats)<2: 
	return mats
    result = mats[0]
    for mat in mats[1:]:
	result = multiply_2(result,mat)
    return result


# multiplication of a matrix and a vector

def mult_vector (m,v):
    result = [0,0,0,0];
    for i in range(4):
        for k in range(4):
	    result[i] += m[i][k] * v[k]
    return result


# convert a shape to homogenized coordinates (add weight 1 to every point)

def lift_shape (shape):
    for line in shape["lines"]:
      line[0].append(1)
      line[1].append(1)
    return shape


# homogenize a vector (rescale so that weight is 1)

def homogenize_vector (vec):
    w = vec[3]
    return [vec[0]/w,vec[1]/w,vec[2]/w,1]


# a shape representing the positive axes

def axes ():
    return lift_shape({"color":"lightgray",
		       "lines":[[[0,0,0],[10,0,0]],
                                [[0,0,0],[0,10,0]],
                                [[0,0,0],[0,0,10]]]})


# the shapes describing the house (split so they can have different colors)

def house_front ():
    return lift_shape({"color":"red",
		       "lines":[[[0,0,0],[2,0,0]],
				[[2,0,0],[2,2,0]],
				[[2,2,0],[1,3,0]],
				[[1,3,0],[0,2,0]],
				[[0,2,0],[0,0,0]]]})


def house_back ():
    return lift_shape({"color":"blue",
		       "lines":[[[0,0,4],[2,0,4]],
				[[2,0,4],[2,2,4]],
				[[2,2,4],[1,3,4]],
				[[1,3,4],[0,2,4]],
				[[0,2,4],[0,0,4]]]})


def house_sides ():
    return lift_shape({"color":"green",
		       "lines":[[[0,0,0],[0,0,4]],
				[[2,0,0],[2,0,4]],
				[[2,2,0],[2,2,4]],
				[[1,3,0],[1,3,4]],
				[[0,2,0],[0,2,4]]]})


# after applying a camera matrix to a 3d point, we have the
# coordinates of the point on the "window" plane. But we
# need to translate those to screen coordinates. And remember
# that the camera is "behind" the window. 
def screen (p):
    hp = homogenize_vector(p)
    return (200-200*hp[0],200-200*hp[1])


# compute the camera matrix corresponding to a camera
# at position (cx,cy,cz) pointing in direction given by 
# angles thetax, thetay, thetaz from the x, y, and z axes
# respectively. (The order is that rotation is first done
# with respect to y, then x, then z. Warning: it's tricky.) 
# The focal length f is roughly how far from the
# camera the "window" plane is.

def camera_angles (cx,cy,cz,thetax,thetay,thetaz,f):
    m1 = m_translate(-cx,-cy,-cz)
    m3 = m_rotateY(-thetay)
    m2 = m_rotateX(-thetax)
    m4 = m_rotateZ(-thetaz)
    m5 = [[1,0,0,0],[0,1,0,0],[0,0,1,0],[0,0,1/f,0]]
    return multiply([m5,m4,m2,m3,m1])


# compute the camera matrix corresponding to a camera
# at position (cx,cy,cz) pointing in direction given by
# a vector (dx,dy,dz).
# The focal length f is roughly how far from the camera
# the "window" plane is.

def camera_direction (cx,cy,cz,dx,dy,dz,f):
    thetay = atan2(dx,dz)
    thetax = -asin(dy/sqrt(dx*dx+dy*dy+dz*dz))
    return camera_angles(cx,cy,cz,thetax,thetay,0,f)


# compute the camera matrix corresponding to a camera
# at position (cx,cy,cz) tracking point (px,py,pz).
# The focal length f is roughly how far from the camera
# the "window" plane is.

def camera_tracking (cx,cy,cz,px,py,pz,f):
    return camera_direction(cx,cy,cz,(px-cx),(py-cy),(pz-cz),f)
  

# draw a shape in the given window according to the given
# camera matrix
def draw_shape (window,shape,cam):
    for line in shape["lines"]:
        (x1,y1) = screen(mult_vector(cam,line[0]))
        (x2,y2) = screen(mult_vector(cam,line[1]))
        line = Line(Point(x1,y1),Point(x2,y2))
        line.setFill(shape["color"])
        line.draw(window)
        window.memory.append(line)

# clear the window

def clear (window):
    for item in window.memory:
        item.undraw()
    window.memory = []
    window.update()

# draw the house (and axis) in the window, according to the
# given camera matrix
# wait for a key press and then clear the window

def draw_house (window,cam):
    draw_shape (window,axes(),cam);
    draw_shape (window,house_front(),cam);
    draw_shape (window,house_back(),cam);
    draw_shape (window,house_sides(),cam);
    window.update()
    window.getKey()
    clear(window)


# draw the house according to a sequence of camera positions
# (fake fly through!)

def main ():

    window = GraphWin("3D Demo", 400,400,autoflush=False)
    window.memory = []

    # Playing with different camera angles
    #
    # Feel free to uncomment

    # cams = [camera_angles(0,0,-6,0,0,0,1),
    #         camera_angles(0,0,-5,0,0,0,1),
    #         camera_angles(2,2,-5,0,0,0,1),
    #         camera_angles(4,4,-5,0,0,0,1),
    #         camera_angles(6,6,-5,0,0,0,1),
    #         camera_angles(6,6,-5,0.2,-0.2,0,1),
    #         camera_angles(6,6,-3,0.4,-0.4,0,1),
    #         camera_angles(6,6,-1,0.6,-0.6,0,1),
    #         camera_angles(6,6,1,0.6,-0.6,0,1),
    #         camera_angles(6,6,3,0.6,-0.6,0,1),
    #         camera_angles(6,6,5,0.6,-0.6,0,1)]

    # cams = [camera_direction(0,0,-6,0,0,1,1),
    #         camera_direction(0,0,-4,0,0,1,1),
    #         camera_direction(2,2,-4,0,0,1,1),
    #         camera_direction(4,4,-4,0,0,1,1),
    #         camera_direction(4,4,-4,-1,0,2,1),
    #         camera_direction(4,4,-4,-1,0,1,1),
    #         camera_direction(4,4,-4,-2,-1,2,1),
    #         camera_direction(4,4,-4,-1,-1,1,1),
    #         camera_direction(6,6,-4,-1,-1,1,1),
    #         camera_direction(6,6,-2,-1,-1,1,1)]


    # positions the camera will be in (all will be tracking the same point)
    positions = [(0,0,-6),
                 (1,1,-6),
                 (2,2,-6),
                 (4,4,-6),
                 (6,6,-6),
                 (8,8,-6),
                 (8,8,-4),
                 (8,8,-2),
                 (8,8,0),
                 (8,8,2),
                 (8,8,4),
                 (8,8,6),
                 (8,8,8),
                 (6,6,8),
                 (4,4,8),
                 (2,2,8),
                 (0,0,8)]
    cams = [ camera_tracking(x,y,z,1,2,2,1) for (x,y,z) in positions]
            

    for cam in cams:
        draw_house(window,cam)

    window.close()

if __name__ == '__main__':
    main()

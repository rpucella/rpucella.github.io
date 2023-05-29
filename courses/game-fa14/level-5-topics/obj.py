
# Reads an OBJ file and converts it into a shape
#
# file = OBJ file name
# scale = the factor by which to scale the vertices
# color = the color to assign to the shape

def read_obj (file,scale,color):

    # first read the file
    vertices = []
    faces = []
    with open(file,"r") as f:
        for line in f:
            l = line.lstrip();
            if l:
                if l[0] == "#":
                    continue
                bits = l.split()
                if bits[0]=="v":
                    vertices.append([float(bits[1]),float(bits[2]),float(bits[3])])
                if bits[0]=="f":
                    faces.append([int(x) for x in bits[1:]])

    # then scale the vertices
    vertices = [[x[0]*scale,x[1]*scale,x[2]*scale] for x in vertices]

    # then process the faces to construct the lines
    lines = []
    for vs in faces:
        for i in range(len(vs)-1):
            p1 = vertices[vs[i]-1]
            p2 = vertices[vs[i+1]-1]
            lines.append([p1,p2])
        lines.append([vertices[vs[-1]-1],vertices[vs[0]-1]])
    return {"color":color, "lines":lines}

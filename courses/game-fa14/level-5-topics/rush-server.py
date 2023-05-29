
#
# Implementation of Rush Hour as a server
#
# - Start by passing in a port as an argument
#
# - Use telnet to the port to access the server
#
# - The interface is very fickle: if you mess
#   up a move, you might end up in a state
#   where the system is hopelessly confused
#   about what you're typing. Why? (Hint: every
#   time the server tries to read a move, it reads
#   five characters off the socket. What if you give
#   it only four?)
#


import sys
import socket


# fail somewhat gracefully

def fail (msg):
    raise StandardError(msg)


GRID_SIZE = 6

# Cars and Trucks

PIECES_MAP = {
  'X': 2,
  'A': 2,
  'B': 2,
  'C': 2,
  'D': 2,
  'E': 2,
  'F': 2,
  'G': 2,
  'H': 2,
  'I': 2,
  'J': 2,
  'K': 2,
  'O': 3,
  'P': 3,
  'Q': 3,
  'R': 3
}


DIR_MAP = {
  'u': (0,-1),
  'd': (0,1),
  'l': (-1,0),
  'r': (1,0)
}

def shift_coord ((i,j),dir,n):
    return (i+n*DIR_MAP[dir][0], j+n*DIR_MAP[dir][1])

def index ((i,j)):
    return (i-1)+(j-1)*GRID_SIZE

def in_bounds ((i,j)):
    return (i > 0 and i <= GRID_SIZE and j > 0 and j <= GRID_SIZE)

def coords_of_piece (piece,coord,dir):
    return [ shift_coord(coord,dir,n) for n in range(0,PIECES_MAP[piece]) ]


def check_free (brd,coord,dir,len):
    array = materialize_board(brd)
    if in_bounds(incr_in_dir(coord,dir,len-1)):
        for m in range(0,len):
            if array[index(incr_in_dir(coord,dir,m))] != '.':
                return False
        return True
    return False
    

def validate_move (brd,piece,dir,dist):
    def first_coord_past ((i,j)):
        if dir == 'u':
            return (i,j-1)
        if dir == 'd':
            return (i,j+PIECES_MAP[piece]-1+1)
        if dir == 'l':
            return (i-1,j)
        if dir == 'r':
            return (i+PIECES_MAP[piece]-1+1,j)
    # check that piece is on the board
    if not piece in brd:
        print "Piece not on the board"
        return False
    # check that piece placed so it can move in that direction
    (b_coord,b_dir) = brd[piece]
    if ((b_dir == 'd' and (dir != 'd' and dir != 'u'))
          or (b_dir == 'r' and (dir != 'l' and dir != 'r'))):
        print "Direction not compatible with piece placement"
        return False
    # check that piece would be in bound
    for coord in coords_of_piece(piece,shift_coord(b_coord,dir,dist),b_dir):
        if not in_bounds(coord):
            print "Move would shift piece out of bounds"
            return False
    # check that path to target position is free
    array = materialize_board (brd)
    for n in range(0,dist):
        coord = shift_coord(first_coord_past(b_coord),dir,n)
        if (array[index(coord)] != '.' and
            array[index(coord)] != piece):
            print "There is a piece in the way"
            return False
    return True


def update_board (brd,(piece,dir,dist)):
    if piece in brd:
        brd[piece] = (shift_coord(brd[piece][0],dir,dist),
                      brd[piece][1])
    return brd


def place_piece (piece,((i,j),dir),array):
    for incr in range(0,PIECES_MAP[piece]):
        coord = shift_coord((i,j),dir,incr)
        if not in_bounds(coord):
            fail('piece ' + piece.upper() + ' out of bounds')
        pos = index(coord)
        if array[pos] != '.':
            fail('piece ' + piece.upper() + ' conflicts with existing piece')
        array[pos] = piece

def array_to_string (arr):
    groups = [arr[x:x+GRID_SIZE] for x in xrange(0,len(arr), GRID_SIZE)]
    substrings = [' '.join(lst) for lst in groups]
    substrings[2] += '  ===>'
    return '\n'.join(substrings)

def materialize_board (brd):
    array = ['.'] * (GRID_SIZE * GRID_SIZE)
    for piece in brd:
        place_piece(piece,brd[piece],array)
    return array

def print_board (brd):
    array = materialize_board(brd)
    print '\n'+array_to_string(array)+'\n'
    
def done (brd):
    if 'X' in brd:
        return (brd['X'][0] == (GRID_SIZE-1,3))
    return False
            

# initial board:
#
# X23R
# A24R
# B25D
# C36R
# O43D
# P64D

def create_initial_level ():
    brd = {'X': ((2,3),'r'), 
           'A': ((2,4),'r'), 
           'B': ((2,5),'d'), 
           'C': ((3,6),'r'),
           'O': ((4,3),'d'), 
           'P': ((6,4),'d')}
    materialize_board(brd)   # to check for overlap
    return brd


# send a string on a socket 
# (keep sending until string fully sent)
def full_send (sock,msg):
    length = len(msg)
    totalsent = 0
    while totalsent < length:
        sent = sock.send(msg[totalsent:])
        if sent == 0:
            raise RuntimeError("socket connection broken")
        totalsent = totalsent + sent

# read a given number of characters from a socket
# (keep reading until all wanted characters received)
def full_receive (sock,length):
    chunks = []
    bytes_recd = 0
    while bytes_recd < length:
        chunk = sock.recv(min(length - bytes_recd, 2048))
        if chunk == '':
            raise RuntimeError("socket connection broken")
        chunks.append(chunk)
        bytes_recd = bytes_recd + len(chunk)
    return ''.join(chunks)



def send_board (csock, brd):
    def array_to_string (arr):
        groups = [arr[x:x+GRID_SIZE] for x in xrange(0,len(arr), GRID_SIZE)]
        substrings = [''.join(lst)+"\n" for lst in groups]
        return ''.join(substrings)
    str = array_to_string(materialize_board(brd))
    full_send (csock,str)



def recv_move (csock,brd):

    input = full_receive(csock,5)
    # should provide some checking to make sure the input is \r\n
    # terminated
    if input == "qqq\r\n":
        return "quit"
    if not input[0].upper() in PIECES_MAP:
        return None
    if not input[1] in DIR_MAP:
        return None
    if not input[2].isdigit or int(input[2])<1 or int(input[2])>6:
        return None
    piece,dir,dist = input[0].upper(), input[1].lower(),int(input[2])
    if validate_move(brd,piece,dir,dist):
        return piece,dir,dist
    return None
    

def play (csock):

    brd = create_initial_level()

    send_board(csock,brd)

    while not done(brd):
        move = recv_move(csock,brd)
        if move:
            if move == "quit":
                return
            brd = update_board(brd,move)
        send_board(csock,brd)

    print "Player wins!"

def main(port):

    # Create a listening socket to listen for connection attempts
    serversocket = socket.socket(socket.AF_INET, 
                                 socket.SOCK_STREAM)
    serversocket.bind(('',port))
    serversocket.listen(1)

    while True:
        # wait for a connection to happen
        print "Waiting for player"
        (clientsocket,address) = serversocket.accept()
        play(clientsocket)
        clientsocket.close()
        

if __name__ == '__main__':
    if len(sys.argv) > 1:
        main(int(sys.argv[1]))
    else:
        print "Need a port number"

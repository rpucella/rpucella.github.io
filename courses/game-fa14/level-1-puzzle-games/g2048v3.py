#!/usr/bin/env python
# 


# Clone of 2048 on the command line
# 
# v3: do not rotate board, but instead use a different indexing scheme into the
#     array to "mimic" having rotated it without actually doing anything.


import random

GRID_SIZE = 4
INITIAL_NUMBER_OF_TILES = 2
PROB_OF_2_VS_4 = 0.9



def add_random_free_cell (board):
    if 0 in board:
        free_positions = [pos for (pos,val) in list(enumerate(board)) 
                                  if val == 0]
        # return list of indices 
        new_cell_pos = random.choice(free_positions)
        new_cell_val = (2 if random.random() < PROB_OF_2_VS_4 else 4)
        board[new_cell_pos] = new_cell_val
    return board



def initialize_board ():
    board = [0]*(GRID_SIZE * GRID_SIZE)

    for i in range(INITIAL_NUMBER_OF_TILES):
        board = add_random_free_cell(board)

    return board


def read_player_input (board):
    while True:
        move = raw_input('Move (u|d|l|r|q)? ')
        if len(move) == 1 and move in 'udlrq':
            if move == 'q':
                exit(0)
            return MOVE_MAP[move]


def center(n):
    if n == 0:
        return '      '
    return str(n).center(6)


def board_to_string (board):
    line = '-'*(6 * GRID_SIZE + GRID_SIZE + 1) + '\n'
    groups = [board[x:x+GRID_SIZE] for x in xrange(0,len(board), GRID_SIZE)]
    substrings = ['|' + '|'.join(map(center,lst)) + '|' for lst in groups]
    return '\n' + line + (('\n'+line).join(substrings)) + '\n' + line


def index (x,y):    # compute the index of a x,y position in the array of cells
    def fix (n):  # allows the use of -1, -2,... to refer to last, one-before-last, etc
        return GRID_SIZE + n if n < 0 else n - 1
    return fix(x) + fix(y)*GRID_SIZE


# these are different indexing functions that mimic having rotated the board a few
# times for a corresponding move

def index_left (i,j):
    return index(i,j)

def index_right (i,j):
    return index(GRID_SIZE+1-i,j)

def index_up (i,j):
    return index(j,i)

def index_down (i,j):
    return index(j,GRID_SIZE+1-i)


MOVE_MAP = {
    'u': index_up,
    'd': index_down,
    'l': index_left,
    'r': index_right
}



def move_left (board,index):
    changed = False
    for j in range(1,GRID_SIZE+1):
        next_free = 1
        block_value = 0
        for i in range(1, GRID_SIZE+1):
            pos = index(i,j)
            # print pos, board[pos], next_free, block_value
            if board[pos] > 0:
                if board[pos] == block_value:
                    board[pos] = 0
                    board[index(next_free-1,j)] = block_value * 2
                    block_value = 0
                    changed = True
                else:
                    block_value = board[pos]
                    if index(next_free,j) != pos:
                        changed = True
                        board[index(next_free,j)] = board[pos]
                        board[pos] = 0
                    next_free += 1
    return board, changed


def update_board (board,index):
    board,changed = move_left(board,index)
    if changed:
        board = add_random_free_cell(board)
    return board



def done (board):
    def join_possible ():
        for i in range(1,GRID_SIZE+1):
            for j in range(1,GRID_SIZE+1):
                if (i < GRID_SIZE and board[index(i,j)] == board[index(i+1,j)]):
                    return True
                if (j < GRID_SIZE and board[index(i,j)] == board[index(i,j+1)]):
                    return True
        return False
    return (2048 in board or not (0 in board or join_possible()))


def winning_board (board):
    return (2048 in board)


def print_board (brd):
    print board_to_string(brd)


def main ():
    board = initialize_board()

    print_board(board)

    while not done(board):
        move = read_player_input(board)
        board = update_board(board,move)
        print_board(board)

    if winning_board (board):
        print 'Congratulations!\n'
    else:
        print 'Ouch. Sorry about that...\n'
        

if __name__ == '__main__':
    main()

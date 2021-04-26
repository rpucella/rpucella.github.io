#!/usr/bin/env python
# 


# simple game of tic-tac-toe


def fail (msg):
    raise StandardError(msg)


# representation: array of 9 cells (0 in upper-left corner, row by row)
#   each cell one of 'O',' ','X'

WIN_SEQUENCES = [
    [0,1,2],
    [3,4,5],
    [6,7,8],
    [0,3,6],
    [1,4,7],
    [2,5,8],
    [0,4,8],
    [2,4,6]
]

MARK_VALUE = {
    'O': 1,
    ' ': 0,
    'X': 10
}


def initialize_board ():
    return [' '] * 9


# returns 'O' if the board is a win for O
# returns 'X' if the board is a win for X
# returns False otherwise

def has_win (board):
    for positions in WIN_SEQUENCES:
        s = sum(MARK_VALUE[board[pos]] for pos in positions)
        if s == 3:
            return 'O'
        if s == 30:
            return 'X'
    return False


def print_board (board):
    for i in range(0,3):
        print '  ',board[i*3],board[i*3+1],board[i*3+2]
    print


def read_player_input (board):
    valid = [ i for (i,e) in enumerate(board) if e == ' ']
    while True:
        move = raw_input('Position (0-8)? ')
        if move == 'q':
            exit(0)
        if len(move)>0 and int(move) in valid:
            return int(move)


# we're done if there's a win or the board is full
def done (board):
    return (has_win(board) or not [ e for e in board if (e == ' ')])


# given a board and a move (a position 0-8) and a mark 'O' or 'X'
#  returns a new board with that move recorded
def make_move (board,move,mark):
    # returns a copy of the board with the move recorded
    new_board = board[:]
    new_board[move] = mark
    return new_board

# return list of possible moves in a given board
def possible_moves (board):
    return [i for (i,e) in enumerate(board) if e == ' ']


def utility (board):
    # fix me
    return 0

def min_value (board):
    # fix me 
    return 0

def max_value (board):
    # fix me
    return 0

def best_move (board,player):
    # fix me
    return possible_moves(board)[0]


def main (): 
    board = initialize_board()

    print_board(board)

    while not done(board):
        move = read_player_input(board)
        board[move] = 'X'
        print_board(board)
        if not done(board):
            move = best_move (board,'O')
            print 'Computer moves to',move
            board[move] = 'O'
            print_board(board)

    winner = has_win(board)
    if winner:
        print winner,'wins!'
    else:
        print 'Draw'
        

if __name__ == '__main__':
    main()

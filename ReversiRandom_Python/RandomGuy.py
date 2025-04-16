import sys
import socket
import time
import copy
from random import randint

t1 = 0.0  # the amount of time remaining to player 1
t2 = 0.0  # the amount of time remaining to player 2

state = [[0 for x in range(8)] for y in range(8)]  # state[0][0] is the bottom left corner of the board (on the GUI)

DIRECTIONS = [
    (-1, -1), (-1, 0), (-1, 1),
    (0, -1), (0, 1),
    (1, -1), (1, 0), (1, 1)
]

WEIGHTS = [
    [100, -20, 10, 5, 5, 10, -20, 100],
    [-20, -50, -2, -2, -2, -2, -50, -20],
    [10, -2, 0, 0, 0, 0, -2, 10],
    [5, -2, 0, 0, 0, 0, -2, 5],
    [5, -2, 0, 0, 0, 0, -2, 5],
    [10, -2, 0, 0, 0, 0, -2, 10],
    [-20, -50, -2, -2, -2, -2, -50, -20],
    [100, -20, 10, 5, 5, 10, -20, 100],
]


# You should modify this function
# validMoves is a list of valid locations that you could place your "stone" on this turn
# Note that "state" is a global variable 2D list that shows the state of the game
def move(validMoves, round, me):
    depth = 1
    maximizing = True
    _, move_index = minimax(depth, round, me, maximizing, -1000000, 100000)

    return move_index


def minimax(depth, round, me, maximizing, alpha, beta):
    global state

    moves = getValidMoves(round, me)
    if depth == 0 or not moves:
        return heuristic(me, round), 0

    original_state = copy.deepcopy(state)
    best_value = float('-inf') if maximizing else float('inf')
    best_index = 0
    for i, move in enumerate(moves):
        getNewState(move, me, round)
        value, _ = minimax(depth - 1, round + 1, 1 if me == 2 else 2, not maximizing, alpha, beta)
        state = original_state

        if maximizing:
            if value > best_value:
                best_value = value
                best_index = i
            alpha = max(alpha, best_value)
        else:
            if value < best_value:
                best_value = value
                best_index = i
            beta = min(beta, best_value)
        if beta <= alpha:
            break
    return best_value, best_index


def heuristic(me, round):
    # corner = get_corner_control(state, me)
    # mobility = get_mobility_heuristic(state, me, round)
    # parity = get_coin_parity_heuristic(me)
    position = get_positional_heuristic(me)

    # if round < 20:
    #     return 0.3 * parity + 0.7 * position  # + 0.2 * corner
    # elif round < 50:
    #     return 0.6 * position + 0.4 * parity
    # else:
    #     return 0.9 * parity + 0.1 * position

    return position


def get_positional_heuristic(me):
    opp = 1 if me == 2 else 2
    score = 0
    for i in range(8):
        for j in range(8):
            if state[i][j] == me:
                score += WEIGHTS[i][j]
            elif state[i][j] == opp:
                score -= WEIGHTS[i][j]
    return score


def get_coin_parity_heuristic(me):
    my_tiles = sum(row.count(me) for row in state)
    opp = 1 if me == 2 else 2
    opp_tiles = sum(row.count(opp) for row in state)
    if my_tiles + opp_tiles == 0:
        return 0
    return 10 * (my_tiles - opp_tiles) / (my_tiles + opp_tiles)


def getNewState(move, player, round):
    row, col = move[0], move[1]

    if round < 4:
        state[row][col] = player

    for dx, dy in DIRECTIONS:
        r, c = row + dx, col + dy
        captured = []
        while 0 <= r < 8 and 0 <= c < 8 and state[r][c] not in [0, player]:
            captured.append((r, c))
            r += dx
            c += dy

        if 0 <= r < 8 and 0 <= c < 8 and state[r][c] == player:
            for rr, cc in captured:
                state[rr][cc] = player

    state[row][col] = player


# establishes a connection with the server
def initClient(me, thehost):
    sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)

    server_address = (thehost, 3333 + me)
    # print >> sys.stderr, 'starting up on %s port %s' % server_address
    sock.connect(server_address)

    info = sock.recv(1024)

    # print(info)

    return sock


# reads messages from the server
def readMessage(sock):
    mensaje = sock.recv(1024).decode().split("\n")
    # print mensaje

    turn = int(mensaje[0])
    print("Turn: " + str(turn))

    if (turn == -999):
        time.sleep(1)
        sys.exit()

    round = int(mensaje[1])
    print("Round: " + str(round))
    t1 = float(mensaje[2])  # update of the amount of time available to player 1
    # print t1
    t2 = float(mensaje[3])  # update of the amount of time available to player 2
    # print t2

    count = 4
    for i in range(8):
        for j in range(8):
            state[i][j] = int(mensaje[count])
            count = count + 1
        # print(state[i])

    return turn, round


def checkDirection(row, col, incx, incy, me):
    sequence = []
    for i in range(1, 8):
        r = row + incy * i
        c = col + incx * i

        if ((r < 0) or (r > 7) or (c < 0) or (c > 7)):
            break

        sequence.append(state[r][c])

    count = 0
    for i in range(len(sequence)):
        if (me == 1):
            if (sequence[i] == 2):
                count = count + 1
            else:
                if ((sequence[i] == 1) and (count > 0)):
                    return True
                break
        else:
            if (sequence[i] == 1):
                count = count + 1
            else:
                if ((sequence[i] == 2) and (count > 0)):
                    return True
                break

    return False


def couldBe(row, col, me):
    for incx in range(-1, 2):
        for incy in range(-1, 2):
            if ((incx == 0) and (incy == 0)):
                continue

            if (checkDirection(row, col, incx, incy, me)):
                return True

    return False


# generates the set of valid moves for the player; returns a list of valid moves (validMoves)
def getValidMoves(round, me):
    validMoves = []
    print("Round: " + str(round))

    for i in range(8):
        print(state[i])

    if (round < 4):
        if (state[3][3] == 0):
            validMoves.append([3, 3])
        if (state[3][4] == 0):
            validMoves.append([3, 4])
        if (state[4][3] == 0):
            validMoves.append([4, 3])
        if (state[4][4] == 0):
            validMoves.append([4, 4])
    else:
        for i in range(8):
            for j in range(8):
                if (state[i][j] == 0):
                    if (couldBe(i, j, me)):
                        validMoves.append([i, j])

    return validMoves


# main function that (1) establishes a connection with the server, and then plays whenever it is this player's turn
def playGame(me, thehost):
    # create a random number generator

    sock = initClient(me, thehost)

    while (True):
        print("Read")
        status = readMessage(sock)

        if (status[0] == me):
            print("Move")
            validMoves = getValidMoves(status[1], me)
            print(validMoves)

            myMove = move(validMoves, status[1], me)

            sel = str(validMoves[myMove][0]) + "\n" + str(validMoves[myMove][1]) + "\n";
            print("<" + sel + ">")
            sock.send(sel.encode())
            print("sent the message")
        else:
            print("It isn't my turn")

    return


# call: python RandomGuy.py [ipaddress] [player_number]
#   ipaddress is the ipaddress on the computer the server was launched on.  Enter "localhost" if it is on the same computer
#   player_number is 1 (for the black player) and 2 (for the white player)
if __name__ == "__main__":
    print('Number of arguments:', len(sys.argv), 'arguments.')
    print('Argument List:', str(sys.argv))

    print(str(sys.argv[1]))

    playGame(int(sys.argv[2]), sys.argv[1])


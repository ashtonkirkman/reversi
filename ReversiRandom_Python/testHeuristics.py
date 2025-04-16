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

state = [
    [0, 0, 0, 0, 0, 0, 0, 0],
    [0, 0, 0, 0, 0, 0, 0, 0],
    [0, 0, 0, 0, 0, 0, 0, 0],
    [0, 0, 0, 2, 1, 0, 0, 0],
    [0, 0, 0, 2, 1, 0, 0, 0],
    [0, 0, 0, 0, 1, 0, 0, 0],
    [0, 0, 0, 0, 2, 0, 0, 0],
    [0, 0, 0, 0, 0, 0, 0, 0],
]

def get_positional_heuristic(state, me):
    opp = 1 if me == 2 else 2
    score = 0
    for i in range(8):
        for j in range(8):
            if state[i][j] == me:
                score += WEIGHTS[i][j]
            elif state[i][j] == opp:
                score -= WEIGHTS[i][j]
    return score

def get_mobility_heuristic(cur_state, me, round):
    opp = 1 if me == 2 else 2
    my_moves = len(getValidMoves(round, me))
    opp_moves = len(getValidMoves(round, opp))
    if my_moves + opp_moves == 0:
        return 0
    return 100 * (my_moves - opp_moves) / (my_moves + opp_moves)

def getValidMoves(round, me):
    validMoves = []
    # print("Round: " + str(round))


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

def couldBe(row, col, me):
    for incx in range(-1,2):
        for incy in range(-1,2):
            if ((incx == 0) and (incy == 0)):
                continue

            if (checkDirection(row,col,incx,incy,me)):
                return True

    return False


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

def heuristic(state, me, round):
    # corner = get_corner_control(state, me)
    mobility = get_mobility_heuristic(state, me, round)
    # parity = get_coin_parity_heuristic(state, me)
    # position = get_positional_heuristic(state, me)

    # if round < 20:
    #     return 0.5 * mobility + 0.3 * position + 0.2 * corner
    # elif round < 50:
    #     return 0.4 * corner + 0.3 * position + 0.2 * mobility + 0.1 * parity
    # else:
    #     return 0.5 * parity + 0.3 * position + 0.2 * corner

    return mobility

# Test the heuristic function
me = 1
round = 15
print("Heuristic value:", heuristic(state, me, round))
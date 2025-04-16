import copy

DIRECTIONS = [
    (-1, -1), (-1, 0), (-1, 1),
    (0, -1),          (0, 1),
    (1, -1),  (1, 0), (1, 1)
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

def minimax(depth, round, me, maximizing, alpha, beta):
    global state

    moves = getValidMoves(round, me)
    if depth == 0 or not moves:
        return evaluate(), 0

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

def evaluate():
    score = 0
    for i in range(8):
        for j in range(8):
            if state[i][j] == 1:
                score += 1
            elif state[i][j] == 2:
                score -= 1
    return score

depth = 2
me = 1 # black
round = 7
# Test the minimax function
print(minimax(depth, round, me, True, float('-inf'), float('inf')))
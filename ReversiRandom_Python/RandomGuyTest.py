DIRECTIONS = [
    (-1, -1), (-1, 0), (-1, 1),
    (0, -1),          (0, 1),
    (1, -1),  (1, 0), (1, 1)
]

def getNewState(state, move, player, round):
    new_state = [row[:] for row in state]
    row, col = move // 8, move % 8

    if round < 4:
        new_state[row][col] = player
        return new_state

    for dx, dy in DIRECTIONS:
        r, c = row + dx, col + dy
        captured = []
        while 0 <= r < 8 and 0 <= c < 8 and new_state[r][c] not in [0, player]:
            captured.append((r, c))
            r += dx
            c += dy

        if 0 <= r < 8 and 0 <= c < 8 and new_state[r][c] == player:
            for rr, cc in captured:
                new_state[rr][cc] = player

    new_state[row][col] = player
    return new_state



reversi_board = [
    [0, 0, 0, 0, 0, 0, 0, 0],
    [0, 0, 0, 0, 0, 0, 0, 0],
    [0, 0, 0, 0, 0, 0, 0, 0],
    [0, 0, 0, 0, 1, 0, 0, 0],
    [0, 0, 0, 0, 1, 0, 0, 0],
    [0, 0, 0, 0, 1, 0, 0, 0],
    [0, 0, 0, 0, 2, 0, 0, 0],
    [0, 0, 0, 0, 0, 0, 0, 0],
]

# Test the get_new_state function
new_state = getNewState(reversi_board, 20, 2, 15)
for row in new_state:
    print(row)

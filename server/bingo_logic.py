import random

class BingoCard:

    def __init__(self):
        # generează cartela 5×5
        nums = random.sample(range(1, 76), 25)
        self.grid = [[0]*5 for _ in range(5)]
        self.marked = [[False]*5 for _ in range(5)]

        idx = 0
        for r in range(5):
            for c in range(5):
                self.grid[r][c] = nums[idx]
                idx += 1

    def serialize(self):
        flat = []
        for r in range(5):
            for c in range(5):
                flat.append(str(self.grid[r][c]))
        return ",".join(flat)     # OBLIGATORIU! ion handle client trimitem cartela ca si lista

    def mark_number(self, number):
        for r in range(5):
            for c in range(5):
                if self.grid[r][c] == number:
                    self.marked[r][c] = True

    def has_bingo(self):
        # linii
        for r in range(5):
            if all(self.marked[r][c] for c in range(5)):
                return True

        # coloane
        for c in range(5):
            if all(self.marked[r][c] for r in range(5)):
                return True

        # diagonale
        if all(self.marked[i][i] for i in range(5)):
            return True
        if all(self.marked[i][4-i] for i in range(5)):
            return True

        return False

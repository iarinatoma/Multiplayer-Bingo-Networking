import socket
import threading
import time
import random

from bingo_logic import BingoCard
from udp_discovery import start_udp_discovery

TCP_PORT = 6000
clients = []              # (conn, addr, bingo_card)
lock = threading.Lock()
game_started = False
winner_found = False

# =============================
# Handle Client TCP Connection
# =============================

def handle_client(conn, addr, bingo_card):
    print(f"[TCP] Client connected: {addr}")

    # Trimitem cartela BINGO cu \n la final
    card_msg = "CARD|" + bingo_card.serialize() + "\n"
    conn.sendall(card_msg.encode())

    while True:
        try:
            data = conn.recv(1024).decode().strip()
            if not data:
                break

            if data == "BINGO":
                print(f"[TCP] {addr} has claimed BINGO!")

                if bingo_card.has_bingo():
                    global winner_found
                    winner_found = True
                    print(f"[WINNER] {addr} WON!")
                    broadcast(f"WINNER|{addr}\n")
                else:
                    conn.sendall("INVALID|No Bingo\n".encode())

        except Exception as e:
            print("[ERROR handle_client]", e)
            break

    print(f"[TCP] Client disconnected: {addr}")
    conn.close()


#BROADCAST#
def broadcast(message):
    with lock:
        for (conn, addr, card) in clients:
            try:
                # fiecare mesaj SE TERMINĂ cu \n
                if not message.endswith("\n"):
                    message += "\n"
                conn.sendall(message.encode())
            except:
                pass


# =============================
# Number Generator
# =============================
def start_number_generator():
    global game_started

    # Așteptăm până avem cel puțin un client
    while len(clients) == 0:
        time.sleep(1)

    game_started = True
    numbers = list(range(1, 76))
    random.shuffle(numbers)

    print("[GAME] Starting Bingo game...")
    broadcast("START\n")

    for number in numbers:
        if winner_found:
            print("[GAME] Winner found — stopping number generator.")
            break

        print(f"[GAME] Number: {number}")

        # Marcam numărul pe cartela fiecărui client
        with lock:
            for (_, _, card) in clients:
                card.mark_number(number)

        broadcast(f"NUMBER|{number}\n")
        time.sleep(2)   # pauză între numere


# =============================
# TCP Server
# =============================
def start_tcp_server():
    server = socket.socket(socket.AF_INET, socket.SOCK_STREAM) # creeaza un socket tcp
    server.bind(("0.0.0.0", TCP_PORT)) #bind la portu 6000
    server.listen(10) # astept sa vina clienti

    print(f"[TCP] Server running on port {TCP_PORT}...")

    while True:
        conn, addr = server.accept() # acceptu blocheaza pana intra un client
        card = BingoCard() # se  creeaza un bingocard unif pentru fiecare student

        with lock:
            clients.append((conn, addr, card)) # pentru fiecare client se gaba clientu la lista de clienti

        thread = threading.Thread(target=handle_client, args=(conn, addr, card)) # se deschide un  thread diferit pentru fiecare client
        thread.start()


# =============================
# MAIN
# =============================
if __name__ == "__main__":
    print("=== Bingo Multiplayer Server ===")

    # Pornește UDP discovery
    # din folderu udp_discovery se va apela start_udp_discovery unde basically el asculta pe port 5000 si raspunde oricarui client care intreaba  DISCOVER_SERVER cu SERVER_HERE|TCP=6000
    udp_thread = threading.Thread(target=start_udp_discovery, daemon=True)
    udp_thread.start()

    # Pornește TCP server
    # dupa ce conexiunea a fost asigurata se trimit cardurile, apoi numerele, si ulterior castigatorii
    # threadul creeaza un socket utcp, il leaga la portu 6000 asulta conexiuni (sa se zica castigator)
    tcp_thread = threading.Thread(target=start_tcp_server, daemon=True)
    tcp_thread.start()

    # pornirea generatorului de numere bingo
    # Pornește generatorul de numere
    generator_thread = threading.Thread(target=start_number_generator, daemon=True)
    generator_thread.start()

    # Ținem serverul deschis
    while True:
        time.sleep(1)

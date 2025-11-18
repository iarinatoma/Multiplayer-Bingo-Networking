import socket
import threading

UDP_PORT = 5555
TCP_PORT = 6000

def start_udp_discovery():
    # creeaza un socket udp
    print("[DEBUG] Creating UDP socket...")
    sock = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
    print("[DEBUG] Socket created. Binding...")
    sock.bind(("0.0.0.0", UDP_PORT)) # asculta pe portu 5555

    print(f"[UDP] Discovery server running on port {UDP_PORT}...")

    while True:
        data, addr = sock.recvfrom(1024) # loop infinit
        message = data.decode().strip()

        if message == "DISCOVER_BINGO_SERVER":
            print(f"[UDP] Discovery request from {addr}")
            response = f"SERVER_HERE|TCP={TCP_PORT}"
            sock.sendto(response.encode(), addr)

# Multiplayer Bingo — Java Client & Python Server

A client–server Bingo game built for a computer networking project. The graphical client is written in **Java (Swing)** and runs on **Windows**, while the game server is written in **Python** and runs in **WSL (Linux)** on the same machine.

The application uses **UDP to discover the server** and **TCP to exchange game messages**. A complete game has been manually tested in this Windows–WSL configuration, including card delivery, number updates, a Bingo claim, server-side validation, and winner notification.

## Features

- Java Swing interface with a **5 × 5 Bingo card** and a **BINGO!** button.
- A separate, randomly generated card for each client: **25 distinct numbers selected from 1–75**.
- Server-side drawing of numbers **without repetition**, in randomized order, approximately every **2 seconds**.
- Automatic marking of matching numbers in green on the client.
- Bingo detection across **rows, columns, and both diagonals** (five marked cells; there is no free center square).
- Client-side Bingo detection and a manual Bingo button; the **server independently validates** submitted claims against the numbers it has drawn.
- TCP server code that accepts multiple client connections and starts a separate handler thread for each. **Simultaneous multiplayer gameplay has not been fully verified.**

## Architecture

```text
                 Same Windows computer

  Windows: Java client                    WSL: Python server
  --------------------                    ------------------
  Main.java
    |-- UDPDiscovery.java -- UDP :5555 --> udp_discovery.py
    |   (queries WSL IP) <-- server info --  (returns TCP=6000)
    |
    |-- TCPClient.java <-- TCP :6000 ----> server.py
    |                                        |-- client threads
    |-- UI.java (Swing)                      |-- number generator
    |-- BingoCard.java                       `-- bingo_logic.py
    |   (display & local checks)                 (cards & validation)
```

### How server discovery actually works

1. The **Windows Java client** executes `wsl.exe hostname -I` and takes the first IP address returned by WSL.
2. The client sends the UDP message `DISCOVER_BINGO_SERVER` **directly to that IP** on port **5555**.
3. The Python UDP listener replies with `SERVER_HERE|TCP=6000`.
4. The client connects to the discovered WSL IP using **TCP port 6000**.

This is **UDP unicast discovery directed at WSL**, not automatic discovery of arbitrary servers on a local network. Although `setBroadcast(true)` appears in the Java source, the current destination is the specific WSL IP, rather than a broadcast address.

**An internet connection is not required** for the Windows–WSL setup. Changing Wi-Fi networks does not, by itself, require an IP address to be updated in the code; connectivity still depends on the local Windows/WSL and firewall configuration. The current client is **not designed for automatic discovery across different physical computers or the public internet**.

## Project structure

```text
proiect_retele_bingo/
├── server/
│   ├── server.py           # TCP server, client handlers, broadcasts, number drawing
│   ├── bingo_logic.py      # Server-side card creation, marking and win validation
│   └── udp_discovery.py    # UDP server discovery responder
├── client/
│   └── java/
│       └── src/
│           ├── Main.java          # Client entry point
│           ├── UDPDiscovery.java  # WSL IP lookup and UDP discovery
│           ├── TCPClient.java     # TCP connection and message handling
│           ├── UI.java            # Swing game window and number highlighting
│           └── BingoCard.java     # Client-side card data and Bingo checks
├── .gitignore
└── README.md
```

Only source files are needed for the repository. Generated `.class` / `.pyc` files, `__pycache__` folders, and the `UDPDiscovery.java.save` backup are not part of the runnable source set.

## Requirements

- **Windows** with a working **WSL** installation and a Linux distribution.
- **Python 3** available inside WSL (`python3 --version`).
- **JDK 11+** on Windows, with `java` and `javac` available in PowerShell.
- UDP port **5555** and TCP port **6000** available and reachable between Windows and WSL.
- **No third-party Python or Java dependencies**: the project uses Python's standard library and Java's standard library / Swing.

## Run the application

Unzip or clone the project first. Open **two terminals**: one in WSL for the server and one in Windows PowerShell for the client.

### 1. Start the Python server (WSL)

Navigate to the `server` folder in a WSL terminal. For example, if the project is in Windows Downloads:

```bash
cd /mnt/c/Users/YOUR_USERNAME/Downloads/proiect_retele_bingo/server
python3 server.py
```

Alternatively, if you copied the project into your WSL home directory:

```bash
cd ~/proiect_retele_bingo/server
python3 server.py
```

The server starts a UDP discovery listener on `5555`, a TCP listener on `6000`, and a number generator that **waits until at least one client has connected**. Leave this terminal running.

### 2. Start the Java client (Windows PowerShell)

In **a separate Windows PowerShell terminal** (not inside WSL), navigate to the Java source folder:

```powershell
cd "C:\Users\YOUR_USERNAME\Downloads\proiect_retele_bingo\client\java\src"
javac Main.java UI.java BingoCard.java TCPClient.java UDPDiscovery.java
java Main
```

Adjust the path to wherever you extracted the project. The client looks for the WSL server over UDP and, if discovery succeeds, opens the Bingo interface and connects via TCP.

### 3. Play

1. Each connected client receives its own 5 × 5 card from the server.
2. After the first client connects, the server begins drawing numbers from a shuffled list of **1–75**.
3. Clients receive `NUMBER` messages and mark matches automatically (green cells).
4. The Java client checks for a complete row, column or diagonal and can automatically send `BINGO`; the **BINGO!** button can also send a claim manually.
5. The server checks the claim using its own record of drawn numbers, sends `WINNER` to connected clients if valid, and stops drawing further numbers. An invalid claim receives `INVALID`.

To play another round with the current implementation, stop the Python server with **Ctrl+C**, start it again, then restart/reconnect the Java client. Close the Swing window to exit the client.

## Network protocol

| Transport | Direction | Message | Meaning |
| --- | --- | --- | --- |
| UDP :5555 | Client → server | `DISCOVER_BINGO_SERVER` | Discover the WSL server |
| UDP reply | Server → client | `SERVER_HERE\|TCP=6000` | Advertise the TCP port |
| TCP :6000 | Server → client | `CARD\|<25 comma-separated integers>` | Send a newly generated card |
| TCP | Server → clients | `START` | Signal that drawing has started |
| TCP | Server → clients | `NUMBER\|<integer>` | Broadcast a drawn number |
| TCP | Client → server | `BINGO` | Submit a win claim |
| TCP | Server → clients | `WINNER\|<client address>` | Announce the validated winner |
| TCP | Server → claimant | `INVALID\|No Bingo` | Reject a premature or invalid claim |

Game messages are sent as **newline-terminated text**. The winner value is the connected client's socket address, not a username.

## Implementation details and scope

- **Game state:** `server.py` maintains connected clients and records drawn numbers in each client's server-side `BingoCard`; `bingo_logic.py` checks winning lines.
- **Concurrency:** the server uses separate threads for UDP discovery, TCP listening, number drawing and individual client connections. Shared client-list access is protected in the main update/broadcast operations by a lock.
- **UI:** `UI.java` displays the card; `TCPClient.java` receives server messages in a background listener thread.
- **Discovery scope:** the WSL address is selected from `wsl.exe hostname -I`; it is not a LAN-wide discovery protocol, and the first returned address might not be usable in every WSL configuration.
- **Timeouts:** the current Java UDP receive operation has **no timeout**, so it can wait indefinitely if no discovery reply arrives.
- **Gameplay lifecycle:** this is a single-round prototype; no in-app reset or rematch is implemented. Players joining after drawing has begun are not synchronized with previously drawn numbers.
- **Multiplayer:** the TCP server implements multiple-client handling, but a full simultaneous multi-player session has not been independently demonstrated by the available test.
- **Security:** the protocol does not include authentication or encryption. Run it in a trusted environment; it is not a production-ready internet game server.

## Verification

The Java sources compile successfully, and all three Python source files pass syntax checks. A manual run of the **Windows Java client + WSL Python server** also showed numbers being received and highlighted, a Bingo claim being accepted by the server, a `WINNER` event, and the number generator stopping. These checks **do not** establish portability to other computers or full multiplayer behavior.

---

**Project focus:** Java/Python integration, socket programming, UDP discovery, TCP messaging, threading and a cross-environment Windows–WSL client–server application.

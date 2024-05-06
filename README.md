# Mastermind Multithreading Game

## Overview
This project implements the classic game of Mastermind using Java Socket Programming to enable networking capabilities. It is designed to create a game room application comprising a server and multiple clients, leveraging features from the Java 8 standard library.

## Server Implementation
- **Functionality:** The server is responsible for generating a secret code and managing game rounds by receiving guesses from clients and providing feedback.
- **Concurrency:** It supports multiple clients concurrently, allowing them to make guesses simultaneously to deduce the secret code.
- **Class and Method:** The server's primary class is `ServerMain.java`, which contains a `main()` method to initialize and run the server.

## Client Implementation
- **Interaction:** Clients communicate with the server by sending their guesses and receiving responses tailored to their guesses.
- **Gameplay Rules:** Each client operates independently with a limited number of guesses. The first client to correctly guess the secret code wins the round.
- **Waiting Mechanism:** If a client exhausts its guesses, it waits until the current round concludes before proceeding.
- **Class and Method:** Clients are implemented in `ClientMain.java`, which includes a `main()` method to execute the client program.

## Networking and Game Flow
- **Instant Replay:** Following the conclusion of a round, clients can immediately initiate a new game once the server announces the start of the next round. This setup ensures a seamless play experience without requiring acknowledgments from every client before proceeding.
- **Reliability Assumption:** It is assumed that clients will respond promptly. Scenarios involving delayed or absent responses are not considered critical edge cases for this implementation.

This project not only enhances understanding of network programming and Java sockets but also reinforces OOP design principles through practical application in a familiar game context.

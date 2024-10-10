package com.ayb;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Random;

/**
 * A simple server game where clients attempt to guess a secret number.
 * The server handles multiple clients and announces the winner when the correct number is guessed.
 */
public class NumberGuessingServer extends Thread {
    private boolean isServerActive = true; // Flag to indicate if the server is running
    private int clientCount = 0; // Counter for the number of connected clients
    private int secretNumber; // The secret number that clients need to guess
    private boolean gameEnded = false; // Flag to indicate if the game has ended
    private String winner; // Stores the IP address of the winning client

    public static void main(String[] args) {
        new NumberGuessingServer().start(); // Start the server thread
    }

    @Override
    public void run() {
        try {
            // Start the server on port 1234 and generate a random secret number
            ServerSocket serverSocket = new ServerSocket(1234);
            secretNumber = new Random().nextInt(1000); // Generate a random number between 0 and 1000

            // Keep the server running and accept client connections
            while (isServerActive) {
                Socket clientSocket = serverSocket.accept(); // Wait for a client to connect
                clientCount++; // Increment client count on each new connection
                new ClientHandler(clientSocket, clientCount).start(); // Start a new thread for each client
            }
        } catch (IOException e) {
            throw new RuntimeException(e); // Handle exceptions by throwing a runtime exception
        }
    }

    /**
     * Handles the interaction with a connected client, including receiving guesses and providing feedback.
     */
    class ClientHandler extends Thread {
        private Socket clientSocket; // Socket for communication with the client
        private int clientNumber; // Unique number assigned to each client

        public ClientHandler(Socket clientSocket, int clientNumber) {
            this.clientSocket = clientSocket; // Initialize client socket
            this.clientNumber = clientNumber; // Initialize client number
        }

        @Override
        public void run() {
            try {
                // Set up input and output streams to communicate with the client
                InputStream inputStream = clientSocket.getInputStream();
                InputStreamReader isr = new InputStreamReader(inputStream);
                BufferedReader br = new BufferedReader(isr);

                PrintWriter pw = new PrintWriter(clientSocket.getOutputStream(), true); // Output stream to send messages to the client
                String clientIp = clientSocket.getRemoteSocketAddress().toString(); // Get client IP address

                // Send a welcome message to the client
                pw.println("Welcome to the guessing game server, you are client number " + clientNumber);
                System.out.println("Connection from client number " + clientNumber + ", IP: " + clientIp);
                pw.println("Guess the secret number...");

                // Continuously handle guesses from the client
                while (true) {
                    String request = br.readLine(); // Read client input
                    int guessedNumber = 0;
                    boolean isValidNumber = false;

                    // Try to parse the guess from the client, handle invalid input
                    try {
                        guessedNumber = Integer.parseInt(request);
                        isValidNumber = true; // If parsing is successful, the format is correct
                    } catch (NumberFormatException e) {
                        isValidNumber = false; // If parsing fails, the format is incorrect
                    }

                    // If the input is valid, proceed with game logic
                    if (isValidNumber) {
                        System.out.println("Client " + clientIp + " guessed number " + guessedNumber);

                        if (!gameEnded) {
                            // Provide feedback based on the client's guess
                            if (guessedNumber > secretNumber) {
                                pw.println("Your number is greater than the secret number.");
                            } else if (guessedNumber < secretNumber) {
                                pw.println("Your number is less than the secret number.");
                            } else {
                                // The client guessed the correct number
                                pw.println("Bingo! You are the winner!");
                                winner = clientIp; // Store the winner's IP address
                                System.out.println("The winner is " + clientIp);
                                gameEnded = true; // End the game once the correct number is guessed
                            }
                        } else {
                            // Inform the client that the game has ended
                            pw.println("Game over, the winner is " + winner);
                        }
                    } else {
                        // If the input format is incorrect, notify the client
                        pw.println("Incorrect number format, please enter a valid integer.");
                    }
                }
            } catch (IOException e) {
                throw new RuntimeException(e); // Handle exceptions during client interaction
            }
        }
    }
}

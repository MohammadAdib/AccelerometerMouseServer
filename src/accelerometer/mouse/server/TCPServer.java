package accelerometer.mouse.server;

import java.io.*;
import java.net.*;
import java.util.LinkedList;
import javax.swing.JOptionPane;

class TCPServer {
    //Picks a random port from 49151 to 65535

    private int port = new java.util.Random().nextInt(16384) + 49151;
    private ServerSocket serverSocket;
    int clients = 0, packetCount = 0, packPerSec = 0, packetsPerSec = 0;
    public static boolean paused = false;
    private boolean running = true;
    private LinkedList<String> messages = new LinkedList<String>();
    private String lastMessage = "0,0,0,false,false,false,false";
    public String serverStatus = "", clientStatus = "";

    public TCPServer(String ip, int port) {
        //Constructor
        try {
            messages.add(lastMessage);
            this.port = port;
            serverSocket = new ServerSocket(this.port, 0, InetAddress.getByName(ip));
            System.out.println("Server started on socket " + InetAddress.getByName(ip) + ":" + port);
            serverStatus = "Listening on port " + this.port;
            clientStatus = "None Connected";
            Runnable r = new Runnable() {

                @Override
                public void run() {
                    while (running) {
                        try {
                            if (clients > 0) {
                                System.out.println("Packets/Second: " + packPerSec);
                                packPerSec = 0;
                                Thread.sleep(1000);
                                packetsPerSec = packPerSec;
                            }
                        } catch (InterruptedException ex) {
                        }
                    }
                }
            };
            new Thread(r).start();
        } catch (Exception e) {
            //Server socket cannot be created
            JOptionPane.showMessageDialog(null, "Failed to open socket on port " + port + "!");
            serverStatus = "Error";
            clientStatus = "None Connected";
            e.printStackTrace();
        }
    }

    public void start() {
        running = true;
        Runnable server = new Runnable() {

            public void run() {
                try {
                    while (running) {
                        //Constructor blocks until a client connects
                        Socket clientSocket = serverSocket.accept();
                        //New client has joined the server
                        System.out.println("New client connected: " + clientSocket.getInetAddress());
                        serverStatus = "Connected on port " + port;
                        clients++;
                        listen(clientSocket);
                        Thread.sleep(250);
                    }
                    serverSocket.close();
                } catch (Exception e) {
                    //Error occured
                    System.out.println("Error occured: " + e.getMessage());
                    clients--;
                    if (clients == 0) {
                        serverStatus = "Listening on port " + port;
                        clientStatus = "None Connected";
                    }
                }
            }
        };
        new Thread(server).start();
    }

    public boolean isRunning() {
        return running;
    }

    public boolean isConnected() {
        return clients > 0;
    }

    private void listen(final Socket socket) {
        Runnable r = new Runnable() {

            public void run() {
                while (running) {
                    try {
                        BufferedReader inFromClient = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                        String clientInput = inFromClient.readLine();
                        if (clientInput.contains("|")) {
                            //Input is from Windows Phone
                            clientInput = clientInput.substring(0, clientInput.indexOf("|"));
                        }
                        packetCount++;
                        packPerSec++;
                        System.out.println("Packet #" + packetCount + ": " + clientInput);
                        if (clientInput.contains("jam")) {
                            clients--;
                            break;
                        }
                        if (clientInput != null) {
                            if (clientInput.contains("paused")) {
                                paused = true;
                                clientStatus = "Paused";
                            } else {
                                lastMessage = clientInput;
                                messages.add(lastMessage);
                                paused = false;
                                clientStatus = "Active";
                            }
                        }
                        if (clientInput == null) {
                            clients--;
                            if (clients == 0) {
                                serverStatus = "Listening on port " + port;
                                clientStatus = "None Connected";
                            }
                            System.out.println("Client DC'd");
                            break;
                        }
                        if (messages.size() > 2500) {
                            messages.remove(0);
                        }
                        Thread.sleep(20);
                    } catch (Exception e) {
                        System.err.println("Error: " + e.getMessage());
                        clients--;
                        if (clients == 0) {
                            serverStatus = "Listening on port " + port;
                            clientStatus = "None Connected";
                        }
                        System.out.println("Client DC'd");
                        break;
                    }
                }
            }
        };
        //Starts the listening process on a new thread
        new Thread(r).start();
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public String[] getAllMessages() {
        return messages.toArray(new String[messages.size()]);
    }

    public void stop() {
        try {
            serverSocket.close();
        } catch (Exception e) {
        }
        running = false;
    }
}
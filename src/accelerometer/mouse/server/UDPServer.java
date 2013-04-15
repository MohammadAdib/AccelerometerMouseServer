package accelerometer.mouse.server;

import java.net.*;

public class UDPServer {

    private boolean running = true, discoverable = true;
    
    public UDPServer() {
    }

    public void start() {
        Runnable r = new Runnable() {
            @Override
            public void run() {
                try {
                    DatagramSocket serverSocket = new DatagramSocket(18250);
                    byte[] receiveData = new byte[1024];
                    byte[] sendData = new byte[1024];
                    while (running) {
                        DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                        serverSocket.receive(receivePacket);
                        String input = new String(receivePacket.getData());
                        InetAddress IPAddress = receivePacket.getAddress();
                        int port = receivePacket.getPort();
                        System.out.println(IPAddress.getHostAddress().toString() + ":" + port + " - " + input);
                        String capitalizedSentence = "CONFACK";
                        sendData = capitalizedSentence.getBytes();
                        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, port);
                        if(discoverable) serverSocket.send(sendPacket);
                        Thread.sleep(50);
                    }
                } catch (Exception e) {
                }
            }
        };
        new Thread(r).start();
    }
    
    
    public void setDiscoverable(boolean b) {
        discoverable = b;
    }
    
    public void stop() {
        running = false;
    }
}

package com.example.network;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class LanDiscovery {

    public static final int DISCOVERY_PORT = 5556;
    private static final int SCAN_TIMEOUT_MS = 1500;

    public static class DiscoveredGame {
        public String roomCode;
        public String hostName;
        public int    currentPlayers;
        public int    maxPlayers;
        public String ip;
        public int    port;
    }

    private DatagramSocket responderSocket;
    private Thread responderThread;

    public void startResponder(String roomCode, String hostName,
                               int currentPlayers, int maxPlayers) {
        stopResponder();
        try {
            responderSocket = new DatagramSocket(DISCOVERY_PORT);
            responderSocket.setBroadcast(true);
            final String reply = NetworkMessage.DISCOVER_RESP
                    + "|" + roomCode + "|" + hostName
                    + "|" + currentPlayers + "|" + maxPlayers;

            responderThread = new Thread(() -> {
                byte[] buf = new byte[256];
                while (!Thread.currentThread().isInterrupted()) {
                    try {
                        DatagramPacket pkt = new DatagramPacket(buf, buf.length);
                        responderSocket.receive(pkt);
                        String msg = new String(pkt.getData(), 0, pkt.getLength()).trim();
                        if (NetworkMessage.DISCOVER_REQ.equals(msg)) {
                            byte[] data = reply.getBytes();
                            DatagramPacket resp = new DatagramPacket(
                                data, data.length, pkt.getAddress(), pkt.getPort());
                            responderSocket.send(resp);
                        }
                    } catch (Exception e) {
                        break;
                    }
                }
            }, "discovery-responder");
            responderThread.setDaemon(true);
            responderThread.start();
        } catch (Exception e) {
            System.err.println("LanDiscovery responder error: " + e.getMessage());
        }
    }

    public void updatePlayerCount(String roomCode, String hostName,
                                  int currentPlayers, int maxPlayers) {
        stopResponder();
        startResponder(roomCode, hostName, currentPlayers, maxPlayers);
    }

    public void stopResponder() {
        if (responderThread != null) responderThread.interrupt();
        if (responderSocket != null && !responderSocket.isClosed())
            responderSocket.close();
    }

    public static List<DiscoveredGame> scan(Consumer<DiscoveredGame> onFound) {
        List<DiscoveredGame> results = new ArrayList<>();
        try (DatagramSocket socket = new DatagramSocket()) {
            socket.setBroadcast(true);
            socket.setSoTimeout(SCAN_TIMEOUT_MS);

            byte[] req = NetworkMessage.DISCOVER_REQ.getBytes();
            DatagramPacket broadcast = new DatagramPacket(
                req, req.length,
                InetAddress.getByName("255.255.255.255"), DISCOVERY_PORT);
            socket.send(broadcast);

            long deadline = System.currentTimeMillis() + SCAN_TIMEOUT_MS;
            byte[] buf = new byte[512];
            while (System.currentTimeMillis() < deadline) {
                try {
                    DatagramPacket pkt = new DatagramPacket(buf, buf.length);
                    socket.receive(pkt);
                    String raw = new String(pkt.getData(), 0, pkt.getLength()).trim();
                    String[] parts = raw.split("\\|", -1);
                    if (parts.length >= 5
                            && NetworkMessage.DISCOVER_RESP.equals(parts[0])) {
                        DiscoveredGame g = new DiscoveredGame();
                        g.roomCode = parts[1];
                        g.hostName = parts[2];
                        g.currentPlayers = Integer.parseInt(parts[3]);
                        g.maxPlayers = Integer.parseInt(parts[4]);
                        g.ip = pkt.getAddress().getHostAddress();
                        try {
                            String[] decoded = RoomCode.decode(g.roomCode);
                            g.port = Integer.parseInt(decoded[1]);
                        } catch (Exception ignored) { g.port = 5555; }
                        results.add(g);
                        if (onFound != null) onFound.accept(g);
                    }
                } catch (java.net.SocketTimeoutException ignored) {
                    break;
                }
            }
        } catch (Exception e) {
            System.err.println("LanDiscovery scan error: " + e.getMessage());
        }
        return results;
    }
}

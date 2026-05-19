package com.example.network;

import java.net.InetAddress;

public final class RoomCode {

    private RoomCode() {}

    public static String encode(String ip, int port) {
        try {
            byte[] bytes = InetAddress.getByName(ip).getAddress();
            long ipInt = ((bytes[0] & 0xFFL) << 24)
                       | ((bytes[1] & 0xFFL) << 16)
                       | ((bytes[2] & 0xFFL) << 8)
                       |  (bytes[3] & 0xFFL);
            long combined = (ipInt << 16) | (port & 0xFFFFL);
            return Long.toString(combined, 36).toUpperCase();
        } catch (Exception e) {
            return "INVALID";
        }
   }

    public static String[] decode(String code) throws Exception {
        long combined = Long.parseLong(code.toLowerCase(), 36);
        int port = (int)(combined & 0xFFFFL);
        long ipInt = combined >> 16;
        String ip = ((ipInt >> 24) & 0xFF) + "."
                  + ((ipInt >> 16) & 0xFF) + "."
                  + ((ipInt >>  8) & 0xFF) + "."
                  +  (ipInt & 0xFF);
        return new String[]{ ip, String.valueOf(port) };
    }

    public static String getLocalIp() {
        try {
            java.util.Enumeration<java.net.NetworkInterface> nets =
                java.net.NetworkInterface.getNetworkInterfaces();
            while (nets.hasMoreElements()) {
                java.net.NetworkInterface ni = nets.nextElement();
                if (!ni.isUp() || ni.isLoopback() || ni.isVirtual()) continue;
                java.util.Enumeration<InetAddress> addrs = ni.getInetAddresses();
                while (addrs.hasMoreElements()) {
                    InetAddress a = addrs.nextElement();
                    if (a instanceof java.net.Inet4Address && !a.isLoopbackAddress()) {
                        return a.getHostAddress();
                    }
                }
            }
        } catch (Exception ignored) {}
        return "127.0.0.1";
    }
}

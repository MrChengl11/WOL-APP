package com.example.wakeonlan;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Locale;

public class WakeOnLan {

    private WakeOnLan() {
    }

    public static boolean isValidMac(String mac) {
        String cleaned = cleanMac(mac);
        if (cleaned == null || cleaned.length() != 12) {
            return false;
        }
        for (int i = 0; i < cleaned.length(); i++) {
            char c = cleaned.charAt(i);
            if (!isHex(c)) {
                return false;
            }
        }
        return true;
    }

    public static void wake(String mac, String host, int port) throws Exception {
        byte[] macBytes = parseMac(mac);
        byte[] packet = new byte[6 + 16 * macBytes.length];

        for (int i = 0; i < 6; i++) {
            packet[i] = (byte) 0xFF;
        }
        for (int i = 0; i < 16; i++) {
            System.arraycopy(macBytes, 0, packet, 6 + i * macBytes.length, macBytes.length);
        }

        DatagramSocket socket = new DatagramSocket();
        try {
            socket.setBroadcast(true);
            InetAddress address = InetAddress.getByName(host);
            DatagramPacket datagramPacket = new DatagramPacket(packet, packet.length, address, port);
            socket.send(datagramPacket);
        } finally {
            socket.close();
        }
    }

    private static byte[] parseMac(String mac) {
        String cleaned = cleanMac(mac);
        byte[] bytes = new byte[6];
        for (int i = 0; i < 6; i++) {
            int value = Integer.parseInt(cleaned.substring(i * 2, i * 2 + 2), 16);
            bytes[i] = (byte) value;
        }
        return bytes;
    }

    private static String cleanMac(String mac) {
        if (mac == null) {
            return null;
        }
        String cleaned = mac.replace(":", "").replace("-", "").replace(".", "").replace(" ", "");
        return cleaned.toUpperCase(Locale.US);
    }

    private static boolean isHex(char c) {
        return (c >= '0' && c <= '9') || (c >= 'A' && c <= 'F');
    }
}

package com.example.wakeonlan;

public class Device {
    private String name;
    private String mac;
    private String broadcastAddress;
    private int port;

    public Device() {
        this("", "", "255.255.255.255", 9);
    }

    public Device(String name, String mac, String broadcastAddress, int port) {
        this.name = name;
        this.mac = mac;
        this.broadcastAddress = broadcastAddress;
        this.port = port;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMac() {
        return mac;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }

    public String getBroadcastAddress() {
        return broadcastAddress;
    }

    public void setBroadcastAddress(String broadcastAddress) {
        this.broadcastAddress = broadcastAddress;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }
}

package com.example.wakeonlan;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class DeviceStorage {
    private static final String PREFS_NAME = "wol_devices";
    private static final String KEY_DEVICES = "devices";

    private DeviceStorage() {
    }

    public static List<Device> load(Context context) {
        List<Device> devices = new ArrayList<>();
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String json = prefs.getString(KEY_DEVICES, "");
        if (json.isEmpty()) {
            return devices;
        }
        try {
            JSONArray array = new JSONArray(json);
            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = array.getJSONObject(i);
                Device device = new Device();
                device.setName(obj.optString("name", ""));
                device.setMac(obj.optString("mac", ""));
                device.setBroadcastAddress(obj.optString("broadcastAddress", "255.255.255.255"));
                device.setPort(obj.optInt("port", 9));
                devices.add(device);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return devices;
    }

    public static void save(Context context, List<Device> devices) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        JSONArray array = new JSONArray();
        try {
            for (Device device : devices) {
                JSONObject obj = new JSONObject();
                obj.put("name", device.getName());
                obj.put("mac", device.getMac());
                obj.put("broadcastAddress", device.getBroadcastAddress());
                obj.put("port", device.getPort());
                array.put(obj);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        prefs.edit().putString(KEY_DEVICES, array.toString()).apply();
    }
}

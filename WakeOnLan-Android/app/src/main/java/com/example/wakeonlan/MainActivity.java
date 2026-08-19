package com.example.wakeonlan;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {

    private static final Pattern IPV4_PATTERN = Pattern.compile(
            "^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$"
    );

    private List<Device> devices = new ArrayList<>();
    private DeviceListAdapter adapter;
    private ListView listView;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        listView = findViewById(R.id.deviceList);
        Button addButton = findViewById(R.id.addButton);

        devices = DeviceStorage.load(this);
        adapter = new DeviceListAdapter(this, devices);
        listView.setAdapter(adapter);

        addButton.setOnClickListener(v -> showDeviceDialog(null));
    }

    private void showDeviceDialog(final Device existing) {
        final boolean isEdit = existing != null;

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        int padding = dp(16);
        layout.setPadding(padding, 0, padding, 0);

        final EditText nameInput = new EditText(this);
        nameInput.setHint(R.string.device_name);
        nameInput.setSingleLine(true);

        final EditText macInput = new EditText(this);
        macInput.setHint(R.string.hint_mac);
        macInput.setSingleLine(true);
        macInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);

        final EditText addressInput = new EditText(this);
        addressInput.setHint(R.string.hint_broadcast);
        addressInput.setSingleLine(true);
        addressInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_URI);

        final EditText portInput = new EditText(this);
        portInput.setHint(R.string.port);
        portInput.setSingleLine(true);
        portInput.setInputType(InputType.TYPE_CLASS_NUMBER);

        layout.addView(nameInput);
        layout.addView(macInput);
        layout.addView(addressInput);
        layout.addView(portInput);

        if (isEdit) {
            nameInput.setText(existing.getName());
            macInput.setText(existing.getMac());
            addressInput.setText(existing.getBroadcastAddress());
            portInput.setText(String.valueOf(existing.getPort()));
        } else {
            addressInput.setText("255.255.255.255");
            portInput.setText("9");
        }

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(isEdit ? R.string.edit_device : R.string.add_device)
                .setView(layout)
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.save, null)
                .create();

        dialog.setOnShowListener(d -> dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String name = nameInput.getText().toString().trim();
            String mac = macInput.getText().toString().trim();
            String address = addressInput.getText().toString().trim();
            String portText = portInput.getText().toString().trim();

            if (name.isEmpty()) {
                Toast.makeText(this, R.string.empty_name, Toast.LENGTH_SHORT).show();
                return;
            }
            if (!WakeOnLan.isValidMac(mac)) {
                Toast.makeText(this, R.string.invalid_mac, Toast.LENGTH_SHORT).show();
                return;
            }
            if (!isValidIp(address)) {
                Toast.makeText(this, R.string.invalid_broadcast, Toast.LENGTH_SHORT).show();
                return;
            }

            int port;
            try {
                port = Integer.parseInt(portText);
            } catch (NumberFormatException e) {
                port = 9;
            }
            if (port < 1 || port > 65535) {
                port = 9;
            }

            if (isEdit) {
                existing.setName(name);
                existing.setMac(mac);
                existing.setBroadcastAddress(address);
                existing.setPort(port);
            } else {
                devices.add(new Device(name, mac, address, port));
            }
            DeviceStorage.save(this, devices);
            adapter.notifyDataSetChanged();
            dialog.dismiss();
        }));

        dialog.show();
    }

    private void confirmDelete(final Device device, final int position) {
        new AlertDialog.Builder(this)
                .setTitle(device.getName())
                .setMessage(R.string.confirm_delete)
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.delete, (dialog, which) -> {
                    devices.remove(position);
                    DeviceStorage.save(this, devices);
                    adapter.notifyDataSetChanged();
                })
                .show();
    }

    private void sendWake(final Device device) {
        new Thread(() -> {
            try {
                WakeOnLan.wake(device.getMac(), device.getBroadcastAddress(), device.getPort());
                mainHandler.post(() -> Toast.makeText(this, R.string.wol_sent, Toast.LENGTH_SHORT).show());
            } catch (Exception e) {
                e.printStackTrace();
                mainHandler.post(() -> Toast.makeText(this, getString(R.string.wol_failed) + ": " + e.getMessage(), Toast.LENGTH_LONG).show());
            }
        }).start();
    }

    private boolean isValidIp(String ip) {
        return ip != null && IPV4_PATTERN.matcher(ip.trim()).matches();
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }

    private class DeviceListAdapter extends BaseAdapter {

        private final Context context;
        private final List<Device> items;

        DeviceListAdapter(Context context, List<Device> items) {
            this.context = context;
            this.items = items;
        }

        @Override
        public int getCount() {
            return items.size();
        }

        @Override
        public Device getItem(int position) {
            return items.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                convertView = LayoutInflater.from(context).inflate(R.layout.item_device, parent, false);
                holder = new ViewHolder();
                holder.name = convertView.findViewById(R.id.deviceName);
                holder.mac = convertView.findViewById(R.id.deviceMac);
                holder.address = convertView.findViewById(R.id.deviceAddress);
                holder.wake = convertView.findViewById(R.id.btnWake);
                holder.edit = convertView.findViewById(R.id.btnEdit);
                holder.delete = convertView.findViewById(R.id.btnDelete);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            final Device device = getItem(position);
            holder.name.setText(device.getName());
            holder.mac.setText(device.getMac());
            holder.address.setText(device.getBroadcastAddress() + ":" + device.getPort());

            holder.wake.setOnClickListener(v -> sendWake(device));
            holder.edit.setOnClickListener(v -> showDeviceDialog(device));
            holder.delete.setOnClickListener(v -> confirmDelete(device, position));

            return convertView;
        }
    }

    private static class ViewHolder {
        TextView name;
        TextView mac;
        TextView address;
        Button wake;
        Button edit;
        Button delete;
    }
}

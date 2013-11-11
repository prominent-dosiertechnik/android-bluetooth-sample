//Copyright (c) 2013, ProMinent Dosiertechnik GmbH
//
//Permission to use, copy, modify, and/or distribute this software for any
//purpose with or without fee is hereby granted, provided that the above
//copyright notice and this permission notice appear in all copies.
//
//THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
//WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
//MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR
//ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
//WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
//ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF
//OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.

package com.example.testapp;

import java.util.*;
import android.os.Bundle;
import android.app.Activity;
import android.bluetooth.*;
import android.content.*;
import android.view.*;
import android.widget.*;

public class LambdaNativeSelectBluetoothDeviceActivity extends Activity {
	private ProgressBar progressBar;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_lambda_native_select_bluetooth_device);

		progressBar = ((ProgressBar) findViewById(R.id.progressBar1));
		
		final List<LambdaNativeBluetoothDevice> devicesFound = new ArrayList<LambdaNativeBluetoothDevice>();

		final ListView btDeviceList = (ListView) findViewById(R.id.btDeviceList);
		btDeviceList.clearChoices();
		final ArrayAdapter<LambdaNativeBluetoothDevice> arrayAdapter = new ArrayAdapter<LambdaNativeBluetoothDevice>(
				this, android.R.layout.simple_list_item_1, devicesFound);
		btDeviceList.setAdapter(arrayAdapter);

		LambdaNativeBluetoothAdapter adapter = null;

		try {
			adapter = LambdaNativeBluetoothAdapter.getInstance();
		} catch (Exception ex) {
		}

		if (adapter == null)
			finish();

		adapter.cancelDiscovery(); // Restart a new discovery

		registerReceiver(new BroadcastReceiver() {
			@Override
			public void onReceive(Context arg0, Intent arg1) {
				final BluetoothDevice device = arg1.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
				devicesFound.add(new LambdaNativeBluetoothDevice(device
						.getName(), device.getAddress()));
				arrayAdapter.notifyDataSetChanged();
			}
		}, new IntentFilter(BluetoothDevice.ACTION_FOUND));

		registerReceiver(new BroadcastReceiver() {
			@Override
			public void onReceive(Context arg0, Intent arg1) {
				progressBar.setVisibility(View.INVISIBLE);
			}
		}, new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED));

		btDeviceList
				.setOnItemClickListener(new AdapterView.OnItemClickListener() {
					@Override
					public void onItemClick(AdapterView<?> parent, View view,
							int position, long id) {
						final Intent returnIntent = new Intent();
						returnIntent.putExtra("result",
								arrayAdapter.getItem(position).getMacAddress());

						// Check for a parent activity first, because a direct
						// setResult(...) will silently discard the result
						// otherwise
						if (getParent() == null)
							setResult(RESULT_OK, returnIntent);
						else
							getParent().setResult(RESULT_OK, returnIntent);

						finish();
					}
				});

		adapter.startDiscovery();
		progressBar.setVisibility(View.VISIBLE);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.lambda_native_select_bluetooth_device,
				menu);
		return true;
	}
}

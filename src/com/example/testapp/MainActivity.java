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

import java.io.IOException;

import android.os.Bundle;
import android.view.*;
import android.widget.TextView;
import android.app.*;
import android.content.*;

public class MainActivity extends Activity {
	// TODO:
	// Use request ID that doesn't clash with the others used by lambdanative
	private final int BT_SEL_DEVICE_REQ = 4711;

	// We'll abstract away the native BT interface:
	private LambdaNativeBluetoothAdapter adapter = null;
	private LambdaNativeBluetoothConnection conn = null;
	private Procedure<Intent> deviceSelectedContinuation = null;

	private TextView textView1;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		// Initialize the GUI. Standard Android stuff.
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		textView1 = (TextView) findViewById(R.id.textView1);
		textView1.setText("");

		// Connect to the BT adapter, close program if BT not enabled
		try {
			adapter = LambdaNativeBluetoothAdapter.getInstance();
		} catch (Exception ex) {
			caught("onCreate", ex);
		}
	}

	private final StringBuilder output = new StringBuilder();

	private synchronized void print(String what) {
		output.append(what);
		output.append('\n');
		refreshOutput();
	}

	private synchronized void printChar(char what) {
		output.append(what);
		refreshOutput();
	}

	private void refreshOutput() {
		textView1.post(new Runnable() {
			@Override
			public void run() {
				textView1.setText(output.toString());
			}
		});
	}

	private Thread readThread;

	private void readThreadLoop() {
		while (conn != null) {
			int i;
			try {
				i = conn.getInputStream().read();
			} catch (IOException e) {
				print("\n> IOException: " + e.getMessage());
				break;
			}

			if (i == -1) {
				print("\n> Connection closed.");
				break;
			}

			printChar((char) i);
		}
	}

	private void connect() {
		if (conn != null)
			disconnect();

		try {
			// Connect to BT device using a pseudo continuation to
			// keep the source together
			deviceSelectedContinuation = new Procedure<Intent>() {
				@Override
				public void run(Intent data) {
					final String address = data.getStringExtra("result");
					try {
						conn = adapter.connect(address);
						print("\n> Connection established :)");
						readThread = new Thread(new Runnable() {
							@Override
							public void run() {
								readThreadLoop();
							}
						});
						readThread.start();
					} catch (Exception ex) {
						caught("deviceSelectedContinuation", ex);
					}
				}
			};

			final Intent intent = new Intent(this,
					LambdaNativeSelectBluetoothDeviceActivity.class);
			startActivityForResult(intent, BT_SEL_DEVICE_REQ);
		} catch (Exception ex) {
			caught("connect", ex);
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// Second part of BT connecting: Just call the continuation if a BT
		// device was selected
		if (requestCode == BT_SEL_DEVICE_REQ && resultCode == RESULT_OK) {
			if (deviceSelectedContinuation != null) {
				deviceSelectedContinuation.run(data);
				deviceSelectedContinuation = null;
			}
		}
	}

	private void sendData() {
		if (conn == null) {
			print("\n> Please connect to a device first!");
			return;
		}

		try {
			conn.getOutputStream().write("Hello, World!\n".getBytes());
		} catch (Exception ex) {
			caught("sendData", ex);
		}
	}

	private void disconnect() {
		try {
			if (conn != null)
				conn.close();
			if (readThread != null)
				readThread.join();
		} catch (Exception ex) {
			caught("disconnect", ex);
		}
		conn = null;
		readThread = null;
	}

	private void caught(String sender, Exception ex) {
		print("\n> Exception in " + sender + ":\n" + ex.getMessage());
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_connect:
			connect();
			return true;
		case R.id.action_send:
			sendData();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
}

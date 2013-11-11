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

import android.os.Bundle;
import android.view.*;
import android.widget.Button;
import android.app.*;
import android.content.*;
import android.content.DialogInterface.OnClickListener;

public class MainActivity extends Activity {
	// TODO:
	// Use request ID that doesn't clash with the others used by lambdanative
	private final int BT_SEL_DEVICE_REQ = 4711;

	// We'll abstract away the native BT interface:
	private LambdaNativeBluetoothAdapter adapter = null;
	private LambdaNativeBluetoothConnection conn = null;
	private Procedure<Intent> deviceSelectedContinuation = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		// Initialize the GUI. Standard Android stuff.
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		final Button connectButton = (Button) findViewById(R.id.connectButton);
		connectButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				connect();
			}
		});

		final Button sendButton = (Button) findViewById(R.id.sendButton);
		sendButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				sendData();
			}
		});

		// Connect to the BT adapter, close program if BT not enabled
		try {
			adapter = LambdaNativeBluetoothAdapter.getInstance();
		} catch (Exception ex) {
			caught("onCreate", ex);
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
						showMessageBox("Connection established :)");
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
			showMessageBox("Please connect to a device first!");
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
		} catch (Exception ex) {
			caught("disconnect", ex);
		}
		conn = null;
	}

	private void showMessageBox(String message) {
		final AlertDialog dlg = new AlertDialog.Builder(this).create();
		dlg.setTitle("Whoops");
		dlg.setMessage(message);
		dlg.setButton(DialogInterface.BUTTON_POSITIVE, "OK",
				new OnClickListener() {
					public void onClick(DialogInterface arg0, int arg1) {
						dlg.dismiss();
					}
				});
		dlg.show();
	}

	private void caught(String sender, Exception ex) {
		new AlertDialog.Builder(this).setTitle("Whoops")
				.setMessage("Exception in " + sender + ": " + ex.getMessage())
				.setPositiveButton("OK", new OnClickListener() {
					public void onClick(DialogInterface arg0, int arg1) {
						finish();
					}
				}).show();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
}

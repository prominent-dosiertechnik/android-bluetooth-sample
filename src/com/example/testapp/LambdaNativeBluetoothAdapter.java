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
import java.util.*;
import android.bluetooth.*;

public final class LambdaNativeBluetoothAdapter {
	// We're using the well-known "default" UUID here instead of a custom one
	// Just to keep it simple until we need more flexibility
	private static final UUID wellKnownUuid = UUID
			.fromString("00001101-0000-1000-8000-00805F9B34FB");

	private static LambdaNativeBluetoothAdapter instance = null;
	private final BluetoothAdapter adapter;

	public static synchronized LambdaNativeBluetoothAdapter getInstance()
			throws Exception {
		if (instance == null)
			instance = new LambdaNativeBluetoothAdapter();
		return instance;
	}

	private LambdaNativeBluetoothAdapter() throws Exception {
		adapter = BluetoothAdapter.getDefaultAdapter();

		if (adapter == null)
			throw new Exception("No bluetooth support!");
		if (!adapter.isEnabled()) {
			throw new Exception("Bluetooth is not enabled!"); // KISS
		}
	}

	public LambdaNativeBluetoothConnection connect(String address)
			throws IOException {
		final BluetoothDevice device = adapter.getRemoteDevice(address);
		final BluetoothSocket socket = device
				.createRfcommSocketToServiceRecord(wellKnownUuid);
		adapter.cancelDiscovery(); // Too resource intensive if running
		return new LambdaNativeBluetoothConnection(socket);
	}

	public void cancelDiscovery() {
		adapter.cancelDiscovery();
	}

	public void startDiscovery() {
		adapter.startDiscovery();
	}
}

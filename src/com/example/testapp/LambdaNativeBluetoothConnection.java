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

import android.bluetooth.BluetoothSocket;
import java.io.*;

public class LambdaNativeBluetoothConnection {
	private BluetoothSocket socket = null;
	private OutputStream outStream = null;
	private InputStream inStream = null;

	public LambdaNativeBluetoothConnection(BluetoothSocket socket)
			throws IOException {
		this.socket = socket;
		socket.connect(); // Blocks
		outStream = socket.getOutputStream();
		inStream = socket.getInputStream();
	}

	public OutputStream getOutputStream() {
		return outStream;
	}

	public InputStream getInputStream() {
		return inStream;
	}

	public void close() throws IOException {
		outStream.flush();
		socket.close();
	}
}

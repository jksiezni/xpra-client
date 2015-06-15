package com.github.jksiezni.xpra;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class XpraService extends Service {
	
	public XpraService() {
	}

	@Override
	public IBinder onBind(Intent intent) {
		// TODO: Return the communication channel to the service.
		throw new UnsupportedOperationException("Not yet implemented");
	}
}

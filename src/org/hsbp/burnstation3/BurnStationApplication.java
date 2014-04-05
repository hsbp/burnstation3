package org.hsbp.burnstation3;

import android.app.Application;
import android.content.Context;

public class BurnStationApplication extends Application {

	private volatile static Context context;

	public void onCreate(){
		super.onCreate();
		context = getApplicationContext();
	}

	static Context getAppContext() {
		return context;
	}
}

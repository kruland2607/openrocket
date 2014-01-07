package net.sf.openrocket.android;

import net.sf.openrocket.android.util.AndroidLogWrapper;
import roboguice.RoboGuice;
import android.content.pm.ApplicationInfo;
import android.preference.PreferenceManager;

import com.google.inject.Injector;

public class Application extends android.app.Application {

	// Big B boolean so I can synchronize on it.
	private static Boolean initialized = false;

	public void initialize() {
		synchronized (initialized) {
			if ( initialized == true ) {
				return;
			}

			// Android does not have a default sax parser set.  This needs to be defined first.
			System.setProperty("org.xml.sax.driver","org.xmlpull.v1.sax2.Driver");

	        Injector injector = RoboGuice.setBaseApplicationInjector(this, RoboGuice.DEFAULT_STAGE, 
	                RoboGuice.newDefaultRoboModule(this), new AppModule(this));

	        net.sf.openrocket.startup.Application.setInjector(injector);
	        
			initialized = true;
		}
	}

	public Application() {
	}

	/* (non-Javadoc)
	 * @see android.app.Application#onCreate()
	 */
	@Override
	public void onCreate() {
		super.onCreate();
		initialize();
		boolean isDebuggable = (0 != (getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE));
		AndroidLogWrapper.setLogEnabled(isDebuggable);
		PreferencesActivity.initializePreferences(this, PreferenceManager.getDefaultSharedPreferences(this));
	}

}

package net.sf.openrocket.android;

import java.util.Locale;

import net.sf.openrocket.database.ComponentPresetDao;
import net.sf.openrocket.database.ComponentPresetDatabase;
import net.sf.openrocket.database.motor.MotorDatabase;
import net.sf.openrocket.database.motor.ThrustCurveMotorSetDatabase;
import net.sf.openrocket.formatting.MotorDescriptionSubstitutor;
import net.sf.openrocket.formatting.RocketDescriptor;
import net.sf.openrocket.formatting.RocketDescriptorImpl;
import net.sf.openrocket.formatting.RocketSubstitutor;
import net.sf.openrocket.l10n.DebugTranslator;
import net.sf.openrocket.l10n.ResourceBundleTranslator;
import net.sf.openrocket.l10n.Translator;
import net.sf.openrocket.startup.Preferences;
import android.content.Context;

import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.Scopes;
import com.google.inject.multibindings.Multibinder;


public class AppModule implements Module {

	private final Context ctx;
	
	public AppModule( Context ctx) {
		this.ctx = ctx;
	};
	
	@Override
	public void configure(Binder binder) {
		
		Multibinder<RocketSubstitutor> subBinder = Multibinder.newSetBinder( binder, RocketSubstitutor.class);
		subBinder.addBinding().to(MotorDescriptionSubstitutor.class);
		
		binder.bind(RocketDescriptor.class).to(RocketDescriptorImpl.class).in(Scopes.SINGLETON);

		binder.bind(Preferences.class).to(PreferencesAdapter.class).in(Scopes.SINGLETON);

		binder.bind(ComponentPresetDao.class).to(ComponentPresetDatabase.class).in(Scopes.SINGLETON);
		
		MotorDatabaseAdapter db = new MotorDatabaseAdapter(ctx);

		binder.bind(MotorDatabase.class).toInstance(db);

		Translator t;
		t = new ResourceBundleTranslator("l10n.messages");
		if (Locale.getDefault().getLanguage().equals("xx")) {
			t = new DebugTranslator(t);
		}

		binder.bind(Translator.class).toInstance(t);

	}

}

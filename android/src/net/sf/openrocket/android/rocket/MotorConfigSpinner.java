package net.sf.openrocket.android.rocket;

import net.sf.openrocket.android.Application;
import net.sf.openrocket.formatting.RocketDescriptor;
import net.sf.openrocket.rocketcomponent.Rocket;
import roboguice.RoboGuice;
import android.content.Context;
import android.util.AttributeSet;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.google.inject.Inject;

public class MotorConfigSpinner extends Spinner {

	@Inject
	private RocketDescriptor rocketFormatter;
	
	public MotorConfigSpinner(Context context, AttributeSet attrs,int defStyle, int mode) {
		super(context, attrs, defStyle, mode);
		RoboGuice.injectMembers(this.getContext(),this);
	}

	public MotorConfigSpinner(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		RoboGuice.injectMembers(this.getContext(),this);
	}

	public MotorConfigSpinner(Context context, AttributeSet attrs) {
		super(context, attrs);
		RoboGuice.injectMembers(this.getContext(),this);
	}

	public MotorConfigSpinner(Context context, int mode) {
		super(context, mode);
		RoboGuice.injectMembers(this.getContext(),this);
	}

	public MotorConfigSpinner(Context context) {
		super(context);
		RoboGuice.injectMembers(this.getContext(),this);
	}

	public void createAdapter(Rocket rocket ) {
	
		setAdapter(new MotorConfigSpinnerAdapter(this.getContext(), rocket) );
		
	}
	
	public void setSelectedConfiguration( String configId ) {
		this.setSelection( ((MotorConfigSpinnerAdapter)getAdapter()).getConfigurationPosition( configId ));
	}
	
	public String getSelectedConfiguration() {
		return ((MotorConfigSpinnerAdapter)getAdapter()).getConfiguration( this.getSelectedItemPosition() );
	}
	
	public class MotorConfigSpinnerAdapter extends ArrayAdapter<String> {

		private String[] motorConfigs;

		public MotorConfigSpinnerAdapter(Context context, Rocket rocket) {
			super(context, android.R.layout.simple_spinner_item);
			setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			motorConfigs = rocket.getFlightConfigurationIDs();

			for( String config: motorConfigs ) {
				String configName = rocketFormatter.format(rocket, config);
				this.add(configName);
			}

		}

		public int getConfigurationPosition(String configId) {

			int selectedIndex = 0;

			if ( configId == null ) {
				return selectedIndex;
			}

			for( String s : motorConfigs ) {
				// Note - s may be null since it is a valid id.
				if ( configId.equals(s) ) {
					break;
				}
				selectedIndex++;
			}
			if( selectedIndex >= motorConfigs.length ) {
				selectedIndex = 0;
			}

			return selectedIndex;
		}
		
		public String getConfiguration( int position ) {
			return motorConfigs[position];
		}
	}
}

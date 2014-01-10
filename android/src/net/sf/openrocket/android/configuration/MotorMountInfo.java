package net.sf.openrocket.android.configuration;

import net.sf.openrocket.android.motor.ExtendedThrustCurveMotor;
import net.sf.openrocket.rocketcomponent.MotorMount;
import net.sf.openrocket.rocketcomponent.RocketComponent;
import net.sf.openrocket.unit.UnitGroup;

class MotorMountInfo {

	private MotorMount mmt;
	private String config;
	private ExtendedThrustCurveMotor motor;
	private double delay;

	public MotorMountInfo(MotorMount mmt, String config) {
		super();
		this.mmt = mmt;
		this.config = config;
		this.motor = (ExtendedThrustCurveMotor) mmt.getMotor(config);

		if ( this.motor != null ) {
			this.delay = mmt.getMotorDelay(config);
		} else {
			this.delay = -1;
		}


	}

	public RocketComponent getMmt() {
		return (RocketComponent) mmt;
	}

	public void setMmt(MotorMount mmt) {
		this.mmt = mmt;
	}

	public String getConfig() {
		return config;
	}

	public void setConfig(String config) {
		this.config = config;
	}

	public ExtendedThrustCurveMotor getMotor() {
		return motor;
	}

	public void setMotor(ExtendedThrustCurveMotor motor) {
		this.motor = motor;
		mmt.getMotorConfiguration().get(config).setMotor(motor);
	}

	public double getDelay() {
		return delay;
	}

	public void setDelay(double delay) {
		this.delay = delay;
		mmt.getMotorConfiguration().get(config).setEjectionDelay(delay);
	}

	String getMotorMountDescription() {
		String mmtDesc = ((RocketComponent)mmt).getComponentName();
		mmtDesc += " (" + UnitGroup.UNITS_MOTOR_DIMENSIONS.toStringUnit( ((MotorMount)mmt).getMotorMountDiameter()) + ")";
		return mmtDesc;
	}

	String getMotorDescription() {
		return motor.getManufacturer().getDisplayName() + " " + motor.getDesignation();
	}


}

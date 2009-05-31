package net.sf.openrocket.aerodynamics;

import java.util.ArrayList;
import java.util.List;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import net.sf.openrocket.rocketcomponent.Configuration;
import net.sf.openrocket.util.ChangeSource;
import net.sf.openrocket.util.MathUtil;


public class FlightConditions implements Cloneable, ChangeSource {

	private List<ChangeListener> listenerList = new ArrayList<ChangeListener>();
	private ChangeEvent event = new ChangeEvent(this);
	
	/** Modification count */
	private int modCount = 0;
	
	/** Reference length used in calculations. */
	private double refLength = 1.0;
	
	/** Reference area used in calculations. */
	private double refArea = Math.PI * 0.25;

	
	/** Angle of attack. */
	private double aoa = 0;
	
	/** Sine of the angle of attack. */
	private double sinAOA = 0;
	
	/** 
	 * The fraction <code>sin(aoa) / aoa</code>.  At an AOA of zero this value 
	 * must be one.  This value may be used in many cases to avoid checking for
	 * division by zero. 
	 */
	private double sincAOA = 1.0;
	
	/** Lateral wind direction. */
	private double theta = 0;
	
	/** Current Mach speed. */
	private double mach = 0.3;
	
	/**
	 * Sqrt(1 - M^2)  for M<1
	 * Sqrt(M^2 - 1)  for M>1
	 */
	private double beta = Math.sqrt(1 - mach*mach);

	
	/** Current roll rate. */
	private double rollRate = 0;
	
	private double pitchRate = 0;
	private double yawRate = 0;
	
	
	private AtmosphericConditions atmosphericConditions = new AtmosphericConditions();
	
	
	
	/**
	 * Sole constructor.  The reference length is initialized to the reference length
	 * of the <code>Configuration</code>, and the reference area accordingly.
	 * If <code>config</code> is <code>null</code>, then the reference length is set
	 * to 1 meter.
	 * 
	 * @param config   the configuration of which the reference length is taken.
	 */
	public FlightConditions(Configuration config) {
		if (config != null)
			setRefLength(config.getReferenceLength());
	}
	
	
	/**
	 * Set the reference length from the given configuration.
	 * @param config	the configuration from which to get the reference length.
	 */
	public void setReference(Configuration config) {
		setRefLength(config.getReferenceLength());
	}
	
	
	/**
	 * Set the reference length and area.
	 */
	public void setRefLength(double length) {
		refLength = length;

		refArea = Math.PI * MathUtil.pow2(length/2);
		fireChangeEvent();
	}

	/**
	 * Return the reference length.
	 */
	public double getRefLength() {
		return refLength;
	}

	/**
	 * Set the reference area and length.
	 */
	public void setRefArea(double area) {
		refArea = area;
		refLength = Math.sqrt(area / Math.PI)*2;
		fireChangeEvent();
	}
	
	/**
	 * Return the reference area.
	 */
	public double getRefArea() {
		return refArea;
	}

	
	/**
	 * Sets the angle of attack.  It calculates values also for the methods 
	 * {@link #getSinAOA()} and {@link #getSincAOA()}. 
	 * 
	 * @param aoa   the angle of attack.
	 */
	public void setAOA(double aoa) {
		aoa = MathUtil.clamp(aoa, 0, Math.PI);
		if (MathUtil.equals(this.aoa, aoa))
			return;
		
		this.aoa = aoa;
		if (aoa < 0.001) {
			this.sinAOA = aoa;
			this.sincAOA = 1.0;
		} else {
			this.sinAOA = Math.sin(aoa);
			this.sincAOA = sinAOA / aoa;
		}
		fireChangeEvent();
	}

	
	/**
	 * Sets the angle of attack with the sine.  The value <code>sinAOA</code> is assumed
	 * to be the sine of <code>aoa</code> for cases in which this value is known.
	 * The AOA must still be specified, as the sine is not unique in the range
	 * of 0..180 degrees.
	 * 
	 * @param aoa		the angle of attack in radians.
	 * @param sinAOA	the sine of the angle of attack.
	 */
	public void setAOA(double aoa, double sinAOA) {
		aoa = MathUtil.clamp(aoa, 0, Math.PI);
		sinAOA = MathUtil.clamp(sinAOA, 0, 1);
		if (MathUtil.equals(this.aoa, aoa))
			return;
		
		assert(Math.abs(Math.sin(aoa) - sinAOA) < 0.0001) : 
			"Illegal sine: aoa="+aoa+" sinAOA="+sinAOA;
		
		this.aoa = aoa;
		this.sinAOA = sinAOA;
		if (aoa < 0.001) {
			this.sincAOA = 1.0;
		} else {
			this.sincAOA = sinAOA / aoa;
		}
		fireChangeEvent();
	}


	/**
	 * Return the angle of attack.
	 */
	public double getAOA() {
		return aoa;
	}

	/**
	 * Return the sine of the angle of attack.
	 */
	public double getSinAOA() {
		return sinAOA;
	}

	/**
	 * Return the sinc of the angle of attack (sin(AOA) / AOA).  This method returns
	 * one if the angle of attack is zero.
	 */
	public double getSincAOA() {
		return sincAOA;
	}
	
	
	/**
	 * Set the direction of the lateral airflow.
	 */
	public void setTheta(double theta) {
		if (MathUtil.equals(this.theta, theta))
			return;
		this.theta = theta;
		fireChangeEvent();
	}

	/**
	 * Return the direction of the lateral airflow.
	 */
	public double getTheta() {
		return theta;
	}
	

	/**
	 * Set the current Mach speed.  This should be (but is not required to be) in 
	 * reference to the speed of sound of the atmospheric conditions.
	 */
	public void setMach(double mach) {
		mach = Math.max(mach, 0);
		if (MathUtil.equals(this.mach, mach))
			return;
		
		this.mach = mach;
		if (mach < 1)
			this.beta = Math.sqrt(1 - mach*mach);
		else
			this.beta = Math.sqrt(mach*mach - 1);
		fireChangeEvent();
	}
	
	/**
	 * Return the current Mach speed.
	 */
	public double getMach() {
		return mach;
	}
	
	/**
	 * Returns the current rocket velocity, calculated from the Mach number and the
	 * speed of sound.  If either of these parameters are changed, the velocity changes
	 * as well.
	 * 
	 * @return  the velocity of the rocket.
	 */
	public double getVelocity() {
		return mach * atmosphericConditions.getMachSpeed();
	}
	
	/**
	 * Sets the Mach speed according to the given velocity and the current speed of sound.
	 * 
	 * @param velocity	the current velocity.
	 */
	public void setVelocity(double velocity) {
		setMach(velocity / atmosphericConditions.getMachSpeed());
	}
	

	/**
	 * Return sqrt(abs(1 - Mach^2)).  This is calculated in the setting call and is
	 * therefore fast.
	 */
	public double getBeta() {
		return beta;
	}
	
	
	/**
	 * Return the current roll rate.
	 */
	public double getRollRate() {
		return rollRate;
	}
	
	
	/**
	 * Set the current roll rate.
	 */
	public void setRollRate(double rate) {
		if (MathUtil.equals(this.rollRate, rate))
			return;
		
		this.rollRate = rate;
		fireChangeEvent();
	}

	
	public double getPitchRate() {
		return pitchRate;
	}


	public void setPitchRate(double pitchRate) {
		if (MathUtil.equals(this.pitchRate, pitchRate))
			return;
		this.pitchRate = pitchRate;
		fireChangeEvent();
	}


	public double getYawRate() {
		return yawRate;
	}


	public void setYawRate(double yawRate) {
		if (MathUtil.equals(this.yawRate, yawRate))
			return;
		this.yawRate = yawRate;
		fireChangeEvent();
	}


	/**
	 * Return the current atmospheric conditions.  Note that this method returns a
	 * reference to the {@link AtmosphericConditions} object used by this object.
	 * Changes made to the object will modify the encapsulated object, but will NOT
	 * generate change events.
	 * 
	 * @return		the current atmospheric conditions.
	 */
	public AtmosphericConditions getAtmosphericConditions() {
		return atmosphericConditions;
	}

	/**
	 * Set the current atmospheric conditions.  This method will fire a change event
	 * if a change occurs.
	 */
	public void setAtmosphericConditions(AtmosphericConditions cond) {
		if (atmosphericConditions == cond)
			return;
		atmosphericConditions = cond;
		fireChangeEvent();
	}
	
	
	/**
	 * Retrieve the modification count of this object.  Each time it is modified
	 * the modification count is increased by one.
	 * 
	 * @return	the number of times this object has been modified since instantiation.
	 */
	public int getModCount() {
		return modCount;
	}
	
	
	@Override
	public String toString() {
		return String.format("FlightConditions[aoa=%.2f\u00b0,theta=%.2f\u00b0,"+
				"mach=%.2f,rollRate=%.2f]", 
				aoa*180/Math.PI, theta*180/Math.PI, mach, rollRate);
	}
	
	
	/**
	 * Return a copy of the flight conditions.  The copy has no listeners.  The
	 * atmospheric conditions is also cloned.
	 */
	@Override
	public FlightConditions clone() {
		try {
			FlightConditions cond = (FlightConditions) super.clone();
			cond.listenerList = new ArrayList<ChangeListener>();
			cond.event = new ChangeEvent(cond);
			cond.atmosphericConditions = atmosphericConditions.clone();
			return cond;
		} catch (CloneNotSupportedException e) {
			throw new RuntimeException("BUG: clone not supported!",e);
		}
	}

	
	
	@Override
	public void addChangeListener(ChangeListener listener) {
		listenerList.add(0,listener);
	}

	@Override
	public void removeChangeListener(ChangeListener listener) {
		listenerList.remove(listener);
	}
	
	protected void fireChangeEvent() {
		modCount++;
		ChangeListener[] listeners = listenerList.toArray(new ChangeListener[0]);
		for (ChangeListener l: listeners) {
			l.stateChanged(event);
		}
	}
}
package com.example.arontandrix;

import java.util.List;

import org.andrix.motors.Motor;
import org.andrix.motors.Servo;
import org.andrix.sensors.Analog;
import org.andrix.sensors.Digital;

/**
 * Continuously reads values from of the hardware.
 * 
 * @author Andrej
 *
 */
public class ReadValues implements Runnable {

	/**
	 * List of objects to update values in.
	 */
	private List<ObjValVec> list;
	/**
	 * True when the program should terminate.
	 */
	private boolean done;

	/**
	 * Setting of the list.
	 * @param list List with objects.
	 */
	public ReadValues(List<ObjValVec> list) {
		this.list = list;
		done = false;
	}

	/**
	 * Goes through all the objects in the list, reads their new values and saves these.
	 */
	@Override
	public void run() {
		Object o;
		while (!done) {
			for (ObjValVec ovv : list) {
				try {
					o = ovv.getObj();
					if (o instanceof Analog) {
						ovv.setVal(((Analog) o).getValue());
					} else if (o instanceof Digital) {
						ovv.setVal(((Digital) o).getValue().compareTo(false));
					} else if (o instanceof Motor) {
						ovv.setVal(((Motor) o).getVelocity());
					} else if (o instanceof Servo) {
						ovv.setVal(((Servo) o).getPosition());
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				done = true;
				// e.printStackTrace();
			}
		}
	}
}

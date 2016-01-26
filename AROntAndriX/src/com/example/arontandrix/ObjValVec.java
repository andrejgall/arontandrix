package com.example.arontandrix;

import com.metaio.sdk.jni.Vector2d;

/**
 * Container class for objects, their values and their positions.
 * 
 * @author Andrej
 *
 */
public class ObjValVec {
	private Object obj;
	private int val;
	private Vector2d vec;

	/**
	 * Object with default value and vector.
	 * @param obj Any existing object.
	 */
	public ObjValVec(Object obj) {
		this.obj = obj;
		val = 0;
		vec = new Vector2d(0);
	}

	/**
	 * Contained object.
	 * @return An object.
	 */
	public Object getObj() {
		return obj;
	}

	/**
	 * Contained value.
	 * @return A value.
	 */
	public int getVal() {
		return val;
	}

	/**
	 * Sets a new value.
	 * @param val A value.
	 */
	public void setVal(int val) {
		this.val = val;
	}

	/**
	 * Contained vector.
	 * @return A vector.
	 */
	public Vector2d getVec() {
		return vec;
	}

	/**
	 * Sets a new vector.
	 * @param vec A vector.
	 */
	public void setVec(Vector2d vec) {
		this.vec = vec;
	}

}

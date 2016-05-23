package com.example.arontandrix;

import com.metaio.sdk.jni.Vector2d;
import com.metaio.sdk.jni.Vector3d;

/**
 * Container class for objects, their values and their positions.
 * 
 * @author Andrej
 *
 */
public class ObjValVec {
	private Object obj;
	private int val;
	private Vector2d vec2;
	private Vector3d vec3;

	/**
	 * Object with default value and vector.
	 * 
	 * @param obj
	 *            Any existing object.
	 */
	public ObjValVec(Object obj) {
		this.obj = obj;
		val = 0;
		vec2 = new Vector2d(0);
		vec3 = new Vector3d(0);
	}

	/**
	 * Contained object.
	 * 
	 * @return An object.
	 */
	public Object getObj() {
		return obj;
	}

	/**
	 * Contained value.
	 * 
	 * @return A value.
	 */
	public int getVal() {
		return val;
	}

	/**
	 * Sets a new value.
	 * 
	 * @param val
	 *            A value.
	 */
	public void setVal(int val) {
		this.val = val;
	}

	/**
	 * Contained vector.
	 * 
	 * @return A vector.
	 */
	public Vector2d getVec2() {
		return vec2;
	}

	/**
	 * Sets a new vector.
	 * 
	 * @param vec
	 *            A vector.
	 */
	public void setVec2(Vector2d vec) {
		this.vec2 = vec;
	}

	/**
	 * Contained vector.
	 * 
	 * @return A vector.
	 */
	public Vector3d getVec3() {
		return vec3;
	}

	/**
	 * Sets a new vector.
	 * 
	 * @param vec
	 *            A vector.
	 */
	public void setVec3(Vector3d vec) {
		this.vec3 = vec;
	}

}

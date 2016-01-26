package com.example.arontandrix;

import java.util.ArrayList;

/**
 * Calculates The position of the tip and the middle of the arm out of servo
 * positions.
 * 
 * @author Andrej
 *
 */
public class ForwardKinematics {

	/**
	 * Length of the lower part of the arm.
	 */
	private static int LOWERARMLENGTH;
	/**
	 * Length of the upper part of the arm.
	 */
	private static int UPPERARMLENGTH;
	/**
	 * Angle of the turn servo.
	 */
	private double angle_turn;
	/**
	 * Angle of the lower arm servo.
	 */
	private double angle_lower;
	/**
	 * Length of the upper arm servo.
	 */
	private double angle_upper;
	/**
	 * List with the calculated results.
	 */
	private ArrayList<Integer> results = new ArrayList<Integer>();

	/**
	 * One time setting of the arm lengths.
	 * 
	 * @param lower
	 *            Lower arm length.
	 * @param upper
	 *            Upper arm length.
	 */
	public ForwardKinematics(int lower, int upper) {
		LOWERARMLENGTH = lower;
		UPPERARMLENGTH = upper;
	}

	/**
	 * Calculates the positions with the help of trigonometric functions.
	 * 
	 * @param turn
	 *            Turn servo angle.
	 * @param lower
	 *            Lower arm servo angle.
	 * @param upper
	 *            Upper arm servo angle.
	 * @return List of calculated values.
	 */
	public ArrayList<Integer> calculate(int turn, int lower, int upper) {
		results.clear();
		angle_turn = Math.toRadians(turn);
		angle_lower = Math.toRadians(lower);
		angle_upper = Math.toRadians(upper);
		double z_lower = Math.sin(angle_lower) * LOWERARMLENGTH;
		double z_upper = Math.sin(angle_lower + angle_upper) * UPPERARMLENGTH + z_lower;
		double x_lower_temp = Math.cos(angle_lower) * LOWERARMLENGTH;
		double x_upper_temp = Math.cos(angle_lower + angle_upper) * UPPERARMLENGTH + x_lower_temp;
		double y_lower = x_lower_temp * Math.cos(angle_turn);
		double y_upper = x_upper_temp * Math.cos(angle_turn);
		double x_lower = x_lower_temp * Math.sin(angle_turn);
		double x_upper = x_upper_temp * Math.sin(angle_turn);
		results.add((int) x_lower);
		results.add((int) y_lower * -1);
		results.add((int) z_lower);
		results.add((int) x_upper);
		results.add((int) y_upper * -1);
		results.add((int) z_upper);
		return results;
	}

	public ArrayList<Integer> calculateM(int turn, int lower, int upper) {
		
		return null;
	}

}

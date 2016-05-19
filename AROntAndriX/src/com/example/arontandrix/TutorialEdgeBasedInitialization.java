package com.example.arontandrix;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.andrix.low.NotConnectedException;
import org.andrix.misc.InvalidPortException;
import org.andrix.motors.Servo;
import org.andrix.sensors.Analog;

import Jama.Matrix;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.LinearLayout;

import com.metaio.sdk.ARViewActivity;
import com.metaio.sdk.MetaioDebug;
import com.metaio.sdk.jni.ETRACKING_STATE;
import com.metaio.sdk.jni.IGeometry;
import com.metaio.sdk.jni.IMetaioSDKCallback;
import com.metaio.sdk.jni.Rotation;
import com.metaio.sdk.jni.TrackingValuesVector;
import com.metaio.sdk.jni.Vector2d;
import com.metaio.sdk.jni.Vector3d;
import com.metaio.tools.io.AssetsManager;

/**
 * Starts the AR application. First it searches for the 3D object in the
 * pictures from the camera. When the object is found it is tracked and its
 * position is shown in screen coordinates.
 * 
 * @author metaio, Andrej
 */
public class TutorialEdgeBasedInitialization extends ARViewActivity implements Runnable {

	/**
	 * Enum for defining of the actual state of the application.
	 * 
	 * @author metaio
	 */
	private enum EState {
		INITIALIZATION, TRACKING
	};

	/**
	 * Defines the actual state of the application.
	 */
	EState mState = EState.INITIALIZATION;

	/**
	 * Geometry of the searched 3D model.
	 */
	private IGeometry mModel = null;

	/**
	 * Geometry of the augmented 3D model.
	 */
	private IGeometry mVizAidModel = null;

	/**
	 * Geometry of the augmented 3D model.
	 */
	private IGeometry helpline = null;

	/**
	 * Thread for showing of the position.
	 */
	Thread t;
	/**
	 * Thread for reading of the values.
	 */
	Thread v;

	/**
	 * metaio SDK callback handler.
	 */
	private MetaioSDKCallbackHandler mCallbackHandler;

	/**
	 * Transparent overlay displaying the values.
	 */
	private OverlayView overlay;

	// private OntAccess ontology;

	/**
	 * Calculating of the forward kinematics.
	 */
	private ForwardKinematics fk;

	/**
	 * Continuously reads hardware values.
	 */
	private ReadValues rv;

	/**
	 * X distance to origin of tracking.
	 */
	private int xToOr = -3;
	/**
	 * Y distance to origin of tracking.
	 */
	private int yToOr = 95;
	/**
	 * Z distance to origin of tracking.
	 */
	private int zToOr = 20;

	/**
	 * True when program should stop.
	 */
	private boolean done;

	/**
	 * Upper servo on the arm.
	 */
	private Servo upper;
	/**
	 * Lower servo on the arm.
	 */
	private Servo lower;
	/**
	 * Base turning servo.
	 */
	private Servo turn;
	/**
	 * Grabber servo.
	 */
	private Servo grab;

	/**
	 * Analog sensor in the grabber.
	 */
	private Analog a15;

	/**
	 * List of objects, their values and their positions.
	 */
	private List<ObjValVec> list;

	private boolean started;
	private Thread t1;
	private Vector3d vt1 = new Vector3d(0);
	private Vector3d vt2 = new Vector3d(0);
	private Vector2d olddp = new Vector2d(0);
	private Vector2d dp = new Vector2d(0);
	private Vector3d ip = new Vector3d(0);
	private Vector3d oldip = new Vector3d(0);
	private int mode = 0;
	private LinearLayout l0;
	private LinearLayout l1;
	private LinearLayout l2;
	private LinearLayout l3;

	/**
	 * Creating of the application.
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);

		mCallbackHandler = new MetaioSDKCallbackHandler();

		// System.err.println();
		// // System.out.println(intersection(new Vector3d(3, 0, 0), new
		// // Vector3d(3, 5, 0), new Vector3d(0, 3, 0), new Vector3d(5, 3, 0)));
		// System.err.println(intersection(new Vector3d(0, 0, 0), new
		// Vector3d(5, 5, 5), new Vector3d(0, 5, 5), new Vector3d(5, 0, 0)));
		// System.err.println(ip.getX() + "," + ip.getY() + "," + ip.getZ());
		//
		// float[] t = angleV(new Vector3d(0, 1, 0), new Vector3d(1, 0, 0));
		//
		// MetaioDebug.log(Log.ERROR, t[0] + " " + t[1] + " " + t[2]);
		// MetaioDebug.log(Log.ERROR, t[3] + " " + t[4] + " " + t[5]);
		// MetaioDebug.log(Log.ERROR, t[6] + " " + t[7] + " " + t[8]);
		//
		// helpline.setRotation(new Rotation(t));

		overlay = (OverlayView) mGUIView.findViewById(R.id.OverlayView);
		// ImageView iv = (ImageView) mGUIView.findViewById(R.id.test_image);
		// overlay.visible = true;
		// overlay.moveAlongPath(iv);

		// ontology = new OntAccess(this.getApplicationContext());

		l0 = (LinearLayout) mGUIView.findViewById(R.id.buttonBar0);
		l1 = (LinearLayout) mGUIView.findViewById(R.id.buttonBar1);
		l2 = (LinearLayout) mGUIView.findViewById(R.id.buttonBar2);
		l3 = (LinearLayout) mGUIView.findViewById(R.id.buttonBar3);
		l2.setVisibility(4);
		l3.setVisibility(4);

		try {
			a15 = new Analog(15);

			grab = new Servo(3);
			upper = new Servo(2);
			lower = new Servo(1);
			turn = new Servo(0);

			grab.setPosition(125);
			upper.setPosition(50);
			lower.setPosition(190);
			turn.setPosition(125);

			grab.on();
			upper.on();
			lower.on();
			turn.on();
		} catch (InvalidPortException | NotConnectedException e) {
			e.printStackTrace();
		}

		fk = new ForwardKinematics(135, 85);

		done = false;

		list = Collections.synchronizedList(new ArrayList<ObjValVec>());
		list.clear();
		list.add(new ObjValVec(new String("Hedgehog")));
		list.add(new ObjValVec(turn));
		list.add(new ObjValVec(lower));
		list.add(new ObjValVec(upper));
		list.add(new ObjValVec(grab));
		list.add(new ObjValVec(a15));
		list.add(new ObjValVec(new String("target")));
		// list.add(new ObjValVec(new String("testv")));
		// list.add(new ObjValVec(new String("testvd")));

		started = false;

		rv = new ReadValues(list);

		v = new Thread(rv);
		// v.start();

		t = new Thread(this);
		// t.start();

		t1 = new Thread(this);
	}

	private void movementLine() throws NotConnectedException, InterruptedException {

		ArrayList<Integer> point = new ArrayList<Integer>();
		ArrayList<Vector2d> path = new ArrayList<Vector2d>();

		while (ip.equals(new Vector3d(0))) {
			Thread.sleep(100);
		}

		while (!done && mode == 1) {

			double x = ip.getY() - yToOr;
			double y = ip.getZ() - zToOr;
			double z = ip.getX() - xToOr;
			double s = y;
			double r = Math.sqrt(Math.pow(x, 2) + Math.pow(z, 2));
			double a1 = 135;
			double a2 = 85;
			double cost2 = (Math.pow(r, 2) + Math.pow(s, 2) - Math.pow(a1, 2) - Math.pow(a2, 2)) / (2 * a1 * a2);
			double sint2 = Math.sqrt(1 - Math.pow(cost2, 2));
			double theta1 = Math.atan2(sint2, cost2);
			double theta2 = Math.atan2(s, r) - Math.atan2(a2 * sint2, a1 + a2 * cost2);
			double theta0 = Math.atan2(z, x);

			double t0 = Math.toDegrees(-theta0);
			double t1 = Math.toDegrees(theta1);
			double t2 = Math.toDegrees(theta2) - 90;

			int t = aFSTurn(turn.getPosition());
			int l = aFSLower(lower.getPosition());
			int u = aFSUpper(upper.getPosition());

			System.err.println("angles: t0:" + t0 + ",t1:" + t1 + ",t2:" + t2);
			System.err.println("servos: t:" + t + ",l:" + l + ",u:" + u);

			if (mode == 1) {
				if (t < t0) {
					turn.setPosition(turn.getPosition() + 1);
				} else if (t > t0) {
					turn.setPosition(turn.getPosition() - 1);
				}

				if (l < t1) {
					lower.setPosition(lower.getPosition() + 1);
				} else if (l > t1) {
					lower.setPosition(lower.getPosition() - 1);
				}

				if (u < t2) {
					upper.setPosition(upper.getPosition() + 1);
				} else if (u > t2) {// && upper.getPosition() > 0
					upper.setPosition(upper.getPosition() - 1);
				}
			}

			// servo positions
			// turn:
			// 0: 123
			// -80: 0
			// 85: 255
			// lower:
			// 0: 53
			// 90: 193
			// upper:
			// 0: 196
			// -90: 43
			// 45: 255

			if (!ip.equals(oldip)) {

				int tt = (int) t;
				int lt = (int) l;
				int ut = (int) u;
				path.clear();

				while (tt != t0 && lt != t1 && ut != t2) {

					if (tt != t0) {
						if (t < t0) {
							tt++;
						} else if (t > t0) {
							tt--;
						}
					}

					if (lt != t1) {
						if (l < t1) {
							lt++;
						} else if (l > t1) {
							lt--;
						}
					}

					if (ut != t2) {
						if (u < t2) {
							ut++;
						} else if (u > t2) {
							ut--;
						}
					}

					point = fk.calculate(tt, lt, ut);
					path.add(v3Tov2(xToOr + point.get(3), yToOr + point.get(4), zToOr + point.get(5)));
				}

				overlay.updatePointList(path);

				oldip.setX(ip.getX());
				oldip.setY(ip.getY());
				oldip.setZ(ip.getZ());
			}

			Thread.sleep(100);
		}

		path.clear();
		overlay.updatePointList(path);
	}

	/**
	 * Calculates turn servo position in degrees.
	 * 
	 * @param pos
	 *            Actual servo position.
	 * @return Position in degrees.
	 */
	private int aFSTurn(int pos) {
		double ang = (pos / 255.0 * 165.0 - 80.0);
		return (int) Math.round(ang);
	}

	/**
	 * Calculates lower arm servo position in degrees.
	 * 
	 * @param pos
	 *            Actual servo position.
	 * @return Position in degrees.
	 */
	private int aFSLower(int pos) {
		double ang = (pos / 255.0 * 165.0 - 33.0);
		return (int) Math.round(ang);
	}

	/**
	 * Calculates upper arm servo position in degrees.
	 * 
	 * @param pos
	 *            Actual servo position.
	 * @return Position in degrees.
	 */
	private int aFSUpper(int pos) {
		double ang = (pos / 255.0 * 165.0 - 122.0);
		return (int) Math.round(ang);
	}

	/**
	 * Response for touch events.
	 */
	@Override
	protected void onGeometryTouched(IGeometry geometry) {
	}

	private boolean intersection(Vector3d a1, Vector3d a2, Vector3d b1, Vector3d b2) {

		// nA = dot(cross(B2-B1,A1-B1),cross(A2-A1,B2-B1));
		// nB = dot(cross(A2-A1,A1-B1),cross(A2-A1,B2-B1));
		// d = dot(cross(A2-A1,B2-B1),cross(A2-A1,B2-B1));
		// A0 = A1 + (nA/d)*(A2-A1);
		// B0 = B1 + (nB/d)*(B2-B1);

		float nA = b2.subtract(b1).cross(a1.subtract(b1)).dot(a2.subtract(a1).cross(b2.subtract(b1)));
		float nB = a2.subtract(a1).cross(a1.subtract(b1)).dot(a2.subtract(a1).cross(b2.subtract(b1)));
		float d = a2.subtract(a1).cross(b2.subtract(b1)).dot(a2.subtract(a1).cross(b2.subtract(b1)));
		Vector3d a0 = a1.add(a2.subtract(a1).multiply(nA / d));
		Vector3d b0 = b1.add(b2.subtract(b1).multiply(nB / d));

		if (Math.abs(a0.getX() - b0.getX()) < 5 || Math.abs(a0.getY() - b0.getY()) < 5 || Math.abs(a0.getZ() - b0.getZ()) < 5) {
			ip = a0;
			return true;
		}

		return false;
	}

	// distance = sqrt[(z2 - z1)^2 + (x2 - x1)^2 + (y2 - y1)^2]
	// midpoint = (x1+x2)/2, (y1+y2)/2, (z1+z2)/2
	// angle = arccos((v1.dot(v2))/(||v1||*||v2||))
	// http://math.stackexchange.com/questions/180418/calculate-rotation-matrix-to-align-vector-a-to-vector-b-in-3d

	private float[] angleV(Vector3d a, Vector3d b) {
		Vector3d v = a.cross(b);
		double v1 = v.getX();
		double v2 = v.getY();
		double v3 = v.getZ();
		double s = Math.sqrt(Math.pow(v1, 2) + Math.pow(v2, 2) + Math.pow(v3, 2));
		double c = a.dot(b);

		double[][] i = { { 1, 0, 0 }, { 0, 1, 0 }, { 0, 0, 1 } };
		Matrix I = new Matrix(i);
		double[][] vxa = { { 0, -v3, v2 }, { v3, 0, -v1 }, { -v2, v1, 0 } };
		Matrix vx = new Matrix(vxa);
		Matrix vx2 = vx.times(vx);
		// vx.print(5, 2);
		// vx2.print(5, 2);
		double con = (1 - c) / Math.pow(s, 2);
		Matrix R = I.plus(vx).plus(vx2.times(con));

		double[][] temp = R.getArrayCopy();
		float[] out = new float[9];

		out[0] = (float) temp[0][0];
		out[1] = (float) temp[0][1];
		out[2] = (float) temp[0][2];
		out[3] = (float) temp[1][0];
		out[4] = (float) temp[1][1];
		out[5] = (float) temp[1][2];
		out[6] = (float) temp[2][0];
		out[7] = (float) temp[2][1];
		out[8] = (float) temp[2][2];

		return out;
	}

	/**
	 * Calculates the screen coordinates of the object from 3D vectors and
	 * outputs these.
	 */
	@Override
	public void run() {
		if (started) {
			try {
				movementLine();
			} catch (NotConnectedException | InterruptedException e) {
				e.printStackTrace();
			}
		} else {

			started = true;

			t1.start();
			// ontology.updateAll();

			ArrayList<Integer> al = new ArrayList<Integer>();
			int turnangle = 0;
			int lowerangle = 90;
			int upperangle = 90;

			while (!done) {
				try {
					// ontology.updateAll();

					turnangle = angleFromServoTurn(turn.getPosition());
					lowerangle = angleFromServoLower(lower.getPosition());
					upperangle = angleFromServoUpper(upper.getPosition());

					al = fk.calculate(turnangle, lowerangle, upperangle);

					list.get(0).setVec(v3Tov2(0, 0, 0));
					list.get(1).setVec(v3Tov2(xToOr, yToOr, zToOr - 40));
					list.get(2).setVec(v3Tov2(xToOr, yToOr, zToOr));
					list.get(3).setVec(v3Tov2(xToOr + al.get(0), yToOr + al.get(1), zToOr + al.get(2)));
					list.get(4).setVec(v3Tov2(xToOr + al.get(3), yToOr + al.get(4), zToOr + al.get(5)));
					list.get(5).setVec(v3Tov2(xToOr + al.get(3) + 25, yToOr + al.get(4) + 10, zToOr + al.get(5) + 10));
					list.get(6).setVec(v3Tov2(ip.getX(), ip.getY(), ip.getZ()));
					// list.get(7).setVec(v3Tov2(vt1.getX(), vt1.getY(),
					// vt1.getZ()));
					// list.get(8).setVec(v3Tov2(vt2.getX(), vt2.getY(),
					// vt2.getZ()));
					overlay.updateMap(list);

					System.out.println(ip.getX() + "," + ip.getY() + "," + ip.getZ());

					dp = overlay.dp();

					if (!dp.equals(olddp) && mRendererInitialized) {
						Vector3d vt1t = new Vector3d(vt1);
						Vector3d vt2t = new Vector3d(vt2);
						vt1 = metaioSDK.get3DPositionFromViewportCoordinates(1, dp, new Vector3d(-100));
						vt2 = metaioSDK.get3DPositionFromViewportCoordinates(1, dp, new Vector3d(300));
						if (!intersection(vt1t, vt2t, vt1, vt2)) {
							float vtdist = (float) Math.sqrt(Math.pow(vt2.getX() - vt1.getX(), 2) + Math.pow(vt2.getY() - vt1.getY(), 2) + Math.pow(vt2.getZ() - vt1.getZ(), 2));
							Vector3d vtmid = new Vector3d(vt1.add(vt2).divide(2));
							float[] vtangle = angleV(new Vector3d(0, 1, 0), vt2.subtract(vt1).normalize());
							helpline.setScale(new Vector3d(1, vtdist * 50, 1));
							helpline.setRotation(new Rotation(vtangle));
							helpline.setTranslation(vtmid);
						} else {
							helpline.setScale(new Vector3d(1, 50, 1));
							helpline.setTranslation(ip);
							vt1 = new Vector3d(vt1t);
							vt2 = new Vector3d(vt2t);
						}
						olddp.setX(dp.getX());
						olddp.setY(dp.getY());
					}

					overlay.postInvalidate();
					Thread.sleep(50);

				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * Closing button.
	 * 
	 * @param v
	 */
	public void onModeButtonClick(View v) {
		mode = (mode + 1) % 3;

		switch (mode) {
		case 0:
			l0.setVisibility(0);
			l1.setVisibility(0);
			l2.setVisibility(4);
			l3.setVisibility(4);
			break;
		case 1:
			l0.setVisibility(4);
			l1.setVisibility(4);
			break;
		case 2:
			l2.setVisibility(0);
			l3.setVisibility(0);
			break;
		}
	}

	/**
	 * End of the application.
	 */
	@Override
	protected void onDestroy() {
		done = true;
		v.interrupt();
		try {
			grab.off();
			upper.off();
			lower.off();
			turn.off();
			System.out.println("Done.");
		} catch (NotConnectedException e) {
			e.printStackTrace();
		}
		super.onDestroy();
		mCallbackHandler.delete();
		mCallbackHandler = null;
	}

	/**
	 * metaio SDK callback handler getter.
	 */
	@Override
	protected IMetaioSDKCallback getMetaioSDKCallbackHandler() {
		return mCallbackHandler;
	}

	/**
	 * Closing button.
	 * 
	 * @param v
	 */
	public void onButtonClick(View v) {
		finish();
	}

	/**
	 * Reset button.
	 * 
	 * @param v
	 */
	public void onResetButtonClick(View v) {
		loadTrackingConfig();
	}

	/**
	 * Loads the 3D models and sets their coordinates.
	 */
	@Override
	protected void loadContents() {
		mModel = loadModel("TutorialEdgeBasedInitialization/Assets/Custom/tracking/TubeModel.obj");
		mVizAidModel = loadModel("TutorialEdgeBasedInitialization/Assets/Custom/tracking/TubeModel.obj");
		helpline = loadModel("TutorialEdgeBasedInitialization/Assets/Custom/tracking/helpline.obj");

		String envmapPath = AssetsManager.getAssetPath(getApplicationContext(), "TutorialEdgeBasedInitialization/Assets/Custom/env_map.zip");
		metaioSDK.loadEnvironmentMap(envmapPath);

		if (mModel != null)
			mModel.setCoordinateSystemID(1);

		if (mVizAidModel != null)
			mVizAidModel.setCoordinateSystemID(2);

		if (helpline != null)
			helpline.setCoordinateSystemID(1);

		helpline.setScale(new Vector3d(0, 0, 0));
		helpline.setVisible(true);

		loadTrackingConfig();
	}

	/**
	 * Loads the tracking configuration.
	 */
	void loadTrackingConfig() {
		boolean result = setTrackingConfiguration("TutorialEdgeBasedInitialization/Assets/Custom/tracking/Tracking.xml");

		if (!result)
			MetaioDebug.log(Log.ERROR, "Failed to load tracking configuration.");

		mState = EState.INITIALIZATION;
	}

	final class MetaioSDKCallbackHandler extends IMetaioSDKCallback {

		/**
		 * Shows the GUI when the SDK is loaded.
		 */
		@Override
		public void onSDKReady() {
			// show GUI
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					mGUIView.setVisibility(View.VISIBLE);
				}
			});
		}

		/**
		 * When initialized, starts tracking.
		 */
		@Override
		public void onTrackingEvent(TrackingValuesVector trackingValues) {
			if (trackingValues.size() > 0 && trackingValues.get(0).getState() == ETRACKING_STATE.ETS_REGISTERED) {
				mState = EState.TRACKING;
			}
		}
	}

	/**
	 * Loads the geometry from the given 3D model.
	 * 
	 * @param path
	 *            Path to the 3D model.
	 * @return Loaded geometry.
	 */
	public IGeometry loadModel(final String path) {
		IGeometry geometry = null;
		try {
			// Load model
			String modelPath = AssetsManager.getAssetPath(getApplicationContext(), path);
			geometry = metaioSDK.createGeometry(modelPath);

			MetaioDebug.log("Loaded geometry " + modelPath);
		} catch (Exception e) {
			MetaioDebug.log(Log.ERROR, "Error loading geometry: " + e.getMessage());
			return geometry;
		}
		return geometry;
	}

	/**
	 * Sets the tracking configuration from the given XML file.
	 * 
	 * @param path
	 *            Path to the XML file.
	 * @return Loaded configuration.
	 */
	public boolean setTrackingConfiguration(final String path) {
		boolean result = false;
		try {
			// set tracking configuration
			String xmlPath = AssetsManager.getAssetPath(getApplicationContext(), path);
			result = metaioSDK.setTrackingConfiguration(xmlPath);
			MetaioDebug.log("Loaded tracking configuration " + xmlPath);
		} catch (Exception e) {
			MetaioDebug.log(Log.ERROR, "Error loading tracking configuration: " + path + " " + e.getMessage());
			return result;
		}
		return result;
	}

	/**
	 * Returns the GUi layout.
	 */
	@Override
	protected int getGUILayout() {
		return R.layout.activity_tutorial_edge_based_initialization;
	}

	/**
	 * Calculates a 2D on screen position from a 3D vector.
	 * 
	 * @param x
	 *            X coordinate of the 3D vector.
	 * @param y
	 *            Y coordinate of the 3D vector.
	 * @param z
	 *            Z coordinate of the 3D vector.
	 * @return 2D vector.
	 */
	private Vector2d v3Tov2(float x, float y, float z) {
		Vector3d v = new Vector3d(x, y, z);
		return metaioSDK.getViewportCoordinatesFrom3DPosition(1, v);
	}

	/**
	 * Calculates turn servo position in degrees.
	 * 
	 * @param pos
	 *            Actual servo position.
	 * @return Position in degrees.
	 */
	private int angleFromServoTurn(int pos) {
		double ang = (pos / 255.0 * 180.0 - 90.0);
		return (int) Math.round(ang);
	}

	/**
	 * Calculates lower arm servo position in degrees.
	 * 
	 * @param pos
	 *            Actual servo position.
	 * @return Position in degrees.
	 */
	private int angleFromServoLower(int pos) {
		double ang = 180.0 - (pos - 70.0) / 120.0 * 90.0;
		return (int) Math.round(ang);
	}

	/**
	 * Calculates upper arm servo position in degrees.
	 * 
	 * @param pos
	 *            Actual servo position.
	 * @return Position in degrees.
	 */
	private int angleFromServoUpper(int pos) {
		double ang = 90.0 - (pos - 50.0) / 150.0 * 90.0;
		return (int) Math.round(ang);
	}

	/**
	 * Upper servo up button listener.
	 * 
	 * @param v
	 *            View.
	 */
	public void onU2ButtonClick(View v) {
		if (upper.getPosition() < 255) {
			try {
				upper.setPosition(upper.getPosition() + 10);
			} catch (NotConnectedException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Upper servo down button listener.
	 * 
	 * @param v
	 *            View.
	 */
	public void onD2ButtonClick(View v) {
		if (upper.getPosition() > 0) {
			try {
				upper.setPosition(upper.getPosition() - 10);
			} catch (NotConnectedException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Lower servo up button listener.
	 * 
	 * @param v
	 *            View.
	 */
	public void onU1ButtonClick(View v) {
		if (lower.getPosition() < 255) {
			try {
				lower.setPosition(lower.getPosition() + 10);
			} catch (NotConnectedException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Lower servo down button listenerT
	 * 
	 * @param v
	 *            View.
	 */
	public void onD1ButtonClick(View v) {
		if (lower.getPosition() > 0) {
			try {
				lower.setPosition(lower.getPosition() - 10);
			} catch (NotConnectedException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Turn servo left button listener.
	 * 
	 * @param v
	 *            View.
	 */
	public void onLButtonClick(View v) {
		if (turn.getPosition() < 255) {
			try {
				turn.setPosition(turn.getPosition() + 10);
			} catch (NotConnectedException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Turn servo right button listener.
	 * 
	 * @param v
	 *            View.
	 */
	public void onRButtonClick(View v) {
		if (turn.getPosition() > 0) {
			try {
				turn.setPosition(turn.getPosition() - 10);
			} catch (NotConnectedException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Grabber servo open/close button listener.
	 * 
	 * @param v
	 *            View.
	 */
	public void onOCButtonClick(View v) {
		if (grab.getPosition() < 128) {
			try {
				grab.setPosition(200);
			} catch (NotConnectedException e) {
				e.printStackTrace();
			}
		} else {
			try {
				grab.setPosition(64);
			} catch (NotConnectedException e) {
				e.printStackTrace();
			}
		}
	}
}
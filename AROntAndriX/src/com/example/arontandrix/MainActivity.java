package com.example.arontandrix;

import java.io.IOException;

import org.andrix.Controller;
import org.andrix.listeners.StateListener;
import org.andrix.low.AXCPAccessor;
import org.andrix.low.ConnectionState;
import org.andrix.low.HardwareController;
import org.andrix.low.NotConnectedException;
import org.andrix.low.RequestTimeoutException;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;

import com.metaio.sdk.MetaioDebug;
import com.metaio.tools.io.AssetsManager;

/**
 * Starting class, manages assets and connection to Hedgehog.
 * 
 * @author Andrej
 *
 */
public class MainActivity extends Activity implements StateListener {

	/**
	 * Task that will extract all the assets
	 */
	AssetsExtracter mTask;

	ConnectionState connectionState = ConnectionState.DISCONNECTED;

	ProgressDialog dialog;

	/**
	 * Start the assets extractor, initialise the AndriX controller.
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		// extract all the assets
		mTask = new AssetsExtracter();
		mTask.execute(0);
		StateListener._l_state.add(this);
		Controller.initialize(this);
		//dialog = ProgressDialog.show(this, "Connecting", "Please wait...", true);
	}

	@Override
	protected void onDestroy() {
		AXCPAccessor.getInstance().connectController(null);
		super.onDestroy();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	/** Called when the user clicks the Start AR button */
	public void startAR(View view) {
		Intent intent = new Intent(this, TutorialEdgeBasedInitialization.class);
		startActivity(intent);
	}

	/** Called when the user clicks the Read Ont button */
	public void readOnt(View view) {
		Intent intent = new Intent(this, DisplayMessageActivity.class);
		startActivity(intent);
	}

	/**
	 * This task extracts all the assets to an external or internal location to
	 * make them accessible to metaio SDK
	 */
	private class AssetsExtracter extends AsyncTask<Integer, Integer, Boolean> {

		@Override
		protected Boolean doInBackground(Integer... params) {
			try {
				// Extract all assets and overwrite existing files if debug
				// build
				AssetsManager.extractAllAssets(getApplicationContext(), BuildConfig.DEBUG);
			} catch (IOException e) {
				MetaioDebug.printStackTrace(Log.ERROR, e);
				return false;
			}

			return true;
		}

		@Override
		protected void onPostExecute(Boolean result) {

			if (result) {
			} else {
				MetaioDebug.log(Log.ERROR, "Error extracting assets, closing the application...");
				finish();
			}
		}
	}

	@Override
	public void connectionStateChange(ConnectionState state, HardwareController hwc) {
		if (state == ConnectionState.CONNECTED_NOAUTH) {
			try {
				hwc.authenticate("");
			} catch (NotConnectedException | RequestTimeoutException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else if (state == ConnectionState.CONNECTED_AUTH) {
			System.out.println("-----" + state.toString());
		}
		connectionState = state;
		System.out.println("-----" + state.toString());
		dialog.dismiss();
	}

	@Override
	public void scanUpdate(HardwareController hwc) {
		if (connectionState == ConnectionState.DISCONNECTED)
			hwc.connect();
	}

	@Override
	public void controllerCharge(int charge) {
		// TODO Auto-generated method stub
	}

	@Override
	public void controllerChargingState(boolean charging) {
		// TODO Auto-generated method stub
	}

}

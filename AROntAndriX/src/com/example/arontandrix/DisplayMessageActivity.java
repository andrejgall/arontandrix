package com.example.arontandrix;

//import org.andrix.low.NotConnectedException;
//import org.andrix.low.RequestTimeoutException;
//import org.andrix.misc.InvalidPortException;
//import org.andrix.sensors.Analog;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.widget.EditText;

/**
 * Test class for displaying values and ontology.
 * 
 * @author Andrej
 *
 */
public class DisplayMessageActivity extends Activity {

	OntAccess rdftest;

	@SuppressLint("NewApi")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		readAndSet();
	}

	/**
	 * Reads the data from the ontology and displays them in a text field.
	 */
	public void readAndSet() {
		rdftest = new OntAccess(this.getApplicationContext());
		// Analog a0;
		// String message = "";
		// try {
		// a0 = new Analog(0);
		// message = "" + a0.getValue();
		// } catch (InvalidPortException | NotConnectedException |
		// RequestTimeoutException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }

		// Create the text view
		EditText editText = new EditText(this);
		editText.setTextSize(10);
		editText.setText(rdftest.test());

		// Set the text view as the activity layout
		setContentView(editText);
	}
}

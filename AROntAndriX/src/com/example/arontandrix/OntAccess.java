package com.example.arontandrix;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import org.andrix.low.NotConnectedException;
import org.andrix.low.RequestTimeoutException;
import org.andrix.misc.InvalidPortException;
import org.andrix.motors.Motor;
import org.andrix.motors.Servo;
import org.andrix.sensors.Digital;

import android.content.Context;
import android.content.res.AssetManager;
import android.os.Environment;

import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntProperty;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFWriter;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;

/**
 * Contains the methods for using the ontology.
 * 
 * @author Andrej
 * 
 */
public class OntAccess {

	/**
	 * Output String.
	 */
	String out;
	/**
	 * Input ontology file.
	 */
	String fileName;
	/**
	 * RDF namespace.
	 */
	String NS;
	/**
	 * Android assets manager.
	 */
	AssetManager am;
	/**
	 * For the file.
	 */
	InputStream ins;
	/**
	 * Ontology model for ontology manipulation.
	 */
	OntModel om;
	/**
	 * Property containing the port of an individual.
	 */
	OntProperty withport_port;
	/**
	 * Property containing the analog value of an individual.
	 */
	OntProperty analog_value;
	/**
	 * Property containing the digital value of an individual.
	 */
	OntProperty digital_value;
	/**
	 * Property containing the actuator position of an individual.
	 */
	OntProperty actuator_position;
	OntProperty next_point;
	/**
	 * Individual in the ontology, a sensor, a servo or a motor.
	 */
	Individual in;
	/**
	 * External storage directory.
	 */
	File root;
	/**
	 * Directory where the ontology is stored.
	 */
	File dir;
	/**
	 * The ontology as file.
	 */
	File file;

	/**
	 * Constructor with the application context as parameter.
	 * 
	 * @param con
	 *            Application context.
	 */
	public OntAccess(Context con) {
		out = "";
		fileName = "ontology.owl";
		// Auto generated name space.
		NS = "http://www.owl-ontologies.com/Ontology1288802320.owl#";
		om = ModelFactory.createOntologyModel();
		root = android.os.Environment.getExternalStorageDirectory();
		dir = new File(root.getAbsolutePath());
		file = new File(dir, fileName);
		file.delete();
		readFile(con);
		// Getting properties from the ontology.
		withport_port = om.getOntProperty(NS + "withport_port");
		analog_value = om.getOntProperty(NS + "analog_value");
		digital_value = om.getOntProperty(NS + "digital_value");
		actuator_position = om.getOntProperty(NS + "actuator_position");
		next_point = om.getOntProperty(NS + "next_point");
	}

	/**
	 * Checks if a file is already present and reads from it. If not, the
	 * ontology is read from the assets folder and saved.
	 * 
	 * @param con
	 *            Application context for reading the assets.
	 */
	public void readFile(Context con) {
		if (!file.exists()) {
			am = con.getAssets();
			try {
				ins = am.open(fileName);
			} catch (Exception e) {
				out += e.toString();
			}
			om.read(ins, "");
			saveOnt(file);
		}
		try {
			FileInputStream fins = new FileInputStream(file);
			om.read(fins, "");
			fins.close();
		} catch (Exception e) {
			out += e.toString();
		}
	}

	/**
	 * Checks if external storage is available for read and write.
	 * 
	 * @return True if available.
	 */
	public boolean isExternalStorageWritable() {
		String state = Environment.getExternalStorageState();
		if (Environment.MEDIA_MOUNTED.equals(state)) {
			return true;
		}
		return false;
	}

	/**
	 * Saves the ontology into a file in RDF/XML format.
	 * 
	 * @param file
	 *            To be saved in.
	 */
	public void saveOnt(File file) {
		if (isExternalStorageWritable()) {
			RDFWriter rw = om.getWriter();
			try {
				OutputStream os = new FileOutputStream(file);
				rw.write(om, os, null);
				os.close();
			} catch (Exception e) {
				out += e.toString();
			}
		} else {
			out += "SD not writable!";
		}
	}

	/**
	 * Test method. Outputs all analog sensor values and changes them.
	 * 
	 * @return String with values.
	 */
	public String readAll() {

		try {
			Individual ind;
			int iter = 1;

			// Changing the values.
			for (int i = 0; i <= iter; i++) {
				updateASensor(i);
			}

			for (int i = 0; i <= iter; i++) {
				ind = om.getIndividual(NS + "ASensor" + i);
				out += "\n" + ind.getLocalName() + "\n Port: " + ind.getProperty(withport_port).getInt() + "\n Value: " + ind.getProperty(analog_value).getInt();
			}

			// Changing the values.
			for (int i = 0; i <= iter; i++) {
				updateASensor(i);
			}

			// Changed output.
			for (int i = 0; i <= iter; i++) {
				ind = om.getIndividual(NS + "ASensor" + i);
				out += "\n" + ind.getLocalName() + "\n Port: " + ind.getProperty(withport_port).getInt() + "\n Value: " + ind.getProperty(analog_value).getInt();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		saveOnt(file);

		return fileName + ":\n" + out;
	}

	public String test() {
		Individual ind = om.getIndividual(NS + "Hedgehog0");
		List<Statement> al = ind.listProperties(next_point).toList();
		out += "\n" + ind.getLocalName() + "\n Next: ";
		for (Statement s : al) {
			out += s.getResource().getLocalName() + ", ";
		}
		return fileName + ":\n" + out;
	}

	/**
	 * Updates all sensors and actuators.
	 */
	public void updateAll() {
		try {
			for (int i = 0; i < 15; i++) {
				updateASensor(i);
			}
			for (int i = 0; i < 15; i++) {
				updateDSensor(i);
			}
			for (int i = 0; i < 5; i++) {
				updateMotor(i);
			}
			for (int i = 0; i < 5; i++) {
				updateServo(i);
			}
		} catch (InvalidPortException | NotConnectedException | RequestTimeoutException e) {
		}
	}

	public ExtendedIterator<Individual> getIndividuals() {
		return om.listIndividuals();
	}

	/**
	 * Reads an analog sensor value from the ontology.
	 * 
	 * @param port
	 *            Sensor port.
	 * @return Sensor value in the ontology.
	 */
	public int getASensorVal(int port) {
		in = om.getIndividual(NS + "ASensor" + port);
		return in.getProperty(analog_value).getInt();
	}

	/**
	 * Reads a digital sensor value from the ontology.
	 * 
	 * @param port
	 *            Sensor port.
	 * @return Sensor value in the ontology.
	 */
	public boolean getDSensorVal(int port) {
		in = om.getIndividual(NS + "DSensor" + port);
		return in.getProperty(analog_value).getBoolean();
	}

	/**
	 * Reads a servo value from the ontology.
	 * 
	 * @param port
	 *            Servo port.
	 * @return Servo value in the ontology.
	 */
	public int getServoVal(int port) {
		in = om.getIndividual(NS + "Servo" + port);
		return in.getProperty(analog_value).getInt();
	}

	/**
	 * Reads a motor value from the ontology.
	 * 
	 * @param port
	 *            Motor port.
	 * @return Motor value in the ontology.
	 */
	public int getMotorVal(int port) {
		in = om.getIndividual(NS + "Motor" + port);
		return in.getProperty(analog_value).getInt();
	}

	/**
	 * Reads the port of an individual.
	 * 
	 * @param in
	 *            Individual to be read from.
	 * @return Port.
	 */
	public int getPort(Individual in) {
		return in.getProperty(withport_port).getInt();
	}

	/**
	 * Sets the position of a physical servo.
	 * 
	 * @param port
	 *            Port of the servo.
	 * @param pos
	 *            Position to be set.
	 * @throws NotConnectedException
	 * @throws InvalidPortException
	 */
	public void setServo(int port, int pos) throws InvalidPortException, NotConnectedException {
		Servo se = new Servo(port);
		se.setPosition(pos);
		updateServo(port);
	}

	/**
	 * Sets the power of a physical motor.
	 * 
	 * @param port
	 *            Port of the motor.
	 * @param pow
	 *            Power to be set.
	 * @throws NotConnectedException
	 * @throws InvalidPortException
	 * @throws RequestTimeoutException
	 */
	public void setMotor(int port, int pow) throws InvalidPortException, NotConnectedException, RequestTimeoutException {
		Motor mo = new Motor(port);
		mo.moveAtPower(pow);
		updateMotor(port);
	}

	/**
	 * Internal. Updates an analog sensor value in the ontology.
	 * 
	 * @param port
	 *            Sensor port.
	 * @throws NotConnectedException
	 * @throws InvalidPortException
	 * @throws RequestTimeoutException
	 */
	public void updateASensor(int port) throws InvalidPortException, NotConnectedException, RequestTimeoutException {
		// Analog an = new Analog(port);
		in = om.getIndividual(NS + "ASensor" + port);
		int anval = in.getProperty(analog_value).getInt() + 1;
		// int anval = an.getValue();
		in.getProperty(analog_value).changeLiteralObject(anval);
	}

	/**
	 * Internal. Updates a digital sensor value in the ontology.
	 * 
	 * @param port
	 *            Sensor port.
	 * @throws NotConnectedException
	 * @throws InvalidPortException
	 * @throws RequestTimeoutException
	 */
	public void updateDSensor(int port) throws InvalidPortException, NotConnectedException, RequestTimeoutException {
		Digital di = new Digital(port);
		boolean dival = di.getValue();
		in = om.getIndividual(NS + "DSensor" + port);
		in.getProperty(digital_value).changeLiteralObject(dival);
	}

	/**
	 * Internal. Updates a servo value in the ontology.
	 * 
	 * @param port
	 *            Servo port.
	 * @throws NotConnectedException
	 * @throws InvalidPortException
	 */
	public void updateServo(int port) throws InvalidPortException, NotConnectedException {
		Servo se = new Servo(port);
		int anval = se.getPosition();
		in = om.getIndividual(NS + "Servo" + port);
		in.getProperty(actuator_position).changeLiteralObject(anval);
	}

	/**
	 * Internal. Updates a motor value in the ontology.
	 * 
	 * @param port
	 *            Motor port.
	 * @throws NotConnectedException
	 * @throws InvalidPortException
	 * @throws RequestTimeoutException
	 */
	public void updateMotor(int port) throws InvalidPortException, NotConnectedException, RequestTimeoutException {
		Motor mo = new Motor(port);
		int anval = mo.getPosition();
		in = om.getIndividual(NS + "Motor" + port);
		in.getProperty(actuator_position).changeLiteralObject(anval);
	}
}

package com.csumb.cst363;


import java.sql.*;
import java.util.Calendar;
import java.text.SimpleDateFormat;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.text.ParseException;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.validation.ValidationException;


/*
 * Controller class for patient interactions.
 *   register as a new patient.
 *   update patient profile.
 */
@Controller
public class ControllerPatient {

	private static final java.text.SimpleDateFormat sdf =
			new java.text.SimpleDateFormat("YYYY-MM-dd");
	@Autowired
	private JdbcTemplate jdbcTemplate;
	private static final String child_specialty = "Pediatrics";
	private static final String[] invalid_specialties = { "Orthpedics", "Dermatology",
			"Cardiology", "Gynecology", "Gastroenterology", "Psychiatry", "Oncology"};

	private static final java.text.SimpleDateFormat sdf_temp =
			new java.text.SimpleDateFormat("YYYY-MM-dd");
	
	/*
	 * Request blank patient registration form.
	 */
	@GetMapping("/patient/new")
	public String newPatient(Model model) {
		// return blank form for new patient registration
		model.addAttribute("patient", new Patient());
		return "patient_register";
	}
	
	/*
	 * Process new patient registration	 */
	@PostMapping("/patient/new")
	public String newPatient(Patient p, Model model) {
		// check for blank lines, do if guard statement. if not a-z A-Z etc, then return.

		int zipint = -1;
		try {
			zipint = Integer.parseInt(p.getZipcode());
		} catch (NumberFormatException e ) {
			model.addAttribute("message","Invalid Zipcode, 5 number digits required. Please check zipcode and resubmit.");
			return "patient_register";
		}

		// if its not 9 numbers or blank
		if (!validateSSN(p.getSsn()) || p.getSsn().isBlank()) {
			model.addAttribute("message","Invalid patient SSN entered, must be a 9 digit number.");
			return "patient_register";
		}
		// if name, street, city, state is != a-z,A-Z or blank
		else if (!isAlpha(p.getFirst_name()) || p.getFirst_name().isBlank()) {
			model.addAttribute("message","Patient first name can only contain a-z, A-Z and cannot be blank");
			return "patient_register";
		}
		else if (!isAlpha(p.getLast_name())|| p.getLast_name().isBlank()) {
			model.addAttribute("message","Patient last name can only contain a-z, A-Z and cannot be blank");
			return "patient_register";
		}
		else if(!isAlpha(p.getStreet())|| p.getStreet().isBlank()) {
			model.addAttribute("message","Invalid street name. Please check street name and resubmit.");
			return "patient_register";
		}
		else if (!isAlpha(p.getCity()) || p.getCity().isBlank() ) {
			model.addAttribute("message","Invalid city name. Please check city name and resubmit.");
			return "patient_register";
		}
		else if (!isAlpha(p.getState()) || p.getState().isBlank()) {
			model.addAttribute("message","Invalid state name. Please check state name and resubmit.");
			return "patient_register";
		}
		// if zip is not 5 digits or 0-99999
		else if ( (p.getZipcode().length() !=5 ) && zipint < 0 || zipint > 99999 ) {
			model.addAttribute("message","Invalid zipcode. Please check zipcode name and resubmit.");
			return "patient_register";
		}
		// if doctor name is empty, as it will look for the name later
		else if (p.getPrimaryName().isEmpty()) {
			model.addAttribute("message","Doctor name not entered. Please check doctor name and resubmit.");
			return "patient_register";
		}

		// if birthdate is a valid date -- yyyy,mm,,dd , 1900-2020, 1-12, 1-31
		if (validateDateInput(p.getBirthdate())) {
			model.addAttribute("message","Invalid Birthdate, please re-enter");
			return "patient_register";
		}

		// try connection
		try ( Connection con = getConnection(); ) {

			// get doctor data of the primary physician for the patient, then check if child or correct field and handle
			//  then if its good, can continue on setting all the patient info

			// if doctor is pediatrics and patient is child or not
			// pediatrics is under 16 years of age, so {current date} - {patient.birthdate} = age, should be less than 16
			String doc_specialty = GetDoctorSpecialty(p.getPrimaryName(), con);
			// check for child
			if (doc_specialty.compareTo(child_specialty) == 0 ) {
				// found child input
				// now find the age of patient and branch accordingly
				String patient_date_str = p.getBirthdate();
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
				Calendar cal = Calendar.getInstance();
				Date valid_date = new Date();
				cal.add(Calendar.YEAR, -16);
				valid_date = cal.getTime();
				Date patient_date = sdf.parse(patient_date_str);

				// adult
				if (patient_date.before(valid_date)) {
					model.addAttribute("message","Invalid Doctor, Family or Internal medicine required. You are an adult so please choose another doctor and resubmit.");
					return "patient_register";
				}

			}
			else {
				// now find the age of patient and branch accordingly
				String patient_date_str = p.getBirthdate();
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
				Calendar cal = Calendar.getInstance();
				Date valid_date = new Date();
				cal.add(Calendar.YEAR, -16);
				valid_date = cal.getTime();
				Date patient_date = sdf.parse(patient_date_str);

				// child
				if (!patient_date.before(valid_date)) {
					model.addAttribute("message","Invalid Doctor, Pediatrics required. You are a child so choose another doctor and resubmit.");
					return "patient_register";
				}

			}

			// other invalid options
			for (String invalid_spec : invalid_specialties) {
				if (doc_specialty.compareTo(invalid_spec) == 0 ) {
					// found invalid input
					model.addAttribute("message","Invalid Doctor, please check doctor name and resubmit.");
					return "patient_register";
				}
			}


			// need to get the doctor id from the name. split up the name, and then look for the doctor with first last name
			// this will fail with doctors of same names. ps.getResultSet()
			int doctor_id = GetPatientDoctorId(p.getPrimaryName(),con);

			// if error, then handle up here.
			// setup prepared statement and insert SQL statement ( starting at 1)
			PreparedStatement ps = con.prepareStatement(
				"INSERT INTO patient(last_name, first_name, birthdate, ssn, street, city, state, zipcode, doctors_doctorId) VALUES (?,?,?,?,?,?,?,?,?)",
					Statement.RETURN_GENERATED_KEYS);

			// https://stackoverflow.com/questions/26097451/preparedstatement-return-generated-keys-and-mysql

			// PATIENT
			ps.setString(1, p.getLast_name());
			ps.setString(2, p.getFirst_name());
			ps.setString(3, p.getBirthdate());
			ps.setString(4, p.getSsn());
			ps.setString(5, p.getStreet());
			ps.setString(6, p.getCity());
			ps.setString(7, p.getState());
			ps.setString(8, p.getZipcode());
			// DOCTOR ID
			ps.setInt(9,doctor_id );

			// execute insertion
			ps.execute();
			// now update patient
			p.setPrimaryID(doctor_id);
			ResultSet rs = ps.getGeneratedKeys();

			while(rs.next())
			{
				p.setPatientId(rs.getString(1));
			}

			model.addAttribute("message", "Registration successful.");
			model.addAttribute("patient", p);
			return "patient_show";
		} catch (SQLException e) {
			model.addAttribute("message","ERROR: SQL Error "+ e.getMessage());
			model.addAttribute("patient", p);
			e.printStackTrace();
					return "patient_register";
		} catch (ParseException e) {
			model.addAttribute("message","ERROR:  Invalid Date "+ e.getMessage());
			model.addAttribute("patient", p);
			e.printStackTrace();
			return "patient_register";
		}
	}

	int GetPatientDoctorId(String doc_name, Connection connection)
	{
		String [] names = doc_name.split("\\s+", 2); // whitespace regex
		// what to do if name is longer than two words? some could be
		//  or maybe just use two fields for first and last name?
		// just split first word as first name, rest as last name
		String tfirst_name = names[0];
		String tlast_name = names[1];

		try {
			PreparedStatement ps = connection.prepareStatement(
					"SELECT doctorId FROM doctor WHERE first_name =? AND last_name =?",
					Statement.RETURN_GENERATED_KEYS);
			ps.setString(1, tfirst_name);
			ps.setString(2,tlast_name);

			// need result set
			ResultSet rs = ps.executeQuery();
			// todo how to find if there is more than one row in the result set
			// https://docs.oracle.com/javase/8/docs/api/java/sql/ResultSet.html
			// get row
			int doc_id = -1; // default to error, should get changed on success
			if ( rs.next() ){
				doc_id = rs.getInt("doctorId");
			}

			return doc_id;

		}  catch (SQLException e) {
			return -1;
		}
	}


	String GetDoctorSpecialty(String doc_name, Connection connection) {
		// search for the doctor, and get the specialty from them and return this as a string!
		String [] names = doc_name.split("\\s+", 2); // whitespace regex

		// just split first word as first name, rest as last name
		String tfirst_name = names[0];
		String tlast_name = names[1];

		try {
			PreparedStatement ps = connection.prepareStatement(
					"SELECT specialty FROM doctor WHERE first_name =? AND last_name =?",
					Statement.RETURN_GENERATED_KEYS);
			ps.setString(1, tfirst_name);
			ps.setString(2, tlast_name);

			ResultSet rs = ps.executeQuery();
			String tstr = "null";
			if (rs.next()) {
				tstr = rs.getString("specialty");

			}
			return tstr;

		} catch (SQLException e) {
			// error
			return null;
		}
	}

	/*
	 * Request blank form to search for patient by id
	 */
	@GetMapping("/patient/edit")
	public String getPatientForm(Model model) {
		return "patient_get";
	}

	/*
	 * Perform search for patient by patient id and name.
	 */
	@PostMapping("/patient/show")
	public String getPatientForm(@RequestParam("patientId") String patientId, @RequestParam("last_name") String last_name,
								 Model model) {

		// TODO
		//  need to use patient_input to find the patient with that id,
		//  and the query all their information with prepared statement
		//  then setup a patient just like below, with this queried information to then-
		//  add to the model to be displayed to the web app

		/*
		 * code to search for patient by id and name retrieve patient data and primary
		 * doctor
		 */
		Patient p = new Patient();

		p.setPatientId(patientId);
		p.setLast_name(last_name);
		try (Connection con = getConnection();) {
			//checking for correct details
			System.out.println("Patient data "+ patientId + " "+ last_name);

			PreparedStatement ps = con.prepareStatement("select p.first_name,p.birthdate, p.street, p.city, p.state, p.zipcode, d.first_name, d.last_name, d.doctorId, d.specialty, d.practice_since from patient p inner join doctor d on p.doctors_doctorId = d.doctorId where p.patientId=? and p.last_name=?");
			// Validate patient last name
			if (!isAlpha(p.getLast_name()) || p.getLast_name().isBlank()) {
				throw new ValidationException("Patient last name can only contain characters a-z and A-Z and cannot be blank");
			}

			ps.setString(1, patientId);
			ps.setString(2, last_name);

			ResultSet rs = ps.executeQuery();

			if(rs.next()){


				p.setFirst_name(rs.getString(1));
				p.setBirthdate(rs.getString(2));
				p.setStreet(rs.getString(3));
				p.setCity(rs.getString(4));
				p.setState(rs.getString(5));
				p.setZipcode(rs.getString(6));
				p.setPrimaryID(rs.getInt(9));
				p.setPrimaryName(rs.getString(7) + " " + rs.getString(8));
				p.setSpecialty(rs.getString(10));
				p.setYears(rs.getString(11));

				model.addAttribute("patient", p);
				return "patient_show";

			}else{
				model.addAttribute("message", "Patient not found.");
				model.addAttribute("patient", p);
				return"patient_get";
			}


		} catch (SQLException e) {

			System.out.println("SQL error in getPatient "+ e.getMessage());
			model.addAttribute("message", "SQL Error."+e.getMessage());
			model.addAttribute("patient", p);
			return "patient_get";
		}
		catch (ValidationException e) {
			model.addAttribute("message", "Validation Error. "+e.getMessage());
			model.addAttribute("patient", p);
			return "patient_get";
		}


	}

	/*
	 *  Display patient profile for patient id.
	 */
	@GetMapping("/patient/edit/{patientId}")
	public String updatePatient(@PathVariable String patientId, Model model) {

		Patient p = new Patient();
		p.setPatientId(patientId);


		// TODO Complete database logic search for patient by id.
		try (Connection con = getConnection();) {

			//checking for correct details
			System.out.println("Patient data "+ patientId);

			PreparedStatement ps = con.prepareStatement("select p.first_name,p.last_name, p.birthdate, p.street, p.city, p.state, p.zipcode, d.first_name, d.doctorId, d.specialty, d.practice_since from patient p inner join doctor d on p.doctors_doctorId = d.doctorId where p.patientId=?");
			ps.setString(1, patientId);


			ResultSet rs = ps.executeQuery();

			if(rs.next()){


				p.setFirst_name(rs.getString(1));
				p.setLast_name(rs.getString(2));
				p.setBirthdate(rs.getString(3));
				p.setStreet(rs.getString(4));
				p.setCity(rs.getString(5));
				p.setState(rs.getString(6));
				p.setZipcode(rs.getString(7));
				p.setPrimaryID(rs.getInt(9));
				p.setPrimaryName(rs.getString(8));
				p.setSpecialty(rs.getString(10));
				p.setYears(rs.getString(11));

				model.addAttribute("patient", p);
				return "patient_edit";

			}else{
				model.addAttribute("message", "Patient not found.");
				model.addAttribute("patient", p);
				return"patient_get";
			}


		} catch (SQLException e) {


			System.out.println("SQL error in getPatient "+ e.getMessage());
			model.addAttribute("message", "SQL Error."+e.getMessage());
			model.addAttribute("patient", p);
			return "patient_get";

		}

	}

	/*
	 * Process changes to patient profile.
	 */
	@PostMapping("/patient/edit")
	public String updatePatient(Patient p, Model model) {

		try (Connection con = getConnection();) {
			PreparedStatement ps = con.prepareStatement("update patient set first_name=?,last_name =?, birthdate=? ,street =?, city=?, state =? where patientId=?");

			// Validate patient last name
			if (!isAlpha(p.getLast_name()) || p.getLast_name().isBlank()) {
				throw new ValidationException("Patient last name can only contain characters a-z and A-Z and cannot be blank");
			}
			// Validate patient first name
			if (!isAlpha(p.getFirst_name()) || p.getFirst_name().isBlank()) {
				throw new ValidationException("Patient first name can only contain characters a-z and A-Z and cannot be blank");
			}

			// Validate state
			if (!isAlpha(p.getState()) || p.getState().isBlank()) {
				throw new ValidationException("State can only contain characters a-z and A-Z and cannot be blank");
			}
			// Validate city
			if (!isAlpha(p.getCity()) || p.getCity().isBlank()) {
				throw new ValidationException("City can only contain characters a-z and A-Z and cannot be blank");
			}

			int zipInt = Integer.parseInt(p.getZipcode());
			// Validate zipcode
			if (zipInt < 10000 || zipInt > 99999) {
				throw new ValidationException("Zipcode must be a five digit number");
			}

			ps.setString(1, p.getFirst_name());
			ps.setString(2, p.getLast_name());
			ps.setString(3, p.getBirthdate());
			ps.setString(4, p.getStreet());
			ps.setString(5, p.getCity());
			ps.setString(6, p.getState());
			ps.setString(7, p.getPatientId());

			int rc = ps.executeUpdate();
			if (rc==1) {
				model.addAttribute("message", "Update successful");
				model.addAttribute("patient", p);
				return "patient_show";

			}else {
				model.addAttribute("message", "Error. Update was not successful");
				model.addAttribute("doctor", p);
				return "patient_edit";
			}

		} catch (SQLException e) {
			model.addAttribute("message", "SQL Error. "+e.getMessage());
			model.addAttribute("patient", p);
			return "patient_edit";
		}
		catch (ValidationException e) {
			model.addAttribute("message", "Validation Error. "+e.getMessage());
			model.addAttribute("patient", p);
			return "patient_edit";
		}


	}

	// Used to validate that a string only contains alphabetic characters
	public boolean isAlpha(String name) {
		return name.matches("[a-zA-Z]+");
	}

	// Used to validate ssn format
	public boolean validateSSN(String ssn) {
		char[] chars = ssn.toCharArray();
		if (chars.length != 9) {
			return false;
		}
		for (int i = 0; i < chars.length; i++) {

			// Check if character is
			// not a digit between 0-9
			// then return false
			if (chars[i] < '0'
					|| chars[i] > '9') {
				return false;
			}
		}
		if (chars[0] == '0' || chars[0] == '9') {
			return false;
		}
		else if (chars[3] == '0' && chars[4] == '0') {
			return false;
		}
		else if (chars[5] == '0' && chars[6] == '0' && chars[7] == '0' && chars[8] == '0') {
			return false;
		}
		return true;
	}
	/*
	 * return JDBC Connection using jdbcTemplate in Spring Server
	 */
	private Connection getConnection() throws SQLException {
		Connection conn = jdbcTemplate.getDataSource().getConnection();
		return conn;
	}

	private static boolean validateDateInput(String dateString) {
		try {
			if (!dateString.isEmpty()) {
				Date date = sdf_temp.parse(dateString.trim());
				// create calender to extract and check year, month and day
				Calendar check_date = Calendar.getInstance();
				check_date.setTime(date);
				// year between 1900-2022)
				if (check_date.get(Calendar.YEAR) >= 1900 && check_date.get(Calendar.YEAR) <= 2022)
				{
					return sdf_temp.format(date).equals(dateString.trim());
				}
				// month between 1-12
				if ( check_date.get(Calendar.MONTH) >=0  || check_date.get(Calendar.MONTH) < 12)
				{
					return sdf_temp.format(date).equals(dateString.trim());
				}
				// day between 1-31
				if (check_date.get(Calendar.DAY_OF_MONTH) >=1 || check_date.get(Calendar.DAY_OF_MONTH) <= 31)
				{
					return sdf_temp.format(date).equals(dateString.trim());
				}
				return false;

			} else {
				return false;
			}
		} catch (ParseException var2) {
			var2.printStackTrace();
			return false;
		}
	}

}

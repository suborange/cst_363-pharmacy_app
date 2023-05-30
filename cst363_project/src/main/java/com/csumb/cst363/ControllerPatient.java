package com.csumb.cst363;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

/*
 * Controller class for patient interactions.
 *   register as a new patient.
 *   update patient profile.
 */
@Controller
public class ControllerPatient {
	
	@Autowired
	private JdbcTemplate jdbcTemplate;
	private static final String child_specialty = "Pediatrics";
	private static final String[] invalid_specialties = { "Orthpedics", "Dermatology",
			"Cardiology", "Gynecology", "Gastroenterology", "Psychiatry", "Oncology"};
	
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

		// try connection
		try ( Connection con = getConnection(); ) {
			// todo check for blank lines, do if guard statement. if not a-z A-Z etc, then return.
			// TODO
//		if () {
//
//		}
			// todo get doctor data of the primary physician for the patient, then check if child or correct field and handle
			//  then if its good, can continue on setting all the patient info

			// if doctor is pediatrics and patient is child or not
			// pediatrics is under 16 years of age, so {current date} - {patient.birthdate} = age, should be less than 16
			String doc_specialty = GetDoctorSpecialty(p.getPrimaryName(), con);
			// check for child
			if (doc_specialty.compareTo(child_specialty) == 0 ) {
				// found child input
				// now find the age of patient and branch accordingly

			}

			// other invalid options
			for (String invalid_spec : invalid_specialties) {
				if (doc_specialty.compareTo(invalid_spec) == 0 ) {
					// found invalid input
					model.addAttribute("message","Invalid Doctor Specialty, please re-enter");
					return "patient_register";

				}
			}





			// need to get the doctor id from the name. split up the name, and then look for the doctor with first last name
			// this will fail with doctors of same names. ps.getResultSet()
			int doctor_id = GetPatientDoctorId(p.getPrimaryName(),con);

			// if error, then handle up here.
			// setup prepared statement and insert SQL statement ( starting at 1)
			PreparedStatement ps = con.prepareStatement(
				"INSERT INTO patient(last_name, first_name, birthdate, ssn, street, city, state, zipcode, doctor_doctorId) VALUES (?,?,?,?,?,?,?,?,?)",
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


			// todo need to think about getting doctor info for primary physician, check if patient is child
			//  if child, then pediatrics, if not then ONLY internal or family medicine.


			model.addAttribute("message", "Registration successful.");
			model.addAttribute("patient", p);
			return "patient_show";
		} catch (SQLException e) {
			model.addAttribute("message","! SQL Error !"+ e.getMessage());
			model.addAttribute("patient", p);
			e.printStackTrace();
					return "patient_register";
		}


	}

	int GetPatientDoctorId(String doc_name, Connection connection)
	{
		String [] names = doc_name.split("\\s+", 2); // whitespace regex
		// what to do if name is longer than two words? some could be..
		// todo need to fix this because the name is quite long..
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

			// need resultset
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
	 * Request blank form to search for patient by and and id
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
		
		// return fake data for now.
		Patient p = new Patient();
		p.setPatientId(patientId);
		p.setLast_name(last_name);
		p.setBirthdate("2001-01-01");
		p.setStreet("123 Main");
		p.setCity("SunCity");
		p.setState("CA");
		p.setZipcode("99999");
		p.setPrimaryID(11111);
		//p.setPrimaryName("Dr. Watson");
//		p.setSpecialty("Family Medicine");
//		p.setYears("1992");

		model.addAttribute("patient", p);
		return "patient_show";
	}

	/*
	 *  Display patient profile for patient id.
	 */
	@GetMapping("/patient/edit/{patientId}")
	public String updatePatient(@PathVariable String patientId, Model model) {

		// TODO Complete database logic search for patient by id.

		// return fake data.
		Patient p = new Patient();
		p.setPatientId(patientId);
		p.setFirst_name("Alex");
		p.setLast_name("Patient");
		p.setBirthdate("2001-01-01");
		p.setStreet("123 Main");
		p.setCity("SunCity");
		p.setState("CA");
		p.setZipcode("99999");
		p.setPrimaryID(11111);
		//p.setPrimaryName("Dr. Watson");
//		p.setSpecialty("Family Medicine");
//		p.setYears("1992");

		model.addAttribute("patient", p);
		return "patient_edit";
	}
	
	
	/*
	 * Process changes to patient profile.  
	 */
	@PostMapping("/patient/edit")
	public String updatePatient(Patient p, Model model) {

		// TODO

		model.addAttribute("patient", p);
		return "patient_show";
	}

	/*
	 * return JDBC Connection using jdbcTemplate in Spring Server
	 */

	private Connection getConnection() throws SQLException {
		Connection conn = jdbcTemplate.getDataSource().getConnection();
		return conn;
	}

}

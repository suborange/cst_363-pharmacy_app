package com.csumb.cst363;

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

		// TODO

		/*
		 * Complete database logic to verify and process new patient
		 */
		// remove this fake data.
		p.setPatientId("300198");
		model.addAttribute("message", "Registration successful.");
		model.addAttribute("patient", p);
		return "patient_show";

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

}

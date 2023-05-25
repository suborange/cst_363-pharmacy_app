package com.csumb.cst363;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

@Controller   
public class ControllerPrescriptionFill {

	@Autowired
	private JdbcTemplate jdbcTemplate;


	/* 
	 * Patient requests form to search for prescription.
	 */
	@GetMapping("/prescription/fill")
	public String getfillForm(Model model) {
		model.addAttribute("prescription", new Prescription());
		return "prescription_fill";
	}


	/*
	 * Process the prescription fill request from a patient.
	 * 1.  Validate that Prescription p contains rxid, pharmacy name and pharmacy address
	 *     and uniquely identify a prescription and a pharmacy.
	 * 2.  update prescription with pharmacyid, name and address.
	 * 3.  update prescription with today's date.
	 * 4.  Display updated prescription 
	 * 5.  or if there is an error show the form with an error message.
	 */
	@PostMapping("/prescription/fill")
	public String processFillForm(Prescription p,  Model model) {


		// TODO  

		// temporary code to set fake data for now.
		p.setPharmacyID("70012345");
		p.setCost(String.format("%.2f", 12.5));
		p.setDateFilled( new java.util.Date().toString() );

		// display the updated prescription

		model.addAttribute("message", "Prescription has been filled.");
		model.addAttribute("prescription", p);
		return "prescription_show";

	}

	/*
	 * return JDBC Connection using jdbcTemplate in Spring Server
	 */

	private Connection getConnection() throws SQLException {
		Connection conn = jdbcTemplate.getDataSource().getConnection();
		return conn;
	}

}
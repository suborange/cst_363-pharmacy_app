package com.csumb.cst363;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import javax.validation.ValidationException;

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
		PreparedStatement ps;
		ResultSet rs;
		try (Connection con = getConnection();) {
			// Retrieve matching RXNumber based on user input
			int rxNumber = 0;
			String pharma_corp_name = "";
			double price = 0;
			// Validate Rx digit count is = 10
			int rxNumLength = String.valueOf(p.getRxid()).length();
			if (rxNumLength != 10) {
				throw new ValidationException("Rx must be a 10 digit number.");
			}

			// Retrieve variety of related information needed to update data in db
			ps = con.prepareStatement("select dp.RXnumber, dp.quantity, doc.first_name, doc.last_name, doc.ssn, d.TradeName, dpc.pharma_corp_name, pp.price " +
					"from doctor_prescription dp, doctor doc, drug d, drug_has_pharma_corp dpc, drug_has_prescription dhp, pharmacy_prescription pp " +
					"where dp.doctor_doctorid = doc.doctorId AND dp.drug_drugsid = d.drugid AND d.drugid = dpc.drug_drugid AND d.drugid = dhp.drug_prescriptionid AND dhp.pharmacy_prescription_drugKey = pp.drugKey" +
					" AND dp.RXnumber = " + p.getRxid());
			rs = ps.executeQuery();
			while (rs.next()) {
				rxNumber = rs.getInt("RXnumber");
				p.setRxid(rxNumber);
				p.setDoctorFirstName(rs.getString("first_name"));
				p.setDoctorLastName(rs.getString("last_name"));
				p.setDoctor_ssn(rs.getString("ssn"));
				p.setDrugName(rs.getString("TradeName"));
				p.setQuantity(rs.getInt("quantity"));
				pharma_corp_name = rs.getString("pharma_corp_name");
				price = rs.getDouble("price");
				p.setCost(String.format("%.2f", price));
			}
			if (rxNumber == 0) {
				throw new SQLException("Prescription not found. Please check Rx and resubmit.");
			}
			// Retrieve matching pharmacy info based on user input
			int pharmacyId = 0;
			ps = con.prepareStatement("select pharmacyId, phonenumber from pharmacy where name = '" + p.getPharmacyName() + "' AND address = '" + p.getPharmacyAddress() + "'");
			rs = ps.executeQuery();
			while (rs.next()) {
				pharmacyId = rs.getInt("pharmacyId");
				p.setPharmacyID(pharmacyId);
				p.setPharmacyPhone(rs.getString("phonenumber"));
			}
			if (pharmacyId == 0) {
				throw new SQLException("Pharmacy not found. Please check pharmacy details and resubmit.");
			}
			// Retrieve matching patient info based on user input
			int patientId = 0;
			ps = con.prepareStatement("select patientId, first_name, last_name, ssn from patient where last_name = '" + p.getPatientLastName() + "'");
			rs = ps.executeQuery();
			while (rs.next()) {
				patientId = rs.getInt("patientId");
				p.setPatientFirstName(rs.getString("first_name"));
				p.setPatientLastName(rs.getString("last_name"));
				p.setPatient_ssn(rs.getString("ssn"));
			}
			if (patientId == 0) {
				throw new SQLException("Patient not found. Please check patient last name and resubmit.");
			}

			p.setDateFilled(new SimpleDateFormat("YYYY-MM-dd").format(new Date()));

			// Write to Fill table
			ps = con.prepareStatement("insert into Fill(dateFilled, pharmacy_pharmacyid, pharma_corp_name, doctor_prescription_RXnumber) values( ?, ?, ?, ?)",
					Statement.RETURN_GENERATED_KEYS);
			ps.setString(1, p.getDateFilled());
			ps.setInt(2, pharmacyId);
			ps.setString(3, pharma_corp_name);
			ps.setInt(4, rxNumber);
			ps.executeUpdate();
			rs = ps.getGeneratedKeys();

			// display message and patient information
			model.addAttribute("message", "Prescription created.");
			model.addAttribute("doctor_prescription", p);
			return "prescription_show";

		} catch (SQLException e) {
			model.addAttribute("message", "SQL Error."+e.getMessage());
			model.addAttribute("doctor_prescription", p);
			return "prescription_fill";
		}
		catch (ValidationException e) {
			model.addAttribute("message", "Validation Error: "+e.getMessage());
			model.addAttribute("doctor_prescription", p);
			return "prescription_fill";
		}
	}

	/*
	 * return JDBC Connection using jdbcTemplate in Spring Server
	 */

	private Connection getConnection() throws SQLException {
		Connection conn = jdbcTemplate.getDataSource().getConnection();
		return conn;
	}

}
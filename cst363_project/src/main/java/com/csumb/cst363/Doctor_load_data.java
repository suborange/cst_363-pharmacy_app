package com.csumb.cst363;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * This is an example of a JDBC Java application.
 * Run this program as a Java application.
 */

public class Doctor_load_data {
		
	static final String DBURL = "jdbc:mysql://localhost:3306/drugstorechain";  // database URL


	// Replace USERID with correct user info
	static final String USERID = "";

	// Replace PASSWORD with correct password info
	static final String PASSWORD = "";

	
	static final String[] specialties= {"Internal Medicine", "Family Medicine", "Pediatrics", "Orthpedics", "Dermatology", 
			"Cardiology", "Gynecology", "Gastroenterology", "Psychiatry", "Oncology"};

	public static void main(String[] args) {
		
		Random gen = new Random();
		

		
		// connect to mysql server
		
		try (Connection conn = DriverManager.getConnection(DBURL, USERID, PASSWORD);) {
			
			PreparedStatement ps;
			ResultSet rs;
			int id;
			int row_count;
			
			// delete all doctor rows 
			ps = conn.prepareStatement("delete from doctor");
			row_count = ps.executeUpdate();
			System.out.println("rows deleted "+row_count);

			// We want to generated column "doctorId" value to be returned
			// as a generated key

			String sqlINSERT = "insert into doctor(last_name, first_name, specialty, practice_since, ssn) values( ?, ?, ?, ?, ?)";
			String[] keycols = {"doctorId"};
			ps = conn.prepareStatement(sqlINSERT, keycols);

			// Used to generate a random ssn
			List<Integer> randomNums = new ArrayList<>(10000);
			for (int i = 10001; i < 20001; i++) {
				randomNums.add(i);
			}
			Collections.shuffle(randomNums, gen);

			// Insert 10 rows with data
			for (int k=1; k<=10; k++) {
				String practice_since = Integer.toString(2000+gen.nextInt(20));
				// DONE - the generated ssn is now unique to this individual
				String ssn = Integer.toString(123450000+randomNums.get(k));
				ps.setString(1,  "Doctor Number "+k);
				ps.setString(2, "Dr.");
				ps.setString(3, specialties[k%specialties.length]);
				ps.setString(4, practice_since);
				ps.setString(5, ssn);
				row_count = ps.executeUpdate();
				System.out.println("row inserted "+row_count);

				// Retrieve and print the generated primary key
				rs = ps.getGeneratedKeys();
				rs.next();
				id = rs.getInt(1);
				System.out.println("row inserted for doctor id "+id);
			}

			// Display all rows
			System.out.println("All randomly generated doctors");

			String sqlSELECT = "select doctorId, last_name, first_name, specialty, practice_since, ssn from doctor";
			ps = conn.prepareStatement(sqlSELECT);
			// There are no parameter markers to set
			rs = ps.executeQuery();
			while (rs.next()) {
				id = rs.getInt("doctorId");
				String last_name = rs.getString("last_name");
				String first_name = rs.getString("first_name");
				String specialty = rs.getString("specialty");
				String practice_since = rs.getString("practice_since");
				String ssn = rs.getString("ssn");
				System.out.printf("%10d   %-30s  %-20s %4s %11s \n", id, last_name+", "+first_name, specialty, practice_since, ssn);
			}
		} catch (SQLException e) {
			System.out.println("Error: SQLException "+e.getMessage());
			e.printStackTrace();
		}
	}
}

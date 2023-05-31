package com.csumb.cst363;

import java.sql.*;
import java.util.Scanner;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;

@Controller
public class ManagerReport {

    /**
     * A pharmacy manager requests a report of the quantity of drugs that have been used to fill prescriptions by the pharmacy.
     * The report will contain the names of drugs used and the quantity of each drug used.
     * Input is pharmacy id and a start and end date range. ( create a new class, that queries this report, and displays it.
     */
    @Autowired

    static final String DBURL = "jdbc:mysql://localhost:3306/drugstorechain";  // database URL
    static final String USERID = "root";
    static final String PASSWORD = "hahathis15paws";

    public static void main(String[] args) {
        // get user input for the pharmacy id, start, and end date( for contract? not really sure).

        // query information from the database - quantity (doctor prescription), tradename( if exists) and generic name of drugs.
        // start and end date for filled date between that range

        // so go from pharmacy -> fill -> doctor prescription -> drug ?



        /**
         *  SELECT tradeName, genericName, quantity FROM drug
         * 	JOIN doctor_prescription ON drug.drugid = doctor_prescription.drug_drugid
         *     JOIN fill ON fill.doctor_prescription_RXnumber = doctor_prescription.RXnumber
         *     JOIN pharmacy ON pharmacy.pharmacyid = fill.pharmacy_pharmacyid
         *     WHERE pharmacyid = "10" AND fill.dateFilled BETWEEN "2000-01-01" AND "2020-01-01" ;
         */
         try (Connection con = DriverManager.getConnection(DBURL, USERID, PASSWORD); )
         {
             String pharmacy_id;
             String start_date;
             String end_date;
             // get user input
             Scanner input = new Scanner(System.in);
             System.out.println("Please enter a pharmacy id number: ");
             pharmacy_id = input.nextLine();// validate an integer?
             System.out.println("Please enter the starting date in yyyy-mm-dd format: ");
             // check for date ranges?
             start_date = input.nextLine();
             System.out.println("Please enter the ending date in yyyy-mm-dd format: ");
             end_date = input.nextLine();


             // query
             String SQLSelect = "SELECT tradeName, genericName, SUM(quantity) FROM drug " +
                     " JOIN doctor_prescription ON drug.drugid = doctor_prescription.drug_drugid" +
                     " JOIN fill ON fill.doctor_prescription_RXnumber = doctor_prescription.RXnumber" +
                     " JOIN pharmacy ON pharmacy.pharmacyid = fill.pharmacy_pharmacyid" +
                     " WHERE pharmacyid = ? AND fill.dateFilled BETWEEN DATE ? AND ? GROUP BY tradeName, genericName" ;

             PreparedStatement ps = con.prepareStatement(SQLSelect);

             ps.setString(1, pharmacy_id);
             ps.setDate(2, java.sql.Date.valueOf(start_date));
             ps.setDate(3, java.sql.Date.valueOf(end_date));

             // get results set
             ResultSet manager_report = ps.executeQuery();
             System.out.println("-------------------------------------------------------------");
             System.out.printf("%-20s  %-20s %14s \n", "Trade Name","Generic Name","Quantity");
             System.out.println("-------------------------------------------------------------");


             while (manager_report.next()) {
                 // display everything

                 String trade_name = manager_report.getString("tradeName");
                 String generic_name = manager_report.getString("genericName");
                 String quantity = manager_report.getString("SUM(quantity)");
                 System.out.printf("%-20s  %-20s %14s \n", trade_name , generic_name, quantity);
             }

             System.out.println("-------------------------------------------------------------");


         } catch (SQLException e ) {
             System.out.println("SQL ERROR: " + e.getMessage());
             e.printStackTrace();

         } catch (IllegalArgumentException e)
         {
             System.out.println("SQL Invalid Input ERROR: " + e.getMessage());
             e.printStackTrace();
         }


    }




}

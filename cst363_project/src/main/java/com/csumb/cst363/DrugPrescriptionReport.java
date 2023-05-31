package com.csumb.cst363;

import javax.validation.ValidationException;
import java.sql.*;
import java.text.ParseException;
import java.util.Date;
import java.util.Scanner;


public class DrugPrescriptionReport {

    private static final java.text.SimpleDateFormat sdf =
            new java.text.SimpleDateFormat("YYYY-MM-dd");
    public static void main(String[] args) throws SQLException {

        String url = "jdbc:mysql://localhost:3306/DrugStoreChain";
        String username = "root";
        String password = "pRimealiAs15279!";

        try (Connection conn = DriverManager.getConnection(url, username, password)) {
            generateReport(conn);
        } catch (SQLException e) {

            e.printStackTrace();
        }


    }

    public static void generateReport(Connection conn) {

        String sql = "SELECT d.last_name, d.first_name, SUM(dp.quantity) AS total_quantity " +
                "FROM doctor_prescription dp " +
                "JOIN doctor d ON dp.doctor_doctorId = d.doctorId " +
                "JOIN drug dr ON dp.drug_drugsid = dr.drugid " +
                "WHERE dr.tradeName LIKE ? " +
                "AND dp.datePrescribed BETWEEN ? AND ? " +
                "GROUP BY d.last_name, d.first_name";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            String drugName;
            String startDate;
            String endDate;
            // get user input
            Scanner input = new Scanner(System.in);
            System.out.println("Please enter the trade name of the drug: ");
            drugName = input.nextLine();// validate an integer?
            System.out.println("Please enter the start date in yyyy-mm-dd format: ");
            // Validate Date Ranges
            startDate = input.nextLine();
            if (!validateDateInput(startDate)) {
                throw new ValidationException("Start Date must be in the format yyyy-mm-dd");
            }
            System.out.println("Please enter the end date in yyyy-mm-dd format: ");
            endDate = input.nextLine();
            if (!validateDateInput(endDate)) {
                throw new ValidationException("End Date must be in the format yyyy-mm-dd");
            }
            ps.setString(1, "%" + drugName + "%");
            ps.setString(2, startDate);
            ps.setString(3, endDate);

            ResultSet rs = ps.executeQuery();

            System.out.println("Prescription Report: " + drugName);
            System.out.println("--------------------");
            System.out.println("Doctor Name\t\tQuantity Prescribed");
            System.out.println("-----------------------------------");

            while (rs.next()) {
                String lastName = rs.getString("last_name");
                String firstName = rs.getString("first_name");
                int totalQuantity = rs.getInt("total_quantity");

                String doctorName = lastName + ", " + firstName;
                System.out.println(doctorName + "\t\t" + totalQuantity);
            }


        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        catch (ValidationException e) {
            System.out.println(e.getMessage());
        }


    }

    private static boolean validateDateInput(String dateString) {
        try {
            if (!dateString.isEmpty()) {
                Date date = sdf.parse(dateString.trim());
                return sdf.format(date).equals(dateString.trim());
            } else {
                return false;
            }
        } catch (ParseException var2) {
            var2.printStackTrace();
            return false;
        }
    }

}
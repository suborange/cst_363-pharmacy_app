package com.csumb.cst363;

import java.sql.*;


public class DrugPrescriptionReport {


    public static void main(String[] args) throws SQLException {

        String url = "jdbc:mysql://localhost:3306/DrugStoreChain";
        String username = "root";
        String password = "Admin123";

        String drugName = "Asp";
        String startDate = "2022-01-01";
        String endDate = "2022-12-31";


        try (Connection conn = DriverManager.getConnection(url, username, password)) {
            generateReport(conn, drugName, startDate, endDate);
        } catch (SQLException e) {

            e.printStackTrace();
        }


    }

    public static void generateReport(Connection conn, String drugName, String startDate, String endDate) {

        String sql = "SELECT d.last_name, d.first_name, SUM(dp.quantity) AS total_quantity " +
                "FROM doctor_prescription dp " +
                "JOIN doctor d ON dp.doctor_doctorId = d.doctorId " +
                "JOIN drug dr ON dp.drug_drugsid = dr.drugid " +
                "WHERE dr.tradeName LIKE ? " +
                "AND dp.date BETWEEN ? AND ? " +
                "GROUP BY d.last_name, d.first_name";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, "%" + drugName + "%");
            ps.setString(2, startDate);
            ps.setString(3, endDate);

            ResultSet rs = ps.executeQuery();

            System.out.println("Prescription Report:");
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


    }

}

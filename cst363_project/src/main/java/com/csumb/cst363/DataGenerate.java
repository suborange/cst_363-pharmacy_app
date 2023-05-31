package com.csumb.cst363;

import java.sql.*;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.*;

public class DataGenerate {
    static final String DBURL = "jdbc:mysql://localhost:3306/drugstorechain";  // database URL

    // Replace USERID with correct user info
    static final String USERID = "";

    // Replace PASSWORD with correct password info
    static final String PASSWORD = "";

    static final String[] specialties= {"Internal Medicine", "Family Medicine", "Pediatrics", "Orthopedics", "Dermatology",
            "Cardiology", "Gynecology", "Gastroenterology", "Psychiatry", "Oncology"};

    // Clear database, then generate random data and insert into table.
    public static void main(String[] args) {
        deleteExistingData();
        Random gen = new Random();
        generateDoctors(gen);
        generatePatients(gen);
        generatePrescriptions(gen);
    }

    // Generate doctor data and insert into table (copied & modified from 'Doctor_load_data.java')
    private static void generateDoctors(Random gen) {
        try (Connection conn = DriverManager.getConnection(DBURL, USERID, PASSWORD);) {
            PreparedStatement ps;
            ResultSet rs;
            int id;
            int row_count;

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
        }
    }

    // Generate patient data and insert into table.
    private static void generatePatients(Random gen) {
        try (Connection conn = DriverManager.getConnection(DBURL, USERID, PASSWORD);) {
            PreparedStatement ps;
            ResultSet rs;
            int id;
            int row_count;

            // Retrieve doctorIds to assign a random doctor to each randomly generated patient
            List<Integer> doctorIds = new ArrayList<>(10);
            ps = conn.prepareStatement("select doctorId from doctor");
            rs = ps.executeQuery();
            while (rs.next()) {
                doctorIds.add(rs.getInt("doctorId"));
            }

            // We want to generated column "patientId" value to be returned
            // as a generated key
            String sqlINSERT = "insert into patient(last_name, first_name, birthdate, ssn, street, city, state, zipcode, doctors_doctorId) values( ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            String[] keycols = {"patientId"};
            ps = conn.prepareStatement(sqlINSERT, keycols);

            // Used to generate a random ssn
            List<Integer> randomNums = new ArrayList<>(10000);
            for (int i = 1; i< 10001; i++) {
                randomNums.add(i);
            }
            Collections.shuffle(randomNums, gen);

            // Used to generate a random birthdate
            SimpleDateFormat simpleDateFormat =
                    new SimpleDateFormat("YYYY-MM-dd");
            Calendar c = Calendar.getInstance();

            Date dt = new Date(c.getTimeInMillis());
            String random_birthdate = null;

            String[] firstNames = { "Janet",
                    "Jim", "Bob", "Alice",
                    "Peter",  "Margaret", "Chloe",
                    "Allison", "Douglas", "Petunia",
                    "Barbara", "Alex", "Jesse",
                    "Manuel", "Jack", "Pat",
                    "Aaron", "Ethan", "Christopher",
                    "Sarah", "Amanda", "Gina", "Tony"};

            String[] lastNames = { "Johnson",
                    "Bourdeaux", "Bonavida", "Gates",
                    "Jackson",  "Smith", "Anderson",
                    "Miller", "Small", "Martin",
                    "Campbell", "Davis", "Taylor",
                    "Harris", "Martinez", "Robinson",
                    "Adams", "Moore", "Thompson",
                    "Scott", "Clark", "Lewis", "Evans"};

            String[] streetNames = { "Main St.",
                    "1st St.", "Jackson Ave.", "Gates Blvd.",
                    "2nd St.",  "Bleecker St.", "Anderson Way",
                    "3rd St.", "4th St.", "5th St.",
                    "Acorn Way", "Madison Ave.", "Center Ave.",
                    "Harrison Ave.", "Martingale Ct.", "Indian Creek Pl."};

            String[] cities = { "Townsville",
                    "Paris", "Boise", "Montgomery",
                    "Villestown",  "Smithstown"};

            String[] states = { "CA",
                    "WI", "ND", "NY",
                    "MI",  "MO"};

            // Insert 100 rows with data
            for (int k=1; k<=100; k++) {

                String ssn = Integer.toString(123450000+randomNums.get(k));
                c.set(Calendar.YEAR,  1930+gen.nextInt(92));
                c.set(Calendar.DAY_OF_YEAR, 1);
                c.add(Calendar.DAY_OF_YEAR, gen.nextInt(365));
                dt = new Date(c.getTimeInMillis());
                random_birthdate = simpleDateFormat.format(dt);

                String random_first_name = firstNames[gen.nextInt(firstNames.length)];
                String random_last_name = lastNames[gen.nextInt(lastNames.length)];
                String random_street = streetNames[gen.nextInt(streetNames.length)];
                String random_city = cities[gen.nextInt(cities.length)];
                String random_state = states[gen.nextInt(states.length)];
                String random_zipcode = Integer.toString(200000+gen.nextInt(799999));
                int random_doctorId = doctorIds.get(gen.nextInt(doctorIds.size()));

                ps.setString(1,  random_last_name);
                ps.setString(2, random_first_name);
                ps.setString(3, random_birthdate);
                ps.setString(4, ssn);
                ps.setString(5, random_street);
                ps.setString(6, random_city);
                ps.setString(7, random_state);
                ps.setString(8, random_zipcode);
                ps.setInt(9, random_doctorId);
                row_count = ps.executeUpdate();
                System.out.println("row inserted "+row_count);

                // retrieve and print the generated primary key

                rs = ps.getGeneratedKeys();
                rs.next();
                id = rs.getInt(1);
                System.out.println("row inserted for patient id "+id);
            }

            // display all rows
            System.out.println("All randomly generated patients");

            String sqlSELECT = "select patientId, last_name, first_name, birthdate, ssn, street, city, state, zipcode, doctors_doctorId from patient";
            ps = conn.prepareStatement(sqlSELECT);
            // there are no parameter markers to set
            rs = ps.executeQuery();
            while (rs.next()) {
                id = rs.getInt("patientId");
                String last_name = rs.getString("last_name");
                String first_name = rs.getString("first_name");
                String birthdate = rs.getString("birthdate");
                String ssn = rs.getString("ssn");
                String street = rs.getString("street");
                String city = rs.getString("city");
                String state = rs.getString("state");
                String zipcode = rs.getString("zipcode");
                System.out.printf("%10d   %-30s  %-20s %11s %20s %20s  %20s  %20s\n", id, last_name+", "+first_name, birthdate, ssn, street, city, state, zipcode);
            }
        } catch (SQLException e) {
            System.out.println("Error: SQLException "+e.getMessage());
        }
    }

    // Generate prescription data and insert into table.
    private static void generatePrescriptions(Random gen) {
        try (Connection conn = DriverManager.getConnection(DBURL, USERID, PASSWORD);) {
            PreparedStatement ps;
            ResultSet rs;
            int id;
            int row_count;

            // Retrieve doctorIds to assign a random doctor to each randomly generated prescription
            List<Integer> doctorIds = new ArrayList<>(10);
            ps = conn.prepareStatement("select doctorId from doctor");
            rs = ps.executeQuery();
            while (rs.next()) {
                doctorIds.add(rs.getInt("doctorId"));
            }

            // Retrieve patientIds to assign a random doctor to each randomly generated prescription
            List<Integer> patientIds = new ArrayList<>(100);
            ps = conn.prepareStatement("select patientId from patient");
            rs = ps.executeQuery();
            while (rs.next()) {
                patientIds.add(rs.getInt("patientId"));
            }

            // Used to generate unique ten digit RXNumbers
            List<Integer> randomNums = new ArrayList<>(999999);
            for (int i = 1; i < 999999; i++) {
                randomNums.add(i);
            }
            Collections.shuffle(randomNums, gen);

            // Used to generate random datePrescribed
            SimpleDateFormat simpleDateFormat =
                    new SimpleDateFormat("YYYY-MM-dd");
            Calendar c = Calendar.getInstance();
            Date dt = new Date(c.getTimeInMillis());
            String random_prescribed_date = null;

            // Generate prescription data and insert into table
            String sqlINSERT = "insert into doctor_prescription(RXnumber, drug_drugsid, quantity, datePrescribed, doctor_doctorId, patient_patientId) values( ?, ?, ?, ?, ?, ?)";
            String[] keycols = {"RXnumber"};
            ps = conn.prepareStatement(sqlINSERT, keycols);

            // Insert 100 rows with data
            for (int k=1; k<=100; k++) {
                int random_rx_number = 1000000000+randomNums.get(k);
                int random_drug_id = 1 + gen.nextInt(99);
                int random_quantity = gen.nextInt(121);
                c.set(Calendar.YEAR,  2020+gen.nextInt(3));
                c.set(Calendar.DAY_OF_YEAR, 1);
                c.add(Calendar.DAY_OF_YEAR, gen.nextInt(365));
                dt = new Date(c.getTimeInMillis());
                random_prescribed_date = simpleDateFormat.format(dt);
                int random_doctorId = doctorIds.get(gen.nextInt(doctorIds.size()));
                int random_patientId = patientIds.get(gen.nextInt(patientIds.size()));
                ps.setInt(1,  random_rx_number);
                ps.setInt(2,  random_drug_id);
                ps.setInt(3, random_quantity);
                ps.setString(4, random_prescribed_date);
                ps.setInt(5, random_doctorId);
                ps.setInt(6, random_patientId);
                row_count = ps.executeUpdate();
                System.out.println("row inserted "+row_count);

                // Retrieve and print the generated primary key
                rs = ps.getGeneratedKeys();
                rs.next();
                System.out.println("row inserted for prescription rx# "+random_rx_number);
            }

            // Display all rows
            System.out.println("All randomly generated prescriptions");

            String sqlSELECT = "select RXnumber, drug_drugsid, quantity, datePrescribed, doctor_doctorId, patient_patientId from doctor_prescription";
            ps = conn.prepareStatement(sqlSELECT);
            // There are no parameter markers to set
            rs = ps.executeQuery();
            while (rs.next()) {
                int RXnumber = rs.getInt("RXnumber");
                int drugId = rs.getInt("drug_drugsid");
                int quantity = rs.getInt("quantity");
                String datePrescribed = rs.getString("datePrescribed");
                int doctor_doctorId = rs.getInt("doctor_doctorId");
                int patient_patientId = rs.getInt("patient_patientId");
                System.out.printf("%10d   %10d %20s %10d %10d \n", RXnumber, quantity, datePrescribed, doctor_doctorId, patient_patientId);
            }
        } catch (SQLException e) {
            System.out.println("Error: SQLException "+e.getMessage());
        }
    }

    // Clears the database of existing data in order to avoid foreign key constraint errors
    private static void deleteExistingData() {
        try (Connection conn = DriverManager.getConnection(DBURL, USERID, PASSWORD);) {
            PreparedStatement ps;
            ResultSet rs;
            int id;
            int row_count;

            // Delete all prescription first
            ps = conn.prepareStatement("delete from doctor_prescription");
            row_count = ps.executeUpdate();
            System.out.println("rows deleted " + row_count + " doctor_prescription");

            // Delete all patient rows second
            ps = conn.prepareStatement("delete from patient");
            row_count = ps.executeUpdate();
            System.out.println("rows deleted " + row_count + " from patient");

            // Delete all doctor rows last
            ps = conn.prepareStatement("delete from doctor");
            row_count = ps.executeUpdate();
            System.out.println("rows deleted " + row_count + " from doctor");

        } catch (SQLException e) {
        System.out.println("Error: SQLException "+e.getMessage());
    }
    }
}

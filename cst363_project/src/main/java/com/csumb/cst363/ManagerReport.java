package com.csumb.cst363;

public class ManagerReport {

    /**
     * A pharmacy manager requests a report of the quantity of drugs that have been used to fill prescriptions by the pharmacy.
     * The report will contain the names of drugs used and the quantity of each drug used.
     * Input is pharmacy id and a start and end date range. ( create a new class, that queries this report, and displays it.
     */

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
    }


}

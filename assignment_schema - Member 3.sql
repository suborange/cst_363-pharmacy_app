-- MySQL Workbench Forward Engineering

SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0;
SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0;
SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION';

-- -----------------------------------------------------
-- Schema mydb
-- -----------------------------------------------------
-- -----------------------------------------------------
-- Schema drugstorechain
-- -----------------------------------------------------

-- -----------------------------------------------------
-- Schema drugstorechain
-- -----------------------------------------------------
CREATE SCHEMA IF NOT EXISTS `drugstorechain` DEFAULT CHARACTER SET utf8mb3 ;
USE `drugstorechain` ;

-- -----------------------------------------------------
-- Table `drugstorechain`.`doctor`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `drugstorechain`.`doctor` (
  `doctorId` INT NOT NULL DEFAULT floor((rand() * 100)),
  `last_name` VARCHAR(45) NOT NULL,
  `first_name` VARCHAR(45) NOT NULL,
  `specialty` VARCHAR(45) NOT NULL,
  `practice_since` VARCHAR(45) NOT NULL,
  `ssn` VARCHAR(45) NOT NULL,
  PRIMARY KEY (`doctorId`))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8mb3
COMMENT = ' ';


-- -----------------------------------------------------
-- Table `drugstorechain`.`patient`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `drugstorechain`.`patient` (
  `patientId` VARCHAR(45) NOT NULL DEFAULT uuid(),
  `last_name` VARCHAR(45) NOT NULL,
  `first_name` VARCHAR(45) NOT NULL,
  `birthdate` VARCHAR(45) NOT NULL,
  `ssn` VARCHAR(45) NOT NULL,
  `street` VARCHAR(45) NOT NULL,
  `city` VARCHAR(45) NOT NULL,
  `state` VARCHAR(45) NOT NULL,
  `zipcode` VARCHAR(45) NOT NULL,
  `doctors_doctorId` INT NOT NULL,
  PRIMARY KEY (`patientId`),
  INDEX `fk_patients_doctors1_idx` (`doctors_doctorId` ASC) VISIBLE,
  CONSTRAINT `fk_patients_doctors1`
    FOREIGN KEY (`doctors_doctorId`)
    REFERENCES `drugstorechain`.`doctor` (`doctorId`))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8mb3;


-- -----------------------------------------------------
-- Table `drugstorechain`.`drug`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `drugstorechain`.`drug` (
  `drugid` INT NOT NULL DEFAULT floor((rand() * 100)),
  `tradeName` VARCHAR(45) NULL DEFAULT NULL,
  `genericName` VARCHAR(45) NOT NULL,
  PRIMARY KEY (`drugid`))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8mb3;


-- -----------------------------------------------------
-- Table `drugstorechain`.`doctor_prescription`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `drugstorechain`.`doctor_prescription` (
  `RXnumber` INT NOT NULL DEFAULT floor((rand() * 100)),
  `drug_drugsid` INT NOT NULL,
  `quantity` INT NOT NULL,
  `date` DATE NOT NULL,
  `datePrescribed` DATE NULL DEFAULT NULL,
  `doctor_doctorId` INT NOT NULL,
  `patient_patientId` VARCHAR(45) NOT NULL,
  PRIMARY KEY (`RXnumber`, `drug_drugsid`, `doctor_doctorId`, `patient_patientId`),
  INDEX `fk_doctor_prescriptions_prescription_drugs1_idx` (`drug_drugsid` ASC) VISIBLE,
  INDEX `fk_doctor_prescriptions_doctors1_idx` (`doctor_doctorId` ASC) VISIBLE,
  INDEX `fk_doctor_prescriptions_patients1_idx` (`patient_patientId` ASC) VISIBLE,
  CONSTRAINT `fk_doctor_prescriptions_doctors1`
    FOREIGN KEY (`doctor_doctorId`)
    REFERENCES `drugstorechain`.`doctor` (`doctorId`),
  CONSTRAINT `fk_doctor_prescriptions_patients1`
    FOREIGN KEY (`patient_patientId`)
    REFERENCES `drugstorechain`.`patient` (`patientId`),
  CONSTRAINT `fk_doctor_prescriptions_prescription_drugs1`
    FOREIGN KEY (`drug_drugsid`)
    REFERENCES `drugstorechain`.`drug` (`drugid`))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8mb3;


-- -----------------------------------------------------
-- Table `drugstorechain`.`pharma_corp`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `drugstorechain`.`pharma_corp` (
  `name` VARCHAR(45) NOT NULL,
  `phoneNumber` INT NOT NULL,
  PRIMARY KEY (`name`))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8mb3;


-- -----------------------------------------------------
-- Table `drugstorechain`.`drug_has_pharma_corp`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `drugstorechain`.`drug_has_pharma_corp` (
  `drug_drugid` INT NOT NULL,
  `pharma_corp_name` VARCHAR(45) NOT NULL,
  PRIMARY KEY (`drug_drugid`, `pharma_corp_name`),
  INDEX `fk_drugs_has_pharma_corps_pharma_corps1_idx` (`pharma_corp_name` ASC) VISIBLE,
  INDEX `fk_drugs_has_pharma_corps_drugs1_idx` (`drug_drugid` ASC) VISIBLE,
  CONSTRAINT `fk_drugs_has_pharma_corps_drugs1`
    FOREIGN KEY (`drug_drugid`)
    REFERENCES `drugstorechain`.`drug` (`drugid`),
  CONSTRAINT `fk_drugs_has_pharma_corps_pharma_corps1`
    FOREIGN KEY (`pharma_corp_name`)
    REFERENCES `drugstorechain`.`pharma_corp` (`name`))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8mb3;


-- -----------------------------------------------------
-- Table `drugstorechain`.`pharmacy`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `drugstorechain`.`pharmacy` (
  `pharmacyid` INT NOT NULL DEFAULT floor((rand() * 100)),
  `name` VARCHAR(45) NOT NULL,
  `address` VARCHAR(45) NOT NULL,
  `phonenumber` INT NOT NULL,
  PRIMARY KEY (`pharmacyid`))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8mb3;


-- -----------------------------------------------------
-- Table `drugstorechain`.`pharmacy_prescription`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `drugstorechain`.`pharmacy_prescription` (
  `drugKey` INT NOT NULL DEFAULT floor((rand() * 100)),
  `price` DECIMAL(6,2) NOT NULL,
  `pharmacy_pharmacyid` INT NOT NULL,
  PRIMARY KEY (`drugKey`, `pharmacy_pharmacyid`),
  INDEX `fk_pharmacy_prescriptions_pharmacy1_idx` (`pharmacy_pharmacyid` ASC) VISIBLE,
  CONSTRAINT `fk_pharmacy_prescriptions_pharmacy1`
    FOREIGN KEY (`pharmacy_pharmacyid`)
    REFERENCES `drugstorechain`.`pharmacy` (`pharmacyid`))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8mb3;


-- -----------------------------------------------------
-- Table `drugstorechain`.`drug_has_prescription`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `drugstorechain`.`drug_has_prescription` (
  `drug_prescriptionid` INT NOT NULL,
  `pharmacy_prescription_drugKey` INT NOT NULL,
  PRIMARY KEY (`drug_prescriptionid`, `pharmacy_prescription_drugKey`),
  INDEX `fk_drugs_has_pharmacy_prescriptions_pharmacy_prescriptions1_idx` (`pharmacy_prescription_drugKey` ASC) VISIBLE,
  INDEX `fk_drugs_has_pharmacy_prescriptions_drugs1_idx` (`drug_prescriptionid` ASC) VISIBLE,
  CONSTRAINT `fk_drugs_has_pharmacy_prescriptions_drugs1`
    FOREIGN KEY (`drug_prescriptionid`)
    REFERENCES `drugstorechain`.`drug` (`drugid`),
  CONSTRAINT `fk_drugs_has_pharmacy_prescriptions_pharmacy_prescriptions1`
    FOREIGN KEY (`pharmacy_prescription_drugKey`)
    REFERENCES `drugstorechain`.`pharmacy_prescription` (`drugKey`))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8mb3;


-- -----------------------------------------------------
-- Table `drugstorechain`.`fill`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `drugstorechain`.`fill` (
  `fillId` INT NOT NULL,
  `dateFilled` DATE NULL DEFAULT NULL,
  `pharmacy_pharmacyid` INT NOT NULL,
  `pharma_corp_name` VARCHAR(45) NOT NULL,
  `doctor_prescription_RXnumber` INT NOT NULL,
  PRIMARY KEY (`fillId`, `doctor_prescription_RXnumber`),
  INDEX `fk_Fill_pharmacies1_idx` (`pharmacy_pharmacyid` ASC) VISIBLE,
  INDEX `fk_Fill_pharma_corps1_idx` (`pharma_corp_name` ASC) VISIBLE,
  INDEX `fk_Fill_doctor_prescriptions1_idx` (`doctor_prescription_RXnumber` ASC) VISIBLE,
  CONSTRAINT `fk_Fill_doctor_prescriptions1`
    FOREIGN KEY (`doctor_prescription_RXnumber`)
    REFERENCES `drugstorechain`.`doctor_prescription` (`RXnumber`),
  CONSTRAINT `fk_Fill_pharma_corps1`
    FOREIGN KEY (`pharma_corp_name`)
    REFERENCES `drugstorechain`.`pharma_corp` (`name`),
  CONSTRAINT `fk_Fill_pharmacies1`
    FOREIGN KEY (`pharmacy_pharmacyid`)
    REFERENCES `drugstorechain`.`pharmacy` (`pharmacyid`))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8mb3;


-- -----------------------------------------------------
-- Table `drugstorechain`.`supervisor`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `drugstorechain`.`supervisor` (
  `supervisorid` INT NOT NULL,
  `name` VARCHAR(45) NOT NULL,
  PRIMARY KEY (`supervisorid`))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8mb3;


-- -----------------------------------------------------
-- Table `drugstorechain`.`pharma_contract`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `drugstorechain`.`pharma_contract` (
  `pharmacy_pharmacyid` INT NOT NULL,
  `pharma_corp_name` VARCHAR(45) NOT NULL,
  `supervisor_supervisorid` INT NOT NULL,
  `startdate` DATE NOT NULL,
  `enddate` DATE NOT NULL,
  `text` VARCHAR(1000) NULL DEFAULT NULL,
  PRIMARY KEY (`pharmacy_pharmacyid`, `pharma_corp_name`, `supervisor_supervisorid`),
  INDEX `fk_pharmacy_has_pharma_corps_pharma_corps1_idx` (`pharma_corp_name` ASC) VISIBLE,
  INDEX `fk_pharmacy_has_pharma_corps_pharmacy1_idx` (`pharmacy_pharmacyid` ASC) VISIBLE,
  INDEX `fk_pharmacy_pharma_corps_contracts_supervisor1_idx` (`supervisor_supervisorid` ASC) VISIBLE,
  CONSTRAINT `fk_pharmacy_has_pharma_corps_pharma_corps1`
    FOREIGN KEY (`pharma_corp_name`)
    REFERENCES `drugstorechain`.`pharma_corp` (`name`),
  CONSTRAINT `fk_pharmacy_has_pharma_corps_pharmacy1`
    FOREIGN KEY (`pharmacy_pharmacyid`)
    REFERENCES `drugstorechain`.`pharmacy` (`pharmacyid`),
  CONSTRAINT `fk_pharmacy_pharma_corps_contracts_supervisor1`
    FOREIGN KEY (`supervisor_supervisorid`)
    REFERENCES `drugstorechain`.`supervisor` (`supervisorid`))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8mb3;


SET SQL_MODE=@OLD_SQL_MODE;
SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS;
SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS;

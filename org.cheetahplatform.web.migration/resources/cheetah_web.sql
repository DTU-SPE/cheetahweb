-- MySQL dump 10.13  Distrib 5.6.24, for Win64 (x86_64)
--
-- Host: localhost    Database: cheetah_web
-- ------------------------------------------------------
-- Server version	5.6.26-log

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `applied_edge_mapping`
--

DROP TABLE IF EXISTS `applied_edge_mapping`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `applied_edge_mapping` (
  `process_instance` int(10) unsigned NOT NULL,
  `edge` int(10) unsigned NOT NULL,
  `edge_mapping` int(10) unsigned NOT NULL,
  PRIMARY KEY (`process_instance`,`edge`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `applied_edge_mapping`
--

LOCK TABLES `applied_edge_mapping` WRITE;
/*!40000 ALTER TABLE `applied_edge_mapping` DISABLE KEYS */;
/*!40000 ALTER TABLE `applied_edge_mapping` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `applied_node_mapping`
--

DROP TABLE IF EXISTS `applied_node_mapping`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `applied_node_mapping` (
  `audittrail_entry` bigint(20) unsigned NOT NULL,
  `node_mapping` bigint(20) unsigned NOT NULL,
  PRIMARY KEY (`audittrail_entry`,`node_mapping`) USING BTREE,
  CONSTRAINT `FK_audittrail_entry` FOREIGN KEY (`audittrail_entry`) REFERENCES `audittrail_entry` (`database_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `applied_node_mapping`
--

LOCK TABLES `applied_node_mapping` WRITE;
/*!40000 ALTER TABLE `applied_node_mapping` DISABLE KEYS */;
/*!40000 ALTER TABLE `applied_node_mapping` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `audio`
--

DROP TABLE IF EXISTS `audio`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `audio` (
  `pk_audio` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `process_instance` int(10) unsigned NOT NULL,
  `start_time` time NOT NULL,
  `mp3` longblob NOT NULL,
  PRIMARY KEY (`pk_audio`)
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `audio`
--

LOCK TABLES `audio` WRITE;
/*!40000 ALTER TABLE `audio` DISABLE KEYS */;
/*!40000 ALTER TABLE `audio` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `audittrail_entry`
--

DROP TABLE IF EXISTS `audittrail_entry`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `audittrail_entry` (
  `database_id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `process_instance` bigint(20) unsigned NOT NULL,
  `timestamp` varchar(255) DEFAULT NULL,
  `type` varchar(255) DEFAULT NULL,
  `workflow_element` text,
  `originator` text,
  `data` longtext,
  `synchronized_from` bigint(20) unsigned DEFAULT NULL,
  PRIMARY KEY (`database_id`),
  KEY `FK_process_instance` (`process_instance`),
  CONSTRAINT `FK_process_instance` FOREIGN KEY (`process_instance`) REFERENCES `process_instance` (`database_id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=3498201 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `audittrail_entry`
--

LOCK TABLES `audittrail_entry` WRITE;
/*!40000 ALTER TABLE `audittrail_entry` DISABLE KEYS */;
/*!40000 ALTER TABLE `audittrail_entry` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `audittrailentry_for_note`
--

DROP TABLE IF EXISTS `audittrailentry_for_note`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `audittrailentry_for_note` (
  `ppmnote` bigint(20) unsigned NOT NULL,
  `entryid` bigint(20) unsigned NOT NULL,
  KEY `FK_audittrailentry` (`entryid`),
  KEY `FK_ppmnote` (`ppmnote`),
  CONSTRAINT `FK_audittrailentry` FOREIGN KEY (`entryid`) REFERENCES `audittrail_entry` (`database_id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `FK_ppmnote` FOREIGN KEY (`ppmnote`) REFERENCES `ppmnote` (`id`) ON DELETE CASCADE ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `audittrailentry_for_note`
--

LOCK TABLES `audittrailentry_for_note` WRITE;
/*!40000 ALTER TABLE `audittrailentry_for_note` DISABLE KEYS */;
/*!40000 ALTER TABLE `audittrailentry_for_note` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `database_configuration`
--

DROP TABLE IF EXISTS `database_configuration`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `database_configuration` (
  `pk_database_configuration` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `fk_user` bigint(20) unsigned DEFAULT NULL,
  `host` varchar(255) DEFAULT NULL,
  `port` bigint(20) unsigned DEFAULT NULL,
  `schema` varchar(255) DEFAULT NULL,
  `user` varchar(255) DEFAULT NULL,
  `password` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`pk_database_configuration`),
  KEY `fk_user` (`fk_user`),
  CONSTRAINT `fk_user` FOREIGN KEY (`fk_user`) REFERENCES `user_table` (`pk_user`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `database_configuration`
--

LOCK TABLES `database_configuration` WRITE;
/*!40000 ALTER TABLE `database_configuration` DISABLE KEYS */;
/*!40000 ALTER TABLE `database_configuration` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `databasechangelog`
--

DROP TABLE IF EXISTS `DATABASECHANGELOG`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `DATABASECHANGELOG` (
  `ID` varchar(255) NOT NULL,
  `AUTHOR` varchar(255) NOT NULL,
  `FILENAME` varchar(255) NOT NULL,
  `DATEEXECUTED` datetime NOT NULL,
  `ORDEREXECUTED` int(11) NOT NULL,
  `EXECTYPE` varchar(10) NOT NULL,
  `MD5SUM` varchar(35) DEFAULT NULL,
  `DESCRIPTION` varchar(255) DEFAULT NULL,
  `COMMENTS` varchar(255) DEFAULT NULL,
  `TAG` varchar(255) DEFAULT NULL,
  `LIQUIBASE` varchar(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `databasechangelog`
--

LOCK TABLES `DATABASECHANGELOG` WRITE;
/*!40000 ALTER TABLE `DATABASECHANGELOG` DISABLE KEYS */;
INSERT INTO `DATABASECHANGELOG` VALUES ('CEP2.0-1','Jakob','changeLog.xml','2016-06-15 13:38:21',1,'EXECUTED','7:231cc1003eb62bb2b5a66830831b7141','createTable, addNotNullConstraint (x4), addPrimaryKey, addAutoIncrement, createIndex, createTable, addNotNullConstraint (x4), addPrimaryKey, addAutoIncrement, createIndex, addForeignKeyConstraint (x2)','Create tables for user management. Email needs to be duplicated for authentication.',NULL,'3.0.7'),('CEP2.0-2','Jakob','changeLog.xml','2016-06-15 13:38:23',2,'EXECUTED','7:336411c975a820e6085832315476dcd4','createTable, addPrimaryKey, addAutoIncrement, addNotNullConstraint (x5), addForeignKeyConstraint','Create Notification Table',NULL,'3.0.7'),('CEP2.0-3','Jakob','changeLog.xml','2016-06-15 13:38:23',3,'EXECUTED','7:c0482ff364cba435cfb92a854ba86468','customChange','Create Users and Roles',NULL,'3.0.7'),('CEP2.0-4','Jakob','changeLog.xml','2016-06-15 13:38:24',4,'EXECUTED','7:0997bd951ff2d5c9299dc79b73503a72','createTable, addPrimaryKey, addAutoIncrement, addNotNullConstraint (x4), addForeignKeyConstraint','Create User Files Table',NULL,'3.0.7'),('CEP2.0-5','Jakob','changeLog.xml','2016-06-15 13:38:25',5,'EXECUTED','7:f39037dd608e3aaf9ce9b126b58978a4','createTable, addNotNullConstraint (x3), addPrimaryKey, addAutoIncrement, addForeignKeyConstraint','Create Study Table',NULL,'3.0.7'),('CEP2.0-6','Jakob','changeLog.xml','2016-06-15 13:38:25',6,'EXECUTED','7:ec0ceb94d38aa69eee06392c009669de','createTable, addPrimaryKey, addAutoIncrement, addNotNullConstraint, addForeignKeyConstraint','Create Subject Table',NULL,'3.0.7'),('CEP2.0-7','Jakob','changeLog.xml','2016-06-15 13:38:26',7,'EXECUTED','7:74721eccbc5f9b8ba775c0b0a588b058','addColumn, addForeignKeyConstraint (x2)','Link user data to process instances and subject',NULL,'3.0.7'),('CEP2.0-8','Jakob','changeLog.xml','2016-06-15 13:38:27',8,'EXECUTED','7:c435caffd9536f1e527f99d2d468800d','createTable, addNotNullConstraint (x4), addPrimaryKey, addAutoIncrement, addForeignKeyConstraint','Create Table for eyetracking movies',NULL,'3.0.7'),('preCEP2.0-9','Stefan','changeLog.xml','2016-06-15 13:38:28',9,'EXECUTED','7:bfe278b75c35574741e1d54666a896f6','sql (x6)','Change storage engine to InnoDB (old MySQL databases will have MyISAM as default engine)',NULL,'3.0.7'),('CEP2.0-9','Jakob','changeLog.xml','2016-06-15 13:38:28',10,'EXECUTED','7:546df5b6f2e336c004251a5819a6cda3','addColumn, addForeignKeyConstraint','Add Reference from Process Instance to Subject',NULL,'3.0.7'),('CEP2.0-10','Jakob','changeLog.xml','2016-06-15 13:38:28',11,'EXECUTED','7:be1f1d6bee85709db62bd3ced0562681','modifyDataType','Change type of data column in audittrail entries to allow for very long lists',NULL,'3.0.7'),('CEP2.0-11','Jakob','changeLog.xml','2016-06-15 13:38:29',12,'EXECUTED','7:7589bc9436254c33b8b0f5ca2aa9cc80','dropForeignKeyConstraint, dropColumn, createTable, addForeignKeyConstraint (x2)','Allow sharing of studies over multiple user',NULL,'3.0.7'),('CEP2.0-12','Jakob','changeLog.xml','2016-06-15 13:38:29',13,'EXECUTED','7:a67431d4d92396bc55d31f1b58b4bea2','addColumn (x2)','Add type and notation column to process',NULL,'3.0.7'),('CEP2.0-14','Jakob','changeLog.xml','2016-06-15 13:38:29',14,'EXECUTED','7:e4b44c8de9be5ead2c785ff528eebe7a','addColumn','Add timestamp to eyetracking video files',NULL,'3.0.7'),('CEP2.0-15','Jakob','changeLog.xml','2016-06-15 13:38:29',15,'EXECUTED','7:352f90f1587b10dc9de4fee90fb85ec4','addColumn','Add comment column to subject table',NULL,'3.0.7'),('CEP2.0-16','Stefan','changeLog.xml','2016-06-15 13:38:30',16,'EXECUTED','7:57996f8202cb879abc6a60d1e8984587','createTable, addPrimaryKey, addAutoIncrement, addForeignKeyConstraint','Add table for database configuration',NULL,'3.0.7'),('CEP2.0-17','Stefan','changeLog.xml','2016-06-15 13:38:31',17,'EXECUTED','7:ea1d5adb672771988cb0d197d71a2156','addColumn (x5)','Add columns for synchronizing data (to keep track of where data was synchronized from)',NULL,'3.0.7'),('CEP2.0-18','Jakob','changeLog.xml','2016-06-15 13:38:31',18,'EXECUTED','7:73d613d91d0ca416df7f45f07e5cd375','createTable, addForeignKeyConstraint','Add table for tagging files',NULL,'3.0.7'),('CEP2.0-19','Jakob','changeLog.xml','2016-06-15 13:38:31',19,'EXECUTED','7:ee1a5e3340f25a516841e679c7f163f6','addAutoIncrement','Forgot the auto increment for user_data_tags primary key',NULL,'3.0.7'),('CEP2.0-20','Stefan','changeLog.xml','2016-06-15 13:38:31',20,'EXECUTED','7:1f45a68029d00d4aaefc742d6376a2b9','dropForeignKeyConstraint, addForeignKeyConstraint','Add delete cascade to user_data_tags',NULL,'3.0.7'),('CEP2.0-21','Stefan','changeLog.xml','2016-06-15 13:38:32',21,'EXECUTED','7:69fd04915d8d82e32197d916023f2038','dropNotNullConstraint, addColumn, addForeignKeyConstraint','Remove non-null constraint on eyetracking_movie.process_instance to allow for movies that are not related to process instances. Also add fk_subject to connect movies to the respective subject.',NULL,'3.0.7'),('CEP2.0-22','Jakob','changeLog.xml','2016-06-15 13:38:32',22,'EXECUTED','7:d0ca76a092f9d2780a240ff9e675e2cf','createTable, insert','Add settings table for #449',NULL,'3.0.7'),('CEP2.0-23','Jakob','changeLog.xml','2016-06-15 13:38:32',23,'EXECUTED','7:7cf553d9417f545242a81d6d5a99d904','addColumn, addForeignKeyConstraint','Add link of movie file to user file in order to implement #433',NULL,'3.0.7'),('CEP2.0-24','Stefan','changeLog.xml','2016-06-17 13:09:32',24,'EXECUTED','7:d9da41d5df89355f34700df48769476e','addColumn, addForeignKeyConstraint','Add foreign key to track derived files, #466',NULL,'3.0.7');
/*!40000 ALTER TABLE `DATABASECHANGELOG` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `databasechangeloglock`
--

DROP TABLE IF EXISTS `DATABASECHANGELOGLOCK`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `DATABASECHANGELOGLOCK` (
  `ID` int(11) NOT NULL,
  `LOCKED` bit(1) NOT NULL,
  `LOCKGRANTED` datetime DEFAULT NULL,
  `LOCKEDBY` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `databasechangeloglock`
--

LOCK TABLES `DATABASECHANGELOGLOCK` WRITE;
/*!40000 ALTER TABLE `DATABASECHANGELOGLOCK` DISABLE KEYS */;
/*!40000 ALTER TABLE `DATABASECHANGELOGLOCK` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `eyetracking_movie`
--

DROP TABLE IF EXISTS `eyetracking_movie`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `eyetracking_movie` (
  `pk_eyetracking_movie` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `process_instance` bigint(20) unsigned DEFAULT NULL,
  `movie_path` varchar(255) NOT NULL,
  `movie_type` varchar(255) NOT NULL,
  `start_timestamp` bigint(20) unsigned NOT NULL,
  `fk_subject` bigint(20) unsigned DEFAULT NULL,
  `fk_user_file` bigint(20) unsigned DEFAULT NULL,
  PRIMARY KEY (`pk_eyetracking_movie`),
  KEY `fk_eyetracking_movie_to_process_instance` (`process_instance`),
  KEY `fk_subject_to_subject` (`fk_subject`),
  KEY `fk_movie_to_user_data` (`fk_user_file`),
  CONSTRAINT `fk_eyetracking_movie_to_process_instance` FOREIGN KEY (`process_instance`) REFERENCES `process_instance` (`database_id`),
  CONSTRAINT `fk_movie_to_user_data` FOREIGN KEY (`fk_user_file`) REFERENCES `user_data` (`pk_user_data`),
  CONSTRAINT `fk_subject_to_subject` FOREIGN KEY (`fk_subject`) REFERENCES `subject` (`pk_subject`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `eyetracking_movie`
--

LOCK TABLES `eyetracking_movie` WRITE;
/*!40000 ALTER TABLE `eyetracking_movie` DISABLE KEYS */;
/*!40000 ALTER TABLE `eyetracking_movie` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `id`
--

DROP TABLE IF EXISTS `id`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `id` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=4341 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `id`
--

LOCK TABLES `id` WRITE;
/*!40000 ALTER TABLE `id` DISABLE KEYS */;
/*!40000 ALTER TABLE `id` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `log`
--

DROP TABLE IF EXISTS `log`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `log` (
  `message` text,
  `trace` mediumtext,
  `attributes` text,
  `host` text,
  `timestamp` datetime NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `log`
--

LOCK TABLES `log` WRITE;
/*!40000 ALTER TABLE `log` DISABLE KEYS */;
/*!40000 ALTER TABLE `log` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `node_mapping`
--

DROP TABLE IF EXISTS `node_mapping`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `node_mapping` (
  `database_id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `process` varchar(255) NOT NULL,
  `description` varchar(255) NOT NULL,
  `possible_activity_names` text NOT NULL,
  `color` varchar(255) NOT NULL,
  `default_model_element` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`database_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1818 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `node_mapping`
--

LOCK TABLES `node_mapping` WRITE;
/*!40000 ALTER TABLE `node_mapping` DISABLE KEYS */;
/*!40000 ALTER TABLE `node_mapping` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `notecategory`
--

DROP TABLE IF EXISTS `notecategory`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `notecategory` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `name` varchar(255) NOT NULL,
  `parent` bigint(20) unsigned DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_notecategory_parent` (`parent`),
  CONSTRAINT `FK_notecategory_parent` FOREIGN KEY (`parent`) REFERENCES `notecategory` (`id`) ON DELETE CASCADE ON UPDATE NO ACTION
) ENGINE=InnoDB AUTO_INCREMENT=69 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `notecategory`
--

LOCK TABLES `notecategory` WRITE;
/*!40000 ALTER TABLE `notecategory` DISABLE KEYS */;
/*!40000 ALTER TABLE `notecategory` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `notification`
--

DROP TABLE IF EXISTS `notification`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `notification` (
  `pk_notification` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `fk_user` bigint(20) unsigned NOT NULL,
  `message` varchar(255) NOT NULL,
  `type` varchar(255) NOT NULL,
  `url` varchar(255) DEFAULT NULL,
  `is_read` bit(1) NOT NULL,
  `timestamp` datetime NOT NULL,
  PRIMARY KEY (`pk_notification`),
  KEY `fk_notification_to_user` (`fk_user`),
  CONSTRAINT `fk_notification_to_user` FOREIGN KEY (`fk_user`) REFERENCES `user_table` (`pk_user`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `notification`
--

LOCK TABLES `notification` WRITE;
/*!40000 ALTER TABLE `notification` DISABLE KEYS */;
/*!40000 ALTER TABLE `notification` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `ppmnote`
--

DROP TABLE IF EXISTS `ppmnote`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `ppmnote` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `text` text NOT NULL,
  `startTime` varchar(255) NOT NULL,
  `endTime` varchar(255) DEFAULT NULL,
  `category` bigint(20) unsigned DEFAULT NULL,
  `originator` varchar(255) DEFAULT NULL,
  `parent` bigint(20) unsigned DEFAULT NULL,
  `processInstance` bigint(20) unsigned NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_ppmnote_parent` (`parent`),
  KEY `FK_category` (`category`),
  CONSTRAINT `FK_category` FOREIGN KEY (`category`) REFERENCES `notecategory` (`id`),
  CONSTRAINT `FK_ppmnote_parent` FOREIGN KEY (`parent`) REFERENCES `ppmnote` (`id`) ON DELETE CASCADE ON UPDATE NO ACTION
) ENGINE=InnoDB AUTO_INCREMENT=256 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `ppmnote`
--

LOCK TABLES `ppmnote` WRITE;
/*!40000 ALTER TABLE `ppmnote` DISABLE KEYS */;
/*!40000 ALTER TABLE `ppmnote` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `process`
--

DROP TABLE IF EXISTS `process`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `process` (
  `database_id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `id` text NOT NULL,
  `data` text,
  `type` varchar(255) DEFAULT NULL,
  `notation` varchar(255) DEFAULT NULL,
  `synchronized_from` bigint(20) unsigned DEFAULT NULL,
  PRIMARY KEY (`database_id`)
) ENGINE=InnoDB AUTO_INCREMENT=61 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `process`
--

LOCK TABLES `process` WRITE;
/*!40000 ALTER TABLE `process` DISABLE KEYS */;
/*!40000 ALTER TABLE `process` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `process_instance`
--

DROP TABLE IF EXISTS `process_instance`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `process_instance` (
  `database_id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `process` bigint(20) unsigned NOT NULL,
  `id` text NOT NULL,
  `data` text,
  `fk_subject` bigint(20) unsigned DEFAULT NULL,
  `synchronized_from` bigint(20) unsigned DEFAULT NULL,
  PRIMARY KEY (`database_id`),
  KEY `fk_process_instance_to_subject` (`fk_subject`),
  CONSTRAINT `fk_process_instance_to_subject` FOREIGN KEY (`fk_subject`) REFERENCES `subject` (`pk_subject`)
) ENGINE=InnoDB AUTO_INCREMENT=2948 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `process_instance`
--

LOCK TABLES `process_instance` WRITE;
/*!40000 ALTER TABLE `process_instance` DISABLE KEYS */;
/*!40000 ALTER TABLE `process_instance` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `settings`
--

DROP TABLE IF EXISTS `settings`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `settings` (
  `pk_settings` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `key_column` varchar(255) NOT NULL,
  `value` varchar(255) NOT NULL,
  PRIMARY KEY (`pk_settings`),
  UNIQUE KEY `key_column` (`key_column`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `settings`
--

LOCK TABLES `settings` WRITE;
/*!40000 ALTER TABLE `settings` DISABLE KEYS */;
INSERT INTO `settings` VALUES (1,'cheetah.web.show-ppm','true');
/*!40000 ALTER TABLE `settings` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `studies_to_user`
--

DROP TABLE IF EXISTS `studies_to_user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `studies_to_user` (
  `fk_user` bigint(20) unsigned DEFAULT NULL,
  `fk_study` bigint(20) unsigned DEFAULT NULL,
  KEY `fk_studies_mapping_to_user` (`fk_user`),
  KEY `fk_studies_mapping_to_study` (`fk_study`),
  CONSTRAINT `fk_studies_mapping_to_study` FOREIGN KEY (`fk_study`) REFERENCES `study` (`pk_study`),
  CONSTRAINT `fk_studies_mapping_to_user` FOREIGN KEY (`fk_user`) REFERENCES `user_table` (`pk_user`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `studies_to_user`
--

LOCK TABLES `studies_to_user` WRITE;
/*!40000 ALTER TABLE `studies_to_user` DISABLE KEYS */;
/*!40000 ALTER TABLE `studies_to_user` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `study`
--

DROP TABLE IF EXISTS `study`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `study` (
  `pk_study` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `name` varchar(255) NOT NULL,
  `comment` longtext,
  `synchronized_from` bigint(20) unsigned DEFAULT NULL,
  PRIMARY KEY (`pk_study`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `study`
--

LOCK TABLES `study` WRITE;
/*!40000 ALTER TABLE `study` DISABLE KEYS */;
/*!40000 ALTER TABLE `study` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `subject`
--

DROP TABLE IF EXISTS `subject`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `subject` (
  `pk_subject` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `email` varchar(255) DEFAULT NULL,
  `subject_id` varchar(255) NOT NULL,
  `fk_study` bigint(20) unsigned DEFAULT NULL,
  `comment` longtext,
  `synchronized_from` bigint(20) unsigned DEFAULT NULL,
  PRIMARY KEY (`pk_subject`),
  KEY `fk_subject_to_study` (`fk_study`),
  CONSTRAINT `fk_subject_to_study` FOREIGN KEY (`fk_study`) REFERENCES `study` (`pk_study`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `subject`
--

LOCK TABLES `subject` WRITE;
/*!40000 ALTER TABLE `subject` DISABLE KEYS */;
/*!40000 ALTER TABLE `subject` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `transcript`
--

DROP TABLE IF EXISTS `transcript`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `transcript` (
  `database_id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `process_instance` bigint(20) unsigned NOT NULL,
  `timestamp` varchar(255) NOT NULL,
  `originator` varchar(255) NOT NULL,
  `text` text NOT NULL,
  PRIMARY KEY (`database_id`) USING BTREE,
  KEY `FK_instance` (`process_instance`),
  CONSTRAINT `FK_instance` FOREIGN KEY (`process_instance`) REFERENCES `process_instance` (`database_id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `transcript`
--

LOCK TABLES `transcript` WRITE;
/*!40000 ALTER TABLE `transcript` DISABLE KEYS */;
/*!40000 ALTER TABLE `transcript` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user_data`
--

DROP TABLE IF EXISTS `user_data`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `user_data` (
  `pk_user_data` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `fk_user` bigint(20) unsigned NOT NULL,
  `filename` varchar(255) NOT NULL,
  `type` varchar(255) NOT NULL,
  `path` varchar(255) NOT NULL,
  `hidden` bit(1) DEFAULT b'0',
  `comment` longtext,
  `fk_process_instance` bigint(20) unsigned DEFAULT NULL,
  `fk_subject` bigint(20) unsigned DEFAULT NULL,
  `fk_derived_from` bigint(20) unsigned DEFAULT NULL,
  PRIMARY KEY (`pk_user_data`),
  KEY `fk_user_data_to_user` (`fk_user`),
  KEY `fk_user_data_to_process_instance` (`fk_process_instance`),
  KEY `fk_user_data_to_subject` (`fk_subject`),
  KEY `fk_derived_from_to_user_data` (`fk_derived_from`),
  CONSTRAINT `fk_derived_from_to_user_data` FOREIGN KEY (`fk_derived_from`) REFERENCES `user_data` (`pk_user_data`),
  CONSTRAINT `fk_user_data_to_process_instance` FOREIGN KEY (`fk_process_instance`) REFERENCES `process_instance` (`database_id`),
  CONSTRAINT `fk_user_data_to_subject` FOREIGN KEY (`fk_subject`) REFERENCES `subject` (`pk_subject`),
  CONSTRAINT `fk_user_data_to_user` FOREIGN KEY (`fk_user`) REFERENCES `user_table` (`pk_user`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user_data`
--

LOCK TABLES `user_data` WRITE;
/*!40000 ALTER TABLE `user_data` DISABLE KEYS */;
/*!40000 ALTER TABLE `user_data` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user_data_tags`
--

DROP TABLE IF EXISTS `user_data_tags`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `user_data_tags` (
  `pk_user_data_tags` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `fk_user_data` bigint(20) unsigned NOT NULL,
  `tag` varchar(255) NOT NULL,
  PRIMARY KEY (`pk_user_data_tags`),
  KEY `fk_user_data_tags_to_user_data` (`fk_user_data`),
  CONSTRAINT `fk_user_data_tags_to_user_data` FOREIGN KEY (`fk_user_data`) REFERENCES `user_data` (`pk_user_data`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user_data_tags`
--

LOCK TABLES `user_data_tags` WRITE;
/*!40000 ALTER TABLE `user_data_tags` DISABLE KEYS */;
/*!40000 ALTER TABLE `user_data_tags` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user_roles`
--

DROP TABLE IF EXISTS `user_roles`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `user_roles` (
  `pk_role` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `role` varchar(255) NOT NULL,
  `email` varchar(255) NOT NULL,
  `fk_user` bigint(20) unsigned NOT NULL,
  PRIMARY KEY (`pk_role`),
  UNIQUE KEY `user_roles_email_idx` (`email`),
  KEY `fk_roles_to_user` (`fk_user`),
  CONSTRAINT `fk_email_to_email` FOREIGN KEY (`email`) REFERENCES `user_table` (`email`),
  CONSTRAINT `fk_roles_to_user` FOREIGN KEY (`fk_user`) REFERENCES `user_table` (`pk_user`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user_roles`
--

LOCK TABLES `user_roles` WRITE;
/*!40000 ALTER TABLE `user_roles` DISABLE KEYS */;
INSERT INTO `user_roles` VALUES (1,'administrator','admin@cheetahplatform.org',1);
/*!40000 ALTER TABLE `user_roles` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user_table`
--

DROP TABLE IF EXISTS `user_table`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `user_table` (
  `pk_user` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `firstname` varchar(255) NOT NULL,
  `lastname` varchar(255) NOT NULL,
  `email` varchar(255) NOT NULL,
  `password` varchar(255) NOT NULL,
  PRIMARY KEY (`pk_user`),
  UNIQUE KEY `user_table_email_idx` (`email`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user_table`
--

LOCK TABLES `user_table` WRITE;
/*!40000 ALTER TABLE `user_table` DISABLE KEYS */;
INSERT INTO `user_table` VALUES (1,'Admin','','admin@cheetahplatform.org','43d52a2882cf8947e927f9a3cd79f11f');
/*!40000 ALTER TABLE `user_table` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `xml_log`
--

DROP TABLE IF EXISTS `xml_log`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `xml_log` (
  `database_id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `timestamp` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `log` longblob NOT NULL,
  `timestamp_converted` timestamp NULL DEFAULT NULL,
  PRIMARY KEY (`database_id`)
) ENGINE=InnoDB AUTO_INCREMENT=562 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `xml_log`
--

LOCK TABLES `xml_log` WRITE;
/*!40000 ALTER TABLE `xml_log` DISABLE KEYS */;
/*!40000 ALTER TABLE `xml_log` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2016-06-17 13:09:53

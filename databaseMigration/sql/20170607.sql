-- MySQL dump 10.13  Distrib 5.7.18, for Linux (x86_64)
--
-- Host: 127.0.0.1    Database: cheetah_web
-- ------------------------------------------------------
-- Server version	5.7.18-0ubuntu0.16.04.1

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
-- Table structure for table `data_processing`
--

DROP TABLE IF EXISTS `data_processing`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `data_processing` (
  `pk_data_processing` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `fk_study` bigint(20) unsigned NOT NULL,
  `name` varchar(255) NOT NULL,
  `comment` varchar(1024) DEFAULT NULL,
  `timestamp_column` varchar(255) NOT NULL,
  `left_pupil_column` varchar(255) NOT NULL,
  `right_pupil_column` varchar(255) NOT NULL,
  `decimal_separator` varchar(10) NOT NULL DEFAULT '.',
  `trial_computation_configuration` mediumtext,
  PRIMARY KEY (`pk_data_processing`),
  KEY `fk_study_from_data_processing` (`fk_study`),
  CONSTRAINT `FK_data_processing_study` FOREIGN KEY (`fk_study`) REFERENCES `study` (`pk_study`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `data_processing_step`
--

DROP TABLE IF EXISTS `data_processing_step`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `data_processing_step` (
  `pk_data_processing_step` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `fk_data_processing` bigint(20) unsigned NOT NULL,
  `type` varchar(255) NOT NULL,
  `version` int(11) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `configuration` mediumtext,
  PRIMARY KEY (`pk_data_processing_step`),
  KEY `fk_data_processing_from_data_processing_step` (`fk_data_processing`),
  CONSTRAINT `FK_data_processing_step_data_processing` FOREIGN KEY (`fk_data_processing`) REFERENCES `data_processing` (`pk_data_processing`)
) ENGINE=InnoDB AUTO_INCREMENT=48 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `event`
--

DROP TABLE IF EXISTS `event`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `event` (
  `pk_event` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `time_start` bigint(20) unsigned DEFAULT NULL,
  `time_end` bigint(20) unsigned NOT NULL,
  `type` varchar(256) NOT NULL,
  `name` varchar(256) DEFAULT NULL,
  `attributes` json DEFAULT NULL,
  `fk_subject` bigint(20) unsigned DEFAULT NULL,
  PRIMARY KEY (`pk_event`),
  KEY `FK_event_subject` (`fk_subject`),
  CONSTRAINT `FK_event_subject` FOREIGN KEY (`fk_subject`) REFERENCES `subject` (`pk_subject`)
) ENGINE=InnoDB AUTO_INCREMENT=484145 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

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
) ENGINE=InnoDB AUTO_INCREMENT=3924 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `session_video`
--

DROP TABLE IF EXISTS `session_video`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `session_video` (
  `pk_session_video` bigint(20) unsigned NOT NULL,
  `fk_event` bigint(20) unsigned DEFAULT NULL,
  `movie_path` varchar(255) NOT NULL,
  `movie_type` varchar(255) NOT NULL,
  `start_timestamp` bigint(20) unsigned NOT NULL,
  `fk_subject` bigint(20) unsigned DEFAULT NULL,
  `fk_user_file` bigint(20) unsigned DEFAULT NULL,
  PRIMARY KEY (`pk_session_video`),
  KEY `fk_subject_to_subject` (`fk_subject`),
  KEY `fk_movie_to_user_data` (`fk_user_file`),
  KEY `fk_event_to_event_idx` (`fk_event`),
  CONSTRAINT `fk_event_to_event` FOREIGN KEY (`fk_event`) REFERENCES `event` (`pk_event`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `fk_movie_to_user_data` FOREIGN KEY (`fk_user_file`) REFERENCES `user_data` (`pk_user_data`),
  CONSTRAINT `fk_subject_to_subject` FOREIGN KEY (`fk_subject`) REFERENCES `subject` (`pk_subject`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

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
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

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
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

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
) ENGINE=InnoDB AUTO_INCREMENT=646 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `time_phase`
--

DROP TABLE IF EXISTS `time_phase`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `time_phase` (
  `pk_time_phase` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `fk_subject` bigint(20) unsigned NOT NULL DEFAULT '0',
  `fk_parent_time_phase` bigint(20) unsigned DEFAULT '0',
  `type` varchar(1024) NOT NULL,
  `name` varchar(1024) DEFAULT NULL,
  `time_start` bigint(20) unsigned NOT NULL,
  `time_end` bigint(20) unsigned NOT NULL,
  `attributes` json NOT NULL,
  PRIMARY KEY (`pk_time_phase`),
  KEY `FK_time_phase_subject` (`fk_subject`),
  CONSTRAINT `FK_time_phase_subject` FOREIGN KEY (`fk_subject`) REFERENCES `subject` (`pk_subject`)
) ENGINE=InnoDB AUTO_INCREMENT=3933 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

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
  `fk_subject` bigint(20) unsigned DEFAULT NULL,
  `fk_derived_from` bigint(20) unsigned DEFAULT NULL,
  `fk_session_video` bigint(20) unsigned DEFAULT NULL,
  PRIMARY KEY (`pk_user_data`),
  KEY `fk_user_data_to_user` (`fk_user`),
  KEY `fk_user_data_to_subject` (`fk_subject`),
  KEY `fk_derived_from_to_user_data` (`fk_derived_from`),
  KEY `fk_session_video_to_session_video_idx` (`fk_session_video`),
  CONSTRAINT `fk_derived_from_to_user_data` FOREIGN KEY (`fk_derived_from`) REFERENCES `user_data` (`pk_user_data`),
  CONSTRAINT `fk_session_video_to_session_video` FOREIGN KEY (`fk_session_video`) REFERENCES `session_video` (`pk_session_video`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `fk_user_data_to_subject` FOREIGN KEY (`fk_subject`) REFERENCES `subject` (`pk_subject`),
  CONSTRAINT `fk_user_data_to_user` FOREIGN KEY (`fk_user`) REFERENCES `user_table` (`pk_user`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

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
  CONSTRAINT `FK_user_data_tags_user_data` FOREIGN KEY (`fk_user_data`) REFERENCES `user_data` (`pk_user_data`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

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
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

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
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2017-06-07 14:09:28

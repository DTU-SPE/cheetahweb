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
-- Table structure for table `data_processing_steps`
--

DROP TABLE IF EXISTS `data_processing_steps`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `data_processing_steps` (
  `pk_data_processing_step` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `fk_data_processing` bigint(20) unsigned NOT NULL,
  `type` varchar(255) NOT NULL,
  `version` int(11) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `configuration` json DEFAULT NULL,
  PRIMARY KEY (`pk_data_processing_step`),
  KEY `fk_data_processing_from_data_processing_step` (`fk_data_processing`),
  CONSTRAINT `FK_data_processing_step_data_processing` FOREIGN KEY (`fk_data_processing`) REFERENCES `data_processings` (`pk_data_processing`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `data_processings`
--

DROP TABLE IF EXISTS `data_processings`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `data_processings` (
  `pk_data_processing` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `fk_study` bigint(20) unsigned NOT NULL,
  `name` varchar(255) NOT NULL,
  `comment` varchar(1024) DEFAULT NULL,
  `timestamp_column` varchar(255) NOT NULL,
  `left_pupil_column` varchar(255) NOT NULL,
  `right_pupil_column` varchar(255) NOT NULL,
  `decimal_separator` varchar(10) NOT NULL DEFAULT '.',
  `trial_computation_configuration` json DEFAULT NULL,
  PRIMARY KEY (`pk_data_processing`),
  KEY `fk_study_from_data_processing` (`fk_study`),
  CONSTRAINT `FK_data_processing_study` FOREIGN KEY (`fk_study`) REFERENCES `studies` (`pk_study`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `events`
--

DROP TABLE IF EXISTS `events`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `events` (
  `pk_event` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `fk_subject` bigint(20) unsigned DEFAULT NULL,
  `time_start` bigint(20) unsigned DEFAULT NULL,
  `time_end` bigint(20) unsigned NOT NULL,
  `type` varchar(256) NOT NULL,
  `name` varchar(256) DEFAULT NULL,
  `attributes` json DEFAULT NULL,
  PRIMARY KEY (`pk_event`),
  KEY `FK_event_subject` (`fk_subject`),
  CONSTRAINT `FK_event_subject` FOREIGN KEY (`fk_subject`) REFERENCES `subjects` (`pk_subject`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `notifications`
--

DROP TABLE IF EXISTS `notifications`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `notifications` (
  `pk_notification` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `fk_user` bigint(20) unsigned NOT NULL,
  `message` varchar(255) NOT NULL,
  `type` varchar(255) NOT NULL,
  `url` varchar(255) DEFAULT NULL,
  `is_read` bit(1) NOT NULL,
  `timestamp` datetime NOT NULL,
  PRIMARY KEY (`pk_notification`),
  KEY `FK_notification_user` (`fk_user`),
  CONSTRAINT `FK_notification_user` FOREIGN KEY (`fk_user`) REFERENCES `users` (`pk_user`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `session_videos`
--

DROP TABLE IF EXISTS `session_videos`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `session_videos` (
  `pk_session_video` bigint(20) unsigned NOT NULL,
  `fk_subject` bigint(20) unsigned DEFAULT NULL,
  `fk_user_file` bigint(20) unsigned DEFAULT NULL,
  `fk_time_phase` bigint(20) unsigned DEFAULT NULL,
  `movie_path` varchar(255) NOT NULL,
  `movie_type` varchar(255) NOT NULL,
  `start_timestamp` bigint(20) unsigned NOT NULL,
  PRIMARY KEY (`pk_session_video`),
  KEY `FK_session_video_subject` (`fk_subject`),
  KEY `FK_session_video_user_data` (`fk_user_file`),
  KEY `FK_session_video_time_phase` (`fk_time_phase`),
  CONSTRAINT `FK_session_video_subject` FOREIGN KEY (`fk_subject`) REFERENCES `subjects` (`pk_subject`),
  CONSTRAINT `FK_session_video_time_phase` FOREIGN KEY (`fk_time_phase`) REFERENCES `time_phases` (`pk_time_phase`),
  CONSTRAINT `FK_session_video_user_data` FOREIGN KEY (`fk_user_file`) REFERENCES `user_data` (`pk_user_data`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `studies`
--

DROP TABLE IF EXISTS `studies`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `studies` (
  `pk_study` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `name` varchar(255) NOT NULL,
  `comment` longtext,
  `synchronized_from` bigint(20) unsigned DEFAULT NULL,
  PRIMARY KEY (`pk_study`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
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
  KEY `FK_studies_to_user_user` (`fk_user`),
  KEY `FK_studies_to_user_study` (`fk_study`),
  CONSTRAINT `FK_studies_to_user_study` FOREIGN KEY (`fk_study`) REFERENCES `studies` (`pk_study`),
  CONSTRAINT `FK_studies_to_user_user` FOREIGN KEY (`fk_user`) REFERENCES `users` (`pk_user`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `subjects`
--

DROP TABLE IF EXISTS `subjects`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `subjects` (
  `pk_subject` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `fk_study` bigint(20) unsigned DEFAULT NULL,
  `email` varchar(255) DEFAULT NULL,
  `subject_id` varchar(255) NOT NULL,
  `comment` longtext,
  `synchronized_from` bigint(20) unsigned DEFAULT NULL,
  PRIMARY KEY (`pk_subject`),
  KEY `FK_subject_study` (`fk_study`),
  CONSTRAINT `FK_subject_study` FOREIGN KEY (`fk_study`) REFERENCES `studies` (`pk_study`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `time_phases`
--

DROP TABLE IF EXISTS `time_phases`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `time_phases` (
  `pk_time_phase` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `fk_subject` bigint(20) unsigned NOT NULL,
  `fk_parent_time_phase` bigint(20) unsigned DEFAULT NULL,
  `type` varchar(1024) NOT NULL,
  `name` varchar(1024) DEFAULT NULL,
  `time_start` bigint(20) unsigned NOT NULL,
  `time_end` bigint(20) unsigned NOT NULL,
  `attributes` json NOT NULL,
  PRIMARY KEY (`pk_time_phase`),
  KEY `FK_time_phase_subject` (`fk_subject`),
  KEY `FK_time_phase_time_phase_idx` (`fk_parent_time_phase`),
  CONSTRAINT `FK_time_phase_subject` FOREIGN KEY (`fk_subject`) REFERENCES `subjects` (`pk_subject`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
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
  `fk_subject` bigint(20) unsigned DEFAULT NULL,
  `fk_derived_from` bigint(20) unsigned DEFAULT NULL,
  `fk_session_video` bigint(20) unsigned DEFAULT NULL,
  `filename` varchar(255) NOT NULL,
  `type` varchar(255) NOT NULL,
  `path` varchar(255) NOT NULL,
  `hidden` bit(1) DEFAULT b'0',
  `comment` longtext,
  PRIMARY KEY (`pk_user_data`),
  KEY `fk_session_video_to_session_video_idx` (`fk_session_video`),
  KEY `FK_user_data_user_table` (`fk_user`),
  KEY `FK_user_data_subject` (`fk_subject`),
  KEY `FK_user_data_user_data` (`fk_derived_from`),
  CONSTRAINT `FK_user_data_session_video` FOREIGN KEY (`fk_session_video`) REFERENCES `session_videos` (`pk_session_video`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `FK_user_data_subject` FOREIGN KEY (`fk_subject`) REFERENCES `subjects` (`pk_subject`),
  CONSTRAINT `FK_user_data_user_data` FOREIGN KEY (`fk_derived_from`) REFERENCES `user_data` (`pk_user_data`),
  CONSTRAINT `FK_user_data_user_table` FOREIGN KEY (`fk_user`) REFERENCES `users` (`pk_user`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
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
  `fk_user` bigint(20) unsigned NOT NULL,
  `role` varchar(255) NOT NULL,
  `email` varchar(255) NOT NULL,
  PRIMARY KEY (`pk_role`),
  UNIQUE KEY `user_roles_email_idx` (`email`),
  KEY `FK_user_roles_user_2` (`fk_user`),
  CONSTRAINT `FK_user_roles_user` FOREIGN KEY (`email`) REFERENCES `users` (`email`),
  CONSTRAINT `FK_user_roles_user_2` FOREIGN KEY (`fk_user`) REFERENCES `users` (`pk_user`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `users`
--

DROP TABLE IF EXISTS `users`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `users` (
  `pk_user` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `firstname` varchar(255) NOT NULL,
  `lastname` varchar(255) NOT NULL,
  `email` varchar(255) NOT NULL,
  `password` varchar(255) NOT NULL,
  PRIMARY KEY (`pk_user`),
  UNIQUE KEY `user_table_email_idx` (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2017-06-09 11:41:50

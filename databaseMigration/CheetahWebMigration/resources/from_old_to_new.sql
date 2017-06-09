INSERT INTO `cheetah_web`.`settings`
(`pk_settings`,
`key_column`,
`value`)
SELECT `settings`.`pk_settings`,
    `settings`.`key_column`,
    `settings`.`value`
FROM `cheetah_web_old`.`settings`;

INSERT INTO `cheetah_web`.`studies`
(`pk_study`,
`name`,
`comment`,
`synchronized_from`)
SELECT `study`.`pk_study`,
    `study`.`name`,
    `study`.`comment`,
    `study`.`synchronized_from`
FROM `cheetah_web_old`.`study`;

INSERT INTO `cheetah_web`.`subjects`
(`pk_subject`,
`fk_study`,
`email`,
`subject_id`,
`comment`,
`synchronized_from`)
SELECT `subject`.`pk_subject`,
	`subject`.`fk_study`,
    `subject`.`email`,
    `subject`.`subject_id`,
    `subject`.`comment`,
    `subject`.`synchronized_from`
FROM `cheetah_web_old`.`subject`;

INSERT INTO `cheetah_web`.`users`
(`pk_user`,
`firstname`,
`lastname`,
`email`,
`password`)
SELECT `user_table`.`pk_user`,
    `user_table`.`firstname`,
    `user_table`.`lastname`,
    `user_table`.`email`,
    `user_table`.`password`
FROM `cheetah_web_old`.`user_table`;

INSERT INTO `cheetah_web`.`data_processings`
(`pk_data_processing`,
`fk_study`,
`name`,
`comment`,
`timestamp_column`,
`left_pupil_column`,
`right_pupil_column`,
`decimal_separator`,
`trial_computation_configuration`)
SELECT `data_processing`.`pk_data_processing`,
    `data_processing`.`fk_study`,
    `data_processing`.`name`,
    `data_processing`.`comment`,
    `data_processing`.`timestamp_column`,
    `data_processing`.`left_pupil_column`,
    `data_processing`.`right_pupil_column`,
    `data_processing`.`decimal_separator`,
    `data_processing`.`trial_computation_configuration`
FROM `cheetah_web_old`.`data_processing`;

INSERT INTO `cheetah_web`.`data_processing_steps`
(`pk_data_processing_step`,
`fk_data_processing`,
`type`,
`version`,
`name`,
`configuration`)
SELECT `data_processing_step`.`pk_data_processing_step`,
    `data_processing_step`.`fk_data_processing`,
    `data_processing_step`.`type`,
    `data_processing_step`.`version`,
    `data_processing_step`.`name`,
    `data_processing_step`.`configuration`
FROM `cheetah_web_old`.`data_processing_step`;

INSERT INTO `cheetah_web`.`user_roles`
(`pk_role`,
`fk_user`,
`role`,
`email`)
SELECT `user_roles`.`pk_role`,
    `user_roles`.`fk_user`,
    `user_roles`.`role`,
    `user_roles`.`email`
FROM `cheetah_web_old`.`user_roles`;

INSERT INTO `cheetah_web`.`notifications`
(`pk_notification`,
`fk_user`,
`message`,
`type`,
`url`,
`is_read`,
`timestamp`)
SELECT `notification`.`pk_notification`,
    `notification`.`fk_user`,
    `notification`.`message`,
    `notification`.`type`,
    `notification`.`url`,
    `notification`.`is_read`,
    `notification`.`timestamp`
FROM `cheetah_web_old`.`notification`;



INSERT INTO `cheetah_web`.`studies_to_user`
(`fk_user`,
`fk_study`) 
SELECT `fk_user`,
`fk_study` 
FROM `cheetah_web_old`.`studies_to_user`;






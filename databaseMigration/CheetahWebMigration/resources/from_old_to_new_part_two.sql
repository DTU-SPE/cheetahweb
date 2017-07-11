INSERT INTO `cheetah_web`.`user_data`
(`pk_user_data`,
`fk_user`,
`filename`,
`type`,
`path`,
`hidden`,
`comment`,
`fk_derived_from`,
`fk_subject`)
SELECT `user_data`.`pk_user_data`,
    `user_data`.`fk_user`,
    `user_data`.`filename`,
    `user_data`.`type`,
    `user_data`.`path`,
    `user_data`.`hidden`,
    `user_data`.`comment`,
    `user_data`.`fk_derived_from`,
    `user_data`.`fk_subject`
    FROM `cheeta_web_old`.`user_data`;

update `user_data`, `session_videos`
set `user_data`.`fk_session_video` = `session_videos`.`pk_session_video`
where `session_videos`.`user_data` = `user_data`.`pk_user_data` and `user_data`.`pk_user_data`>0;


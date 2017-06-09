TRUNCATE `cheetah_web`.`data_processing_steps`;
TRUNCATE `cheetah_web`.`events`;
TRUNCATE `cheetah_web`.`notifications`;
TRUNCATE `cheetah_web`.`settings`;
TRUNCATE `cheetah_web`.`studies_to_user`;
TRUNCATE `cheetah_web`.`user_data_tags`;
TRUNCATE `cheetah_web`.`user_roles`;

delete from `time_phases` where pk_time_phase > 0;
delete from `user_data` where pk_user_data > 0;
delete from `session_videos` where pk_session_video > 0;

delete from `users` where pk_user > 0;
delete from `data_processings` where pk_data_processing > 0;
delete from `subjects` where pk_subject > 0;
delete from `studies` where pk_study > 0;
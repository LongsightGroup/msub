create table if not exists jforum_schedule_grades_gradebook (
  grade_id MEDIUMINT UNSIGNED NOT NULL ,
  open_date DATETIME NOT NULL ,
  PRIMARY KEY (grade_id)
);
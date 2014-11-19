ALTER TABLE device_info RENAME TO device_info_tmp;
CREATE TABLE device_info (
  organization VARCHAR(50),
  location LONG,
  supervisor_name VARCHAR(70),
  supervisor_number VARCHAR(15),
  registered TINYINT
);

INSERT INTO device_info(organization, location, supervisor_name, supervisor_number, registered)
SELECT organization, location, supervisor_name, supervisor_number, registered
FROM device_info_tmp;

DROP TABLE device_info_tmp;
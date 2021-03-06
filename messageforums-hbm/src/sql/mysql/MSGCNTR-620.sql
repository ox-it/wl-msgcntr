DROP TABLE MFR_RANK_T;
DROP TABLE MFR_RANKIMAGE_T;

CREATE TABLE 'MFR_RANK_T' (
  'ID' bigint(20) NOT NULL AUTO_INCREMENT,
  'VERSION' int(11) NOT NULL,
  'UUID' varchar(36) NOT NULL,
  'CREATED' datetime NOT NULL,
  'CREATED_BY' varchar(36) NOT NULL,
  'MODIFIED' datetime NOT NULL,
  'MODIFIED_BY' varchar(36) NOT NULL,
  'TITLE' varchar(255) NOT NULL,
  'RANKTYPE' varchar(19) NOT NULL,
  'ASSIGNTO' varchar(255) DEFAULT NULL,
  'ASSIGNTODISPLAY' varchar(255) DEFAULT NULL,
  'MIN_POST' bigint(20) DEFAULT NULL,
  'CONTEXT_ID' varchar(255) NOT NULL,
  'RANKIMAGE' bigint(20) DEFAULT NULL,
  PRIMARY KEY ('ID')
);
   
CREATE TABLE 'MFR_RANKIMAGE_T' (
  'ID' bigint(20) NOT NULL AUTO_INCREMENT,
  'VERSION' int(11) NOT NULL,
  'CREATED' datetime NOT NULL,
  'CREATED_BY' varchar(255) NOT NULL,
  'MODIFIED' datetime NOT NULL,
  'MODIFIED_BY' varchar(255) NOT NULL,
  'ATTACHMENT_ID' varchar(255) NOT NULL,
  'ATTACHMENT_URL' varchar(255) NOT NULL,
  'ATTACHMENT_NAME' varchar(255) NOT NULL,
  'ATTACHMENT_SIZE' varchar(255) NOT NULL,
  'ATTACHMENT_TYPE' varchar(255) NOT NULL,
  'RANKID' bigint(20) DEFAULT NULL,
  PRIMARY KEY ('ID')
);



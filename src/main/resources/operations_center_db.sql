CREATE DATABASE `OC_DB` /*!40100 DEFAULT CHARACTER SET latin1 */;
CREATE TABLE `OC_DOMAIN` (
  `OC_DOMAIN_ID` int(11) NOT NULL AUTO_INCREMENT,
  `OC_DOMAIN_NAME` varchar(511) NOT NULL,
  PRIMARY KEY (`OC_DOMAIN_ID`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
CREATE TABLE `OC_MACHINE` (
  `OC_MACHINE_ID` int(11) NOT NULL AUTO_INCREMENT,
  `OC_MACHINE_IP` varchar(63) NOT NULL,
  PRIMARY KEY (`OC_MACHINE_ID`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
CREATE TABLE `OC_SERVER` (
  `OC_SERVER_ID` varchar(511) NOT NULL,
  `OC_SERVER_MACHINE_ID` int(11) NOT NULL,
  `OC_SERVER_NAME` varchar(511) NOT NULL,
  `OC_SERVER_IP` varchar(31) NOT NULL,
  `OC_SERVER_DOMAIN` varchar(511) DEFAULT NULL,
  `OC_SERVER_SUB_DOMAIN` varchar(511) DEFAULT NULL,
  PRIMARY KEY (`OC_SERVER_ID`),
  KEY `fk_OC_SERVER_1_idx` (`OC_SERVER_MACHINE_ID`),
  CONSTRAINT `fk_OC_SERVER_1` FOREIGN KEY (`OC_SERVER_MACHINE_ID`) REFERENCES `OC_MACHINE` (`OC_MACHINE_ID`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

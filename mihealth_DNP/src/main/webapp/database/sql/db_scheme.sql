--
-- Create Table for Messages
--
CREATE TABLE `tb_resource_message` (
	`uid` BINARY(16) NOT NULL,
	`category` VARCHAR(50) NULL DEFAULT NULL,
	`code` VARCHAR(50) NOT NULL,
	`messages` BLOB NULL,
	`comment` VARCHAR(100) NULL DEFAULT NULL,
	`sequence` INT(11) NOT NULL DEFAULT '0',
	`enabled` BIT(1) NOT NULL DEFAULT b'1',
	PRIMARY KEY (`uid`),
	UNIQUE INDEX `category_code` (`category`, `code`)
)
COLLATE='utf8_general_ci'
ENGINE=InnoDB
;

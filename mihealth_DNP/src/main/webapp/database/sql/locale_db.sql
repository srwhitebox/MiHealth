--
-- Initalize Locale data
--
REPLACE INTO `tb_locale` (`localeTag`, `displayName`, `subsTag`, `enabled`) VALUES
	('en-CA', 'English', 'en-US', 1),
	('en-GB', 'English', 'en-US', 1),
	('en-US', 'English', 'en-US', 1),
	('fr', 'French', 'en-US', 1),
	('fr-CA', 'French', 'en-US', 1),
	('fr-FR', 'French', 'en-US', 1),
	('ja', 'Japanese', 'ja-JP', 1),
	('ja-JP', 'Japanese', 'ja-JP', 1),
	('ko', 'Korean', 'ko-KR', 1),
	('ko-KR', 'Korean', 'ko-KR', 1),
	('vi-VN', 'Vietnamese', 'vi-VN', 1),
	('zh', 'Chinese', 'zh-CN', 1),
	('zh-CN', 'Chinese', 'zh-CN', 1),
	('zh-TW', 'Chinese', 'zh-TW', 1);

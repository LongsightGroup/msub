ALTER TABLE oauth_provider CHANGE COLUMN consumer_secret hmacSha1SharedSecret VARCHAR(255), ADD COLUMN rsaKey TEXT, ADD COLUMN signatureMethod VARCHAR(255), ADD COLUMN enabled BIT;


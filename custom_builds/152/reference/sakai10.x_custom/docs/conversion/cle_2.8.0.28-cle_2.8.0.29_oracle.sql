/*-----------------------------------
-- BEGIN: CLE-9515
-----------------------------------*/
DELETE FROM oauth_headers WHERE
  providerUUID IN (SELECT uuid FROM oauth_provider WHERE consumer_key='duffy.rsmart.com') AND
  providerUUID NOT IN (SELECT providerUUID FROM oauth_token);

DELETE FROM oauth_provider WHERE
  consumer_key = 'duffy.rsmart.com' AND
  uuid NOT IN (SELECT providerUUID FROM oauth_token) AND
  uuid NOT IN (SELECT providerUUID FROM oauth_headers);

UPDATE oauth_provider
  SET enabled=1 WHERE enabled is NULL;
/*-----------------------------------
-- END: CLE-9515
-----------------------------------*/
# --- First database Schema

# --- !Ups

CREATE SEQUENCE s_access_token_id;

CREATE TABLE twitter_access_token (
  id      bigint DEFAULT nextval('s_access_token_id'),
  token   varchar(256),
  secret  varchar(256)
)

# --- !Downs

DROP TABLE twitter_access_token;

DROP SEQUENCE s_access_token_id;

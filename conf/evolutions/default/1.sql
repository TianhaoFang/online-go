# determine structure
# --- !Ups

CREATE TABLE "User" (
  username  VARCHAR(40)  NOT NULL,
  password  VARCHAR(256) NOT NULL,
  nickname  VARCHAR(40)  NOT NULL,
  email     VARCHAR(256) NOT NULL DEFAULT '',
  google_id VARCHAR(64),
  image_url VARCHAR(512),

  PRIMARY KEY (username)
);

CREATE TABLE Friend (
  user1 VARCHAR(40) NOT NULL ,
  user2 VARCHAR(40) NOT NULL ,
  url VARCHAR(256) NOT NULL ,
  accepted BOOLEAN NOT NULL DEFAULT FALSE ,
  PRIMARY KEY (user1, user2)
);

CREATE TABLE GamePlay (
  id UUID NOT NULL ,
  first_user VARCHAR(40) NOT NULL ,
  second_user VARCHAR(40) NOT NULL ,
  status VARCHAR(40) NOT NULL ,
  rule VARCHAR(40) NOT NULL ,
  first_win BOOLEAN,
  start_time TIMESTAMP NOT NULL ,
  steps INTEGER[],
  PRIMARY KEY (id)
);

CREATE TABLE Admin (
  username VARCHAR(40) NOT NULL PRIMARY KEY ,
  password VARCHAR(256) NOT NULL
);

INSERT INTO Admin(username, password) VALUES ('admin', 'SXyGIiQfPa0MFzwf3oiDH48iXoG4S5PI3YfV0WsZdT0=');

# --- !Downs

DROP TABLE "User";
DROP TABLE Friend;
DROP TABLE GamePlay;
DROP TABLE Admin;
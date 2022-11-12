CREATE SCHEMA IF NOT EXISTS SPORT_RADAR;

CREATE SEQUENCE IF NOT EXISTS SPORT_RADAR.hibernate_sequence START WITH 1;
CREATE SEQUENCE IF NOT EXISTS SPORT_RADAR.default_sequence_generator START WITH 1;

CREATE TABLE IF NOT EXISTS SPORT_RADAR.`SCORE_BOARD`
(
    `id`                 BIGINT                      not null primary key,
    `deleted`            boolean                     NOT NULL default false,
    `created_date_time`  timestamp without time zone NOT NULL,
    `modified_date_time` timestamp without time zone NOT NULL,
    `home_team_name`     VARCHAR(255)                NOT NULL,
    `away_team_name`     VARCHAR(255)                NOT NULL,
    `home_team_score`    INT                         NOT NULL,
    `away_team_score`    INT                         NOT NULL,
);

CREATE TABLE IF NOT EXISTS SPORT_RADAR.`SCORE_BOARD_AUD`
(
    `id`                      BIGINT                      NOT NULL,
    `rev`                     BIGINT                      NOT NULL,
    `revtype`                 SMALLINT                    NOT NULL,
    `deleted`                 boolean                     NOT NULL default false,
    `deleted_mode`            boolean                     NOT NULL default false,
    `modified_date_time`      timestamp without time zone NOT NULL,
    `modified_date_time_mode` boolean                     NOT NULL default false,
    `home_team_name`          VARCHAR(255)                NOT NULL,
    `home_team_name_mode`     boolean                     NOT NULL default false,
    `away_team_name`          VARCHAR(255)                NOT NULL,
    `away_team_name_mode`     boolean                     NOT NULL default false,
    `home_team_score`         INT                         NOT NULL,
    `home_team_score_mode`    boolean                     NOT NULL default false,
    `away_team_score`         INT                         NOT NULL,
    `away_team_score_mode`    boolean                     NOT NULL default false,
);

-- Since H2 database is being used, insert scripts can not be run with flyway scripts

INSERT INTO SPORT_RADAR.`SCORE_BOARD`
VALUES ((select NEXTVAL from NEXTVAL('SPORT_RADAR.default_sequence_generator')), false, DATEADD('DAY', 0, CURRENT_DATE),
        DATEADD('DAY', -1, CURRENT_DATE), 'Uruguay', 'Italy', 6, 6),
       ((select NEXTVAL from NEXTVAL('SPORT_RADAR.default_sequence_generator')), false, DATEADD('DAY', 0, CURRENT_DATE),
        DATEADD('DAY', -2, CURRENT_DATE), 'Spain', 'Brazil', 10, 2),
       ((select NEXTVAL from NEXTVAL('SPORT_RADAR.default_sequence_generator')), false, DATEADD('DAY', 0, CURRENT_DATE),
        DATEADD('DAY', -3, CURRENT_DATE), 'Mexico', 'Canada', 0, 5),
       ((select NEXTVAL from NEXTVAL('SPORT_RADAR.default_sequence_generator')), false, DATEADD('DAY', 0, CURRENT_DATE),
        DATEADD('DAY', -4, CURRENT_DATE), 'Argentina', 'Australia', 3, 1),
       ((select NEXTVAL from NEXTVAL('SPORT_RADAR.default_sequence_generator')), false, DATEADD('DAY', 0, CURRENT_DATE),
        DATEADD('DAY', -5, CURRENT_DATE), 'Germany', 'France', 2, 2);
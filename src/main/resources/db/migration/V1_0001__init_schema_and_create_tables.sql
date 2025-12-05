CREATE SCHEMA IF NOT EXISTS MB_TEST;

CREATE SEQUENCE IF NOT EXISTS MB_TEST.hibernate_sequence START WITH 1 INCREMENT BY 50;
CREATE SEQUENCE IF NOT EXISTS MB_TEST.default_sequence_generator START WITH 1 INCREMENT BY 50;
CREATE SEQUENCE IF NOT EXISTS MB_TEST.revinfo_seq START WITH 1 INCREMENT BY 1;

CREATE TABLE IF NOT EXISTS MB_TEST.REVINFO
(
    rev      INTEGER NOT NULL DEFAULT NEXTVAL('MB_TEST.revinfo_seq') PRIMARY KEY,
    revtstmp BIGINT
);

CREATE TABLE IF NOT EXISTS MB_TEST.SCORE_BOARD
(
    id                 BIGINT       NOT NULL PRIMARY KEY,
    deleted            BOOLEAN      NOT NULL DEFAULT FALSE,
    created_date_time  TIMESTAMP    NOT NULL,
    modified_date_time TIMESTAMP    NOT NULL,
    home_team_name     VARCHAR(255) NOT NULL,
    away_team_name     VARCHAR(255) NOT NULL,
    home_team_score    INT          NOT NULL,
    away_team_score    INT          NOT NULL
);

CREATE TABLE IF NOT EXISTS MB_TEST.SCORE_BOARD_AUD
(
    id                     BIGINT  NOT NULL,
    rev                    INTEGER NOT NULL,
    revtype                SMALLINT,
    deleted                BOOLEAN,
    deleted_mod            BOOLEAN,
    created_date_time      TIMESTAMP,
    created_date_time_mod  BOOLEAN,
    modified_date_time     TIMESTAMP,
    modified_date_time_mod BOOLEAN,
    home_team_name         VARCHAR(255),
    home_team_name_mod     BOOLEAN,
    away_team_name         VARCHAR(255),
    away_team_name_mod     BOOLEAN,
    home_team_score        INT,
    home_team_score_mod    BOOLEAN,
    away_team_score        INT,
    away_team_score_mod    BOOLEAN,
    PRIMARY KEY (rev, id),
    FOREIGN KEY (rev) REFERENCES MB_TEST.REVINFO (rev)
);

CREATE TABLE IF NOT EXISTS MB_TEST.tutorials
(
    id          BIGINT NOT NULL PRIMARY KEY,
    title       VARCHAR(255),
    description VARCHAR(255),
    published   BOOLEAN
);

INSERT INTO MB_TEST.SCORE_BOARD (id, deleted, created_date_time, modified_date_time, home_team_name, away_team_name,
                                 home_team_score, away_team_score)
VALUES (NEXTVAL('MB_TEST.default_sequence_generator'), FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP - INTERVAL '1' DAY,
        'Uruguay', 'Italy', 6, 6),
       (NEXTVAL('MB_TEST.default_sequence_generator'), FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP - INTERVAL '2' DAY,
        'Spain', 'Brazil', 10, 2),
       (NEXTVAL('MB_TEST.default_sequence_generator'), FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP - INTERVAL '3' DAY,
        'Mexico', 'Canada', 0, 5),
       (NEXTVAL('MB_TEST.default_sequence_generator'), FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP - INTERVAL '4' DAY,
        'Argentina', 'Australia', 3, 1),
       (NEXTVAL('MB_TEST.default_sequence_generator'), FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP - INTERVAL '5' DAY,
        'Germany', 'France', 2, 2);

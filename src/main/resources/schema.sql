DROP TABLE IF EXISTS `stock`;

CREATE TABLE IF NOT EXISTS `stock` (
    `symbol` VARCHAR(50)    NOT NULL PRIMARY KEY,
    `value`  DECIMAL(13,2)  NOT NULL,
    `volume` INT            NOT NULL
);
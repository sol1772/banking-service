CREATE TABLE IF NOT EXISTS User_
(
    id           BIGINT AUTO_INCREMENT,
    username     VARCHAR(100) NOT NULL,
    login_       VARCHAR(50)  NOT NULL UNIQUE,
    dateOfBirth  DATE         NOT NULL,
    passwordHash VARCHAR(128),
    registeredAt TIMESTAMP,
    PRIMARY KEY (id)
);
CREATE TABLE IF NOT EXISTS BankAccount
(
    id             INT AUTO_INCREMENT,
    number         VARCHAR(100) NOT NULL,
    initialBalance NUMERIC(20, 2),
    balance        NUMERIC(20, 2),
    userId         BIGINT       NOT NULL,
    PRIMARY KEY (id)
);
CREATE TABLE IF NOT EXISTS BankTransaction
(
    id                   BIGINT AUTO_INCREMENT,
    transactionReference VARCHAR(100) NOT NULL,
    amount               NUMERIC(20, 2),
    status               ENUM ('PENDING', 'PROCESSING', 'SUCCESS', 'FAILED'),
    fromAccountId        INT,
    toAccountId          INT,
    createdAt            TIMESTAMP,
    PRIMARY KEY (id)
);
CREATE TABLE IF NOT EXISTS Email
(
    id      BIGINT AUTO_INCREMENT,
    content VARCHAR(100) NOT NULL,
    userId  INT          NOT NULL,
    PRIMARY KEY (id)
);
CREATE TABLE IF NOT EXISTS Phone
(
    id       BIGINT AUTO_INCREMENT,
    phoneNmr VARCHAR(100) NOT NULL,
    userId   INT          NOT NULL,
    PRIMARY KEY (id)
);
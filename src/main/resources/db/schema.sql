-- Fresh installation only. No DROP statements; existing schemas require a reviewed migration.
CREATE TABLE IF NOT EXISTS book (
    id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    `desc` VARCHAR(255) NOT NULL,
    price DECIMAL(10,2) NOT NULL CHECK (price >= 0),
    deleted BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE TABLE IF NOT EXISTS student (
    id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    sex VARCHAR(10) NOT NULL,
    grade INT NOT NULL
);

CREATE TABLE IF NOT EXISTS user (
    id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(64) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,
    role VARCHAR(20) NOT NULL
);

CREATE TABLE IF NOT EXISTS borrow (
    id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    sid INT NOT NULL,
    bid INT NOT NULL,
    time DATETIME NOT NULL,
    returned_at DATETIME NULL,
    FOREIGN KEY (sid) REFERENCES student(id),
    FOREIGN KEY (bid) REFERENCES book(id)
);
CREATE INDEX idx_borrow_active ON borrow(bid, returned_at);

CREATE TABLE IF NOT EXISTS persistent_logins (
    username VARCHAR(64) NOT NULL,
    series VARCHAR(64) NOT NULL PRIMARY KEY,
    token VARCHAR(64) NOT NULL,
    last_used TIMESTAMP NOT NULL
);

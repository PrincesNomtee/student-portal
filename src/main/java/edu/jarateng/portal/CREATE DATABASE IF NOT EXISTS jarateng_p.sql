CREATE DATABASE root
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

CREATE USER IF NOT EXISTS 'root'@'localhost'
  IDENTIFIED BY 'choose-a-password';

GRANT ALL PRIVILEGES ON root.* TO 'root'@'localhost';
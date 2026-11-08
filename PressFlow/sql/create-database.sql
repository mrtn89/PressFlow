-- Legt die Datenbank und einen eigenen Datenbankbenutzer für PressFlow an.
-- Die Tabellen erzeugt Hibernate beim ersten Start automatisch (hibernate.hbm2ddl.auto=update).
-- Ausführen z. B. in MySQL Workbench oder mit: mysql -u root -p < create-database.sql

CREATE DATABASE IF NOT EXISTS pressflow
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

-- Eigener Benutzer statt root; Passwort muss zu persistence.xml passen
CREATE USER IF NOT EXISTS 'pressflow'@'localhost' IDENTIFIED BY 'pressflow';
GRANT ALL PRIVILEGES ON pressflow.* TO 'pressflow'@'localhost';
FLUSH PRIVILEGES;

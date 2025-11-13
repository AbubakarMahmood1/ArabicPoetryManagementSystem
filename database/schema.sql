-- Classical Arabic Poetry Management System
-- Database Schema

-- Drop existing database if exists
DROP DATABASE IF EXISTS arabic_poetry_db;

-- Create database with UTF-8 encoding for Arabic support
CREATE DATABASE arabic_poetry_db
CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;

USE arabic_poetry_db;

-- Users Table (for authentication)
CREATE TABLE users (
    user_id INT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_login TIMESTAMP NULL,
    is_active BOOLEAN DEFAULT TRUE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Books Table
CREATE TABLE books (
    book_id INT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(200) NOT NULL,
    compiler VARCHAR(100),
    era VARCHAR(100),
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Poets Table
CREATE TABLE poets (
    poet_id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    biography TEXT,
    birth_year VARCHAR(50),
    death_year VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Poems Table
CREATE TABLE poems (
    poem_id INT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(300) NOT NULL,
    poet_id INT,
    book_id INT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (poet_id) REFERENCES poets(poet_id) ON DELETE SET NULL,
    FOREIGN KEY (book_id) REFERENCES books(book_id) ON DELETE SET NULL,
    INDEX idx_poet (poet_id),
    INDEX idx_book (book_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Verses Table
CREATE TABLE verses (
    verse_id INT PRIMARY KEY AUTO_INCREMENT,
    poem_id INT NOT NULL,
    verse_number INT NOT NULL,
    text TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (poem_id) REFERENCES poems(poem_id) ON DELETE CASCADE,
    INDEX idx_poem (poem_id),
    UNIQUE KEY unique_verse_in_poem (poem_id, verse_number)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Insert default admin user (password: admin123)
-- Password hash is SHA-256 of 'admin123'
INSERT INTO users (username, password_hash, full_name) VALUES
('admin', '240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9', 'System Administrator');

-- Sample data for testing
INSERT INTO books (title, compiler, era, description) VALUES
('ديوان الحماسة', 'أبو تمام', 'العصر العباسي', 'مجموعة من أشعار الحماسة والشجاعة');

INSERT INTO poets (name, biography, birth_year, death_year) VALUES
('قُرَيْطُ بنُ أُنَيْفٍ', 'شاعر إسلامي من بني العنبر', 'غير معروف', 'غير معروف');

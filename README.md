# Arabic Poetry Management System

A Java-based desktop application for managing classical Arabic poetry collections. This system provides a comprehensive platform for storing, organizing, and managing books, poets, poems, and verses with full Arabic text support.

![Java](https://img.shields.io/badge/Java-8%2B-orange)
![MySQL](https://img.shields.io/badge/MySQL-5.7%2B-blue)
![License](https://img.shields.io/badge/License-MIT-green)

## 📋 Table of Contents

- [Features](#features)
- [Project Structure](#project-structure)
- [Prerequisites](#prerequisites)
- [Installation & Setup](#installation--setup)
- [Usage](#usage)
- [Architecture](#architecture)
- [Design Patterns](#design-patterns)
- [Database Schema](#database-schema)
- [Troubleshooting](#troubleshooting)

## ✨ Features

### Core Functionality (CRUD Operations)
- **Books Management**: Add, edit, delete, and search classical Arabic poetry books
- **Poets Management**: Maintain detailed poet information including biography and dates
- **Poems Management**: Link poems to poets and books with complete metadata
- **Verses Management**: Store verses with numbering and link them to poems

### Additional Features
- **User Authentication**: Secure login system with SHA-256 password hashing
- **Import Functionality**: Parse and import poems from formatted text files (Poem.txt)
- **Search Capabilities**: Search across all entities (books, poets, poems, verses)
- **Arabic Text Support**: Full UTF-8 (utf8mb4) support with RTL text rendering
- **Clean UI**: Simple Swing-based interface designed for ease of use

## 📁 Project Structure

```
ArabicPoetryManagementSystem/
├── src/
│   └── com/arabicpoetry/
│       ├── Main.java                    # Application entry point
│       ├── model/                       # Entity/Model classes
│       │   ├── Book.java
│       │   ├── Poet.java
│       │   ├── Poem.java
│       │   ├── Verse.java
│       │   └── User.java
│       ├── dal/                         # Data Access Layer
│       │   ├── DAOFactory.java         # Abstract Factory for DAOs
│       │   ├── dao/                    # DAO interfaces
│       │   │   ├── BookDAO.java
│       │   │   ├── PoetDAO.java
│       │   │   ├── PoemDAO.java
│       │   │   ├── VerseDAO.java
│       │   │   └── UserDAO.java
│       │   └── dao/impl/               # DAO implementations
│       │       ├── BookDAOImpl.java
│       │       ├── PoetDAOImpl.java
│       │       ├── PoemDAOImpl.java
│       │       ├── VerseDAOImpl.java
│       │       └── UserDAOImpl.java
│       ├── bll/                         # Business Logic Layer
│       │   └── service/                # Service classes
│       │       ├── AuthenticationService.java
│       │       ├── BookService.java
│       │       ├── PoetService.java
│       │       ├── PoemService.java
│       │       ├── VerseService.java
│       │       └── ImportService.java
│       ├── presentation/                # Presentation Layer
│       │   └── ui/                     # Swing UI components
│       │       ├── LoginFrame.java
│       │       ├── MainFrame.java
│       │       ├── BookManagementFrame.java
│       │       ├── PoetManagementFrame.java
│       │       ├── PoemManagementFrame.java
│       │       └── VerseManagementFrame.java
│       └── util/                        # Utility classes
│           ├── DatabaseConnection.java  # Singleton DB connection
│           ├── DatabaseConfig.java      # Configuration manager
│           └── PasswordUtil.java        # Password hashing utilities
├── database/
│   └── schema.sql                       # MySQL database schema
├── lib/                                 # External libraries (place MySQL JDBC here)
├── Poem.txt                            # Sample poems dataset
├── README.md                           # This file
└── SETUP_INSTRUCTIONS.md               # Detailed setup guide

```

## 🔧 Prerequisites

- **Java Development Kit (JDK)**: Version 8 or higher
- **MySQL Server**: Version 5.7 or higher
- **Eclipse IDE**: Any recent version (or IntelliJ IDEA)
- **MySQL JDBC Driver**: `mysql-connector-java-8.x.x.jar`

## 🚀 Installation & Setup

### 1. Database Setup

**Start MySQL Server** and ensure it's running on port 3306 (default).

**Create the Database:**

```bash
# Option 1: Using MySQL command line
mysql -u root -p < database/schema.sql

# Option 2: Using MySQL Workbench
# Open MySQL Workbench, load database/schema.sql, and execute
```

**Verify Database Creation:**
- Database name: `arabic_poetry_db`
- Default admin user:
  - Username: `admin`
  - Password: `admin123`

### 2. Project Setup in Eclipse

**Import Project:**
1. Open Eclipse IDE
2. File → Open Projects from File System
3. Select the `ArabicPoetryManagementSystem` folder
4. Click Finish

**Add MySQL JDBC Driver:**
1. Download MySQL Connector/J from: https://dev.mysql.com/downloads/connector/j/
2. Right-click project → Build Path → Configure Build Path
3. Click "Add External JARs"
4. Select `mysql-connector-java-8.x.x.jar`
5. Click Apply and Close

**Configure Database Connection:**

When you first run the application, a `config.properties` file will be auto-generated in the project root. Update it with your MySQL credentials:

```properties
db.url=jdbc:mysql://localhost:3306/arabic_poetry_db?useSSL=false&serverTimezone=UTC&characterEncoding=UTF-8
db.username=root
db.password=YOUR_MYSQL_PASSWORD
db.driver=com.mysql.cj.jdbc.Driver
```

### 3. Run the Application

1. Navigate to `src/com/arabicpoetry/Main.java`
2. Right-click → Run As → Java Application
3. Login with default credentials:
   - Username: `admin`
   - Password: `admin123`

## 📖 Usage

### Login
- Launch the application
- Enter username and password
- Click "Login"

### Managing Entities

**Books:**
- Go to Manage → Manage Books
- Add/Edit/Delete books
- Search by title, compiler, or era

**Poets:**
- Go to Manage → Manage Poets
- Add poet information including biography, birth/death years
- Search by name or biography

**Poems:**
- Go to Manage → Manage Poems
- Link poems to poets and books using dropdown menus
- Search by poem title

**Verses:**
- Go to Manage → Manage Verses
- Select a poem from dropdown
- Add verses with verse numbers
- Edit or delete existing verses

### Importing Poems

1. Go to File → Import Poems from File
2. Select `Poem.txt` or any compatible text file
3. Wait for import to complete
4. View imported data in respective management screens

**Import File Format:**
- Book title: `الكتاب : [Book Title]`
- Poem title: `[Poem Title in brackets]`
- Verses: `(verse part 1) (verse part 2)`
- Footnote delimiter: `_________` (skip everything after this)
- Page delimiter: `==========`

## 🏗️ Architecture

This project follows a **3-layered architecture** pattern for clean separation of concerns:

### 1. Presentation Layer (UI)
- Swing-based graphical user interface
- Forms for CRUD operations
- Login and main navigation frames

### 2. Business Logic Layer (BLL)
- Service classes containing business rules
- Validation logic
- Data transformation
- Import/export functionality

### 3. Data Access Layer (DAL)
- DAO (Data Access Object) interfaces
- DAO implementations with JDBC
- Database connection management
- SQL query execution

**Data Flow:**
```
UI → Service → DAO → Database
```

## 🎨 Design Patterns

### 1. Singleton Pattern
Used for ensuring single instances of:
- `DatabaseConnection` - Single database connection
- `DatabaseConfig` - Configuration management
- All Service classes (`BookService`, `PoetService`, etc.)
- `DAOFactory` - DAO instance creation

### 2. Abstract Factory Pattern
- `DAOFactory` creates DAO instances
- Centralizes DAO object creation
- Allows easy swapping of implementations

### 3. Dependency Injection
- Services depend on DAO **interfaces**, not implementations
- DAOs are injected via DAOFactory
- Loose coupling between layers

### 4. Repository Pattern
- DAO layer abstracts all database operations
- Services don't need to know about SQL
- Clean separation of data access logic

## 🗄️ Database Schema

### Tables

**users**
- `user_id` (PK, AUTO_INCREMENT)
- `username` (UNIQUE)
- `password_hash` (SHA-256)
- `full_name`
- `created_at`, `last_login`
- `is_active`

**books**
- `book_id` (PK, AUTO_INCREMENT)
- `title`
- `compiler`
- `era`
- `description`
- `created_at`, `updated_at`

**poets**
- `poet_id` (PK, AUTO_INCREMENT)
- `name`
- `biography`
- `birth_year`, `death_year`
- `created_at`, `updated_at`

**poems**
- `poem_id` (PK, AUTO_INCREMENT)
- `title`
- `poet_id` (FK → poets)
- `book_id` (FK → books)
- `created_at`, `updated_at`

**verses**
- `verse_id` (PK, AUTO_INCREMENT)
- `poem_id` (FK → poems, CASCADE DELETE)
- `verse_number`
- `text`
- `created_at`, `updated_at`
- UNIQUE constraint on (poem_id, verse_number)

### Relationships
- One Book → Many Poems
- One Poet → Many Poems
- One Poem → Many Verses

## 🔍 Troubleshooting

### Database Connection Error
**Issue:** Cannot connect to database

**Solutions:**
- Verify MySQL server is running: `sudo systemctl status mysql`
- Check username/password in `config.properties`
- Ensure database `arabic_poetry_db` exists
- Verify port 3306 is not blocked by firewall

### JDBC Driver Not Found
**Issue:** `ClassNotFoundException: com.mysql.cj.jdbc.Driver`

**Solutions:**
- Download MySQL Connector/J: https://dev.mysql.com/downloads/connector/j/
- Add JAR to Eclipse build path (see installation steps)
- Verify JAR file is not corrupted

### Arabic Text Not Displaying Correctly
**Issue:** Arabic text shows as ??? or boxes

**Solutions:**
- Ensure database charset is `utf8mb4` (check `schema.sql`)
- Verify connection URL includes: `characterEncoding=UTF-8`
- Check that proper fonts are installed on your system

### UI Alignment Issues
**Issue:** Dropdown menus or text fields misaligned

**Solutions:**
- This is normal for RTL (Right-to-Left) text rendering
- Arabic text fields use RTL orientation by design
- Dropdown menus show items correctly despite visual gaps

### Import Errors
**Issue:** Import fails or data is incorrect

**Solutions:**
- Check file encoding is UTF-8
- Verify file follows the correct format (see Usage → Importing Poems)
- Check console for detailed error messages
- Ensure database has sufficient permissions

## 📝 Notes

- All database operations use **prepared statements** to prevent SQL injection
- Passwords are hashed using **SHA-256** before storage
- The system supports full **UTF-8 (utf8mb4)** for Arabic text
- Follow the **3-layered architecture** when making modifications
- Each layer should only communicate with adjacent layers

## 🤝 Contributing

This is a hobby/educational project. Feel free to:
- Report bugs
- Suggest improvements
- Fork and modify for your needs

## 📄 License

This project is for educational purposes. Use freely for learning and personal projects.

## 👨‍💻 Developer Notes

**Code Style:**
- Simple and beginner-friendly
- Well-commented for learning purposes
- Follows standard Java naming conventions
- No complex frameworks - just core Java + Swing + JDBC

**Future Enhancements (Not Implemented):**
- Verse enhancements (notes, translations, diacritics)
- Linguistic processing (tokenization, lemmas, roots)
- Advanced search capabilities
- Export functionality
- User management interface
- Report generation

---

**Version:** 1.0
**Last Updated:** November 2024

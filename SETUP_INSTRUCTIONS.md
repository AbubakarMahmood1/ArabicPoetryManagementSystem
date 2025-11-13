# Arabic Poetry Management System - Setup Instructions

## Prerequisites

1. **Java Development Kit (JDK)**: JDK 8 or higher
2. **MySQL Server**: Version 5.7 or higher
3. **Eclipse IDE**: Any recent version
4. **MySQL JDBC Driver**: mysql-connector-java-8.0.x.jar

## Database Setup

1. **Install and Start MySQL Server**
   - Make sure MySQL server is running on your machine
   - Default port: 3306

2. **Create Database**
   - Open MySQL command line or MySQL Workbench
   - Navigate to the `database` folder in this project
   - Run the schema.sql file:
     ```sql
     source /path/to/ArabicPoetryManagementSystem/database/schema.sql
     ```
   - Or copy and paste the contents of schema.sql into MySQL Workbench and execute

3. **Verify Database Creation**
   - Database name: `arabic_poetry_db`
   - Default user credentials:
     - Username: `admin`
     - Password: `admin123`

## Project Setup in Eclipse

1. **Import Project**
   - Open Eclipse IDE
   - File → Open Projects from File System
   - Select the `ArabicPoetryManagementSystem` folder
   - Click Finish

2. **Add MySQL JDBC Driver**
   - Download MySQL Connector/J from: https://dev.mysql.com/downloads/connector/j/
   - Right-click on project → Build Path → Configure Build Path
   - Click "Add External JARs"
   - Navigate to and select `mysql-connector-java-8.x.x.jar`
   - Click Apply and Close

3. **Configure Database Connection**
   - When you first run the application, a `config.properties` file will be created
   - Update the file with your MySQL credentials:
     ```properties
     db.url=jdbc:mysql://localhost:3306/arabic_poetry_db?useSSL=false&serverTimezone=UTC&characterEncoding=UTF-8
     db.username=root
     db.password=YOUR_MYSQL_PASSWORD
     db.driver=com.mysql.cj.jdbc.Driver
     ```

## Running the Application

1. **Run Main Class**
   - Navigate to: `src/com/arabicpoetry/Main.java`
   - Right-click → Run As → Java Application

2. **Login**
   - Default credentials:
     - Username: `admin`
     - Password: `admin123`

3. **Import Sample Data (Optional)**
   - After logging in, go to: File → Import Poems from File
   - Select the `Poem.txt` file from the project root
   - Wait for the import to complete

## Project Structure

```
ArabicPoetryManagementSystem/
├── src/
│   └── com/arabicpoetry/
│       ├── model/               # Entity classes (Book, Poet, Poem, Verse, User)
│       ├── dal/                 # Data Access Layer
│       │   ├── dao/            # DAO interfaces
│       │   └── dao/impl/       # DAO implementations
│       ├── bll/                 # Business Logic Layer
│       │   └── service/        # Service classes
│       ├── presentation/        # Presentation Layer
│       │   └── ui/             # Swing UI forms
│       ├── util/               # Utility classes
│       └── Main.java           # Entry point
├── database/
│   └── schema.sql              # Database schema
├── lib/                        # External libraries (add MySQL JDBC here)
├── Poem.txt                    # Sample poems data
└── README.md
```

## Design Patterns Used

1. **Singleton Pattern**
   - DatabaseConnection
   - DatabaseConfig
   - All Service classes
   - DAOFactory

2. **Abstract Factory Pattern**
   - DAOFactory creates DAO instances

3. **Dependency Injection**
   - Services depend on DAO interfaces
   - DAOs are injected via DAOFactory

4. **Repository Pattern**
   - DAO layer abstracts database operations

## Features

### Core CRUD Operations
- **Books**: Add, edit, delete, search books
- **Poets**: Manage poet information
- **Poems**: Link poems to poets and books
- **Verses**: Add verses to poems with verse numbers

### Additional Features
- **User Authentication**: Login system with password hashing (SHA-256)
- **Import**: Import poems from text files (Poem.txt format)
- **Search**: Search across all entities
- **Arabic Support**: Full UTF-8 support for Arabic text

## Troubleshooting

### Database Connection Error
- Verify MySQL server is running
- Check username/password in config.properties
- Ensure database `arabic_poetry_db` exists

### JDBC Driver Not Found
- Make sure mysql-connector-java jar is added to build path
- Check lib folder contains the jar file

### Arabic Text Not Displaying
- Ensure database charset is utf8mb4
- Verify connection URL includes: `characterEncoding=UTF-8`

## Notes

- Keep the project structure clean and simple
- All database operations use prepared statements (SQL injection protection)
- Passwords are hashed using SHA-256
- Follow the 3-layered architecture pattern for any modifications

# GEMINI.md - Project Overview

## Project Overview

This project is a Spring Boot application designed for learning and testing Spring Boot features, with a specific focus on security using Apache Shiro. The application provides a RESTful API for user authentication and authorization, with different endpoints for public, protected, and admin-only access.

**Key Technologies:**

*   **Backend:** Java 17, Spring Boot
*   **Security:** Apache Shiro
*   **Build:** Maven

**Architecture:**

The project is a modular monolith with a `shiro` module that encapsulates all security-related functionality. It follows a standard Spring Boot project structure.

*   `shiro/src/main/java/com/lxq/learn/controller/ShiroController.java`: This is the main controller that exposes REST endpoints for login, logout, and accessing different resources.
*   `shiro/src/main/java/com/lxq/learn/config/UserRealm.java`: This is a custom Shiro Realm that defines how to authenticate and authorize users. For demonstration purposes, it uses hardcoded usernames and passwords.
    *   **admin/123456**: Administrator user with access to all resources.
    *   **user/123456**: Regular user with access to protected resources.
*   `pom.xml`: The root Maven configuration file that defines the project structure and dependencies.

## Building and Running

To build and run this project, you will need to have Java 17 and Maven installed.

**Build the project:**

```bash
mvn clean install
```

**Run the application:**

```bash
mvn spring-boot:run -pl shiro
```

The application will start on the default port (8080).

## Development Conventions

*   **Coding Style:** The code follows standard Java conventions.
*   **Testing:** The project includes a `spring-boot-starter-test` dependency, but there are no specific tests implemented yet.
*   **Contribution:** There are no specific contribution guidelines defined.

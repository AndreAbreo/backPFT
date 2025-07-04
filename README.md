# Server Application

## Database Configuration

This project requires database connection parameters to be supplied through environment variables before building or deploying.

Set the following variables in your shell or deployment environment:

- `DB_URL` – JDBC URL to your database (e.g., `jdbc:oracle:thin:@localhost:1521/XE`)
- `DB_USER` – database username
- `DB_PASS` – database password

Example for Unix shells:

```bash
export DB_URL="jdbc:oracle:thin:@localhost:1521/XE"
export DB_USER="myuser"
export DB_PASS="mypassword"
```

Ensure these variables are available to the build process (e.g., Maven) and the application server so that the placeholders in `persistence.xml` are resolved correctly.

# Northwind Commerce

Northwind Commerce is a production-style Spring Boot reference application for demonstrating Oracle-to-PostgreSQL application and database conversion workflows. The application remains intentionally configured as an Oracle client and models an e-commerce platform with operational, financial, fulfillment, procurement, workforce, and reporting concerns.

## Technology

- Java 17
- Spring Boot 3.2
- Spring MVC and Bean Validation
- Spring Data JPA with Oracle sequences and native SQL
- Oracle JDBC and PL/SQL package calls
- Public demonstration UI and REST APIs
- Spring Boot Actuator and OpenAPI
- JUnit 5, Mockito, AssertJ, and JaCoCo

## Functional Areas

- Customer profiles, addresses, preferences, and loyalty
- Categories, products, attributes, images, and bundles
- Price lists, promotions, rules, and coupons
- Warehouses, inventory, reservations, and stock movements
- Shopping carts and order lifecycle management
- Payments, payment attempts, refunds, and fraud review
- Shipments, tracking events, returns, and dispositions
- Suppliers, purchase orders, and goods receipts
- Employees, reporting hierarchies, jobs, departments, and locations
- Operational auditing and management reporting

## Oracle Assumptions

The application expects objects in the `ecomm` Oracle schema. It uses:

- Schema-qualified JPA table mappings
- Schema-qualified Oracle sequences
- PL/SQL package procedures and functions through JDBC callable statements
- Oracle-specific native SQL including `NVL`, `CONNECT BY`, and `ORDER SIBLINGS BY`
- Database-managed auditing and workflow behavior

Hibernate schema generation is disabled. Database objects must exist before the application starts.

Database-specific behavior is intentionally localized. JPA mappings, Spring Data repositories, and Oracle gateway adapters own schema names, sequences, native SQL, and PL/SQL calls. Controllers, request/response models, mappers, and business-rule orchestration do not issue SQL or JDBC calls directly.

### Code-conversion surface

The reference baseline deliberately contains 26 Java files that require application-code conversion:

- 16 repositories with embedded Oracle native SQL
- 9 Oracle package gateway adapters
- 1 shared Oracle callable-statement executor

The embedded queries exercise `NVL`, `DECODE`, `INSTR`, `LISTAGG`, `JSON_VALUE`, `TRUNC`, `SYSDATE`, `SYSTIMESTAMP`, `NUMTODSINTERVAL`, `ROWNUM`, `FETCH FIRST`, `FOR UPDATE SKIP LOCKED`, and `FOR UPDATE NOWAIT`. All values remain parameter-bound; no SQL concatenation is used.

## Oracle Database Setup

Run both scripts through SQL Developer **Run Script (F5)**, SQLcl, or SQL*Plus while connected directly as `ECOMM`:

1. [`database/oracle/test_case.sql`](./database/oracle/test_case.sql) creates the complete schema.
2. [`database/oracle/data.sql`](./database/oracle/data.sql) inserts a deterministic 20-row parity fixture into every physical table and refreshes the reporting materialized view.

The data script uses fixed ID ranges and a fixed UTC timestamp so an equivalent PostgreSQL fixture can produce the same business data. It requires empty application tables and stops without deleting or replacing existing data. It validates all table and reporting-object row counts before committing.

## Required Environment Variables

| Variable | Description |
|---|---|
| `DB_URL_ORCL` | Oracle JDBC URL |
| `DB_USERNAME` | Oracle login user with access to the `ecomm` schema |
| `DB_PASSWORD` | Oracle login password |

Example PowerShell setup:

```powershell
$env:DB_URL_ORCL = "jdbc:oracle:thin:@//localhost:1521/ORCLPDB1"
$env:DB_USERNAME = "ecomm"
$env:DB_PASSWORD = "<password>"
```

VS Code terminals inherit environment variables when the terminal process is created. After changing a user-level variable, open a new terminal or reload the persisted values into the current terminal:

```powershell
$env:DB_URL_ORCL = [Environment]::GetEnvironmentVariable("DB_URL_ORCL", "User")
$env:DB_USERNAME = [Environment]::GetEnvironmentVariable("DB_USERNAME", "User")
$env:DB_PASSWORD = [Environment]::GetEnvironmentVariable("DB_PASSWORD", "User")
```

Oracle connection establishment is bounded to 10 seconds so an unreachable listener fails clearly instead of appearing to hang.

## Build and Test

```powershell
mvn clean verify
```

The JaCoCo HTML report is generated under `target/site/jacoco/`.
The build enforces an intentionally narrow 18–22% line-coverage band for the initial reference baseline.

## Run

```powershell
mvn spring-boot:run
```

After startup:

- Operations console: `http://localhost:8080/ui/dashboard`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- Health: `http://localhost:8080/actuator/health`

All REST API and server-rendered UI endpoints are public for demonstration and conversion-tooling use. No login is required.

The operations console provides a responsive light-theme dashboard plus searchable/editable views for customers, products, inventory, orders, and payments. The dashboard includes a live payment-method mix donut and chronological order-revenue line chart derived through the existing application services. UI controllers do not issue SQL or call repositories directly.

## PostgreSQL Conversion Output

PostgreSQL conversion SQL is stored separately under `database/postgresql/`. It is not loaded by Spring Boot and does not change the application's Oracle runtime assumptions.

[`database/postgresql/data.sql`](./database/postgresql/data.sql) implements the PostgreSQL side of `ECOMM_PARITY_V1`. It loads the same deterministic IDs, timestamps, text, numeric values, and relationships into the 47 currently available application tables. The seven intentional conversion gaps remain uncreated and unseeded, so PostgreSQL parity is explicitly reported as incomplete by design. The legacy `employees2` table is not part of the parity contract and is left untouched.

The PostgreSQL fixture transactionally truncates the 47 parity tables, resets their sequences, loads the deterministic fixture, validates every seeded table at 20 rows, and commits only after validation succeeds. It deliberately avoids `CASCADE` and leaves the legacy `employees2` table untouched.

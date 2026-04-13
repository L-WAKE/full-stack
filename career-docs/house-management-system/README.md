# House Management System MVP

## Structure

- `frontend/house-admin`: Vue 3 admin console
- `backend/house-service`: Spring Boot backend service
- `career-docs/house-management-system`: SQL, deployment and delivery docs

## Completed MVP Scope

- Login and logout
- Dashboard overview and trend
- Whole rent, shared rent and centralized house management
- Tenant management
- Landlord management
- Maintenance order management
- Cleaning order management
- Employee, role and menu management
- Menu and button permission model placeholder

## Local Start

### Environment

Maven, MySQL and Redis are configured on this machine:

- Maven: `E:\project\study\all-project\tools\apache-maven-3.9.9`
- MySQL: `C:\Program Files\MySQL\MySQL Server 8.4`
- Redis: `C:\Program Files\Redis`
- MySQL database: `house_management`
- MySQL app user: `house_admin`
- MySQL app password: `House@123456`

Start the local database/cache environment:

```powershell
powershell -ExecutionPolicy Bypass -File E:\project\study\all-project\tools\start-house-env.ps1
```

### Frontend

```bash
cd frontend/house-admin
npm install
npm run dev
```

### Backend

```bash
cd backend/house-service
mvnw.cmd spring-boot:run
```

Package an executable jar:

```bash
cd backend/house-service
mvnw.cmd clean -DskipTests package
java -jar target/house-service-0.0.1-SNAPSHOT.jar
```

Run backend smoke tests against an already started local service:

```powershell
powershell -ExecutionPolicy Bypass -File E:\project\study\all-project\tools\smoke-test-house-service.ps1 -UseExistingServer
```

Demo account:

- username: `admin`
- password: `Admin@123`

## Deployment

1. Build frontend with `npm run build`
2. Package backend with `mvn -DskipTests package`
3. Use `career-docs/house-management-system/docker-compose.yml` as a base deployment template
4. Use `career-docs/house-management-system/nginx.conf` for reverse proxy and SPA routing

## Current Technical Notes

- Backend data is persisted with MySQL through MyBatis-Plus.
- Login sessions are stored in Redis under the `house:auth:token:*` key prefix.
- Interface shape already follows `/api` prefix and unified `code/message/data` response.
- MySQL schema is created from `backend/house-service/src/main/resources/schema.sql`.
- Demo data is seeded by `DataInitializer` on application startup when tables are empty.

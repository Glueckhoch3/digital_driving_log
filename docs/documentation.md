# Digital Driving Log - User Stories and Project Documentation

## Table of Contents
1. [Project Overview](#project-overview)
2. [System Architecture](#system-architecture)
3. [User Stories](#user-stories)
4. [Technical Stack](#technical-stack)
5. [Database Design](#database-design)
6. [API Endpoints](#api-endpoints)
7. [Installation and Setup](#installation-and-setup)
8. [Development Guidelines](#development-guidelines)

---

## Project Overview

**Digital Driving Log** is a web-based application designed to digitalize driving logs for shared vehicles among multiple shareholders. It eliminates the need for manual logbooks and Excel spreadsheets by providing a centralized platform to track driving distances, fuel consumption, and expenses.

### Key Features
- **Centralized Driving Log**: Track all drives in one place
- **Cost Distribution**: Automatically calculate and distribute costs among shareholders
- **Multiple Cost Types**: Support for fixed costs (insurance) and variable costs (fuel, maintenance)
- **Local Network Support**: Deployed on local intranet for secure data sharing
- **Year-based Distribution**: Automatic annual cost settlements
- **Shareholder Management**: Add/remove participants with flexible cost options
- **Real-time Sync**: Synchronize data with server when on the same network

---

## System Architecture

### Overview Diagram
```
┌───────────────────────────────────────────────────────────────┐
│                    SHARED VEHICLE SYSTEM                      │
├───────────────────────────────────────────────────────────────┤
│                                                               │
│  ┌──────────────────┐            ┌──────────────────┐         │
│  │   Angular App    │ ◄────────► │  Spring Boot API │         │
│  │   (Frontend)     │  (HTTP)    │   (Backend)      │         │
│  ├──────────────────┤            ├──────────────────┤         │
│  │ • Drive Entry    │            │ • REST API       │         │
│  │ • Fuel Tracking  │            │ • Data Validation│         │
│  │ • Cost Overview  │            │ • Cost Calc.     │         │
│  │ • SCSS Styling   │            │ • Business Logic │         │
│  └──────────────────┘            ├──────────────────┤         │
│                                  │  PostgreSQL DB   │         │
│                                  │                  │         │
│                                  │ • Drives         │         │
│                                  │ • Shareholders   │         │
│                                  │ • Costs          │         │
│                                  │ • Fuel Records   │         │
│                                  └──────────────────┘         │
│                                                               │
└───────────────────────────────────────────────────────────────┘
```

### Technology Stack
- **Frontend**: Angular 21 with TypeScript, SCSS for styling
- **Backend**: Spring Boot 4.0.1 with Java 21
- **Database**: PostgreSQL
- **Build Tools**: Maven (Backend), Angular CLI (Frontend)
- **Architecture**: RESTful API architecture with MVC pattern on frontend

---

## User Stories

### Epic 1: Drive Logging

#### US-001: Enter Driving Distance
**As a** driver of the shared vehicle  
**I want to** enter the distance I drove  
**So that** the total kilometers/miles are recorded for cost distribution  

**Acceptance Criteria:**
- [ ] User can access a form to enter driving distance
- [ ] Form validates that distance is a positive number
- [ ] User can specify the date of the drive
- [ ] User can add optional notes about the drive
- [ ] Data is saved to the database
- [ ] User receives confirmation of successful submission

**Technical Requirements:**
- Create REST endpoint: `POST /api/drives`
- Create Angular form component with validation
- Add distance validation (must be > 0)
- Implement error handling and user feedback

---

#### US-002: Track Fuel Refills
**As a** driver  
**I want to** record when I refill the fuel tank  
**So that** fuel consumption and costs are accurately tracked  

**Acceptance Criteria:**
- [ ] User can add fuel refill information to a drive
- [ ] Form captures: refill date, liters/gallons, cost, fuel type
- [ ] System calculates cost per liter
- [ ] Fuel data is linked to the corresponding drive
- [ ] Historical fuel data is displayed in a list

**Technical Requirements:**
- Create REST endpoint: `POST /api/fuel-refills`
- Create Fuel refill form component
- Add validation for volumes and costs
- Implement calculations for fuel efficiency metrics
- Create database schema for fuel tracking

---

#### US-003: View Driving History
**As a** shareholder  
**I want to** view all recorded drives in the system  
**So that** I can verify accuracy and track overall vehicle usage  

**Acceptance Criteria:**
- [ ] User can see a list of all drives
- [ ] List displays: date, driver, distance, notes
- [ ] List is sortable by date, driver, or distance
- [ ] User can filter drives by date range
- [ ] User can filter drives by specific driver
- [ ] Total distance is displayed

**Technical Requirements:**
- Create REST endpoint: `GET /api/drives`
- Implement pagination for large datasets
- Add filtering and sorting capabilities
- Create table/list component with responsive design
- Implement search functionality

---

### Epic 2: Expense Management

#### US-004: Record Fixed Costs
**As a** vehicle owner/shareholder  
**I want to** record fixed costs like insurance and registration  
**So that** these costs can be distributed equally among all shareholders  

**Acceptance Criteria:**
- [ ] User can enter fixed cost information
- [ ] Form captures: cost type, amount, date, description
- [ ] Fixed costs are marked as non-divisible by distance
- [ ] Cost is automatically distributed equally among active shareholders
- [ ] Users can view how much they owe for fixed costs

**Technical Requirements:**
- Create REST endpoint: `POST /api/costs/fixed`
- Create fixed cost form component
- Implement cost distribution algorithm
- Add audit trail for cost entries
- Create reporting views

---

#### US-005: Record Variable Costs
**As a** driver  
**I want to** record variable costs like maintenance and repairs  
**So that** these costs are distributed proportionally to distance driven  

**Acceptance Criteria:**
- [ ] User can enter variable cost information
- [ ] Form captures: cost type, amount, date, description, affected distance period
- [ ] Variable costs are distributed based on distance driven
- [ ] System calculates cost per kilometer/mile
- [ ] Users can view how much they owe for variable costs
- [ ] Cost allocation is transparent and visible

**Technical Requirements:**
- Create REST endpoint: `POST /api/costs/variable`
- Create variable cost form component
- Implement proportional distribution algorithm
- Add visualizations for cost allocation
- Create detailed cost breakdown reports

---

#### US-006: View Cost Distribution Overview
**As a** shareholder  
**I want to** see a clear overview of how much each participant owes/is owed  
**So that** cost settlements can be made fairly and transparently  

**Acceptance Criteria:**
- [ ] Dashboard displays current cost distribution
- [ ] Shows each shareholder's total balance
- [ ] Breaks down costs by type (fixed, variable, fuel)
- [ ] Displays each shareholder's driven distance contribution
- [ ] Shows the current settlement period (year)
- [ ] Data updates in real-time

**Technical Requirements:**
- Create REST endpoint: `GET /api/distribution/current`
- Create dashboard component with charts
- Implement calculation service for cost summaries
- Add visual representations (charts, tables)
- Implement real-time updates using WebSockets or polling

---

### Epic 3: Shareholder Management

#### US-007: Add New Shareholder
**As a** vehicle owner  
**I want to** add new participants to the shared vehicle  
**So that** they can start recording their drives and participating in cost distribution  

**Acceptance Criteria:**
- [ ] Admin can access shareholder management section
- [ ] Form captures: name, email, start date, participant type
- [ ] New shareholders can be set as temporary or permanent
- [ ] System validates email format
- [ ] New shareholder is added to the current settlement period
- [ ] Email confirmation is sent (optional future feature)

**Technical Requirements:**
- Create REST endpoint: `POST /api/shareholders`
- Create shareholder form component
- Implement validation for duplicate shareholders
- Add email validation
- Create participant type enum (permanent, temporary)

---

#### US-008: Remove Shareholder
**As a** vehicle owner  
**I want to** remove shareholders from the vehicle  
**So that** they no longer participate in cost distribution  

**Acceptance Criteria:**
- [ ] Admin can select and remove a shareholder
- [ ] System calculates final balance for removed shareholder
- [ ] Removed shareholder's data is archived
- [ ] Future costs exclude the removed shareholder
- [ ] Confirmation dialog prevents accidental deletion

**Technical Requirements:**
- Create REST endpoint: `DELETE /api/shareholders/{id}`
- Implement soft delete to preserve data history
- Create confirmation dialog in UI
- Implement settlement calculation before removal
- Add audit logging

---

#### US-009: Configure Temporary Participants
**As a** vehicle owner  
**I want to** configure temporary participants who only pay variable costs  
**So that** short-term participants don't bear fixed costs  

**Acceptance Criteria:**
- [ ] Temporary participants can be marked when added
- [ ] Temporary participants don't share fixed costs
- [ ] Temporary participants share variable costs proportionally
- [ ] End date can be set for temporary participants
- [ ] System automatically excludes them after end date

**Technical Requirements:**
- Add participant type field to shareholder model
- Implement conditional logic for cost distribution
- Create UI toggle for temporary/permanent participant
- Add date picker for temporary participant end date
- Update cost calculation algorithms

---

### Epic 4: Settlement and Reporting

#### US-010: Annual Settlement
**As a** a vehicle owner  
**I want to** the system automatically closes the current settlement period at year end  
**So that** a new settlement period begins for the next year  

**Acceptance Criteria:**
- [ ] System automatically triggers settlement on December 31st
- [ ] Final balances are calculated and recorded
- [ ] Settlement history is preserved and viewable
- [ ] New year settlement period is created
- [ ] Notification is sent to all shareholders
- [ ] Option to manually trigger settlement (admin only)

**Technical Requirements:**
- Implement scheduled task for annual settlement
- Create settlement history table
- Implement settlement status tracking
- Add manual settlement trigger endpoint
- Create settlement report generation

---

#### US-011: View Settlement History
**As a** shareholder  
**I want to** view past settlement periods and their final results  
**So that** I can verify historical costs and payments  

**Acceptance Criteria:**
- [ ] User can view all past settlement periods
- [ ] Each settlement shows: period, final balances, payment status
- [ ] User can view detailed breakdown of costs for each period
- [ ] Settlement documents can be exported (PDF, CSV)
- [ ] Payment receipts can be tracked

**Technical Requirements:**
- Create REST endpoint: `GET /api/settlements`
- Create settlement history component
- Implement export functionality (PDF, CSV)
- Add detailed settlement reports
- Create audit trail for settlements

---

#### US-012: Generate Cost Reports
**As a** a shareholder  
**I want to** generate detailed reports about costs  
**So that** I can understand how my costs are calculated  

**Acceptance Criteria:**
- [ ] User can generate reports for a date range
- [ ] Reports show: total distance, fixed costs, variable costs, fuel costs
- [ ] Reports include cost breakdown by type
- [ ] Reports show individual vs. shareholder-wide costs
- [ ] Reports can be exported in multiple formats

**Technical Requirements:**
- Create REST endpoints for report generation
- Implement report generation service
- Create report export functionality (PDF, CSV, Excel)
- Add filtering options for reports
- Implement caching for performance

---

### Epic 5: Data Synchronization

#### US-013: Sync with Server on Network Connection
**As a** a driver with the app offline  
**I want to** automatically sync my data when reconnecting to the network  
**So that** my recorded drives and expenses are saved to the server  

**Acceptance Criteria:**
- [ ] App detects network availability
- [ ] Offline data is queued locally
- [ ] Automatic sync occurs when connection is restored
- [ ] Sync status is visible to the user
- [ ] Conflicts are handled gracefully
- [ ] User is notified of successful sync

**Technical Requirements:**
- Implement service worker for offline support
- Create local storage mechanism for offline data
- Implement sync queue management
- Add conflict resolution strategy
- Create sync status indicators in UI

---

#### US-014: Conflict Resolution for Duplicate Entries
**As a** the system  
**I want to** handle conflicts when the same data is modified offline and online  
**So that** data integrity is maintained  

**Acceptance Criteria:**
- [ ] System detects conflicting changes
- [ ] Latest timestamp wins strategy is applied
- [ ] User is notified of conflicts
- [ ] Detailed logs of conflicts are maintained
- [ ] Manual resolution is available if needed

**Technical Requirements:**
- Implement conflict detection logic
- Add versioning to database records
- Create conflict resolution UI component
- Implement audit logging
- Add conflict reporting

---

## Technical Stack

### Frontend
- **Framework**: Angular 21
- **Language**: TypeScript
- **Styling**: SCSS (Sass)
- **Package Manager**: npm 10.8.2
- **Build Tool**: Angular CLI with Vite
- **Key Dependencies**:
  - `@angular/common` - Common Angular utilities
  - `@angular/forms` - Reactive and template-driven forms
  - `@angular/platform-browser` - Browser platform module
  - `@angular/router` - Routing functionality

### Backend
- **Framework**: Spring Boot 4.0.1
- **Language**: Java 21
- **Build Tool**: Maven
- **Database**: PostgreSQL
- **Key Dependencies**:
  - spring-boot-starter-validation
  - spring-boot-starter-web (likely)
  - spring-boot-starter-data-jpa (likely)
  - PostgreSQL JDBC driver

### Development Environment
- **OS**: Linux/Cross-platform
- **IDE Recommendation**: VS Code (Frontend), IntelliJ IDEA (Backend)
- **Version Control**: Git

---

## Database Design

For database schema, see [digitalDriveLog-database.dbml](digitalDriveLog-database.dbml)

---

## API Endpoints

Current frontend and backend integrations use the base path:

- `http://localhost:8080/ddl/api`

### Users
- `GET /users` - List all users
- `POST /users` - Create user
- `GET /users/{userId}` - Get user by ID
- `PUT /users/{userId}` - Update user
- `DELETE /users/{userId}` - Delete user

### Vehicles
- `GET /vehicles` - List all vehicles
- `POST /vehicles` - Create vehicle
- `GET /vehicles/{carId}` - Get vehicle by ID
- `PUT /vehicles/{carId}` - Update vehicle
- `DELETE /vehicles/{carId}` - Delete vehicle

### Drives
- `POST /drives` - Create drive
- `GET /drives/{driveId}` - Get drive by ID
- `PUT /drives/{driveId}` - Update drive
- `DELETE /drives/{driveId}` - Delete drive
- `GET /vehicles/{carId}/drives` - List drives for one vehicle
- `GET /users/{userId}/drives` - List drives for one user
- `GET /vehicles/{carId}/users/{userId}/drives` - List drives filtered by vehicle and user

### Costs
- `GET /costs` - List all costs
- `POST /costs` - Create cost
- `GET /costs/{costId}` - Get cost by ID
- `PUT /costs/{costId}` - Update cost
- `DELETE /costs/{costId}` - Delete cost
- `GET /vehicles/{carId}/costs` - List costs for one vehicle
- `GET /users/{userId}/costs` - List costs for one user

### Request/response shape highlights
- `CreateDriveRequest`/`UpdateDriveRequest`: `carId`, `currentMileage`, `driverId`, `driveDate`, optional `notes`
- `CreateCostRequest`/`UpdateCostRequest`: `carId`, `buyerId`, `transactionObject`, `price`, `amount`, `dayOfTransaction`, `costType`, optional `notes`
- `costType` accepts `fixed` or `variable` (case-insensitive); responses return enum values in uppercase

### Frontend routes
- `/` - Start page
- `/overview` - Management overview
- `/cars/select` - Car selection
- `/cars/{carId}` - Car workspace
- `/manage/users` - User management (create/update/delete)
- `/manage/cars` - Car management (create/update/delete)

---

## Installation and Setup

### Prerequisites
- Java 21 or higher
- Node.js 18+ and npm 10.8.2+
- Docker / Docker Compose
- Git

### Development Start Guide (Backend + Frontend, Dockerized PostgreSQL)

1. **Start PostgreSQL only (recommended for development)**
   ```bash
   docker compose up -d postgres
   ```

2. **Configure backend environment**
   ```bash
   cd backend
   cp .env.example .env
   ```

3. **Build and run backend**
   ```bash
   ./mvnw clean package
   ./mvnw spring-boot:run
   ```
   Backend API: `http://localhost:8080`

4. **Start frontend**
   ```bash
   cd ../frontend
   npm install
   npm start
   ```
   Frontend app: `http://localhost:4200`

### Full Docker Compose Setup
Use the committed `docker-compose.yml` at repository root:

```bash
docker compose up --build -d
```

This starts:
- PostgreSQL: `localhost:5432`
- Backend API: `http://localhost:8080`
- Frontend app: `http://localhost:4200`

### First-run functional flow
1. Open `/manage/users` and create at least one user.
2. Open `/manage/cars` and create a car with an owner.
3. Deleting users/cars is blocked when dependent entities exist (cars/drives/costs).

---

## Development Guidelines

### Code Style
- **Frontend**:
  - Follow Angular style guide
  - Use TypeScript strict mode
  - Format with Prettier (prettier config in package.json)
  - Use SCSS for styling with BEM naming convention

- **Backend**:
  - Follow Google Java Style Guide
  - Use meaningful variable names
  - Document public methods with Javadoc
  - Keep methods small and focused

### Folder Structure

**Frontend** (`frontend/src/`):
```
src/
├── app/
│   ├── components/          # Reusable UI components
│   │   ├── drive-form/
│   │   ├── cost-form/
│   │   └── ...
│   ├── services/           # API and business logic services
│   │   ├── drive.service.ts
│   │   ├── cost.service.ts
│   │   └── ...
│   ├── models/             # TypeScript interfaces and models
│   ├── guards/             # Route guards
│   └── pipes/              # Custom Angular pipes
├── styles/                 # Global SCSS files
└── assets/                 # Images, fonts, etc.
```

**Backend** (`backend/src/main/java/de/digidrivelog/`):
```
├── models/                 # JPA entities
├── repositories/           # Spring Data repositories
├── services/              # Business logic
├── controllers/           # REST endpoints
├── dto/                   # Data transfer objects
├── exceptions/            # Custom exceptions
├── config/                # Spring configurations
└── utils/                 # Utility classes
```

### Git Workflow
- Create feature branches from `develop`
- Commit messages: `feat:`, `fix:`, `docs:`, `refactor:`, `test:`
- Pull requests require code review
- Merge to `main` for releases only

### Testing
- Backend: JUnit 5 + Mockito
- Frontend: Jasmine + Karma
- Aim for >80% code coverage

### Documentation
- Update this file for architectural changes
- Document API changes in endpoint descriptions
- Add comments for complex logic
- Maintain DBML schema documentation

---

## Future Enhancements

- [ ] Authentication and authorization system
- [ ] Mobile app support
- [ ] Payment integration for settlements
- [ ] Email notifications for reminders
- [ ] Integration with fuel APIs for real-time prices
- [ ] Machine learning for anomaly detection
- [ ] Multi-vehicle support
- [ ] Advanced analytics and insights

---

## Contact and Support

For questions or issues, please refer to the main [README.md](../README.md) and the project documentation.

---

**Last Updated**: February 2026  
**Project Version**: 0.0.1-SNAPSHOT  
**License**: See LICENSE file

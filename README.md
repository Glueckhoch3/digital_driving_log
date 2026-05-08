# Digital Driving Log

> **A comprehensive digital solution for managing shared vehicle driving logs and cost distribution**

[![License](https://img.shields.io/badge/license-MIT-blue.svg)](LICENSE)
[![Java Version](https://img.shields.io/badge/Java-21+-orange.svg)](backend/pom.xml)
[![Angular Version](https://img.shields.io/badge/Angular-21-red.svg)](frontend/package.json)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-12+-blue.svg)](backend/src/main/resources/application.properties)

## Quick Overview

**Digital Driving Log** is a modern web application designed to replace manual driving logs and Excel spreadsheets. It provides a centralized platform for shared vehicle participants to track drives, manage expenses, and automatically calculate fair cost distribution.

### Perfect For
- Family car sharing
- Carpool groups
- Fleet management (personal use)

---

## Table of Contents
- [Key Features](#key-features)
- [Project Structure](#project-structure)
- [Technology Stack](#technology-stack)
- [Quick Start](#quick-start)
- [Architecture](#architecture)
- [Documentation](#documentation)
- [Development](#development)
- [Roadmap](#roadmap)
- [License](#license)

---

## Key Features

### Drive Logging
- Record driven distances with date and optional notes
- Track fuel refills with cost and consumption data
- View comprehensive driving history with filtering and sorting
- Calculate fuel efficiency metrics

### Cost Management
- **Fixed Costs**: Insurance, registration fees (distributed equally)
- **Variable Costs**: Fuel, maintenance, repairs (distributed by distance)
- **Fuel Tracking**: Automatic cost calculation per drive
- **Real-time Distribution**: View current cost balances anytime

### Shareholder Management
- Add and remove participants flexibly
- Support for temporary participants (variable costs only)
- Automatic cost distribution based on participant type
- Transparent cost breakdown per person

### Settlement & Reporting
- Automatic year-end settlements
- Detailed cost reports and breakdowns
- Export reports in multiple formats (PDF, CSV)
- Settlement history and audit trails
- Individual cost summaries

### Data Synchronization
- Local network (intranet) deployment
- Automatic sync when reconnecting to network
- Offline data queuing
- Conflict resolution for concurrent changes

---

## Project Structure

```
digital_driving_log/
├── backend/                       # Spring Boot backend API
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/de/digidrivelog/
│   │   │   │   ├── models/        # JPA entities
│   │   │   │   ├── controllers/   # REST endpoints
│   │   │   │   ├── services/      # Business logic
│   │   │   │   ├── repositories/  # Data access
│   │   │   │   └── DigidrivelogApplication.java
│   │   │   └── resources/
│   │   │       └── application.properties
│   │   └── test/
│   ├── pom.xml                    # Maven configuration
│   └── mvnw / mvnw.cmd           # Maven wrapper
│
├── frontend/                      # Angular frontend application
│   ├── src/
│   │   ├── app/
│   │   │   ├── components/        # Reusable UI components
│   │   │   ├── services/          # API and business logic
│   │   │   ├── models/            # TypeScript interfaces
│   │   │   ├── app.ts             # Root component
│   │   │   ├── app.routes.ts      # Routing configuration
│   │   │   └── app.scss           # Global styles
│   │   ├── styles.scss            # Global SCSS
│   │   ├── index.html             # HTML entry point
│   │   └── main.ts                # Application bootstrap
│   ├── angular.json               # Angular configuration
│   ├── package.json               # npm dependencies
│   ├── tsconfig.json              # TypeScript configuration
│   └── README.md                  # Frontend specific docs
│
├── docs/                          # Documentation
│   ├── digitalDriveLog-database.dbml  # Database schema
│   └── digitalDriveLog-database.json  # Database export
│
├── docs/documentation.md              # Detailed project documentation
├── README.md                      # This file
├── LICENSE                        # Project license
└── .github/                       # GitHub configuration
    └── copilot-instructions.md    # Copilot guidelines
```

---

## Technology Stack

### Backend
| Technology | Version | Purpose |
|-----------|---------|---------|
| Java | 21+ | Programming language |
| Spring Boot | 4.0.1 | Web framework & API |
| Spring Data JPA | Latest | Data access & ORM |
| PostgreSQL | 12+ | Relational database |
| Maven | 3.8+ | Build & dependency management |

### Frontend
| Technology | Version | Purpose |
|-----------|---------|---------|
| Angular | 21 | Web framework |
| TypeScript | Latest | Programming language |
| SCSS | Latest | Styling & theming |
| npm | 10.8.2+ | Package management |
| Vite | Latest | Build tool |

### Database
| Entity | Purpose |
|--------|---------|
| Shareholders | Track vehicle participants |
| Drives | Log individual drives |
| Fuel Refills | Track fuel consumption & costs |
| Fixed Costs | Insurance, registration, etc. |
| Variable Costs | Fuel, maintenance, repairs |
| Settlements | Year-end settlements & history |

---

## Quick Start

### Prerequisites
- **Java 21+** - [Download](https://www.oracle.com/java/technologies/downloads/)
- **Node.js 18+** - [Download](https://nodejs.org/)
- **PostgreSQL 12+** - *will be deployed as docker container; **tbd***
- **Git** - [Download](https://git-scm.com/)

### Backend Setup (5 minutes)

1. **Create PostgreSQL Database**
   ```bash
   psql -U postgres
   CREATE DATABASE digital_driving_log;
   \q
   ```

2. **Configure Database Connection**
   ```bash
   cd backend
   nano src/main/resources/application.properties
   ```
   Update with your PostgreSQL credentials:
   ```properties
   spring.datasource.url=jdbc:postgresql://localhost:5432/digital_driving_log
   spring.datasource.username=postgres
   spring.datasource.password=your_password
   spring.jpa.hibernate.ddl-auto=update
   ```

3. **Build & Run**
   ```bash
   ./mvnw clean install
   ./mvnw spring-boot:run
   ```
   API will be available at `http://localhost:8080`

### Frontend Setup (5 minutes)

1. **Install Dependencies**
   ```bash
   cd frontend
   npm install
   ```

2. **Start Development Server**
   ```bash
   npm start
   ```
   Application will be available at `http://localhost:4200`

3. **Build for Production**
   ```bash
   npm run build
   ```
   Output will be in `frontend/dist/`

### Full Local Deployment with Docker

```bash
docker-compose up -d
```

This will start:
- PostgreSQL (port 5432)
- Backend API (port 8080)
- Frontend (port 80)

---

## Architecture

### System Design
```
┌────────────────────────────────────────────┐
│         USER INTERFACE (Angular)           │
│  • Drive Entry Form                        │
│  • Cost Management Dashboard               │
│  • Settlement Overview                     │
│  • Reports & Analytics                     │
└─────────────┬──────────────────────────────┘
               │ HTTP/REST
┌──────────────▼────────────────────────────┐
│      BACKEND API (Spring Boot)            │
│  • Drive Controller                       │
│  • Cost Distribution Service              │
│  • Settlement Engine                      │
│  • Report Generator                       │
└──────────────┬────────────────────────────┘
               │ JDBC/JPA
┌──────────────▼────────────────────────────┐
│    DATABASE (PostgreSQL)                  │
│  • Shareholders, Drives, Costs            │
│  • Fuel Records, Settlements              │
│  • Audit Trails                           │
└───────────────────────────────────────────┘
```

### Key Design Patterns
- **MVC Pattern**: Separation of concerns (Model-View-Controller)
- **RESTful API**: Standard HTTP methods for resource operations
- **Service Layer**: Business logic separated from controllers
- **Repository Pattern**: Data access abstraction
- **Dependency Injection**: Loose coupling with Spring IoC

---

## Documentation

### Comprehensive Guides
- [**User Stories & Project Documentation**](docs/documentation.md) - Complete feature specifications, user stories, and development guidelines
- [**Database Schema**](docs/digitalDriveLog-database.dbml) - DBML format database design
- [**Frontend README**](frontend/README.md) - Angular specific documentation

### API Documentation
All REST API endpoints are documented in [docs/documentation.md#api-endpoints](docs/documentation.md#api-endpoints)

### Code Examples

**Record a Drive (Backend)**
```java
// POST /ddl/api/drives
{
  "carId": 1,
  "currentMileage": 152340,
  "driverId": 2,
  "driveDate": "2026-02-06",
  "notes": "Commute to office"
}
```

**Record a Drive (Frontend)**
```typescript
// In drive.service.ts
createDrive(request: CreateDriveRequest): Observable<DriveDto> {
  return this.http.post<DriveDto>(`${this.apiUrl}/drives`, request);
}
```

---

## Development

### Getting Started
1. Clone the repository
2. Follow [Quick Start](#quick-start) section
3. Read [docs/documentation.md](docs/documentation.md) for detailed requirements

### Code Style
- **Backend**: Follow [Google Java Style Guide](https://google.github.io/styleguide/javaguide.html)
- **Frontend**: Follow [Angular Style Guide](https://angular.io/guide/styleguide)
- **Styling**: SCSS with BEM naming convention

### Testing
```bash
# Backend
cd backend
./mvnw test

# Frontend
cd frontend
npm test -- --watch=false
```

### Building
```bash
# Backend (JAR file)
cd backend
./mvnw clean package

# Frontend (optimized build)
cd frontend
npm run build -- --configuration production
```

---

## Roadmap

### Current Status: Alpha (v0.0.1)
- [X] Project structure
- [X] Database schema
- [ ] Core drive logging
- [ ] Cost distribution algorithms
- [ ] Basic UI/UX

### Short Term (Q1-Q2 2026)
- [ ] Complete CRUD operations for all entities
- [ ] Cost distribution algorithms (fixed & variable)
- [ ] Dashboard with real-time updates
- [ ] Settlement generation
- [ ] Report exports (PDF, CSV)

### Medium Term (Q3-Q4 2026)
- [ ] Authentication & authorization
- [ ] Email notifications
- [ ] Mobile-responsive design enhancements
- [ ] Advanced analytics
- [ ] Payment integration

### Long Term (2027+)
- [ ] Mobile app (React Native/Flutter)
- [ ] GPS tracking integration (optional)
- [ ] Machine learning insights
- [ ] Multi-vehicle support
- [ ] Public deployment capabilities

---

## Contributing

Contributions are welcome! Please follow these steps:

1. Create a feature branch (`git checkout -b feature/your-feature`)
2. Commit your changes (`git commit -am 'Add your feature'`)
3. Push to the branch (`git push origin feature/your-feature`)
4. Create a Pull Request with a clear description

### Reporting Issues
Please use GitHub Issues to report bugs. Include:
- Clear description of the issue
- Steps to reproduce
- Expected vs actual behavior
- System information

---

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

---

## Support & Contact

For questions, issues, or suggestions:
- Issues: [GitHub Issues](../../issues)
- Discussions: [GitHub Discussions](../../discussions)

---

**Last Updated**: February 6, 2026  
**Project Version**: 0.0.1-SNAPSHOT  
**Maintained By**: [Glueckhoch3]

*This project is done with the help of Copilot and serves the purpos of creating experience in softwaredevelopment.*
*Any issues or discussions might be put on hold until I consider the project as implemented with basic functionality to give myself the chance of learning.*

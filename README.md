# 🚌 Bus Ticket Booking System – Microservices Project

A **Bus Ticket Booking System** built using **Spring Boot Microservices**. The system supports two primary roles: **Admin** and **Passenger (User)**. Each role has dedicated services with secure access using **JWT-based Authentication** and **Spring Security**. The system also supports **PostgreSQL** for data persistence, **JasperReports** for PDF report generation, and uses **JUnit 5**, **Mockito**, and **JaCoCo** for testing and code coverage.

---

## ⚙️ Technology

| Category        | Technology                          |
|----------------|--------------------------------------|
| Language        | Java 17+                            |
| Framework       | Spring Boot, Spring Cloud           |
| Security        | Spring Security, JWT                |
| Microservices   | Eureka Server, API Gateway          |
| Databases       | PostgreSQL                          |
| Frontend        | Thymeleaf(HTML), Bootstrap CSS, JS  |
| PDF Reports     | JasperReports                       |
| Testing         | JUnit 5, Mockito                    |
| Code Coverage   | JaCoCo                              |
| Build Tool      | Maven                               |
| REST Client     | RestTemplate                        |

---

## 🧱 Microservices Overview

### 1. Admin Microservice

**Features:**
- JWT-secured Admin login
- Add/View/Update/Delete/Cancel Bus
- Add/View/Update/Delete Bus Schedule
- Add/View/Update/Delete Bus Routes
- View All Passengers
- View All Bookings
- Generate Bus-wise Booking Reports (PDF via JasperReports)

### 2. User (Passenger) Microservice

**Features:**
- Register and Login with JWT
- Search Available Buses by Route/Date
- Book Bus Tickets
- View Booked Tickets
- Generate PDF Ticket Report
- Update Profile and Password

---

## 🧱Microservice Architecture
                   +----------------+
                   |  Eureka Server |
                   +--------+-------+
                            |
             +--------------+-------------+
             |                            |
    +--------v--------+          +--------v--------+
    |   Admin Service |          |  User Service   |
    +--------+--------+          +--------+--------+
             \                       /
              \                     /
               \                   /
           +----v-----+    +-------v-----+
           | PostgreSQL|   |  PostgreSQL  |
           +----------+    +-------------+
                    |
           +--------v--------+
           |  API Gateway     |
           +------------------+
           

---

## 🧬 ER Diagram

``` text

+------------------+
|      Bus         |
+------------------+
| bus_id (PK)      |
| bus_number       |
| capacity         |
| bus_type         |
+------------------+
         |
         | 1
         |
         | <——
         |        n
+-----------------------+
|      Schedule         |
+-----------------------+
| schedule_id (PK)      |
| route_id (FK)         |
| bus_id (FK)           |
| departure_time        |
| arrival_time          |
| date                  |
| available_seats       |
| fare                  |
+-----------------------+
         |
         | n
         |
         |——>
         | 1
+------------------------+
|        Route           |
+------------------------+
| route_id (PK)          |
| origin                 |
| destination            |
| distance               |
+------------------------+

         |
         | 1
         |
         |——<
         |        n
+-----------------------+
|      Booking          |
+-----------------------+
| booking_id (PK)       |
| user_id (FK)          |
| schedule_id (FK)      |
| seat_number           |
| booking_date          |
| payment_status        |
| pnr                   |
+-----------------------+
         |
         | n
         |
         |——>
         | 1
+------------------------+
|        User            |
+------------------------+
| user_id (PK)           |
| name                   |
| username               |
| email                  |
| password               |
| phone_no               |
| role = 'customer'      |
+------------------------+
```
---

## 🧪 Testing and Code Coverage

**Run unit test:**
```bash
mvn test
```

**Generate JaCoCo code coverage report:**
```bash
mvn jacoco:report
```

**Run all tests and generate coverage in one step:**
```bash
mvn clean verify
```
**Reports Location:**
```bash
target/site/jacoco/index.html
```

---
## 🔐 JWT Authentication and Security

**Public Endpoints:**

- /api/customer/register
- /api/customer/login,/api/admin/login

**Secured Endpoints (require JWT token):**

- Admin: /api/admin/**
- User: /api/customer/**, /api/booking/**

---

**Token Usage:**
**Include in headers:**

```ardunio
Authorization: Bearer <JWT_TOKEN>
```
---

## 🧾 JasperReports Integration

**Reports Generated:**

- Passenger: PDF Ticket after Booking
- Admin: PDF Report of All Bookings for Selected Bus or Date

**Report Templates Location:**
```css
src/main/resources/reports/
```
---

## 🚀 How to Run the Project

**Start Eureka Server:**

```bash
http://localhost:8761
```

**Start API Gateway:**

```bash
http://localhost:9191
```

**Start Microservices:**

- Admin Service: http://localhost:8081
- User Service: http://localhost:8082

---

## 📁 Folder Structure

``` text
bus-ticket-booking/
├── eureka-server/
├── ApiGateway/
├── AdminService/
│   ├── configuration/
│   ├── controller/
│   ├── dto/
│   ├── service/
│   ├── entity/
│   ├── repository/
├── userService/
│   ├── configuration/
│   ├── controller/
│   ├── dto/
│   ├── service/
│   ├── entity/
│   ├── repository/
│   └── reports/
└── ConfigServer/
```


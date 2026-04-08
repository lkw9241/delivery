### 🚚 Delivery Management System
```

납기 및 배송 상태를 관리하고 지연 위험을 모니터링하기 위한 Delivery / Order 관리 시스템입니다.
Spring Boot 기반으로 주문 상태를 관리하고, 납기 기준으로 리스크 상태를 자동 평가하도록 설계했습니다.

실무에서 발생하는 납기 지연, 입고 관리, 발주 상태 관리 문제를 해결하기 위한 간단한 백엔드 프로젝트입니다.
```
### 📌 Project Overview
```
생산 / 구매 / 물류 환경에서는 발주 이후 다음과 같은 문제가 자주 발생합니다.

발주 진행 상태가 명확하지 않음

납기 지연 여부를 실시간으로 알기 어려움

입고 여부에 따른 리스크 판단이 수동으로 이루어짐

이 프로젝트는 이러한 문제를 해결하기 위해

주문(Order) 관리

진행 상태 관리

납기 기반 리스크 평가

기능을 제공하는 Delivery 관리 API 서버입니다.
```

### 🛠 Tech Stack
```
| Category        | Stack           |
| --------------- | --------------- |
| Language        | Java 17         |
| Framework       | Spring Boot     |
| ORM             | Spring Data JPA |
| Database        | MySQL           |
| Build Tool      | Gradle          |
| Version Control | Git / GitHub    |
| API             | REST API        |
```

### Project Structure

```text
delivery
├─ src
│ ├─ main
│ │ ├─ java
│ │ │ └─ com.example.delivery
│ │ │ ├─ controller
│ │ │ ├─ service
│ │ │ ├─ repository
│ │ │ ├─ domain
│ │ │ └─ config
│ │ └─ resources
│ │ ├─ templates
│ │ └─ application.yml
│ └─ test
└─ build.gradle
```
### Layer Architecture

```text
Controller
   ↓
Service
   ↓
Repository
   ↓
Entity (Domain)
```

### 🎯 Project Goals
```
이 프로젝트는 다음을 학습하기 위해 만들었습니다.

Spring Boot 기반 REST API 설계

JPA Entity 중심 도메인 모델링

Service Layer 비즈니스 로직 관리

납기 기반 리스크 평가 로직 구현

계층형 아키텍처 구조 설계
```
### 🔧 Future Improvements
```
납기 지연 알림 기능

Dashboard UI 추가

협력업체별 납기 통계

API 인증 (JWT)

배치 기반 납기 리스크 자동 업데이트

SAP 연동
```
### 👨‍💻 Author
```
Kwangwon Lee

Backend Developer & Purchasing Team
```
GitHub
https://github.com/lkw9241

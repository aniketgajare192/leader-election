# Leader Election System

A Spring Boot application that implements a Zookeeper-like coordinator for leader election among application pods.

## Architecture

- **1 Coordinator Pod**: Acts like Zookeeper, tracks all application pods and manages leader election
- **5 Application Pods**: Register with coordinator, send heartbeats, and one becomes the leader

## Features

- Coordinator keeps track of all application pods
- Automatic leader election based on pod ID (alphabetically sorted)
- Health checks every 2 seconds - marks pods as inactive if no heartbeat received for 5 seconds
- Automatic re-election when leader goes down
- Application pods print their status every second:
  - Leader pod: "I am leader pod"
  - Other pods: "I am application pod"

## Prerequisites

- Java 17 or higher
- Maven 3.6+
- Docker and Docker Compose (for containerized deployment)

## Running Locally

### Option 1: Using Maven with Spring Profiles

#### 1. Start Coordinator
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=coordinator
```

#### 2. Start Application Pods (in separate terminals)
```bash
# Terminal 1
mvn spring-boot:run -Dspring-boot.run.profiles=pod1

# Terminal 2
mvn spring-boot:run -Dspring-boot.run.profiles=pod2

# Terminal 3
mvn spring-boot:run -Dspring-boot.run.profiles=pod3

# Terminal 4
mvn spring-boot:run -Dspring-boot.run.profiles=pod4

# Terminal 5
mvn spring-boot:run -Dspring-boot.run.profiles=pod5
```

### Option 2: Using Docker Compose

#### 1. Build the application
```bash
mvn clean package
```

#### 2. Start all services
```bash
docker-compose up --build
```

#### 3. View logs
```bash
# View all logs
docker-compose logs -f

# View coordinator logs
docker-compose logs -f coordinator

# View specific pod logs
docker-compose logs -f app-pod-1
```

#### 4. Stop all services
```bash
docker-compose down
```

## Testing Leader Election

### 1. Check Coordinator Status
```bash
curl http://localhost:8080/coordinator/status
```

### 2. Check Current Leader
```bash
curl http://localhost:8080/coordinator/leader
```

### 3. Get All Pods
```bash
curl http://localhost:8080/coordinator/pods
```

### 4. Check Application Pod Status
```bash
curl http://localhost:8081/application/status
curl http://localhost:8082/application/status
```

### 5. Test Failover
Stop one of the application pods (Ctrl+C or `docker-compose stop app-pod-1`). The coordinator will detect the failure and re-elect a new leader within 5 seconds.

## API Endpoints

### Coordinator Endpoints

- `POST /coordinator/register` - Register an application pod
  ```json
  {
    "podId": "app-pod-1",
    "url": "http://localhost:8081"
  }
  ```

- `POST /coordinator/heartbeat/{podId}` - Send heartbeat from application pod
- `GET /coordinator/leader` - Get current leader
- `GET /coordinator/pods` - Get all registered pods
- `GET /coordinator/status` - Get coordinator status
- `DELETE /coordinator/unregister/{podId}` - Unregister a pod

### Application Pod Endpoints

- `GET /application/status` - Get pod status (leader/registered)
- `GET /application/health` - Health check endpoint

## Configuration

Configuration is done via Spring Boot profiles (`application-{profile}.yml`) or environment variables:

- `APP_ROLE`: `coordinator` or `application`
- `APP_POD_ID`: Unique identifier for the pod
- `APP_COORDINATOR_URL`: URL of the coordinator (e.g., `http://localhost:8080`)
- `APP_APPLICATION_PORT`: Port for application pods
- `SERVER_PORT`: Server port

## Leader Election Algorithm

1. Pods register with coordinator upon startup
2. Coordinator elects the first pod alphabetically by pod ID as leader
3. All pods send heartbeats every second
4. Coordinator checks pod health every 2 seconds
5. If a pod hasn't sent heartbeat for 5+ seconds, it's marked inactive
6. If the leader is inactive, a new leader is elected from active pods (alphabetically first)

## Project Structure

```
flyra/
├── src/
│   ├── main/
│   │   ├── java/com/flyra/
│   │   │   ├── LeaderElectionApplication.java
│   │   │   ├── config/
│   │   │   │   ├── ApplicationConfig.java
│   │   │   │   └── RestTemplateConfig.java
│   │   │   ├── controller/
│   │   │   │   ├── CoordinatorController.java
│   │   │   │   └── ApplicationPodController.java
│   │   │   ├── model/
│   │   │   │   └── PodInfo.java
│   │   │   └── service/
│   │   │       ├── CoordinatorService.java
│   │   │       └── ApplicationPodService.java
│   │   └── resources/
│   │       ├── application.yml
│   │       ├── application-coordinator.yml
│   │       └── application-pod{1-5}.yml
├── pom.xml
├── Dockerfile
├── docker-compose.yml
└── README.md
```

## Monitoring

Watch the console output to see:
- Leader pod prints: "I am leader pod" every second
- Other pods print: "I am application pod" every second
- Coordinator logs all registration, heartbeat, and election events

## Troubleshooting

1. **Pods not registering**: Ensure coordinator is running and accessible
2. **No leader elected**: Check that pods are sending heartbeats successfully
3. **Port conflicts**: Ensure ports 8080-8085 are available
4. **Docker networking**: If using Docker, ensure all containers are on the same network


# Distributed Microservice System — Docker & Jenkins Guide

This guide covers how to **build**, **push**, and **run** the `order` and `product` microservices using **Docker**, and how to automate the entire pipeline using **Jenkins**.

---

## Project Overview

| Service   | Port | Artifact                      |
|-----------|------|-------------------------------|
| Product   | 8080 | `product-0.0.1-SNAPSHOT.jar`  |
| Order     | 8081 | `order-0.0.1-SNAPSHOT.jar`    |

Both services use **Spring Boot 4.0.5** with **Java 17** and are built using multi-stage Dockerfiles.

---

## Part 1 — Docker

### 1.1 Prerequisites

- Install [Docker Desktop](https://docs.docker.com/desktop/) (Windows/Mac) or Docker Engine (Linux).
- Verify installation:

```bash
docker --version
docker compose version
```

### 1.2 Understanding the Dockerfile

Both services use a **multi-stage build** for smaller image size:

```dockerfile
# Stage 1: Build the JAR using Maven wrapper
FROM eclipse-temurin:17-jdk-alpine AS build
WORKDIR /app
COPY mvnw pom.xml ./
COPY .mvn .mvn
RUN chmod +x mvnw && ./mvnw dependency:go-offline -B
COPY src src
RUN ./mvnw package -DskipTests -B

# Stage 2: Run with lightweight JRE
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar", "--spring.profiles.active=dev"]
```

**Why multi-stage?**
- Stage 1 compiles the source code and produces the JAR (this image is ~500MB+).
- Stage 2 only copies the JAR into a slim JRE image (~200MB).
- The final image does not contain source code, Maven, or build tools.

### 1.3 Build Docker Images Locally

Navigate to the project root and build each service:

```bash
# Build product service image
cd product
docker build -t product-service:latest .

# Build order service image
cd ../order
docker build -t order-service:latest .
```

Verify images were created:

```bash
docker images | grep -E "product-service|order-service"
```

### 1.4 Run Docker Containers Locally

```bash
# Run product service on port 8080
docker run -d --name product-service -p 8080:8080 product-service:latest

# Run order service on port 8081
docker run -d --name order-service -p 8081:8081 order-service:latest
```

Verify containers are running:

```bash
docker ps
```

Test the services:

```bash
curl http://localhost:8080   # Product service
curl http://localhost:8081   # Order service
```

### 1.5 Stop and Remove Containers

```bash
docker stop product-service order-service
docker rm product-service order-service
```

### 1.6 Push Docker Images to Docker Hub

#### Step 1 — Login to Docker Hub

```bash
docker login
# Enter your Docker Hub username and password
```

#### Step 2 — Tag the images

Replace `yourdockerhubusername` with your actual Docker Hub username:

```bash
docker tag product-service:latest yourdockerhubusername/product-service:latest
docker tag order-service:latest yourdockerhubusername/order-service:latest
```

#### Step 3 — Push to Docker Hub

```bash
docker push yourdockerhubusername/product-service:latest
docker push yourdockerhubusername/order-service:latest
```

#### Step 4 — Verify on Docker Hub

Go to `https://hub.docker.com/r/yourdockerhubusername/product-service` and confirm the image is listed.

### 1.7 Pull and Run from Docker Hub (on any machine)

```bash
docker pull yourdockerhubusername/product-service:latest
docker pull yourdockerhubusername/order-service:latest

docker run -d --name product-service -p 8080:8080 yourdockerhubusername/product-service:latest
docker run -d --name order-service -p 8081:8081 yourdockerhubusername/order-service:latest
```

### 1.8 Run Both Services with Docker Compose

Create a `docker-compose.yml` in the project root:

```yaml
version: "3.9"
services:
  product-service:
    build: ./product
    ports:
      - "8080:8080"
    restart: unless-stopped

  order-service:
    build: ./order
    ports:
      - "8081:8081"
    restart: unless-stopped
```

Run both services:

```bash
docker compose up -d --build     # Build and start
docker compose ps                # Check status
docker compose logs -f           # View logs
docker compose down              # Stop and remove
```

---

## Part 2 — Jenkins CI/CD Pipeline

### 2.1 Prerequisites

- **Jenkins** installed and running (default: `http://localhost:8080`).
- **Plugins** installed in Jenkins:
  - Pipeline
  - Git
  - Docker Pipeline
  - Credentials Binding

Install plugins: **Manage Jenkins → Plugins → Available plugins → Search & Install.**

### 2.2 Configure Jenkins Credentials

You need two credentials stored in Jenkins:

| Credential ID      | Type              | Purpose                          |
|--------------------|-------------------|----------------------------------|
| `dockerhub-creds`  | Username/Password | Docker Hub login                 |
| `github-creds`     | Username/Password | GitHub repo access (if private)  |

**Steps:**
1. Go to **Manage Jenkins → Credentials → System → Global credentials**.
2. Click **Add Credentials**.
3. Select **Username with password**.
4. Enter your Docker Hub username & password, set ID to `dockerhub-creds`.
5. Repeat for GitHub if the repo is private.

### 2.3 Create a Jenkins Pipeline Job

1. Go to **Jenkins Dashboard → New Item**.
2. Enter name: `Distributed-Microservice-Pipeline`.
3. Select **Pipeline** → Click **OK**.
4. Scroll to **Pipeline** section → Select **Pipeline script from SCM**.
5. Set:
   - **SCM**: Git
   - **Repository URL**: `https://github.com/ankurnitt21/Distributed_Microservice_System.git`
   - **Branch**: `*/main`
   - **Script Path**: `Jenkinsfile`
6. Click **Save**.

### 2.4 Jenkinsfile (Pipeline Script)

Create a `Jenkinsfile` in the project root with the following content:

```groovy
pipeline {
    agent any

    environment {
        DOCKERHUB_USER = credentials('dockerhub-creds')
        PRODUCT_IMAGE  = "${DOCKERHUB_USER_USR}/product-service"
        ORDER_IMAGE    = "${DOCKERHUB_USER_USR}/order-service"
        IMAGE_TAG      = "${BUILD_NUMBER}"
    }

    stages {

        stage('Checkout') {
            steps {
                git branch: 'main',
                    url: 'https://github.com/ankurnitt21/Distributed_Microservice_System.git'
            }
        }

        stage('Build Product Image') {
            steps {
                dir('product') {
                    sh 'docker build -t ${PRODUCT_IMAGE}:${IMAGE_TAG} .'
                    sh 'docker tag ${PRODUCT_IMAGE}:${IMAGE_TAG} ${PRODUCT_IMAGE}:latest'
                }
            }
        }

        stage('Build Order Image') {
            steps {
                dir('order') {
                    sh 'docker build -t ${ORDER_IMAGE}:${IMAGE_TAG} .'
                    sh 'docker tag ${ORDER_IMAGE}:${IMAGE_TAG} ${ORDER_IMAGE}:latest'
                }
            }
        }

        stage('Push to Docker Hub') {
            steps {
                withCredentials([usernamePassword(
                    credentialsId: 'dockerhub-creds',
                    usernameVariable: 'DOCKER_USER',
                    passwordVariable: 'DOCKER_PASS'
                )]) {
                    sh 'echo $DOCKER_PASS | docker login -u $DOCKER_USER --password-stdin'

                    sh 'docker push ${PRODUCT_IMAGE}:${IMAGE_TAG}'
                    sh 'docker push ${PRODUCT_IMAGE}:latest'
                    sh 'docker push ${ORDER_IMAGE}:${IMAGE_TAG}'
                    sh 'docker push ${ORDER_IMAGE}:latest'
                }
            }
        }

        stage('Deploy - Run Containers') {
            steps {
                // Stop and remove old containers if they exist
                sh '''
                    docker stop product-service || true
                    docker rm product-service || true
                    docker stop order-service || true
                    docker rm order-service || true
                '''

                // Run fresh containers
                sh 'docker run -d --name product-service -p 8080:8080 ${PRODUCT_IMAGE}:${IMAGE_TAG}'
                sh 'docker run -d --name order-service -p 8081:8081 ${ORDER_IMAGE}:${IMAGE_TAG}'
            }
        }

        stage('Health Check') {
            steps {
                sh '''
                    echo "Waiting for services to start..."
                    sleep 15
                    echo "Checking Product Service..."
                    curl -f http://localhost:8080/actuator/health || echo "Product service health check failed"
                    echo "Checking Order Service..."
                    curl -f http://localhost:8081/actuator/health || echo "Order service health check failed"
                '''
            }
        }
    }

    post {
        success {
            echo "Pipeline completed successfully! Services are running."
            echo "Product Service: http://localhost:8080"
            echo "Order Service:   http://localhost:8081"
        }
        failure {
            echo "Pipeline failed. Check the logs above for errors."
        }
        always {
            // Clean up dangling images to save disk space
            sh 'docker image prune -f || true'
        }
    }
}
```

### 2.5 Pipeline Flow (What Happens When You Click "Build Now")

```
┌─────────────┐     ┌──────────────────┐     ┌─────────────────┐
│  Checkout    │────▶│  Build Product   │────▶│  Build Order    │
│  (Git Pull)  │     │  Docker Image    │     │  Docker Image   │
└─────────────┘     └──────────────────┘     └─────────────────┘
                                                      │
                                                      ▼
┌─────────────┐     ┌──────────────────┐     ┌─────────────────┐
│ Health Check │◀────│  Deploy (Run     │◀────│  Push to        │
│              │     │  Containers)     │     │  Docker Hub     │
└─────────────┘     └──────────────────┘     └─────────────────┘
```

### 2.6 Run the Pipeline

1. Go to **Jenkins Dashboard → Distributed-Microservice-Pipeline**.
2. Click **Build Now**.
3. Click the build number → **Console Output** to watch the live logs.
4. Once complete, verify services at:
   - Product: `http://localhost:8080`
   - Order: `http://localhost:8081`

### 2.7 Trigger Builds Automatically (Optional)

#### On every Git push (Webhook):
1. In Jenkins job → **Configure → Build Triggers → Check "GitHub hook trigger for GITScm polling"**.
2. In GitHub repo → **Settings → Webhooks → Add webhook**:
   - Payload URL: `http://your-jenkins-url/github-webhook/`
   - Content type: `application/json`
   - Events: **Just the push event**

#### On a schedule (Poll SCM):
1. In Jenkins job → **Configure → Build Triggers → Check "Poll SCM"**.
2. Set schedule: `H/5 * * * *` (checks every 5 minutes).

---

## Quick Reference

| Action                          | Command                                                    |
|---------------------------------|------------------------------------------------------------|
| Build product image             | `docker build -t product-service:latest ./product`         |
| Build order image               | `docker build -t order-service:latest ./order`             |
| Run product container           | `docker run -d -p 8080:8080 --name product product-service:latest` |
| Run order container             | `docker run -d -p 8081:8081 --name order order-service:latest`     |
| Push image to Docker Hub        | `docker push yourusername/product-service:latest`          |
| Start both with Compose         | `docker compose up -d --build`                             |
| Stop both with Compose          | `docker compose down`                                      |
| View running containers         | `docker ps`                                                |
| View container logs             | `docker logs -f product-service`                           |
| Jenkins build trigger           | Click "Build Now" or push to GitHub                        |

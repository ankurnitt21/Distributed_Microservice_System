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
                sh '''
                    docker stop product-service || true
                    docker rm product-service || true
                    docker stop order-service || true
                    docker rm order-service || true
                '''

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
            sh 'docker image prune -f || true'
        }
    }
}

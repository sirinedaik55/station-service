pipeline {
    agent any

    tools {
        maven 'Maven-3.9'
        jdk 'JDK-21'
        nodejs 'NodeJS-20'
    }

    environment {
        DOCKER_REGISTRY = 'docker.io/stationenterprise'
        IMAGE_BACKEND = "${DOCKER_REGISTRY}/station-backend:${BUILD_NUMBER}"
        IMAGE_FRONTEND = "${DOCKER_REGISTRY}/station-frontend:${BUILD_NUMBER}"
        SONARQUBE_SERVER = 'SonarQube-Server'
    }

    options {
        buildDiscarder(logRotator(numToKeepStr: '10'))
        timestamps()
        timeout(time: 30, unit: 'MINUTES')
    }

    stages {
        stage('📥 Checkout Code') {
            steps {
                echo "Récupération des sources depuis Git..."
                checkout scm
            }
        }

        stage('🧪 Backend : Tests Unitaires JUnit 5 & Mockito') {
            steps {
                dir('backend') {
                    echo "Exécution des tests unitaires et génération du rapport JaCoCo..."
                    sh './mvnw clean test jacoco:report'
                }
            }
            post {
                always {
                    junit 'backend/target/surefire-reports/*.xml'
                    archiveArtifacts artifacts: 'backend/target/site/jacoco/**', allowEmptyArchive: true
                }
            }
        }

        stage('🔍 SonarQube : Analyse Qualité & Quality Gate') {
            steps {
                dir('backend') {
                    withSonarQubeEnv("${SONARQUBE_SERVER}") {
                        sh '''
                            ./mvnw sonar:sonar \
                              -Dsonar.projectKey=station-service-backend \
                              -Dsonar.projectName="Station Service Backend" \
                              -Dsonar.coverage.jacoco.xmlReportPaths=target/site/jacoco/jacoco.xml
                        '''
                    }
                }
                timeout(time: 5, unit: 'MINUTES') {
                    waitForQualityGate abortPipeline: true
                }
            }
        }

        stage('📦 Backend : Package JAR') {
            steps {
                dir('backend') {
                    sh './mvnw package -DskipTests'
                }
            }
        }

        stage('🌐 Frontend : Angular 18 Build') {
            steps {
                dir('frontend') {
                    echo "Installation des dépendances npm et build de production..."
                    sh 'npm ci'
                    sh 'npm run build'
                }
            }
        }

        stage('🐳 Docker : Construction des Images') {
            steps {
                script {
                    echo "Construction des images Docker multi-stage..."
                    sh "docker build -t ${IMAGE_BACKEND} ./backend"
                    sh "docker build -t ${IMAGE_FRONTEND} ./frontend"
                    sh "docker tag ${IMAGE_BACKEND} ${DOCKER_REGISTRY}/station-backend:latest"
                    sh "docker tag ${IMAGE_FRONTEND} ${DOCKER_REGISTRY}/station-frontend:latest"
                }
            }
        }

        stage('☸️ Kubernetes : Déploiement Cluster') {
            steps {
                echo "Déploiement des manifests Kubernetes..."
                sh 'kubectl apply -f k8s/01-namespace.yaml'
                sh 'kubectl apply -f k8s/02-configmap-secrets.yaml'
                sh 'kubectl apply -f k8s/03-postgres.yaml'
                sh 'kubectl apply -f k8s/04-redis.yaml'
                sh 'kubectl apply -f k8s/05-rabbitmq.yaml'
                sh 'kubectl apply -f k8s/06-backend.yaml'
                sh 'kubectl apply -f k8s/07-frontend.yaml'
                sh 'kubectl apply -f k8s/08-ingress.yaml'
                
                echo "Vérification du statut des Pods..."
                sh 'kubectl rollout status deployment/station-backend -n station-service --timeout=120s'
                sh 'kubectl rollout status deployment/station-frontend -n station-service --timeout=60s'
            }
        }
    }

    post {
        success {
            echo "🚀 Pipeline Jenkins exécutée avec SUCCÈS ! Application déployée sur Kubernetes."
        }
        failure {
            echo "❌ Échec de la pipeline Jenkins. Vérifiez les logs des tests ou de SonarQube."
        }
    }
}

pipeline {
    agent any
    tools {
        jdk ("jdk17")
    }
    stages {
        stage('Git Clone') {
            steps {
                git branch: 'main', credentialsId: 'GITHUB_TOKEN', url: 'https://github.com/Fan-Tion/Back-End.git'
                sh 'pwd'  // 현재 작업 디렉토리 출력
                sh 'ls -la'  // 현재 작업 디렉토리의 파일 및 디렉토리 나열
            }
            post {
                failure {
                    echo 'Repository clone failure!'
                }
                success {
                    echo 'Repository clone success!'
                }
            }
        }
        stage('Build') {
            steps {
                // 프로젝트 권한 변경
                sh 'chmod +x ./fan-tion/gradlew'
                // YAML 파일 복사
                withCredentials([file(credentialsId: 'YAML_FILE', variable: 'YAML_FILE_PATH')]) {
                    sh 'cp ${YAML_FILE_PATH} ./Back-End/src/main/resources/application.yml'
                }
                // 프로젝트 빌드
                dir('fan-tion') {
                    sh './gradlew build'
                }
            }
        }
        stage('Docker Hub Login') {
            steps {
                withCredentials([usernamePassword(credentialsId: 'DOCKER_USER', passwordVariable: 'DOCKER_PASSWORD', usernameVariable: 'DOCKER_USERNAME')]) {
                    sh 'echo "$DOCKER_PASSWORD" | docker login -u $DOCKER_USERNAME --password-stdin'
                }
            }
        }
        stage('Docker Build and Push') {
            steps {
                withCredentials([usernamePassword(credentialsId: 'DOCKER_PROJECT', passwordVariable: 'DOCKER_PROJECT_PASSWORD', usernameVariable: 'DOCKER_PROJECT_NAME')]) {
                    sh '''
                        cd ./Back-End
                        docker build -f Dockerfile -t $DOCKER_PROJECT_NAME/$DOCKER_PROJECT_PASSWORD .
                        docker push $DOCKER_PROJECT_NAME/$DOCKER_PROJECT_PASSWORD
                    '''
                }
                echo 'docker push Success!!'
            }
        }
//         stage('Deploy') {
//             steps {
//                 sshagent(credentials: ['my-ssh-credentials']) {
//                     withCredentials([string(credentialsId: 'EC2_SERVER_IP', variable: 'IP')]) {
//                         sh 'ssh -o StrictHostKeyChecking=no ubuntu@$IP "sudo sh deploy.sh"'
//                     }
//                 }
//             }
//         }
    }
}



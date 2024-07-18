pipeline {
	agent any
	stages {
	    stage('Git Clone'){
            steps {
                git branch: 'main', credentialsId: 'github_token', url: 'https://github.com/Fan-Tion/Back-End.git'
            }
            post {
                failure {
                  echo 'Repository clone failure !'
                }
                success {
                  echo 'Repository clone success !'
                }
            }
	    }
	}

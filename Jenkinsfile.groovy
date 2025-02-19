pipeline {
    agent any
    parameters {
        string(name: 'MAVEN_URL', defaultValue: params.MAVEN_URL, description: 'maven url')
        credentials(credentialType:'com.cloudbees.plugins.credentials.impl.UsernamePasswordCredentialsImpl',
                name: 'MAVEN_CREDENTIALS',
                defaultValue: params.MAVEN_CREDENTIALS ?: '___',
                description: 'maven credentials')
        string(name:'PUBLISHING_MAVEN_URL', defaultValue: params.PUBLISHING_MAVEN_URL, description:'publishing maven url')
        credentials(credentialType:'com.cloudbees.plugins.credentials.impl.UsernamePasswordCredentialsImpl',
                name: 'PUBLISHING_MAVEN_CREDENTIALS',
                defaultValue: params.PUBLISHING_MAVEN_CREDENTIALS,
                description: 'publishing maven credentials')
        string(name: 'JIB_FROM_IMAGE', defaultValue: params.JIB_FROM_IMAGE, description: 'container base image')
        credentials(credentialType: 'com.cloudbees.plugins.credentials.impl.UsernamePasswordCredentialsImpl',
                name: 'JIB_FROM_AUTH_CREDENTIALS',
                defaultValue: params.JIB_FROM_AUTH_CREDENTIALS,
                description: 'base image repository credentials')
        string(name: 'JIB_TO_IMAGE_NAMESPACE', defaultValue: params.JIB_TO_IMAGE_NAMESPACE, description: 'target image')
        string(name: 'JIB_TO_TAGS', defaultValue: params.JIB_TO_TAGS, description: 'target image tags')
        credentials(credentialType: 'com.cloudbees.plugins.credentials.impl.UsernamePasswordCredentialsImpl',
                name: 'JIB_TO_AUTH_CREDENTIALS',
                defaultValue: params.JIB_TO_AUTH_CREDENTIALS,
                description: 'target image repository credentials')
        string(name: 'SEND_MESSAGE_TO', defaultValue: params.SEND_MESSAGE_TO ?: '___', description: 'Message platform(SLACK|...)')
    }
    options {
        disableConcurrentBuilds()
    }
    stages {
        stage("prepare") {
            steps {
                cleanWs()
                checkout scm
            }
        }
        stage("test,build") {
            environment {
                MAVEN_CREDENTIALS = credentials('MAVEN_CREDENTIALS')
            }
            steps {
                sh '''
                ./gradlew build --refresh-dependencies --stacktrace \
                -PmavenUrl=${MAVEN_URL} \
                -PmavenUsername=${MAVEN_CREDENTIALS_USR} \
                -PmavenPassword=${MAVEN_CREDENTIALS_PWD} \
                '''.stripIndent()
            }
        }
        stage("publish") {
            environment {
                MAVEN_CREDENTIALS = credentials('MAVEN_CREDENTIALS')
                PUBLISHING_MAVEN_CREDENTIALS = credentials('PUBLISHING_MAVEN_CREDENTIALS')
            }
            steps {
                sh '''
                ./gradlew publish -x test \
                -PmavenUrl=${MAVEN_URL} \
                -PmavenUsername=${MAVEN_CREDENTIALS_USR} \
                -PmavenPassword=${MAVEN_CREDENTIALS_PWD} \
                -PpublishingMavenUrl=${PUBLISHING_MAVEN_URL} \
                -PpublishingMavenUsername=${PUBLISHING_MAVEN_CREDENTIALS_USR} \
                -PpublishingMavenPassword=${PUBLISHING_MAVEN_CREDENTIALS_PSW} \
                '''.stripIndent()
            }
        }
        stage("jib") {
            environment {
                JIB_FROM_AUTH_CREDENTIALS = credentials('JIB_FROM_AUTH_CREDENTIALS')
                JIB_TO_AUTH_CREDENTIALS = credentials('JIB_TO_AUTH_CREDENTIALS')
            }
            steps {
                sh '''
                ./gradlew jib -x test \
                -PjibFromImage=${JIB_FROM_IMAGE} \
                -PjibFromAuthUsername=${JIB_FROM_AUTH_CREDENTIALS_USR} \
                -PjibFromAuthPassword=${JIB_FROM_AUTH_CREDENTIALS_PSW} \
                -PjibToImageNamespace=${JIB_TO_IMAGE_NAMESPACE} \
                -PjibToTags=${JIB_TO_TAGS} \
                -PjibToAuthUsername=${JIB_TO_AUTH_CREDENTIALS_USR} \
                -PjibToAuthPassword=${JIB_TO_AUTH_CREDENTIALS_PSW} \
                '''.stripIndent()
            }
        }
        stage("deploy") {
            steps {
                sh '''
                    kubectl delete pod -l app=fintics-daemon
                    kubectl wait --for=condition=Ready pod -l app=fintics-daemon --timeout=60s
                '''.stripIndent()
                sh '''
                    kubectl rollout restart deployment/fintics-web
                    kubectl rollout status deployment/fintics-web
                '''.stripIndent()
            }
        }
    }
    post {
        always {

            // junit
            junit allowEmptyResults: true, testResults: '**/build/test-results/test/*.xml'

            // send message
            script {
                if(params.SEND_MESSAGE_TO != null && params.SEND_MESSAGE_TO.contains('SLACK')) {
                    slackSend (
                        channel: '#oopscraftorg',
                        message: "Build [${currentBuild.currentResult}] ${env.JOB_NAME} (${env.BUILD_NUMBER}) - ${env.BUILD_URL}"
                    )
                }
            }
        }
    }

}

#!/usr/bin/env groovy

def call() {

pipeline {

parameters {
string(description: "Select a Branch?", name: 'BRANCH')
}

agent any

environment {
APPLICATION="trivagi"
ENVIRONMENT="dev"
AWS_DEFAULT_REGION="us-east-2"
}

stages {
stage ('Clean Workspace') {
steps {
        deleteDir()
     }
}

stage('Clone GIT Repo & build maven') {
steps {
script {
git(url: 'https://github.com/allyodevops/spring-boot-rest-example', branch: '${BRANCH}')
}

sh '''#!/bin/bash
mvn clean install -DskipTests
'''
}
}

stage('Build Docker Image & Push') {
steps {
script {
def App= APPLICATION.toLowerCase()
DEPLOY_TAG=BUILD_NUMBER
      sh "echo \"Building Image by Tag - ${DEPLOY_TAG}\" "
      sh "eval \"\$(aws ecr get-login --no-include-email)\" "
      sh "docker build --no-cache -t 454578700264.dkr.ecr.us-east-2.amazonaws.com/joker:${DEPLOY_TAG} ."
      sh "docker push 454578700264.dkr.ecr.us-east-2.amazonaws.com/joker:${DEPLOY_TAG}"
    }
}
}
stage('Deploy Docker Image') {
steps {
script {
    def App= APPLICATION.toLowerCase()
     DEPLOY_TAG=BUILD_NUMBER
       sh"helm upgrade --install  -f ../java-application/values.yaml test . -n hotel"
        }
}
}
}
}

}

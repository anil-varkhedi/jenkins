#!/usr/bin/env groovy
// CancelDuplicates
 
def print_build_info() {
    echo """
    Build Number - ${env.BUILD_NUMBER}
    Branch - ${env.branch}
    Commit - ${env.commit}
    Repository - ${env.repository}
    Project - ${env.project}
    Trigger - ${env.trigger}
    PR Destination - ${env.pr_destination}
    """
}
 
build_root = '/home/jenkins/jenkins/'
 
pipeline {
    agent {
        node {
            label 'jenkins_master'
            customWorkspace "${build_root}"
        }
    }
    stages{
        stage('Cancelling Duplicate jobs') { // for display purposes
            steps{
                print_build_info()
                wrap([$class: 'BuildUser']) {
                    script {
                        def trigger = "${env.trigger}".replaceAll(' ','_')
                        sh 'echo ${BUILD_USER}'
                        if ("${trigger}" == 'PR_MERGED') {
                            currentBuild.description = "Branch: ${env.pr_destination}<br>Started by user: ${env.BUILD_USER}"
                            sh "python /home/jenkins/jenkins/cancelprevjobs.pyc -b ${env.pr_destination} -j ${env.job_name} -n ${env.BUILD_NUMBER} -t ${env.kill_time} -r ${trigger}"
                        } else {
                            currentBuild.description = "Branch: ${env.branch}<br>Started by user: ${env.BUILD_USER}"
                            sh "python /home/jenkins/jenkins/cancelprevjobs.pyc -b ${env.branch} -j ${env.job_name} -n ${env.BUILD_NUMBER} -t ${env.kill_time} -r ${trigger}"
                        }
                    }
                }
            }
        }
    }
}
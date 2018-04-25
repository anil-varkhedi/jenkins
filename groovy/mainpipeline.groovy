#!/usr/bin/env groovy
// Print
build job: 'CancelDuplicates', parameters: [[$class: 'StringParameterValue', name: 'branch',         value: "${env.branch}"],
                                            [$class: 'StringParameterValue', name: 'commit',         value: "${env.commit}"],
                                            [$class: 'StringParameterValue', name: 'repository',     value: "${env.repository}"],
                                            [$class: 'StringParameterValue', name: 'project',        value: "${env.project}"],
                                            [$class: 'StringParameterValue', name: 'trigger',        value: "${env.trigger}"],
                                            [$class: 'StringParameterValue', name: 'pr_destination', value: "${env.pr_destination}"],
                                            [$class: 'StringParameterValue', name: 'build_number',   value: "${env.BUILD_NUMBER}"],
                                            [$class: 'StringParameterValue', name: 'job_name',       value: "${env.JOB_NAME}"],
                                            [$class: 'StringParameterValue', name: 'kill_time',      value: "30"]],
                                            wait: false
 
def print_build_info() {
    echo """
    Build Number - ${env.BUILD_NUMBER}
    Branch - ${env.branch}
    Commit - ${env.commit}
    Repository - ${env.repository}
    Project - ${env.project}
    Trigger - ${env.trigger}
    Debug Artifacts - ${env.debug_artifacts}
    Master SubUpdate - ${env.master_sub_update}
    PR Destination - ${env.pr_destination}
    PR Id - ${env.pr_id}
    PR URL - ${env.pr_url}
    PR Author - ${env.pr_author}
    PR Title - ${env.pr_title}
    PR Description - ${env.pr_description}
    """
}
 
 
def print_debug_symbols_info(String product_srcroot,
                             String product_name,
                             String product_version,
                             String product_comment) {
echo """
Registering debug symbols
PRODUCT_SRCROOT - ${product_srcroot}
PRODUCT_NAME - ${product_name}
PRODUCT_VERSION - ${product_version}
PRODUCT_COMMENT - ${product_comment}
"""
}
 
def notifyBitbucket(String state) {
                if('SUCCESS' == state || 'FAILURE' == state || 'UNSTABLE' == state || 'ABORTED' == state) {
        currentBuild.result = state   // Set result of currentBuild !Important!
                }
               
                script {
                    if ((env.commit) && (env.commit.trim().length() > 0)) {
                        echo "notifyBitbucket"
 
            catchError {
                notifyBitbucket commitSha1: "${env.commit}",
                        credentialsId: '',
                        disableInprogressNotification: true,
                        considerUnstableAsSuccess: false,
                        ignoreUnverifiedSSLPeer: true,
                        includeBuildNumberInKey: false,
                        prependParentProjectKey: false,
                        projectKey: "${env.project}",
                        stashServerBaseUrl: 'https://bitbucket.velo3d.com'
            }
                }
                }
}
 
def notifyJira(){
    withEnv(['JIRA_SITE=Velo3dJira']) {
        catchError {
            ws("C:\\Users\\jenkins\\velo3d\\build") {
                script{
                    echo "*****Adding Jira Comment*****"
                                                                                title = "${env.pr_title}".replaceAll("'","")
                    sh "echo '${env.branch}' >inputfile.out"
                    sh "echo '${title}' >>inputfile.out"
                   
                    def keys = env.pr_description.split();
                    for(String item: keys) {
                        item = "${item}".replaceAll("'","")
                        sh "echo '${item}' >>inputfile.out"
                    }
                    sh "sh parsefortickets.sh inputfile.out >tickets.out"
                    tickets = readFile('tickets.out').trim()
                    keys = tickets.split();
                    echo "Tickets: ${tickets}"
                    echo "Keys: ${keys}"
                   
                    //RemoteLink for jira issue links
                                                                                //http://www.openwebgraphics.com/resources/data/47/accept.png
                                                                                //http://www.openwebgraphics.com/resources/data/439/exclamation.png
                                                                                //http://www.openwebgraphics.com/resources/data/185/bullet_error.png
                                                                                build_result = "http://www.openwebgraphics.com/resources/data/185/bullet_error.png"
                                                                                if(currentBuild.currentResult == 'SUCCESS') {
                                                                                                build_result = "http://www.openwebgraphics.com/resources/data/47/accept.png"
                                                                                }
                                                                                else if(currentBuild.currentResult == 'FAILURE') {
                                                                                                build_result = "http://www.openwebgraphics.com/resources/data/439/exclamation.png"
                                                                                }
                                                                                               
                                                                                s_title = "${env.JOB_NAME} Build ${env.BUILD_NUMBER} > ${env.branch} > <${currentBuild.currentResult}>"
                    def remoteLink = [globalId: "system=${env.BUILD_URL}",
                                      application: [type: "jenkins.velo3d.com",
                                      name: "Jenkins "],
                                      relationship: "Jenkins Builds",
                                      object: [ url:"${env.BUILD_URL}",
                                                                                                                                                                                                title: "${s_title}",
                                                                                                                                                                                                icon: [ url16x16:"${build_result}",
                                                                                                                                                                                                                                title:"${currentBuild.currentResult}"]]]
                   
                    //Updating jira activity comments and issue links sections
                    if(keys.length > 0){
                        currentBuild.description = "${currentBuild.description}<br> Jira Issues: "
                        for(String item: keys) {
                            //Updating jira activity comments**************************************************************************************************************
                            //jiraAddComment idOrKey: "${item}", comment: "Jenkins Build Number ${env.BUILD_NUMBER}: ${currentBuild.currentResult} | PR URL: ${env.pr_url}"
                            //*********************************************************************************************************************************************
                           
                            //Updating jira issue links section**********************************************
                            jiraNewIssueRemoteLink idOrKey: "${item}", remoteLink: remoteLink
                            //*******************************************************************************
                            currentBuild.description = "${currentBuild.description} | <a href=\"https://jira.velo3d.com/browse/${item}\">${item}</a>"
                        }
                    }
                                                                                sh "rm inputfile.out tickets.out"
                                                                                mail_body = "Hi ${s_build_user},"
                                                                                mail_body = "${mail_body}\n\n${s_title}"
                                                                                if (buildstage == "ok"){
                                                                                                currentBuild.description = "${currentBuild.description}<br><a href=\"file:///v:/Software/Print/Builds/${cc_branch}\">(MSI) v:/Software/Print/Builds/${cc_branch}</a>"
                                                                                                mail_body = "${mail_body}\n(MSI) v:/Software/Print/Builds/${cc_branch}"
                                                                                }
                   
                                                                                if((env.pr_author.trim().length() > 0) && (!env.pr_author.equals("\$PRAUTHOR")))
                    {
                                                                                                email = env.pr_author.trim().toLowerCase().replace(' ','.') + "@velo3d.com"
                                                                                }
                                                                                else
                                                                                {
                                                                                                email = s_build_user.trim().toLowerCase().replace(' ','.') + "@velo3d.com"
                                                                                }
                                                                               
                                                                                mail_body = "${mail_body}\n\nJenkins Administrator"
                                                                                echo "Email to ${email}"
                                                                                sh "python sendmail.pyc -t jenkins@velo3d.com -f jenkins@velo3d.com -s \"${s_title}\" -m \"${mail_body}\""
                                                                                if ((email) && (email.trim().length() > 0)) {
                                                                                                sh "python sendmail.pyc -t \"${email}\" -f jenkins@velo3d.com -s \"${s_title}\" -m \"${mail_body}\""
                                                                                }
                }
            }
                    }
                }
}
 
def clean_git_repo() {
    if (fileExists('.git/index.lock')) {
        sh "rm .git/index.lock"
        echo "The .git/index.lock file is removed"
    }
    if (fileExists('.git/modules/preprint/index.lock')) {
        sh "rm .git/modules/preprint/index.lock"
        echo "The .git/modules/preprint/index.lock file is removed"
    }
                                                                                                               
    bat 'git reset --hard'
    bat 'git clean -dxf'
}
 
buildstage = ""
email = "michael.alper@velo3d.com"
release_version = ""
isrelease = "no"
is_v_subst = "no"
is_o_subst = "no"
cc_branch = "${env.branch}".replaceAll('/','_')
oo_branch = ""
if ("${env.trigger}" == 'PR MERGED') {
                cc_branch = "${env.pr_destination}".replaceAll('/','_')
}
 
if((cc_branch.length() > 7) && (cc_branch.substring(0, 7).equals("Release")))
{
                isrelease = "yes"
}
 
build_root = 'C:\\Users\\jenkins\\velo3d\\build'
build_scripts = 'C:\\Users\\jenkins\\velo3d\\build'
 
if (("${env.debug_artifacts}" == 'on') || ("${isrelease}" == 'yes')) {
                if((cc_branch.length() > 7) && (cc_branch.substring(0, 7).equals("Release")))
                {
                                oo_branch = "Software\\Print\\Debug\\release"
                                cc_branch = "Software\\Print\\Debug\\release\\${env.BUILD_NUMBER}"
                }
                else if(cc_branch.equals("dev"))
                {
                                oo_branch = "Software\\Print\\Debug\\dev"
                                cc_branch = "Software\\Print\\Debug\\dev\\${env.BUILD_NUMBER}"
                }
                else
                {
                                oo_branch = "Software\\Print\\Debug\\branch\\${cc_branch}"
                                cc_branch = "Software\\Print\\Debug\\branch\\${cc_branch}\\${env.BUILD_NUMBER}"
                }
}
build_root_print = "C:\\Users\\jenkins\\velo3d\\build\\print_debug"
build_root2 = "V:\\${cc_branch}"
z_root = "Z:\\${cc_branch}"
s_build_user = ""
 
pipeline {
    options{
        timestamps()
    }
    agent {
        node {
            label 'winbuildprint'
            //customWorkspace "${build_root_print}"
        }
    }
    environment {
        MAYA_ROOT = 'C:\\Program Files\\Autodesk\\Maya2016'
        MSBUILD   = 'C:\\Program Files (x86)\\Microsoft Visual Studio\\2017\\Professional\\MSBuild\\15.0\\Bin\\amd64\\msbuild.exe'
        V3D_PYTHON = 'C:\\Program Files\\Autodesk\\Maya2016\\bin\\mayapy.exe'
        VSTEST = 'C:\\Program Files (x86)\\Microsoft Visual Studio\\2017\\Professional\\Common7\\IDE\\Extensions\\TestPlatform'
        VS140COMNTOOLS = 'C:\\Program Files (x86)\\Microsoft Visual Studio 14.0\\Common7\\Tools'
        QT_DIR = 'C:\\Qt\\Qt5.10.0\\5.10.0\\msvc2017_64'
        QTDIR  = 'C:\\Qt\\Qt5.10.0\\5.10.0\\msvc2017_64'
        WIX = 'C:\\Program Files (x86)\\WiX Toolset v3.11'
        BRANCH = "${env.branch}"
        COMMIT = "${env.commit}"
        REPOSITORY = "${env.repository}"
        PROJECT = "${env.project}"
        TRIGGER = "${env.trigger}"
    }
    stages{
        stage('Build') { // for display purposes
            steps{
                //checkout scm
                print_build_info()
                wrap([$class: 'BuildUser']) {
                    script {
                        sh 'echo ${BUILD_USER}'
                        //currentBuild.displayName = "#${env.BUILD_NUMBER}"
                       
                        prurl = ""
                        if ((env.pr_url) && (env.pr_url.trim().length() > 0)) {
                            prurl = "<br><a href=\"${env.pr_url}\">Pull Request click here</a>"
                        }
                        if ("${env.trigger}" == 'PR MERGED') {
                            currentBuild.description = "Branch: ${env.pr_destination}<br>Started by user: ${env.BUILD_USER} ${prurl}"
                        } else {
                            currentBuild.description = "Branch: ${env.branch}<br>Started by user: ${env.BUILD_USER} ${prurl}"
                        }
                        notifyBitbucket('null')
                                                                                                s_build_user = "${env.BUILD_USER}"
                    }
                }
 
                script {
                    if ("${params.Choice}" != 'Exclude Build') {
                        echo "1. Checking out files for build"
                                                                                                if (("${env.debug_artifacts}" == 'on') || ("${isrelease}" == 'yes')) {
                                                                                                                //If building with debug artifacts then removing previous build and clone the new print repository
                                                                                                                ws("${build_root_print}") { //build_root_print = "C:\\Users\\jenkins\\velo3d\\build\\print"
                                                                                                                                sh "rm -rf *"
                                                                                                                                bat "mkdir ${cc_branch}"
                                                                                                                }
                                                                                                                               
                                                                                                                catchError {
                                                                                                                                build_root = "o:\\${env.BUILD_NUMBER}"
                                                                                                                                bat "subst o: ${build_root_print}\\${oo_branch}"
                                                                                                                                is_o_subst = "yes"
                                                                                                                               
                                                                                                                                ws("${build_root}") { //build_root = O:\Users\jenkins\velo3d\build\print_debug\Software\print\Debug\${env.branch}\${env.BUILD_NUMBER}
                                                                                                                                                sh "git clone ssh://git@bitbucket.velo3d.com:7999/vel/print.git"
                                                                                                                                }
                                                                                                                }
                                                                                                }
       
                        ws("${build_root}\\print") {
                            clean_git_repo()
                            script { 
                                if ("${env.trigger}" == 'PR MERGED') {
                                    bat "git fetch"
                                    bat "git checkout ${env.pr_destination}"
                                    bat "git pull origin ${env.pr_destination}"
                                } else if ("${env.trigger}" == 'PR OPENED') {
                                    bat "git fetch origin ${env.branch}"
                                    bat "git checkout ${env.branch}"
                                    bat "git reset --hard origin/${env.branch}"
                                } else {
                                    //This is for manual and other triggers
                                    bat "git fetch"
                                    bat "git checkout ${env.branch}"
                                    bat "git reset --hard origin/${env.branch}"
                                    bat "git pull origin ${env.branch}"
                                }
                               
                                //Getting email id and jira idOrKey
                                                                                                                                /*
                               catchError {
                                    //bat "git cherry -v dev >..\\inputfile.out"
                                    if ("${env.trigger}" == 'PR MERGED') {
                                        bat "git rev-parse HEAD >commit.out"
                                        commit_merge = readFile('commit.out').trim()
                                        sh "git log ${commit_merge} -1 | grep Author | sed -e \'s/Author: //\' | cut -d \"<\" -f2 | cut -d \">\" -f1 | uniq >email.out"
                                    }else {
                                        sh "git log ${env.commit} -1 | grep Author | sed -e \'s/Author: //\' | cut -d \"<\" -f2 | cut -d \">\" -f1 | uniq >email.out"
                                    }
                                    email = readFile('email.out').trim()
                                }
                                                                                                                                */
 
                                if (fileExists('bootstrap.cmd')) {
                                    bat "bootstrap.cmd"
                                }else{
                                    bat "git submodule init"
                                    bat "git submodule update"
                                }
                            }
                        }
                                                                                               
                                                                                                ws("${build_root}\\print\\preprint") {
                                                                                                                if ((env.master_sub_update) && (env.master_sub_update.trim().length() > 0)) {
                                                                                                                                sub_update = "${env.master_sub_update}".trim()
                                                                                                                                bat "git fetch"
                                                                                                                                bat "git checkout ${sub_update}"
                                                                                                                                bat "git pull origin ${sub_update}"
                                                                                                                }
                            bat "git lfs pull"
                        }
                           
                                                                                                //Getting location of MSI files to store
                                                                                                ws("${build_root}\\print\\Print\\ReleaseCandidate") {
                                                                                                                bat "python PrintSoftware.py version >version.out"
                                                                                                                version = readFile('version.out').trim()
                                                                                                                release_version = "${version}.${env.BUILD_NUMBER}"
                                                                                                                script {
                                                                                                                                release_version = "${release_version}".replace('Release/','').trim()
                                                                                                                               
                                                                                                                                cc_branch = "${env.branch}".replaceAll('/','_')
                                                                                                                                if ("${env.trigger}" == 'PR MERGED') {
                                                                                                                                                cc_branch = "${env.pr_destination}".replaceAll('/','_')
                                                                                                                                }
 
                                                                                                                                if((cc_branch.length() > 7) && (cc_branch.substring(0, 7).equals("Release")))
                                                                                                                                {
                                                                                                                                                cc_branch = "release/${release_version}"
                                                                                                                                }
                                                                                                                                else if(cc_branch.equals("dev"))
                                                                                                                                {
                                                                                                                                                cc_branch = "dev/${release_version}"
                                                                                                                                }
                                                                                                                                else
                                                                                                                                {
                                                                                                                                                cc_branch = "branch/${cc_branch}/${release_version}"
                                                                                                                                }
                                                                                                                }
                                                                                                }
                                                                                               
                                                                                                if (("${env.debug_artifacts}" == 'on') || ("${isrelease}" == 'yes')) {
                                                                                                                bat "subst v: ${build_root_print}"
                                                                                                                is_v_subst = "yes"
                                                                                                                ws("${build_root2}\\print\\Print\\ReleaseCandidate") {
                                                                                                                                echo "2. Build PrintSoftware.py"
                                                                                                                                bat "python -m pip install -r requirements.txt"
                                                                                                                                bat "python PrintSoftware.py --config hybrid --build_num=${env.BUILD_NUMBER} patch"
                                                                                                                }
                                                                                                }else{
                                                                                                                ws("${build_root}\\print\\Print\\ReleaseCandidate") {
                                                                                                                                echo "2. Build PrintSoftware.py"
                                                                                                                                bat "python -m pip install -r requirements.txt"
                                                                                                                                bat "python PrintSoftware.py --config hybrid --build_num=${env.BUILD_NUMBER} patch"
                                                                                                                }
                                                                                                }
                    }
                    else
                    {
                        if (("${env.debug_artifacts}" == 'on') || ("${isrelease}" == 'yes')) {
                                                                                                                bat "subst v: ${build_root_print}"
                                                                                                                is_v_subst = "yes"
                        }
                    }
                }
            }
            post{
                success {
                    script {
                        buildstage = "ok"
                    }
                    echo 'Build successfully completed.'
                }
                failure   {
                    script {
                        buildstage = "failure"
                        notifyBitbucket('FAILURE')
                    }
                    echo 'Build failed'
                }
            }
        }
        stage('Uploading debug artifacts') { // for display purposes
            when {
                expression {
                    // "expression" can be any Groovy expression
                    if ((buildstage == "ok") && (("${env.debug_artifacts}" == 'on') || ("${isrelease}" == 'yes')))
                        return true
                    else
                        return false
                }
            }
            steps{
                //build_root = C:\Users\jenkins\velo3d\build\print_debug\Software\print\Debug\${env.branch}\${env.BUILD_NUMBER}
                                                                //Clone build_tools repository
                                                                echo "Clone build_tools repository"
                                                                ws("${build_root}") {
                                                                                script{
                                                                                                sh "git clone ssh://git@bitbucket.velo3d.com:7999/vel/build_tools.git"
                                                                                }
                                                                }
                                                               
                                                                //Registering symbols
                                                                echo "Registering symbols"
                                                                ws("${build_root}\\build_tools\\symbols") {
                                                                                script{
                                                                                                //SET PRODUCT_SRCROOT=V:\Software\print\Debug\dev\21\print
                                                                                                //SET PRODUCT_NAME=Velo3Dprint
                                                                                                //SET PRODUCT_VERSION=1.38.21
                                                                                                //SET PRODUCT_COMMENT=Print Software v1.38.21
                                                                                               
                                                                                                product_srcroot = "${build_root2}\\print"
                                                                                                product_name = "Velo3D_Print"
                                                                                                product_version = "${release_version}"
                                                                                                product_comment = "Print_Software_V${release_version}"
                                                                                                print_debug_symbols_info("${product_srcroot}", "${product_name}", "${product_version}", "${product_comment}")
                                                                                               
                                                                                                //bat 'AddSymbols.bat "%${product_srcroot}%" "%${product_name}%" "%${product_version}%" "%${product_comment}%"'
                                                                                                //sh "bat AddSymbols.bat \"${product_srcroot}\" \"${product_name}\" \"${product_version}\" \"${product_comment}\""
                                                                                                bat "AddSymbols.bat ${product_srcroot} ${product_name} ${product_version} ${product_comment}"
                                                                                }
                                                                }
                                                               
                                                                //Upload debug artifacts 
                                                                ws("${build_root}\\print") {
                                                                                script{
                                                                                                bat "(robocopy /s ${build_root2} ${z_root} *.c *.h *.cpp *.hpp *.pdb) ^& IF %ERRORLEVEL% LEQ 15 exit 0"
                                                                                }
                }
            }
        }
        stage('Test') { // for display purposes
            when {
                expression {
                    // "expression" can be any Groovy expression
                    if ((buildstage == "ok") && ("${params.Choice}" != 'Exclude Test'))
                        return true
                    else
                        return false
                }
            }
            steps{
                ws("${build_root}\\print\\Print\\Release-x64") {
                    script {
                        //powershell '$names=Get-ChildItem *Test*.dll;vstest.console.exe /Logger:trx /Platform:x64 /InIsolation $names'
                                                                                                bat "copy ${build_scripts}\\runprinttests.sh"
                        sh "sh ./runprinttests.sh"
                    }
                }
            }
            post{
                always {
                    ws("${build_root}\\print\\Print\\Release-x64\\TestResults") {
                                                                                                script {
                                                                                                                bat "copy ${build_scripts}\\saxon9he.jar"
                                                                                                                bat "copy ${build_scripts}\\transform.xslt"
                            sh 'java -jar ./saxon9he.jar -o:outputfile.xml *.trx transform.xslt'
                            junit 'outputfile.xml'
                            echo 'JUnit test completed.'
                                                                                                }
                    }
                }
                failure {
                    echo 'VS test failed'
                }
            }
        }
    }
    post{
        success {
            script {
                notifyBitbucket('SUCCESS')
            }
        }
                                unstable {
                                                script {
                                                                notifyBitbucket('UNSTABLE')
                                                }
                                }
        failure {
            script {
                notifyBitbucket('FAILURE')
            }
        }
                                aborted {
            script {
                notifyBitbucket('ABORTED')
            }
        }
        always {
            script {
                catchError {
                                                                                if (buildstage == "ok"){
                                                                                                ws("${build_root}\\print\\Print\\bin\\Hybrid") {
                                                                                                                if(!fileExists("/z/Software/Print/Builds/${cc_branch}"))
                                                                                                                {
                                                                                                                  sh "mkdir -p /z/Software/Print/Builds/${cc_branch}"
                                                                                                                }
                                                                                                                sh "cp *.msi /z/Software/Print/Builds/${cc_branch}"
                                                                                                }
                                                                                }
                }
                                                               
                                                                if (("${env.debug_artifacts}" == 'on') || ("${isrelease}" == 'yes')) {
                                                                                catchError {
                                                                                                if("${is_v_subst}" == 'yes')
                                                                                                {
                                                                                                                bat "subst v: /D"
                                                                                                }
                                                                                               
                                                                                                if("${is_o_subst}" == 'yes')
                                                                                                {
                                                                                                                bat "subst o: /D"
                                                                                                }
                                                                                }
                                                                }
                notifyJira()
            }
        }
    }
}
 
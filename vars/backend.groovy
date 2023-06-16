#!groovy
def call(body) {
    def config = [:]
    body.resolveStrategy = Closure.DELEGATE_FIRST
    body.delegate = config
    body()

    // variáveis úteis
    def repoPWD = ""
    def shortCommit = ""
    def varVersion = ""
    def git = new libs.git.Git()

    // Pipeline
    pipeline {
        agent any

        environment {
            // configuração do repositório
            REPO_NAME = "" // nome do seu repositório
            REPO_URL = "" // url de conexão do seu repositório (para etapa de clonagem)
            REPO_BRANCH = "" // branch alvo
            FILE_PATH = "/var/jenkins_home/workspace/backend/vougth-api-events/api/Jenkinsfile" // edite conforme o seu projeto

            // configuração do IP do SonarQube
            SONAR_PROJECTKEY = "" // nome da Project Key
            SONAR_ORG = "" // organização do Sonar
            SONAR_URL = "" // URL para acesso ao Sonar
            SONAR_LOGIN = "" // código hash para conexão automatizada
        }

        stages{
            stage('Teste de conexão'){
                steps{
                    script{
                        echo "teste bem sucedido"
                    }
                }
            }

            stage("Clonagem do repositório"){
                steps{
                    script{
                        echo "Início da clonagem do repositório"
                        repoPWD = "${git.doesFileExists("${FILE_PATH}","${REPO_URL}","${REPO_NAME}","${REPO_BRANCH}","${SONAR_PROJECTKEY}")}/api"
                        shortCommit = git.getShortCommitHash("${repoPWD}")
                        echo "Commit Hash: ${repoPWD}"
                    }
                }
            }

            stage("Definição de versão do maven"){
                steps{
                    dir(repoPWD){
                        script{
                            echo "Início da definição da versão do Maven"
                            sh "mvn versions:set -DnewVersion=${REPO_NAME}.${REPO_BRANCH}.${env.BUILD_NUMBER} && mvn versions:commit -DprocessAllModules"
                            echo "Nova versão: ${REPO_NAME}.${REPO_BRANCH}.${env.BUILD_NUMBER}"
                            varVersion = "${REPO_NAME}.${REPO_BRANCH}.${env.BUILD_NUMBER}"
                        }
                    }
                }
            }

            stage("Build do projeto"){
                steps{
                    dir(repoPWD){
                        script{
                            echo "Início do build do projeto via Maven"
                            echo "${shortCommit}"
                            sh "mvn clean install"
                        }
                    }
                }
            }

            stage('Análise de qualidade'){
                steps{
                    dir(repoPWD){
                        script{
                            echo "Início da análise da qualidade do código via SonarQube"
                            sh 'mvn sonar:sonar -Dsonar.projectKey=${SONAR_PROJECTKEY} -Dsonar.organization=${SONAR_ORG} -Dsonar.host.url=${SONAR_URL} -Dsonar.login=${SONAR_LOGIN}'
                        }
                    }
                }
            }

            stage('Resultado do deploy'){
                steps{
                    echo "Parabéns! Deploy bem sucedido."
                }
            }
        }
    }
}

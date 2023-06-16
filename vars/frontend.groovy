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
            REPO_NAME = "Vought"
            REPO_URL = "https://nascimentoadalberto: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@github.com/Vought-Organization/Vought.git"
            REPO_BRANCH = "main"
            FILE_PATH = "/var/jenkins_home/workspace/frontend/package.json"

            // configuração do IP do SonarQube
            SONAR_PROJECTKEY = "frontend"
            SONAR_ORG = "vought"
            SONAR_URL = "https://272c-45-184-194-71.ngrok-free.app"
            SONAR_LOGIN = "sqa_83560df15459290f293d744c7d2b3fb5b6def057"
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
                        repoPWD = "${git.doesFileExists("${FILE_PATH}","${REPO_URL}","${REPO_NAME}","${REPO_BRANCH}","${SONAR_PROJECTKEY}")}/webapp-vought"
                        shortCommit = git.getShortCommitHash("${repoPWD}")
                        echo "Commit Hash: ${repoPWD}"
                    }
                }
            }

            stage("Download das dependências"){
                steps{
                    dir(repoPWD){
                        script{
                            echo "Início do build do projeto via node package manager"
                            echo "${shortCommit}"
                            git.installNpm("${repoPWD}")
                        }
                    }
                }
            }

            stage("Build do projeto"){
                steps{
                    dir(repoPWD){
                        script{
                            echo "Build do projeto via node package manager"
                            sh "npm run build"
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
package libs.git;
import java.nio.file.Files
import java.nio.file.Paths

// métodos
def String getShortCommitHash(String REPO_PWD) {
    script {
        shortCommit = sh (
                script:  "cd ${REPO_PWD} && git rev-parse --short HEAD",
                returnStdout: true
        ).trim()
        return shortCommit
    }
}

def String doesFileExists(String FILE_PATH, REPO_URL, REPO_NAME, REPO_BRANCH, SONAR_PROJECTKEY) {
    script {
        echo "Início da clonagem do repositório"
        if (Files.exists(Paths.get(FILE_PATH))) {
            echo "Repositório já clonado."
        } else {
            echo "O repositório ainda não foi clonado, iremos clonar agora."
            sh "git clone -b ${REPO_BRANCH} ${REPO_URL} && cd ${REPO_NAME} && git checkout ${REPO_BRANCH}"
        }
        repoPWD = sh (
                script: "cd /var/jenkins_home/workspace/${SONAR_PROJECTKEY} && pwd",
                returnStdout: true
            ).trim()
        return repoPWD
    }
}

def void installNpm(String FOLDER_PATH) {
    script {
        def nodeModulesPath = "${FOLDER_PATH}/node_modules"
        if (Files.exists(Paths.get(nodeModulesPath)) && Files.isDirectory(Paths.get(nodeModulesPath))) {
            echo "node_modules já existe"
        } 
        if (Files.exists(Paths.get("${FOLDER_PATH}/package.json"))) {
            sh "npm i --force"
        }
    }
}

return this
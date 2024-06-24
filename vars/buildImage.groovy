def call(DOCKER_REGISTRY, IMAGE_NAME, IMAGE_TAG, CONTAINER_NAME, MAIL_SEND_TO, TELEGRAM_BOT_TOKEN, TELEGRAM_CHAT_ID) {
    cleanDockerImage(DOCKER_REGISTRY, IMAGE_NAME, IMAGE_TAG, CONTAINER_NAME)
    
    try {
        buildDockerImage(DOCKER_REGISTRY, IMAGE_NAME, IMAGE_TAG)
        sendTelegramMessage("Build image successfully")
    } catch (Exception e) {
        echo "Build image failed, retrying..."
        cleanDockerImage(DOCKER_REGISTRY, IMAGE_NAME, IMAGE_TAG, CONTAINER_NAME)
        buildDockerImage(DOCKER_REGISTRY, IMAGE_NAME, IMAGE_TAG)
        sendTelegramMessage("Build image failed")
        throw e
    }
}

def cleanDockerImage(DOCKER_REGISTRY, IMAGE_NAME, IMAGE_TAG, CONTAINER_NAME) {
    sh """
        docker rmi -f ${IMAGE_NAME}:${IMAGE_TAG}
        docker rmi -f ${DOCKER_REGISTRY}/${IMAGE_NAME}:${IMAGE_TAG}
        docker rm -f ${CONTAINER_NAME}
    """
}

def buildDockerImage(DOCKER_REGISTRY, IMAGE_NAME, IMAGE_TAG) {
    sh "docker build -t ${IMAGE_NAME}:${IMAGE_TAG} -t ${DOCKER_REGISTRY}/${IMAGE_NAME}:${IMAGE_TAG} ."
}

def sendTelegramMessage(message) {
    sh "curl -s -X POST https://api.telegram.org/bot${TELEGRAM_BOT_TOKEN}/sendMessage -d chat_id=${TELEGRAM_CHAT_ID} -d text='${message}'"
}

def sendGmailMessage(message) {
    mail bcc: '', body: message, cc: '', from: '', replyTo: '', subject: 'Building Image', to: MAIL_SEND_TO  
}
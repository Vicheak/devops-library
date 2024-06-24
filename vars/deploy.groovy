def call(minPort, maxPort, DOCKER_REGISTRY, IMAGE_NAME, IMAGE_TAG, CONTAINER_NAME, MAIL_SEND_TO, TELEGRAM_BOT_TOKEN, TELEGRAM_CHAT_ID) {
    def minPortValue = minPort.toInteger()
    def maxPortValue = maxPort.toInteger()
    def selectedPort = selectRandomAvailablePort(minPortValue, maxPortValue)

    if (selectedPort) {
        echo "Selected port : $selectedPort"
        sh "docker run -d -p $selectedPort:80 --name ${CONTAINER_NAME} ${DOCKER_REGISTRY}/${CONTAINER_NAME}:${IMAGE_TAG}"
        sendGmailMessage("Deploy application on $selectedPort successfully", MAIL_SEND_TO)
        sendTelegramMessage("Deploy application on $selectedPort successfully", TELEGRAM_BOT_TOKEN, TELEGRAM_CHAT_ID)
        def ipAddress = sh(script: 'curl -s ifconfig.me', returnStdout: true).trim()
        def ipWithPort = "${ipAddress}:${selectedPort}"
        sendTelegramMessage(ipWithPort, TELEGRAM_BOT_TOKEN, TELEGRAM_CHAT_ID)
    } else {
        error "No available ports found in the range $minPort-$maxPort"
    }

    def usedPorts = listPortsInUseForDocker(minPortValue, maxPortValue)
    if (!usedPorts.isEmpty()) {
        echo "Ports already in use mapping on port 80: ${usedPorts.join(', ')}"
        sendTelegramMessage("Ports already in use mapping on port 80: ${usedPorts.join(', ')}", TELEGRAM_BOT_TOKEN, TELEGRAM_CHAT_ID)
    } 
}

def selectRandomAvailablePort(minPort, maxPort) {
    def numberOfPortsToCheck = maxPort - minPort + 1
    def portsToCheck = (minPort..maxPort).toList()
    Collections.shuffle(portsToCheck)

    for (int i = 0; i < numberOfPortsToCheck; i++) {
        def portToCheck = portsToCheck[i]
        if (isPortAvailable(portToCheck) && !isPortInUseForDocker(portToCheck)) {
            return portToCheck
        }
    }
    return null
}

def isPortAvailable(port) {
    def socket
    try {
        socket = new Socket("localhost", port)
        return false //port is already in use
    } catch (Exception e) {
        return true //port is available
    } finally {
        if (socket) {
            socket.close()
        }
    }
}

def isPortInUseForDocker(port) {
    def dockerPsOutput = sh(script: "docker ps --format '{{.Ports}}'", returnStdout: true).trim()
    return dockerPsOutput.contains(":$port->80/tcp")
}

def listPortsInUseForDocker(minPort, maxPort) {
    def usedPorts = []
    for (int port = minPort; port <= maxPort; port++) {
        if (isPortInUseForDocker(port)) {
            usedPorts.add(port)
        }
    }
    return usedPorts
}

def sendTelegramMessage(message, TELEGRAM_BOT_TOKEN, TELEGRAM_CHAT_ID) {
    sh "curl -s -X POST https://api.telegram.org/bot${TELEGRAM_BOT_TOKEN}/sendMessage -d chat_id=${TELEGRAM_CHAT_ID} -d text='${message}'"
}

def sendGmailMessage(message, MAIL_SEND_TO) {
    mail bcc: '', body: message, cc: '', from: '', replyTo: '', subject: 'Deploy Application', to: MAIL_SEND_TO
}
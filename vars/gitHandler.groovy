def call(DEPLOY_ENV, REPO_URL, REPO_TOKEN, REPO_BRANCH, TELEGRAM_BOT_TOKEN, TELEGRAM_CHAT_ID) {
    if (DEPLOY_ENV == "development") {
        echo "Cloning from development environment"
        try {
            // Fetch the code from the Git repository
            git credentialsId: REPO_TOKEN, url: REPO_URL
            sendTelegramMessage("Pull resource successfully", TELEGRAM_BOT_TOKEN, TELEGRAM_CHAT_ID)
        } catch (Exception e) {
            sendTelegramMessage("Pull resource failed", TELEGRAM_BOT_TOKEN, TELEGRAM_CHAT_ID)
            throw e
        }
    }else if (DEPLOY_ENV == "staging") {
        echo "Cloning from staging environment"
        try {
            // Fetch the code from the Git repository
            git branch: REPO_BRANCH, credentialsId: REPO_TOKEN, url: REPO_URL
            sendTelegramMessage("Pull resource successfully", TELEGRAM_BOT_TOKEN, TELEGRAM_CHAT_ID)
        } catch (Exception e) {
            sendTelegramMessage("Pull resource failed", TELEGRAM_BOT_TOKEN, TELEGRAM_CHAT_ID)
            throw e
        }
    }else if (DEPLOY_ENV == "production") {
        echo "Cloning from production environment"
        try {
            // Fetch the code from the Git repository
            git branch: REPO_BRANCH, credentialsId: REPO_TOKEN, url: REPO_URL
            sendTelegramMessage("Pull resource successfully", TELEGRAM_BOT_TOKEN, TELEGRAM_CHAT_ID)
        } catch (Exception e) {
            sendTelegramMessage("Pull resource failed", TELEGRAM_BOT_TOKEN, TELEGRAM_CHAT_ID)
            throw e
        }
    }
}

def sendTelegramMessage(message, TELEGRAM_BOT_TOKEN, TELEGRAM_CHAT_ID) {
    sh "curl -s -X POST https://api.telegram.org/bot${TELEGRAM_BOT_TOKEN}/sendMessage -d chat_id=${TELEGRAM_CHAT_ID} -d text='${message}'"
}
def call(Map config) {
    podTemplate(
            label: 'kube-build-pod',
            cloud: 'kubernetes',
            inheritFrom: 'default',
            namespace: 'jenkins',
            containers: [
                    containerTemplate(name: 'kaniko', image: 'mgit/base:kaniko-executor-debug-stable', ttyEnabled: true, command: 'cat'),
            ],
            volumes: [
                     secretVolume(mountPath: '/kaniko/.docker/', secretName: 'kaniko-secret')
            ]
    ) 
    
    {
        node('kube-build-pod') {
            try {
                stage ('Checkout') {
                    checkout scm
                    config.commitId = sh(script: 'git rev-parse --short HEAD', returnStdout: true).trim()
                    config.repoName = utils.getRepoName()
                    config.lastCommitterEmail = utils.getLastCommitterEmail()
                    config.lastCommit = utils.getLastCommit()
                    config.branchName = env.BRANCH_NAME
                }
                stage ('Build docker image and push') {
                    container ('kaniko') {
				        sh "executor -f `pwd`/Dockerfile -c `pwd` --insecure --skip-tls-verify --cache=true --destination=sivarajp/${config.repoName}:${config.commitId}" 
                        config.dockerimage = "sivarajp/${config.repoName}:${config.commitId}"
                    }
                }
            } finally {

            }
        }
    }
}
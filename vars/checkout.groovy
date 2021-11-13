import com.cloudogu.ces.cesbuildlib.Git
def call(Map config){
    node {
        echo 'Checkout...'
        Git git = new Git(this)

        git url: "${config.url}", branch: "${config.url}", credentialsId:"${config.credentialsId}"
    }
}

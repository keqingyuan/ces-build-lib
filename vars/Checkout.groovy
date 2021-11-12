import com.cloudogu.ces.cesbuildlib.Git
def call(Map config){
    node {
        Git git = new Git(this, '${config.credentials}')

        git url: "${config.url}"
    }
}

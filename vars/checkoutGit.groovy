import com.cloudogu.ces.cesbuildlib.Git
def call(Map config){
    node {
        echo '从Git仓库检出源码'
        Git myGit = new Git(this)
        myGit branch:'${config.branch}',url:'${config.url}',credentialsId:'${config.credentialsId}'
        echo '源码检出完成，myGit.getBranchName()'
    }
}

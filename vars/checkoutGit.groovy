#!/usr/bin/env groovy

import com.cloudogu.ces.cesbuildlib.Git

def call(Map params = [:]){
    echo "从Git仓库检出源码"
    Git myGit = new Git(this,"")
    myGit branch:"${params.branch}",url:"${params.url}",credentialsId:"${params.credentialsId}"
    // 获取检出的分支名称，以便打印
    def branchName = myGit.getBranchName()
    echo "源码检出完成，${branchName}"
}

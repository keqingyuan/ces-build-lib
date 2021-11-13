#!/usr/bin/env groovy

import com.cloudogu.ces.cesbuildlib.Git

def call(Map params = [:]){
    echo "从Git仓库检出源码"
    Git myGit = new Git(this,"")
    dir("${params.local}") {
        myGit branch:"${params.branch}",url:"${params.url}",credentialsId:"${params.credentialsId}"
    }
    echo "源码检出完成，${params.branch}"
}

package com.cloudogu.ces.cesbuildlib

class Git implements Serializable {
    private script
    Sh sh
    def credentials = null
    def retryTimeout = 500
    def maxRetries = 5

    Git(script, credentials) {
        this(script)
        this.credentials = credentials
    }

    Git(script) {
        this.script = script
        this.sh = new Sh(script)
    }

    /**
     * You can set the timeout between retries, when git calls with credentials fail. The default is 500 ms.
     * @param retryTimeout The new timeout in milli seconds.
     */
    void setRetryTimeout(def retryTimeout) {
        this.retryTimeout = retryTimeout
    }

    /**
     * You can set the maximum number of retries, when git calls with credentials fail. The default is 5.
     * @param maxRetries The new maximum number of retries.
     */
    void setMaxRetries(def maxRetries) {
        this.maxRetries = maxRetries
    }

    def call(args) {
        git(args)
    }

    /**
     * Credential-aware wrapper around the global "git" step.
     */
    private def git(args) {

        if (credentials != null) {

            // args instanceof Map ist not allowed due to sandboxing. So find it out the hard way :-(
            try {
                if (!args.containsKey('credentialsId')) {
                    args.put('credentialsId', credentials)
                }
            } catch (MissingMethodException ignored) {
                // This exception indicates that we don't have a Map. Assume url String and add credentials
                args = [url: args.toString(), credentialsId: credentials]
            }
        }
        script.git args
    }

    void clean(String excludes) {
        String excludesParam = ""
        if (excludes != null && "" != excludes) {
            excludesParam = " --exclude $excludes"
        }
        // Remove all untracked files
        script.sh "git clean -df$excludesParam"
        //Clear all unstaged changes
        script.sh 'git checkout -- .'
    }

    /**
     * This is just a convenience wrapper around {@code env.BRANCH_NAME}, provided by jenkins.
     * <b>Only works for multi-branch projects!</b>
     *
     * @return name of the current branch.
     */
    String getBranchName() {
        script.env.BRANCH_NAME
    }

    /**
     * @return the part of the branch name after the slash
     */
    String getSimpleBranchName() {
        return branchName.substring(branchName.lastIndexOf('/') + 1)
    }

    /**
     * @return true if this branch differs from the develop branch
     */
    boolean branchesHaveDiverged(String targetBranch, String sourceBranch) {
        String diff = executeGitWithCredentials("log origin/${targetBranch}..origin/${sourceBranch} --oneline")[0]
        return diff.length() > 0
    }

    /**
     * @return the Git Author of HEAD, in the following form <code>User Name &lt;user.name@doma.in&gt;</code>
     */
    String getCommitAuthorComplete() {
        sh.returnStdOut "git --no-pager show -s --format='%an <%ae>' HEAD"
    }

    String getCommitAuthorName() {
        return getCommitAuthorComplete().replaceAll(" <.*", "")
    }

    String getCommitAuthorEmail() {
        def matcher = getCommitAuthorComplete() =~ "<(.*?)>"
        matcher ? matcher[0][1] : ""
    }

    String getCommitMessage() {
        sh.returnStdOut "git log -1 --pretty=%B"
    }

    String getCommitHash() {
        sh.returnStdOut "git rev-parse HEAD"
    }

    String getCommitHashShort() {
        sh.returnStdOut "git rev-parse --short HEAD"
    }

    /**
     * @return the URL of the Git repository, e.g. {@code https://github.com/orga/repo.git}
     */
    String getRepositoryUrl() {
        sh.returnStdOut "git remote get-url origin"
    }

    /**
     * @return the name of the GitHub Repository e.g. {@code repository/url} or empty String, if no GitHub repo.
     */
    @Deprecated
    String getGitHubRepositoryName() {
        if (!repositoryUrl.contains('github.com')) {
            return ''
        }
        return repositoryName
    }

    /**
     * @return the name of the Repository, assuming it is the URL is formatted like {@code host/getRepositoryName}
     */
    String getRepositoryName() {
        String repoUrl = repositoryUrl
                .replace('https://', '')
                .replace('.git', '')
        if (repoUrl.startsWith('git@')) {
            return repoUrl.substring(repoUrl.indexOf(':') + 1)
        } else {
            return repoUrl.substring(repoUrl.indexOf('/') + 1)
        }
    }

    String getTag() {
        // Note that "git name-rev --name-only --tags HEAD" always seems to append a caret (e.g. "1.0.0^")
        return sh.returnStdOut("git tag --points-at HEAD")
    }

    boolean isTag() {
        return !getTag().isEmpty()
    }

    /**
     * @return true if the specified tag exists
     */
    boolean tagExists(String tag) {
        def (tagFound, exitCode) = this.executeGitWithCredentials("ls-remote origin refs/tags/${tag}")
        return tagFound != null && tagFound.length() > 0
    }

    def add(String pathspec) {
        script.sh "git add $pathspec"
    }

    /**
     * Commits using the name and email of the last committer as author and committer.
     *
     * @param message
     */
    void commit(String message) {
        commit(message, commitAuthorName, commitAuthorEmail)
    }

    /**
     * Commits using the specific name and emails as author and committer.
     *
     * @param message
     */
    void commit(String message, String authorName, String authorEmail) {
        script.withEnv(["GIT_AUTHOR_NAME=$authorName", "GIT_AUTHOR_EMAIL=$authorEmail",
                        "GIT_COMMITTER_NAME=$authorName", "GIT_COMMITTER_EMAIL=$authorEmail"]) {
            script.sh "git commit -m \"$message\""
        }
    }

    /**
     * Sets Tag with message using the name and email of the last committer as author and committer.
     *
     * @param tag
     * @param message
     */
    void setTag(String tag, String message) {
        setTag(tag, message, commitAuthorName, commitAuthorEmail)
    }

    /**
     * Sets a git Tag and message using the specific name and emails as author and committer.
     *
     * @param tag
     * @param message
     * @param authorName
     * @param authorEmail
     */
    void setTag(String tag, String message, String authorName, String authorEmail) {
        script.withEnv(["GIT_AUTHOR_NAME=$authorName", "GIT_AUTHOR_EMAIL=$authorEmail",
                        "GIT_COMMITTER_NAME=$authorName", "GIT_COMMITTER_EMAIL=$authorEmail"]) {
            script.sh "git tag -m \"${message}\" ${tag}"
        }
    }

    /**
     * Fetch remote branches from origin.
     */
    void fetch() {
        // we need to configure remote,
        // because jenkins configures the remote only for the current branch
        script.sh "git config 'remote.origin.fetch' '+refs/heads/*:refs/remotes/origin/*'"
        executeGitWithCredentials "fetch --all"
    }

    /**
     * Fetch remote branches from origin.
     */
    void fetchWithCredentials() {
        // we need to configure remote,
        // because jenkins configures the remote only for the current branch
        script.sh "git config 'remote.origin.fetch' '+refs/heads/*:refs/remotes/origin/*'"
        this.executeGitWithCredentials("fetch --all")
    }

    /**
     * Switch branch of the local repository.
     * Note: In a multibranch pipeline Jenkins will only fetch the changed branch,
     * so you have to call {@link #fetch()} before checkout.
     *
     * @param branchName name of branch to switch to
     */
    void checkout(String branchName) {
        script.sh "git checkout ${branchName}"
    }

    /**
     * Switch the branch of the local repository and then pull changes from origin.
     *
     * @param branchName The name of the branch to checkout and pull from.
     */
    void checkoutAndPull(branchName){
        checkout(branchName)
        executeGitWithCredentials("pull origin ${branchName}")
    }

    /**
     * Switch branch to remote branch. Creates new local branch if it does not exist;
     * Note: In a multibranch pipeline Jenkins will only fetch the changed branch,
     * so you have to call {@link #fetch()} before checkout.
     *
     * @param branchName name of branch to switch to
     */
    void checkoutOrCreate(String branchName) {
        def returnCode = script.sh(returnStatus: true, script: "git checkout ${branchName}") as int
        if(returnCode != 0) {
            script.sh "git checkout -b ${branchName}"
        }
    }

    /**
     * Merge branch into the current checked out branch.
     *
     * Note: In a multibranch pipeline Jenkins will only fetch the changed branch,
     * so you have to call {@link #fetch()} before merge.
     *
     * @param branchName name of branch to merge with
     */
    void merge(String branchName) {
        script.sh "git merge ${branchName}"
    }

    /**
     * Resolve the merge as a fast-forward when possible. When not possible,
     * refuse to merge and fails the build.
     *
     * Note: In a multibranch pipeline Jenkins will only fetch the changed branch,
     * so you have to call {@link #fetch()} before merge.
     *
     * @param branchName name of branch to merge with
     */
    void mergeFastForwardOnly(String branchName) {
        script.sh "git merge --ff-only ${branchName}"
    }

    /**
     * Resolve the merge as a non-fast-forward.
     *
     * Note: In a multibranch pipeline Jenkins will only fetch the changed branch,
     * so you have to call {@link #fetch()} before merge.
     *
     * @param branchName name of branch to merge with
     */
    void mergeNoFastForward(String branchName) {
        script.sh "git merge --no-ff ${branchName}"
    }

    /**
     * Pushes local to remote repo.
     *
     * @param refSpec branch or tag name
     */
    void push(String refSpec) {
        executeGitWithCredentials "push origin ${refSpec}"
    }

    /**
     * Removes a branch at origin.
     *
     * @param refSpec branch name
     */
    void deleteOriginBranch(String refSpec) {
        executeGitWithCredentials "push --delete origin ${refSpec}"
    }
    /**
     * Removes a local branch.
     *
     * @param refSpec branch name
     */
    void deleteLocalBranch(String refSpec) {
        script.sh "git branch -d ${refSpec}"
    }

    /**
     * Commits and pushes a folder to the <code>gh-pages</code> branch of the current repo.
     * Can be used to conveniently deliver websites. See https://pages.github.com/
     *
     * Uses the name and email of the last committer as author and committer.
     *
     * Note that the branch is temporarily checked out to the <code>.gh-pages</code> folder.
     *
     * @param workspaceFolder
     * @param commitMessage
     */
    @Deprecated
    void pushGitHubPagesBranch(String workspaceFolder, String commitMessage, String subFolder = '.') {
        def ghPagesTempDir = '.gh-pages'
        try {
            script.dir(ghPagesTempDir) {
                git url: repositoryUrl, branch: 'gh-pages', changelog: false, poll: false

                script.sh "mkdir -p ${subFolder}"
                script.sh "cp -rf ../${workspaceFolder}/* ${subFolder}"
                add '.'
                commit commitMessage
                push 'gh-pages'
            }
        } finally {
            script.sh "rm -rf ${ghPagesTempDir}"
        }
    }

    /**
     * This method executes the git command with a bash function as credential helper,
     * which return username and password from jenkins credentials.
     *
     * If the script failes with exit code 128, this will retry the call up to the
     * configured max retries before failing.
     *
     * @param args git arguments
     * @return Returns an array with a string array of two elements.
     *         The first element contains the command out put. The second element contans the command status code
     */
    protected String[] executeGitWithCredentials(String args) {
        if (credentials) {
            script.withCredentials([script.usernamePassword(credentialsId: credentials,
                    passwordVariable: 'GIT_AUTH_PSW', usernameVariable: 'GIT_AUTH_USR')]) {
                def pushResultCode = 128
                def commandOutput = ""
                def retryCount = 0
                while (pushResultCode == 128 && retryCount < maxRetries) {
                    if (retryCount > 0) {
                        script.echo "Got error code ${pushResultCode} - retrying in ${retryTimeout} ms ..."
                        sleep(retryTimeout)
                    }
                    ++retryCount
                    (commandOutput, pushResultCode) = executeGit("-c credential.helper=\"!f() { echo username='\$GIT_AUTH_USR'; echo password='\$GIT_AUTH_PSW'; }; f\" ${args}")
                    pushResultCode = pushResultCode as int
                }
                if (pushResultCode != 0) {
                    script.error "Unable to execute git call. Retried ${retryCount} times. Last error code: ${pushResultCode}"
                }
                return [commandOutput, pushResultCode]
            }
        }
        else return executeGit(args)
    }

    /**
     * Executes a git command.
     *
     * @param args git arguments
     * @return Returns an array with a string array of two elements.
     *         The first element contains the command out put. The second element contans the command status code
     */
    protected String[] executeGit(String args){
        def status = script.sh(
                script: "git ${args} > output",
                returnStatus: true
        )
        return [sh.returnStdOut("cat output"), status]
    }


    /**
     * Creates a git tag.
     *
     * @param tagName The name of the tag.
     * @param tagMessage The message of the tag.
     * @param force Force tag creation when true.
     *
     */
    void createTag(String tagName, String tagMessage, boolean force){
        script.sh "git tag${(force) ? ' -f': ''} -m '${tagMessage}' ${tagName}"
    }
}

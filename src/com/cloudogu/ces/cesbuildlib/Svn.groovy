package com.cloudogu.ces.cesbuildlib

class Svn implements Serializable {
    private script
    Sh sh
    def credentials = null
    def retryTimeout = 500
    def maxRetries = 5
    String committerName
    String committerEmail

    Svn(script, credentials) {
        this(script)
        this.credentials = credentials
    }

    Svn(script) {
        this.script = script
        this.sh = new Sh(script)
    }

    def call(args) {
        svn(args)
    }

    /**
     * Credential-aware wrapper around the global "checkout" step.
     */
    private def svn(args) {

        if (credentials != null) {

            // args instanceof Map ist not allowed due to sandboxing. So find it out the hard way :-(
            try {
                if (!args.containsKey('credentialsId')) {
                    args.put('credentialsId', credentials)
                }
            } catch (MissingMethodException ignored) {
                // This exception indicates that we don't have a Map. Assume url String and add credentials
                args = [url: args.toString(), credentialsId: credentials,local: '.']
            }
        }
        script.checkout([$class: 'SubversionSCM',
                         additionalCredentials: [],
                         excludedCommitMessages: '',
                         excludedRegions: '',
                         excludedRevprop: '',
                         excludedUsers: '',
                         filterChangelog: false,
                         ignoreDirPropChanges: false,
                         includedRegions: '',
                         locations: [[credentialsId: "${args.get('credentialsId')}",
                                      depthOption: 'infinity',
                                      ignoreExternalsOption: true,
                                      local: "${args.get('local')}",
                                      remote: "${args.get('url')}"]],
                         workspaceUpdater: [$class: 'UpdateUpdater']])
    }
}

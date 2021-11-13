#!/usr/bin/env groovy

import com.cloudogu.ces.cesbuildlib.MavenLocal

def call(Map params = [:]) {
    def mvnHome = tool "${params.version}"
    def javaHome = tool "${params.jdk}"
    def mvn = new MavenLocal(this, mvnHome, javaHome)
    mvn "${params.cmd}"
}

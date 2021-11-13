#!/usr/bin/env groovy
/**
 * Created by KeQingyuan on 2021/11/13.
 */
import com.cloudogu.ces.cesbuildlib.MavenLocal

def call(Map params = [:]) {
    def mvnHome = tool "${params.version}"
    def javaHome = tool "${params.jdk}"
    def mvn = new MavenLocal(this, mvnHome, javaHome)
    mvn "${params.cmd}"
}

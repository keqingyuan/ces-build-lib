import com.cloudogu.ces.cesbuildlib.MavenLocal

def call(Map config) {
    node {
        def mvnHome = tool 'M3'
        def javaHome = tool 'OpenJDK-8'
        def mvn = new MavenLocal(this, mvnHome, javaHome)
        mvn 'clean package'
    }
}

package com.cloudogu.ces.cesbuildlib.artifact

/**
 * Created by KeQingyuan on 2021/11/19.
 */
/**
 * Interface for classes handling artifacts.
 *
 * <p>For example, JFrog Artifactory class should implement this interface. If we support Nexus,
 * the class should also implement this interface.</p>
 */
interface ArtifactInterface {
    /**
     * Init artifactory configuration
     */
    void init(Map args)

    /**
     * Get artifact information
     */
    Map getArtifact(Map args)

    /**
     * Download artifacts
     */
    void download(Map args)

    /**
     * Upload an artifact
     */
    void upload(Map args)

    /**
     * Promote artifact
     *
     * @return full path to the promoted artifact
     */
    String promote(Map args)
}

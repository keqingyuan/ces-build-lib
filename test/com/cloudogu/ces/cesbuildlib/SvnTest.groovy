package com.cloudogu.ces.cesbuildlib

import org.junit.Test;

import static org.junit.Assert.*;

class SvnTest {

    @Test
    void testCallMap() throws Exception {
        Svn svn = new Svn(new ScriptMock())
        def result = svn url: "https://repoUrl", credentialsId: "creds", local: '.'
        assertEquals("https://repoUrl", result.get('locations')[0].get('remote').toString())
        assertEquals(".", result.get('locations')[0].get('local').toString())
        assertEquals("creds", result.get('locations')[0].get('credentialsId').toString())
    }

    @Test
    void testCallString() throws Exception {
        Svn svn = new Svn(new ScriptMock(),"creds2")
        def result = svn url: "https://repoUrl"
        assertEquals("https://repoUrl", result.get('locations')[0].get('remote').toString())
        assertEquals(".", result.get('locations')[0].get('local').toString())
        assertEquals("creds2", result.get('locations')[0].get('credentialsId').toString())
    }
}

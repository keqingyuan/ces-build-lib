#!/usr/bin/env groovy
/**
 * Created by KeQingyuan on 2021/11/13.
 */
import com.cloudogu.ces.cesbuildlib.Svn

def call(Map params=[:]){
    echo "从Svn仓库检出源码"
    Svn svn = new Svn(this, 'ourCredentials')
    svn url:"${params.url}", local:"${params.local}"
    echo "源码检出完成"
}

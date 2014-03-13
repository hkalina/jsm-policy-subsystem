package org.picketbox.jsmPolicy.subsystem;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.AccessControlException;

import org.junit.Before;
import org.junit.Test;
import org.picketbox.jsmpolicy.subsystem.extension.PolicyManager;

public class PolicyManagerTestCase {

    private File testingFile = new File("/etc/passwd");

    // Permission which PolicyManager has to have for switching
    private String requiredPermissions =
            "    permission java.lang.RuntimePermission \"setSecurityManager\";\n" +
            "    permission java.security.SecurityPermission \"getPolicy\";\n" +
            "    permission java.lang.RuntimePermission \"createSecurityManager\";\n" +
            "    permission java.util.PropertyPermission \"jboss.server.temp.dir\",\"read\";\n" +
            "    permission java.util.PropertyPermission \"java.security.policy\",\"read,write\";\n" +
            "    permission java.io.FilePermission \"${jboss.server.temp.dir}/-\",\"read,write,delete\";\n";

    private String minimalPolicy =
            "grant {\n" + requiredPermissions +
            "};";

    private String testingFileAllowingPolicy =
            "grant {\n" + requiredPermissions +
            "    permission java.io.FilePermission \""+testingFile.getAbsolutePath()+"\",\"read\";\n" +
            "};";


    @Before
    public void initPolicyManagerTests(){
        // ${jboss.server.temp.dir} := /tmp
        System.setProperty("jboss.server.temp.dir",System.getProperty("java.io.tmpdir"));
    }

    @Test
    public void testSetUnset() throws Exception {

        assertTrue(isTestingFileReadable());
        assertNull(System.getSecurityManager());

        PolicyManager.INSTANCE.setPolicyFile(minimalPolicy);

        assertFalse(isTestingFileReadable());
        assertNotNull(System.getSecurityManager());
        assertTrue(System.getSecurityManager() instanceof java.lang.SecurityManager);

        PolicyManager.INSTANCE.setPolicyFile(null);

        assertTrue(isTestingFileReadable());
        assertNull(System.getSecurityManager());

    }

    @Test
    public void testDifferentPoliciesFromMinimal() throws Exception {

        assertTrue(isTestingFileReadable());

        PolicyManager.INSTANCE.setPolicyFile(minimalPolicy);

        assertFalse(isTestingFileReadable());

        PolicyManager.INSTANCE.setPolicyFile(testingFileAllowingPolicy);

        assertTrue(isTestingFileReadable());

        PolicyManager.INSTANCE.setPolicyFile(null);

        assertTrue(isTestingFileReadable());

    }

    @Test
    public void testDifferentPoliciesFromMaximal() throws Exception {

        assertTrue(isTestingFileReadable());

        PolicyManager.INSTANCE.setPolicyFile(testingFileAllowingPolicy);

        assertTrue(isTestingFileReadable());

        PolicyManager.INSTANCE.setPolicyFile(minimalPolicy);

        assertFalse(isTestingFileReadable());

        PolicyManager.INSTANCE.setPolicyFile(null);

        assertTrue(isTestingFileReadable());

    }

    public boolean isTestingFileReadable() throws IOException{
        try{
            FileInputStream is = new FileInputStream(testingFile);
            is.read();
            is.close();
            System.out.println("Testing file readable");
            return true;
        }
        catch(AccessControlException e){
            System.out.println("Testing file not readable");
            return false;
        }
    }
}

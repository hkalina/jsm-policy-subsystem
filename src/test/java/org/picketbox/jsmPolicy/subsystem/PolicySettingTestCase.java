package org.picketbox.jsmPolicy.subsystem;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.AccessControlException;

import org.junit.Before;
import org.junit.Test;
import org.picketbox.jsmpolicy.subsystem.extension.PolicyManager;

public class PolicySettingTestCase {

    // two different files for testing file readability
    private File testingFile1 = new File("/etc/passwd");
    private File testingFile2 = new File("/etc/group");

    // Permission which PolicyManager has to have for switching
    private String requiredPermissions =
            "    permission java.lang.RuntimePermission \"setSecurityManager\";\n" +
            "    permission java.security.SecurityPermission \"getPolicy\";\n" +
            "    permission java.lang.RuntimePermission \"createSecurityManager\";\n" +
            "    permission java.util.PropertyPermission \"jboss.server.temp.dir\",\"read\";\n" +
            "    permission java.util.PropertyPermission \"java.security.policy\",\"read,write\";\n" +
            "    permission java.io.FilePermission \"${jboss.server.temp.dir}/-\",\"read,write,delete\";\n";

    private String policyMinimal =
            "grant {\n" + requiredPermissions +
            "};";

    private String policyAllowingTestingFile1 =
            "grant {\n" + requiredPermissions +
            "    permission java.io.FilePermission \""+testingFile1.getAbsolutePath()+"\",\"read\";\n" +
            "};";

    private String policyAllowingTestingFile2 =
            "grant {\n" + requiredPermissions +
            "    permission java.io.FilePermission \""+testingFile2.getAbsolutePath()+"\",\"read\";\n" +
            "};";


    @Before
    public void initPolicyManagerTests(){
        // ${jboss.server.temp.dir} := /tmp (directory for temporary policy files)
        System.setProperty("jboss.server.temp.dir",System.getProperty("java.io.tmpdir"));
    }

    @Test
    public void testSetUnsetSecurityManager() throws Exception {

        assertTrue(isFileReadable(testingFile1));
        assertNull(System.getSecurityManager());

        PolicyManager.INSTANCE.setPolicyFile(policyMinimal);

        assertFalse(isFileReadable(testingFile1));
        assertNotNull(System.getSecurityManager());
        assertTrue(System.getSecurityManager() instanceof java.lang.SecurityManager);

        PolicyManager.INSTANCE.setPolicyFile(null);

        assertTrue(isFileReadable(testingFile1));
        assertNull(System.getSecurityManager());

    }

    @Test
    public void testSetUnsetOnePolicy() throws Exception {

        assertTrue(isFileReadable(testingFile1));
        assertTrue(isFileReadable(testingFile2));

        PolicyManager.INSTANCE.setPolicyFile(policyAllowingTestingFile1);

        assertTrue(isFileReadable(testingFile1));
        assertFalse(isFileReadable(testingFile2));

        PolicyManager.INSTANCE.setPolicyFile(null);

        assertTrue(isFileReadable(testingFile1));
        assertTrue(isFileReadable(testingFile2));

    }

    @Test
    public void testPolicySwitching() throws Exception {

        assertTrue(isFileReadable(testingFile1));
        assertTrue(isFileReadable(testingFile2));

        PolicyManager.INSTANCE.setPolicyFile(policyAllowingTestingFile1);

        assertTrue(isFileReadable(testingFile1));
        assertFalse(isFileReadable(testingFile2));

        PolicyManager.INSTANCE.setPolicyFile(policyAllowingTestingFile2);

        assertFalse(isFileReadable(testingFile1));
        assertTrue(isFileReadable(testingFile2));

        PolicyManager.INSTANCE.setPolicyFile(null);

        assertTrue(isFileReadable(testingFile1));
        assertTrue(isFileReadable(testingFile2));

    }

    @Test
    public void testPolicySwitchingToTheSame() throws Exception {

        assertTrue(isFileReadable(testingFile1));
        assertTrue(isFileReadable(testingFile2));

        PolicyManager.INSTANCE.setPolicyFile(policyAllowingTestingFile1);

        assertTrue(isFileReadable(testingFile1));
        assertFalse(isFileReadable(testingFile2));

        PolicyManager.INSTANCE.setPolicyFile(policyAllowingTestingFile1);

        assertTrue(isFileReadable(testingFile1));
        assertFalse(isFileReadable(testingFile2));

    }

    public boolean isFileReadable(File file) throws IOException{
        try{
            FileInputStream is = new FileInputStream(file);
            is.read();
            is.close();
            //System.out.println("File "+file.getName()+" readable");
            return true;
        }
        catch(AccessControlException e){
            //System.out.println("File "+file.getName()+" not readable");
            return false;
        }
    }
}

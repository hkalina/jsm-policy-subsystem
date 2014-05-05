package org.picketbox.jsmpolicy.subsystem.extension;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.security.Policy;

import org.jboss.as.controller.OperationFailedException;
import org.jboss.logging.Logger;

/**
 * Mediates work between subsystem and security manager and policies
 */
public class PolicyManager {

    public static final PolicyManager INSTANCE = new PolicyManager();
    private static final Logger log = Logger.getLogger(PolicyManager.class);

    protected static String currentPolicyFileContent = null;

    private PolicyManager() {}

    /**
     * Set policy file used on this JVM by content
     *
     * @param fileContent Content of policy file to use
     * @throws OperationFailedException
     */
    public boolean setPolicyFile(String fileContent) throws OperationFailedException {
        log.info("Setting of new policy:\n" + fileContent);

        if (isCurrentPolicyFileContent(fileContent)) {
            log.warn("Setting of policy skipped - policy is already used");
            return false; // policy not need to be changed
        }

        validatePolicyFile(fileContent);
        log.info("Setting of policy: validation OK");

        if (fileContent == null) {
            setPolicy(null);
        } else {
            try {
                String tempDirString = System.getProperty("jboss.server.temp.dir", null);
                File tempDir = tempDirString == null ? null : new File(tempDirString);
                File temp = File.createTempFile("jsm-", ".policy", tempDir);

                FileOutputStream out = new FileOutputStream(temp);
                out.write(fileContent.getBytes());
                out.close();

                setPolicy(temp.getAbsolutePath());

                temp.delete();
            } catch (IOException e) {
                throw new OperationFailedException("setPolicyFile IOException: " +
                        e.getLocalizedMessage());
            }
        }
        currentPolicyFileContent = fileContent; // if all successful, set as current
        return true; // policy was changed
    }

    /**
     * Set policy file used on this JVM by file URL
     *
     * @param policy URL of policy file (null means disable JSM)
     * @throws OperationFailedException
     */
    public void setPolicy(String policy) throws OperationFailedException {
        log.info("Setting of policy from file " + policy);
        if (policy == null) {
            System.setSecurityManager(null);
        } else {
            System.setProperty("java.security.policy", policy);
            refreshDelegatingPolicy(); // DelegatingPolicy.delegate.refresh()
            System.setSecurityManager(new SecurityManager());
        }
    }

    /**
     * Refresh of policy on this server regardless if it is standard
     * policy object or JACC DelegatingPolicy class object
     */
    private void refreshDelegatingPolicy() {
        Policy p = Policy.getPolicy();
        p.refresh(); // standard policy refresh
        try {
            Class<?> delegatingPolicy = Class.forName("org.jboss.security.jacc.DelegatingPolicy");
            if (delegatingPolicy.isInstance(p)) {
                log.debug("Policy class is DelegatingPolicy and will be refreshed");
                Field f = delegatingPolicy.getDeclaredField("delegate");
                f.setAccessible(true);
                Policy in = (Policy) f.get(p);
                in.refresh();
            }
        } catch (Exception e) {
            log.error("Exception when try refresh DelegatingPolicy", e);
        }
    }

    /**
     * Allow test if input equals content of currently used policy file
     *
     * @param fileContent text to compare with used policy file
     * @return true if equals
     */
    public boolean isCurrentPolicyFileContent(String fileContent) {
        if (fileContent == null && currentPolicyFileContent == null)
            return true;
        if (fileContent == null)
            return false;
        return fileContent.equals(currentPolicyFileContent);
    }

    /**
     * Allow get currently used policy file on this server
     *
     * @return content of policy file
     */
    public String getCurrentPolicyFileContent() {
        return currentPolicyFileContent;
    }

    /**
     * Validate policy file by content
     *
     * @param fileContent
     * @throws OperationFailedException
     */
    @SuppressWarnings("restriction") // because PolicyParser is not public
    public void validatePolicyFile(String fileContent) throws OperationFailedException {
        if (fileContent == null)
            return;
        try {
            ByteArrayInputStream bais = new ByteArrayInputStream(fileContent.getBytes());
            InputStreamReader isr = new InputStreamReader(bais);

            sun.security.provider.PolicyParser pp = new sun.security.provider.PolicyParser(true);
            pp.read(isr);

            isr.close();
            bais.close();
        } catch (sun.security.provider.PolicyParser.ParsingException e) {
            throw new OperationFailedException("Policy file parsing exception: " +
                    e.getLocalizedMessage());
        } catch (IOException e) {
            throw new OperationFailedException("IOException when validating policy file: " +
                    e.getLocalizedMessage());
        }
    }

}

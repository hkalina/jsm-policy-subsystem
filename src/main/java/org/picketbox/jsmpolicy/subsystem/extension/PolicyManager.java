package org.picketbox.jsmpolicy.subsystem.extension;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.security.Policy;

import org.jboss.as.controller.OperationFailedException;
import org.jboss.logging.Logger;
import org.jboss.security.jacc.DelegatingPolicy;
import org.wildfly.security.manager.WildFlySecurityManager;

import sun.security.provider.PolicyParser;

/**
 * Work with security manager and security policy files
 */
public class PolicyManager {

	public static final PolicyManager INSTANCE = new PolicyManager();
	private static final Logger log = Logger.getLogger(PolicyManager.class);

	protected static String currentPolicyFileContent = null;
	protected SecurityManager oldSecurityManager = null;

	private PolicyManager(){}

	/**
	 * Set policy file used on this JVM by content
	 * @param fileContent Content of policy file to use
	 * @throws OperationFailedException
	 */
	public boolean setPolicyFile(String fileContent) throws OperationFailedException {

	    System.err.println("setPolicyFile("+fileContent+")");

	    /*
	    if(isCurrentPolicyFileContent(fileContent)){
	        log.warn("Setting of policy skipped - policy is already used");
	        return false; // policy need not to be changed
	    }
	    */

        validatePolicyFile(fileContent);
        log.info("Setting of policy - validation OK");

	    if(fileContent==null){
	        setPolicy(null);
	    }else{
	        try{
	            String tempDirString = System.getProperty("jboss.server.temp.dir",null);
	            File tempDir = tempDirString==null ? null : new File(tempDirString);
	            File temp = File.createTempFile("jsm-", ".policy", tempDir);

	            FileOutputStream out = new FileOutputStream(temp);
	            out.write(fileContent.getBytes());
	            out.close();

	            setPolicy(temp.getAbsolutePath());

	            temp.delete();
            }
            catch(IOException e){
                throw new OperationFailedException("setPolicyFile IOException: "+e.getLocalizedMessage());
            }
	    }
	    currentPolicyFileContent = fileContent; // if all successful, set as current
	    return true; // policy was changed
	}

	public void enableSecurityManager(){
	    SecurityManager sm = System.getSecurityManager();
	    if(sm!=null){
	        oldSecurityManager = sm;
	        return;
	    }
	    if(oldSecurityManager!=null){
	        System.setSecurityManager(oldSecurityManager);
	    }else{
	        WildFlySecurityManager.install();
	    }
	}

	public void disableSecurityManager(){
	    SecurityManager sm = System.getSecurityManager();
	    if(sm!=null){
	        oldSecurityManager = sm;
	    }
	    System.setSecurityManager(null);
    }

	/**
	 * Set policy file used on this JVM by file URL
	 * @param policy URL of policy file (null means disable JSM)
	 * @throws OperationFailedException
	 */
	public void setPolicy(String policy) throws OperationFailedException {
        log.info("Setting of policy from file "+policy);
        if(policy==null){
            disableSecurityManager();
        }else{
            System.setProperty("java.security.policy", policy);
            Policy.getPolicy().refresh();
            refreshDelegatingPolicy();
            System.err.println("refreshed "+Policy.getPolicy());
            enableSecurityManager();
        }
	}

	public void refreshDelegatingPolicy(){
	    Policy p = Policy.getPolicy();
	    try {
            Class<?> delegatingPolicy = Class.forName("org.jboss.security.jacc.DelegatingPolicy");
            if(delegatingPolicy.isInstance(p)){
                System.err.println("is delegating");

                Field f = delegatingPolicy.getDeclaredField("delegate");
                f.setAccessible(true);
                Policy in = (Policy) f.get(p);
                in.refresh();
                System.err.println("refreshed delegated "+in.toString());

            }else{
                System.err.println("not delegating");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
	}

	public boolean isCurrentPolicyFileContent(String fileContent){
	    if(fileContent==null && currentPolicyFileContent==null) return true;
	    if(fileContent==null && currentPolicyFileContent!=null) return false;
	    return fileContent.equals(currentPolicyFileContent);
	}

	public String getCurrentPolicyFileContent(){
	    return currentPolicyFileContent;
	}

	/**
	 * Validate policy file by content
	 * @param fileContent
	 * @throws OperationFailedException
	 */
	@SuppressWarnings("restriction")
	public void validatePolicyFile(String fileContent) throws OperationFailedException {
	    if(fileContent==null) return;
	    try{
	        ByteArrayInputStream bais = new ByteArrayInputStream(fileContent.getBytes());
	        InputStreamReader isr = new InputStreamReader(bais);

	        PolicyParser pp = new PolicyParser(true);
	        pp.read(isr);

	        isr.close();
	        bais.close();
	    }
	    catch(PolicyParser.ParsingException e){
	        throw new OperationFailedException("Policy file parsing exception: "+e.getLocalizedMessage());
	    }
	    catch(IOException e){
	        throw new OperationFailedException("IOException when validating policy file: "+e.getLocalizedMessage());
        }
	}

}

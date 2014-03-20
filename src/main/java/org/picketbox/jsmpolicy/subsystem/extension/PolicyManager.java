package org.picketbox.jsmpolicy.subsystem.extension;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.Policy;

import org.jboss.as.controller.OperationFailedException;
import org.jboss.logging.Logger;

import sun.security.provider.PolicyParser;

/**
 * Work with security manager and security policy files
 */
public class PolicyManager {

	public static final PolicyManager INSTANCE = new PolicyManager();

	private static final Logger log = Logger.getLogger(PolicyManager.class);

	protected static String currentPolicyFileContent = null;

	private PolicyManager(){}

	/**
	 * Set policy file used on this JVM by content
	 * @param fileContent Content of policy file to use
	 * @throws OperationFailedException
	 */
	public void setPolicyFile(String fileContent) throws OperationFailedException {

	    if(isCurrentPolicyFileContent(fileContent)){
	        log.warn("Setting of policy skipped - policy is already used");
	        return;
	    }

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
	}

	/**
	 * Set policy file used on this JVM by file URL
	 * @param policy URL of policy file (null means disable JSM)
	 * @throws OperationFailedException
	 */
	protected void setPolicy(String policy) throws OperationFailedException {
        log.info("Setting of policy from file "+policy);
        if(policy==null){
            System.setSecurityManager(null);
        }else{
            System.setProperty("java.security.policy", policy);
            Policy.getPolicy().refresh();
            System.setSecurityManager(new SecurityManager());
        }
	}

	protected boolean isCurrentPolicyFileContent(String fileContent){
	    if(fileContent==null && currentPolicyFileContent==null) return true;
	    if(fileContent==null && currentPolicyFileContent!=null) return false;
	    return fileContent.equals(currentPolicyFileContent); // fileContent!=null
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

	/**
	 * Experimental refresh for DelegatingPolicy
	 */
	public void refreshDelegatingPolicy(){
		try{
			Class.forName("org.jboss.security.jacc.DelegatingPolicy"); // catch not existing JACC
			if(Policy.getPolicy() instanceof org.jboss.security.jacc.DelegatingPolicy){
				((org.jboss.security.jacc.DelegatingPolicy)Policy.getPolicy()).getPolicyProxy().refresh();
				log.info("JsmPolicy: JACC refresh");
				return;
			}
		}
		catch(ClassNotFoundException e){}

		Policy.getPolicy().refresh(); // non-JACC policy
		log.info("JsmPolicy: non-JACC refresh");
	}

	/**
	 * Debug
	 */
	public void printStatus(){
		Policy p = Policy.getPolicy();
		String name = p==null ? "null" : p.getClass().getName();
		System.err.println("JsmPolicy: Policy="+name);
	}
}

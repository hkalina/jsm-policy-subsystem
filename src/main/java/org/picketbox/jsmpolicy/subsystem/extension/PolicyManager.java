package org.picketbox.jsmpolicy.subsystem.extension;

import java.io.File;
import java.io.FileInputStream;
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
@SuppressWarnings("restriction")
public class PolicyManager {

	public static final PolicyManager INSTANCE = new PolicyManager();

	private static final Logger log = Logger.getLogger(PolicyManager.class);

	private PolicyManager(){}

	/**
	 * Set policy file used on this JVM
	 * @param fileContent Content of policy file to use
	 * @throws OperationFailedException
	 */
	public void setPolicyFile(String fileContent) throws OperationFailedException {

	    System.err.println("setPolicyFile("+fileContent+")");

	    if(fileContent==null){
	        setPolicy(null);
	    }else{
	        try{
	            File temp = File.createTempFile("jsm-",".policy");

	            FileOutputStream out = new FileOutputStream(temp);
	            out.write(fileContent.getBytes());
	            out.close();

	            setPolicy(temp.getAbsolutePath());

	            //temp.delete();
            }
            catch(IOException e){
                throw new OperationFailedException("setPolicyFile IOException: "+e.getLocalizedMessage());
            }
	    }
	}

	/**
	 * Set policy file used on this JVM
	 * @param policy URL of policy file (null means disable JSM)
	 * @throws OperationFailedException
	 */
	public void setPolicy(String policy) throws OperationFailedException {
        System.err.println("setPolicy("+policy+")");
        if(policy==null){
            System.setSecurityManager(null);
        }else{
            validatePolicyFile(policy);
            System.err.println("Validation OK");

            System.setProperty("java.security.policy", policy);
            Policy.getPolicy().refresh();
            System.setSecurityManager(new SecurityManager());
        }
	}

	public void validatePolicyFile(String path) throws OperationFailedException {
	    try{
	        InputStreamReader isr = new InputStreamReader(new FileInputStream(path));
	        PolicyParser pp = new PolicyParser(true);
	        pp.read(isr);
	    }
	    catch(PolicyParser.ParsingException e){
	        throw new OperationFailedException("Parsing exception when parsing policy file from DMR: "+e.getLocalizedMessage());
	    }
	    catch(FileNotFoundException e){
	        throw new OperationFailedException("Temporary policy file not found: "+e.getLocalizedMessage());
	    }
	    catch(IOException e){
	        throw new OperationFailedException("IOException when validating policy file: "+e.getLocalizedMessage());
        }
	}

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

	public void printStatus(){
		Policy p = Policy.getPolicy();
		String name = p==null ? "null" : p.getClass().getName();
		System.err.println("JsmPolicy: Policy="+name);
	}
}

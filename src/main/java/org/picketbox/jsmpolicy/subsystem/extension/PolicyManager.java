package org.picketbox.jsmpolicy.subsystem.extension;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.Policy;

import org.jboss.as.controller.OperationFailedException;
import org.jboss.logging.Logger;

/**
 * Work with security manager and security policy files
 */
public class PolicyManager {

	public static final PolicyManager INSTANCE = new PolicyManager();

	private static final Logger log = Logger.getLogger(PolicyManager.class);

	private PolicyManager(){}

	/**
	 * Set policy file used on this JVM
	 * @param fileContent Content of policy file to use
	 * @throws IOException When creating of temporary file fails
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
	 */
	public void setPolicy(String policy){

        System.err.println("setPolicy("+policy+")");

        printStatus();

		if(policy==null){ // disable JSM

			log.info("JsmPolicy: unsetting policy");
			System.setSecurityManager(null);

		}else{ // enable JSM with specified policy file

			try{

				if(!new File(policy).canRead()){
					log.error("JsmPolicy: policy file \""+policy+"\" cannot be readed!");
					return; // Exception?
				}

				log.info("JsmPolicy: setting policy \""+policy+"\"");

				System.setProperty("java.security.policy", policy);

				//Policy.setPolicy(new JsmPolicy(Policy.getPolicy()));
				Policy.getPolicy().refresh();

				System.setSecurityManager(new SecurityManager());

				log.info("JsmPolicy: policy set successfuly");

			}catch(Exception e){
				log.error("JsmPolicy: setting policy failed: "+e.toString());
			}

		}

		printStatus();

	}

	public void refreshPolicy(){
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

package org.picketbox.jsmPolicy.subsystem.extension;

import java.security.Policy;

public class PolicyManager {
	
	public static final PolicyManager INSTANCE = new PolicyManager();
	
	private PolicyManager(){}
	
	/**
	 * Set policy used on this JVM
	 * @param policy URL of policy file
	 * NULL means disable JSM
	 */
	public void setPolicy(String policy){
		
		System.err.println("\n\nPolicyManager.setPolicy!!!\n\n");
		
		if(policy==null){ // disable JSM
			
			System.setSecurityManager(null);
			
		}else{ // enable JSM with specified policy file
			
			System.setProperty("java.security.policy", policy);
			Policy.getPolicy().refresh();
			System.setSecurityManager(new SecurityManager());
			
		}
		
	}
	
	
	
}

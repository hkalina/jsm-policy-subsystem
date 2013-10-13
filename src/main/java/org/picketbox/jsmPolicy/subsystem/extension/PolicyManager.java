package org.picketbox.jsmPolicy.subsystem.extension;

import java.security.Policy;

public class PolicyManager {
	
	public static final PolicyManager INSTANCE = new PolicyManager();
	
	private PolicyManager(){}
	
	/**
	 * Set policy used on this JVM
	 * @param policy URL of policy file (null or "undefined" means disable JSM)
	 */
	public void setPolicy(String policy){
		
		if(policy==null || policy.equals("undefined")){ // disable JSM
			
			System.err.println("PolicyManager.unsetPolicy");
			
			System.setSecurityManager(null);
			
		}else{ // enable JSM with specified policy file
			
			System.err.println("PolicyManager.setPolicy("+policy+")");
			
			System.setProperty("java.security.policy", policy);
			Policy.getPolicy().refresh();
			System.setSecurityManager(new SecurityManager());
			
		}
		
	}
	
	/**
	 * Set policy used on given server
	 * @param server server name
	 * @param policy URL of policy file (null or "undefined" means disable JSM)
	 */
	public void setPolicy(String server, String policy){
		
		if(server.equals(System.getProperty("jboss.server.name"))){
			System.err.println("setPolicy("+server+"=="+System.getProperty("jboss.server.name")+","+policy+")");
			setPolicy(policy);
		}else{
			System.err.println("PolicyManager.setPolicy("+server+"!="+System.getProperty("jboss.server.name")+","+policy+")");
		}
		
	}
	
}

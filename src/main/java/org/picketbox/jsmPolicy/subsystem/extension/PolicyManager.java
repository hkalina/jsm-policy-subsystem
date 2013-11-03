package org.picketbox.jsmPolicy.subsystem.extension;

import java.io.File;
import java.security.Policy;

import org.jboss.logging.Logger;

public class PolicyManager {
	
	public static final PolicyManager INSTANCE = new PolicyManager();
	
	private static final Logger log = Logger.getLogger("org.picketbox.jsmPolicy");
	
	private PolicyManager(){}
	
	/**
	 * Set policy used on this JVM
	 * @param policy URL of policy file (null or "undefined" means disable JSM)
	 */
	public void setPolicy(String policy){
		
		if(policy==null || policy.equals("undefined")){ // disable JSM
			
			log.info("JsmPolicy: unsetting policy");
			
			System.setSecurityManager(null);
			
		}else{ // enable JSM with specified policy file
			
			try{
			    
			    if(!new File(policy).isFile()){
			    	log.error("JsmPolicy: policy file \""+policy+"\" is not file!");
			    	return; // Exception?
			    }
			    
			    log.info("JsmPolicy: setting policy \""+policy+"\"");
			    
				System.setProperty("java.security.policy", policy);
				
				//if(Policy.getPolicy() instanceof org.jboss.security.jacc.DelegatingPolicy){
				//	((org.jboss.security.jacc.DelegatingPolicy)Policy.getPolicy()).getPolicyProxy().refresh();
				//}else{
					Policy.getPolicy().refresh();
				//}
			    
			    if(System.getSecurityManager()==null){
			        System.setSecurityManager(new SecurityManager());
			    }
			    
			    log.info("JsmPolicy: policy set successfuly");
			    
			}catch(Exception e){
				log.error("JsmPolicy: setting policy failed: "+e.toString());
			}
			
		}
		
	}
	
	/**
	 * Set policy used on given server
	 * @param server server name
	 * @param policy URL of policy file (null or "undefined" means disable JSM)
	 */
	public void setPolicy(String server, String policy){
		
		if(server.equals(System.getProperty("jboss.server.name"))){
			//System.err.println("setPolicy("+server+"=="+System.getProperty("jboss.server.name")+","+policy+")");
			setPolicy(policy);
		}else{
			//System.err.println("PolicyManager.setPolicy("+server+"!="+System.getProperty("jboss.server.name")+","+policy+")");
		}
		
	}
	
}

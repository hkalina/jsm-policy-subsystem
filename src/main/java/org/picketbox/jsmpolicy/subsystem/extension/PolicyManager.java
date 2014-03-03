package org.picketbox.jsmpolicy.subsystem.extension;

import java.io.File;
import java.security.Policy;

import org.jboss.logging.Logger;

public class PolicyManager {

	public static final PolicyManager INSTANCE = new PolicyManager();

	private static final Logger log = Logger.getLogger(PolicyManager.class);

	private PolicyManager(){}

	/**
	 * Set policy file used on this JVM
	 * @param policy URL of policy file (null means disable JSM)
	 */
	public void setPolicy(String policy){

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

				Policy.setPolicy(new JsmPolicy(Policy.getPolicy()));
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

	/**
	 * Set policy used on given server
	 * @param server server name
	 * @param policy URL of policy file (null or "undefined" means disable JSM)
	 */
	public void setServerPolicy(String server, String policy){

		if(policy!=null && policy.equals("undefined")) policy = null;

		if(server.equals(System.getProperty("jboss.server.name"))){
			log.info("setPolicy("+server+"=="+System.getProperty("jboss.server.name")+","+policy+")");
			System.err.println("setPolicy("+server+"=="+System.getProperty("jboss.server.name")+","+policy+")");
			setPolicy(policy);
		}else{
			log.info("PolicyManager.setPolicy("+server+"!="+System.getProperty("jboss.server.name")+","+policy+")");
			System.err.println("PolicyManager.setPolicy("+server+"!="+System.getProperty("jboss.server.name")+","+policy+")");
		}

	}

}

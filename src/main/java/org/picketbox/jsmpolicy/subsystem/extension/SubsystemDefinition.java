package org.picketbox.jsmpolicy.subsystem.extension;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.DESCRIBE;

import java.security.Policy;
import java.util.List;

import org.jboss.as.controller.AbstractBoottimeAddStepHandler;
import org.jboss.as.controller.AbstractRemoveStepHandler;
import org.jboss.as.controller.OperationContext;
import org.jboss.as.controller.OperationFailedException;
import org.jboss.as.controller.ServiceVerificationHandler;
import org.jboss.as.controller.SimpleResourceDefinition;
import org.jboss.as.controller.operations.common.GenericSubsystemDescribeHandler;
import org.jboss.as.controller.registry.ManagementResourceRegistration;
import org.jboss.as.controller.registry.OperationEntry;
import org.jboss.dmr.ModelNode;
import org.jboss.msc.service.ServiceController;

public class SubsystemDefinition extends SimpleResourceDefinition {

	public static final SubsystemDefinition INSTANCE = new SubsystemDefinition();

	private SubsystemDefinition() {
		super(JsmPolicyExtension.SUBSYSTEM_PATH,
		      JsmPolicyExtension.getResourceDescriptionResolver(null),
		      SubsystemAdd.INSTANCE,
		      SubsystemRemove.INSTANCE);
	}

	public void registerOperations(ManagementResourceRegistration resourceRegistration) {
		super.registerOperations(resourceRegistration);
		resourceRegistration.registerOperationHandler(DESCRIBE, GenericSubsystemDescribeHandler.INSTANCE,
				GenericSubsystemDescribeHandler.INSTANCE, false, OperationEntry.EntryType.PRIVATE);
	}

	static class SubsystemAdd extends AbstractBoottimeAddStepHandler {

	    static final SubsystemAdd INSTANCE = new SubsystemAdd();

	    private SubsystemAdd() {}

	    protected void populateModel(ModelNode operation, ModelNode model) throws OperationFailedException {}

	    public void performBoottime(OperationContext context, ModelNode operation, ModelNode model,
	            ServiceVerificationHandler verificationHandler, List<ServiceController<?>> newControllers)
	            throws OperationFailedException {

	        try{
	            Policy p = Policy.getPolicy();
	            System.err.println("POLICY: "+(p==null?"null":p.getClass().getName()));
	        }
	        catch(Exception e){}

	        try{
	            SecurityManager sm = System.getSecurityManager();
                System.err.println("SM: "+(sm==null?"null":sm.getClass().getName()));
            }
            catch(Exception e){}

	    }
	}

	static class SubsystemRemove extends AbstractRemoveStepHandler {

	    static final SubsystemRemove INSTANCE = new SubsystemRemove();

	    private SubsystemRemove() {}

	    protected void performRuntime(OperationContext context, ModelNode operation, ModelNode model) throws OperationFailedException {
	        PolicyManager.INSTANCE.setPolicyFile(null);
        }

	}
}

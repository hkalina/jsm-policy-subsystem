package org.picketbox.jsmpolicy.subsystem.extension;

import java.util.List;
import org.jboss.as.controller.AbstractAddStepHandler;
import org.jboss.as.controller.OperationContext;
import org.jboss.as.controller.OperationFailedException;
import org.jboss.as.controller.PathAddress;
import org.jboss.as.controller.ServiceVerificationHandler;
import org.jboss.as.controller.descriptions.ModelDescriptionConstants;
import org.jboss.dmr.ModelNode;
import org.jboss.msc.service.ServiceController;

class ServerAdd extends AbstractAddStepHandler {
	
    public static final ServerAdd INSTANCE = new ServerAdd();
    
    private ServerAdd() {}
    
    @Override
    protected void populateModel(ModelNode operation, ModelNode model) throws OperationFailedException {
    	ServerDefinition.POLICY.validateAndSet(operation, model);
    }
    
    @Override
    protected void performRuntime(OperationContext context, ModelNode operation, ModelNode model,
            ServiceVerificationHandler verificationHandler, List<ServiceController<?>> newControllers)
            throws OperationFailedException {
        
    	String serverName = PathAddress.pathAddress(operation.get(ModelDescriptionConstants.ADDRESS)).getLastElement().getValue();
    	PolicyManager.INSTANCE.setServerPolicy(serverName, model.get("policy").asString());
    	
    }
}

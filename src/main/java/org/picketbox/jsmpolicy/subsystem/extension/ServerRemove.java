package org.picketbox.jsmpolicy.subsystem.extension;

import org.jboss.as.controller.AbstractRemoveStepHandler;
import org.jboss.as.controller.OperationContext;
import org.jboss.as.controller.OperationFailedException;
import org.jboss.as.controller.PathAddress;
import org.jboss.as.controller.descriptions.ModelDescriptionConstants;
import org.jboss.dmr.ModelNode;

class ServerRemove extends AbstractRemoveStepHandler{
	
    public static final ServerRemove INSTANCE = new ServerRemove();
    
    private ServerRemove() {}
    
    protected void performRuntime(OperationContext context, ModelNode operation, ModelNode model) throws OperationFailedException {
    	
    	String serverName = PathAddress.pathAddress(operation.get(ModelDescriptionConstants.ADDRESS)).getLastElement().getValue();
    	PolicyManager.INSTANCE.setServerPolicy(serverName, null);
    	
    }
}

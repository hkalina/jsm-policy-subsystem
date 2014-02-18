package org.picketbox.jsmpolicy.subsystem.extension;

import java.util.List;

import org.jboss.as.controller.AbstractBoottimeAddStepHandler;
import org.jboss.as.controller.OperationContext;
import org.jboss.as.controller.OperationFailedException;
import org.jboss.as.controller.ServiceVerificationHandler;
import org.jboss.dmr.ModelNode;
import org.jboss.msc.service.ServiceController;

class SubsystemAdd extends AbstractBoottimeAddStepHandler {
	
	static final SubsystemAdd INSTANCE = new SubsystemAdd();
	
	private SubsystemAdd() {}
	
	protected void populateModel(ModelNode operation, ModelNode model) throws OperationFailedException {}
	
	public void performBoottime(OperationContext context, ModelNode operation, ModelNode model,
			ServiceVerificationHandler verificationHandler, List<ServiceController<?>> newControllers)
			throws OperationFailedException {}
}

package org.picketbox.jsmpolicy.subsystem.extension;

import org.jboss.as.controller.SimpleResourceDefinition;
import org.jboss.as.controller.operations.common.GenericSubsystemDescribeHandler;
import org.jboss.as.controller.registry.ManagementResourceRegistration;
import org.jboss.as.controller.registry.OperationEntry;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.DESCRIBE;

public class JsmPolicySubsystemDefinition extends SimpleResourceDefinition {
	
	public static final JsmPolicySubsystemDefinition INSTANCE = new JsmPolicySubsystemDefinition();
	
	private JsmPolicySubsystemDefinition() {
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
}

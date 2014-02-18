package org.picketbox.jsmpolicy.subsystem.extension;

import org.jboss.as.controller.SimpleAttributeDefinition;
import org.jboss.as.controller.SimpleAttributeDefinitionBuilder;
import org.jboss.as.controller.SimpleResourceDefinition;
import org.jboss.as.controller.registry.AttributeAccess;
import org.jboss.as.controller.registry.ManagementResourceRegistration;
import org.jboss.dmr.ModelType;

public class ServerDefinition extends SimpleResourceDefinition {
	
	public static final ServerDefinition INSTANCE = new ServerDefinition();
	
    protected static final SimpleAttributeDefinition POLICY =
            new SimpleAttributeDefinitionBuilder("policy", ModelType.STRING)
                    .setAllowExpression(true)
                    .setXmlName("policy")
                    .setFlags(AttributeAccess.Flag.RESTART_ALL_SERVICES)
                    .setDefaultValue(null)
                    .setAllowNull(true)
                    .build();
    
    private ServerDefinition() {
        super(JsmPolicyExtension.SERVER_PATH,
                JsmPolicyExtension.getResourceDescriptionResolver("server"),
                ServerAdd.INSTANCE,
                ServerRemove.INSTANCE);
    }
    
    public void registerAttributes(ManagementResourceRegistration resourceRegistration) {
        resourceRegistration.registerReadWriteAttribute(POLICY, null, JsmPolicyAttributeHandler.INSTANCE);
    }
}

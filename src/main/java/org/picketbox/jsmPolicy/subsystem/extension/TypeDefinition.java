package org.picketbox.jsmPolicy.subsystem.extension;

import org.jboss.as.controller.SimpleAttributeDefinition;
import org.jboss.as.controller.SimpleAttributeDefinitionBuilder;
import org.jboss.as.controller.SimpleResourceDefinition;
import org.jboss.as.controller.operations.validation.ModelTypeValidator;
import org.jboss.as.controller.registry.AttributeAccess;
import org.jboss.as.controller.registry.ManagementResourceRegistration;
import org.jboss.dmr.ModelNode;
import org.jboss.dmr.ModelType;

import static org.picketbox.jsmPolicy.subsystem.extension.JsmPolicyExtension.TYPE;
import static org.picketbox.jsmPolicy.subsystem.extension.JsmPolicyExtension.TYPE_PATH;

/**
 * @author <a href="tcerar@redhat.com">Tomaz Cerar</a>
 */
public class TypeDefinition extends SimpleResourceDefinition {
    public static final TypeDefinition INSTANCE = new TypeDefinition();

    protected static final SimpleAttributeDefinition TICK =
            new SimpleAttributeDefinitionBuilder(JsmPolicyExtension.TICK, ModelType.LONG)
                    .setAllowExpression(true)
                    .setXmlName(JsmPolicyExtension.TICK)
                    .setFlags(AttributeAccess.Flag.RESTART_ALL_SERVICES)
                    .setDefaultValue(new ModelNode(1000))
                    .setAllowNull(false)
                    .build();


    private TypeDefinition() {
        super(TYPE_PATH,
                JsmPolicyExtension.getResourceDescriptionResolver(TYPE),
                //We always need to add an 'add' operation
                TypeAdd.INSTANCE,
                //Every resource that is added, normally needs a remove operation
                TypeRemove.INSTANCE);
    }

    @Override
    public void registerAttributes(ManagementResourceRegistration resourceRegistration) {
        resourceRegistration.registerReadWriteAttribute(TICK, null, JsmPolicyTickHandler.INSTANCE);
    }
}

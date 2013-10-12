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

    protected static final SimpleAttributeDefinition POLICY =
            new SimpleAttributeDefinitionBuilder(JsmPolicyExtension.POLICY, ModelType.STRING)
                    .setAllowExpression(true)
                    .setXmlName(JsmPolicyExtension.POLICY)
                    .setFlags(AttributeAccess.Flag.RESTART_ALL_SERVICES)
                    .setDefaultValue(null)
                    .setAllowNull(true)
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
        resourceRegistration.registerReadWriteAttribute(POLICY, null, JsmPolicyTickHandler.INSTANCE);
    }
}

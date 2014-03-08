package org.picketbox.jsmpolicy.subsystem.extension;

import java.util.List;

import org.jboss.as.controller.AbstractAddStepHandler;
import org.jboss.as.controller.AbstractRemoveStepHandler;
import org.jboss.as.controller.AbstractWriteAttributeHandler;
import org.jboss.as.controller.OperationContext;
import org.jboss.as.controller.OperationFailedException;
import org.jboss.as.controller.PathAddress;
import org.jboss.as.controller.ServiceVerificationHandler;
import org.jboss.as.controller.SimpleAttributeDefinition;
import org.jboss.as.controller.SimpleAttributeDefinitionBuilder;
import org.jboss.as.controller.SimpleResourceDefinition;
import org.jboss.as.controller.descriptions.ModelDescriptionConstants;
import org.jboss.as.controller.registry.AttributeAccess;
import org.jboss.as.controller.registry.ManagementResourceRegistration;
import org.jboss.dmr.ModelNode;
import org.jboss.dmr.ModelType;
import org.jboss.msc.service.ServiceController;

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
        resourceRegistration.registerReadWriteAttribute(POLICY, null, ServerAttributeHandler.INSTANCE);
    }

    public static void useNewSettings(OperationContext context, ModelNode operation, ModelNode newPolicyValue) throws OperationFailedException {

        String changedServer = PathAddress.pathAddress(operation.get(ModelDescriptionConstants.ADDRESS)).getLastElement().getValue();
        String policy = newPolicyValue==null ? null : newPolicyValue.asString();
        if(policy!=null && policy.equals("undefined")) policy = null;

        if(System.getProperty("jboss.server.name").equals(changedServer)){
            String file = null;
            if(policy!=null){
                // getting policy file content
                ModelNode address = new ModelNode();
                address.add("subsystem", "jsmpolicy");
                address.add("policy", policy);

                ModelNode result = context.readResourceFromRoot(PathAddress.pathAddress(address)).getModel();
                file = result.get("file").asString();
            }
            PolicyManager.INSTANCE.setPolicyFile(file);
        }
    }

    static class ServerAdd extends AbstractAddStepHandler {

        public static final ServerAdd INSTANCE = new ServerAdd();

        private ServerAdd() {}

        protected void populateModel(ModelNode operation, ModelNode model) throws OperationFailedException {
            ServerDefinition.POLICY.validateAndSet(operation, model);
        }

        protected void performRuntime(OperationContext context, ModelNode operation, ModelNode model,
                ServiceVerificationHandler verificationHandler, List<ServiceController<?>> newControllers)
                throws OperationFailedException {

            useNewSettings(context, operation, model.get("policy"));

        }
    }

    static class ServerRemove extends AbstractRemoveStepHandler{

        public static final ServerRemove INSTANCE = new ServerRemove();

        private ServerRemove() {}

        protected void performRuntime(OperationContext context, ModelNode operation, ModelNode model) throws OperationFailedException {
            useNewSettings(context, operation, null);
        }
    }

    static class ServerAttributeHandler extends AbstractWriteAttributeHandler<Void> {

        public static final ServerAttributeHandler INSTANCE = new ServerAttributeHandler();

        private ServerAttributeHandler() {
            super(ServerDefinition.POLICY);
        }

        protected boolean applyUpdateToRuntime(OperationContext context, ModelNode operation, String attributeName,
                                               ModelNode resolvedValue, ModelNode currentValue, HandbackHolder<Void> handbackHolder) throws OperationFailedException {
            useNewSettings(context, operation, resolvedValue);
            return false; // restart not required
        }

        /**
         * Hook to allow subclasses to revert runtime changes
         * @param valueToRestore the previous value for the attribute, before this operation was executed
         * @param valueToRevert  the new value for the attribute that should be reverted
         * @throws OperationFailedException
         */
        protected void revertUpdateToRuntime(OperationContext context, ModelNode operation, String attributeName,
                                             ModelNode valueToRestore, ModelNode valueToRevert, Void handback) throws OperationFailedException {
            useNewSettings(context, operation, valueToRestore);
        }
    }
}

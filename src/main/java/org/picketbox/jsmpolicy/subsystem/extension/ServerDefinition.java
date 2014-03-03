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

    static class ServerAdd extends AbstractAddStepHandler {

        public static final ServerAdd INSTANCE = new ServerAdd();

        private ServerAdd() {}

        protected void populateModel(ModelNode operation, ModelNode model) throws OperationFailedException {
            ServerDefinition.POLICY.validateAndSet(operation, model);
        }

        protected void performRuntime(OperationContext context, ModelNode operation, ModelNode model,
                ServiceVerificationHandler verificationHandler, List<ServiceController<?>> newControllers)
                throws OperationFailedException {

            String serverName = PathAddress.pathAddress(operation.get(ModelDescriptionConstants.ADDRESS)).getLastElement().getValue();
            PolicyManager.INSTANCE.setServerPolicy(serverName, model.get("policy").asString());

        }
    }

    static class ServerRemove extends AbstractRemoveStepHandler{

        public static final ServerRemove INSTANCE = new ServerRemove();

        private ServerRemove() {}

        protected void performRuntime(OperationContext context, ModelNode operation, ModelNode model) throws OperationFailedException {

            String serverName = PathAddress.pathAddress(operation.get(ModelDescriptionConstants.ADDRESS)).getLastElement().getValue();
            PolicyManager.INSTANCE.setServerPolicy(serverName, null);

        }
    }

    static class ServerAttributeHandler extends AbstractWriteAttributeHandler<Void> {

        public static final ServerAttributeHandler INSTANCE = new ServerAttributeHandler();

        private ServerAttributeHandler() {
            super(ServerDefinition.POLICY);
        }

        /**
         * Hook to allow subclasses to make runtime changes to effect the attribute value change.
         *
         * @param context        the context of the operation
         * @param operation      the operation
         * @param attributeName  the name of the attribute being modified
         * @param resolvedValue  the new value for the attribute, after {@link ModelNode#resolve()} has been called on it
         * @param currentValue   the existing value for the attribute
         * @param handbackHolder holder for an arbitrary object to pass to
         *                       {@link #revertUpdateToRuntime(OperationContext, ModelNode, String, ModelNode, ModelNode, Object)} if
         *                       the operation needs to be rolled back
         * @return {@code true} if the server requires restart to effect the attribute
         *         value change; {@code false} if not
         */
        protected boolean applyUpdateToRuntime(OperationContext context, ModelNode operation, String attributeName,
                                               ModelNode resolvedValue, ModelNode currentValue, HandbackHolder<Void> handbackHolder) throws OperationFailedException {
            if (attributeName.equals("policy")) {
                final String serverName = PathAddress.pathAddress(operation.get(ModelDescriptionConstants.ADDRESS)).getLastElement().getValue();
                PolicyManager.INSTANCE.setServerPolicy(serverName, resolvedValue.asString());
            }
            return false;
        }

        /**
         * Hook to allow subclasses to revert runtime changes made in
         * {@link #applyUpdateToRuntime(OperationContext, ModelNode, String, ModelNode, ModelNode, HandbackHolder)}.
         *
         * @param context        the context of the operation
         * @param operation      the operation
         * @param attributeName  the name of the attribute being modified
         * @param valueToRestore the previous value for the attribute, before this operation was executed
         * @param valueToRevert  the new value for the attribute that should be reverted
         * @param handback       an object, if any, passed in to the {@code handbackHolder} by the {@code applyUpdateToRuntime}
         *                       implementation
         */
        protected void revertUpdateToRuntime(OperationContext context, ModelNode operation, String attributeName,
                                             ModelNode valueToRestore, ModelNode valueToRevert, Void handback) {
            if (attributeName.equals("policy")) {
                final String serverName = PathAddress.pathAddress(operation.get(ModelDescriptionConstants.ADDRESS)).getLastElement().getValue();
                PolicyManager.INSTANCE.setServerPolicy(serverName, valueToRevert.asString());
            }
        }
    }
}

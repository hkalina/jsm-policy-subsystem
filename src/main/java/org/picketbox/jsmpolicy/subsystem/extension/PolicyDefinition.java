package org.picketbox.jsmpolicy.subsystem.extension;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

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
import org.jboss.as.controller.registry.AttributeAccess;
import org.jboss.as.controller.registry.ManagementResourceRegistration;
import org.jboss.as.controller.registry.Resource.ResourceEntry;
import org.jboss.dmr.ModelNode;
import org.jboss.dmr.ModelType;
import org.jboss.logging.Logger;
import org.jboss.msc.service.ServiceController;

public class PolicyDefinition extends SimpleResourceDefinition {

    public static final PolicyDefinition INSTANCE = new PolicyDefinition();

    private static final Logger log = Logger.getLogger(PolicyDefinition.class);

    protected static final SimpleAttributeDefinition FILE =
            new SimpleAttributeDefinitionBuilder("file", ModelType.STRING)
                    .setAllowExpression(true)
                    .setXmlName("file")
                    .setFlags(AttributeAccess.Flag.RESTART_ALL_SERVICES)
                    .setDefaultValue(null)
                    .setAllowNull(true)
                    .build();

    private PolicyDefinition() {
        super(JsmPolicyExtension.POLICY_PATH,
                JsmPolicyExtension.getResourceDescriptionResolver("policy"),
                PolicyAdd.INSTANCE,
                PolicyRemove.INSTANCE);
    }

    public void registerAttributes(ManagementResourceRegistration resourceRegistration) {
        resourceRegistration.registerReadWriteAttribute(FILE, null, PolicyWriteAttributeHandler.INSTANCE);
    }

    private static void refreshRelatedServers(OperationContext context, ModelNode operation, ModelNode resolvedValue) throws OperationFailedException{
        String newFileValue = resolvedValue.asString();
        String affectedPolicy = operation.get("address").get(1).get("policy").asString();

        // TODO: optimalize
        ModelNode address = new ModelNode();
        address.add("subsystem", "jsmpolicy");

        Set<ResourceEntry> set = context.readResourceFromRoot(PathAddress.pathAddress(address),true).getChildren("server");
        Iterator<ResourceEntry> it = set.iterator();
        while(it.hasNext()){
            ResourceEntry entry = it.next();
            String server = entry.getName();
            String policyOfServer = entry.getModel().get("policy").asString();

            if(policyOfServer.equals(affectedPolicy)){
                if(System.getProperty("jboss.server.name").equals(server)){
                    log.info("Policy \""+policyOfServer+"\" used on server \""+server+"\" changed. Reloading...");
                    PolicyManager.INSTANCE.setPolicyFile(newFileValue);
                }
            }
        }
    }

    static class PolicyAdd extends AbstractAddStepHandler {

        public static final PolicyAdd INSTANCE = new PolicyAdd();

        private PolicyAdd() {}

        protected void populateModel(ModelNode operation, ModelNode model) throws OperationFailedException {
            PolicyDefinition.FILE.validateAndSet(operation, model);
        }

        protected void performRuntime(OperationContext context, ModelNode operation, ModelNode model,
                ServiceVerificationHandler verificationHandler, List<ServiceController<?>> newControllers)
                throws OperationFailedException {
        }
    }

    static class PolicyRemove extends AbstractRemoveStepHandler {
        public static final PolicyRemove INSTANCE = new PolicyRemove();

        private PolicyRemove() {}

        protected void performRuntime(OperationContext context, ModelNode operation, ModelNode model)
                throws OperationFailedException {
        }
    }

    static class PolicyWriteAttributeHandler extends AbstractWriteAttributeHandler<Void> {

        public static final PolicyWriteAttributeHandler INSTANCE = new PolicyWriteAttributeHandler();

        private PolicyWriteAttributeHandler() {
            super(PolicyDefinition.FILE);
        }

        protected boolean applyUpdateToRuntime(OperationContext context, ModelNode operation, String attributeName,
                ModelNode resolvedValue, ModelNode currentValue, HandbackHolder<Void> handbackHolder)
                throws OperationFailedException {

            refreshRelatedServers(context, operation, resolvedValue);

            return false; // restart not required
        }

        protected void revertUpdateToRuntime(OperationContext context, ModelNode operation, String attributeName,
                ModelNode valueToRestore, ModelNode valueToRevert, Void handback) {
        }
    }

}

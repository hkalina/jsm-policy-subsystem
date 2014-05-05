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
import org.jboss.as.controller.operations.validation.ParameterValidator;
import org.jboss.as.controller.registry.AttributeAccess;
import org.jboss.as.controller.registry.ManagementResourceRegistration;
import org.jboss.as.controller.registry.Resource;
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
            .setAllowExpression(true).setXmlName("file").setFlags(AttributeAccess.Flag.RESTART_ALL_SERVICES)
            .setDefaultValue(null).setAllowNull(true).setValidator(new ParameterValidator() {
                public void validateParameter(String parameterName, ModelNode value) throws OperationFailedException {
                    if (value.getType() == ModelType.UNDEFINED)
                        return;
                    if (value.getType() != ModelType.STRING)
                        throw new PolicyFileUnvalidException("Content of policy file must be string!");
                    PolicyManager.INSTANCE.validatePolicyFile(value.asString());
                }

                public void validateResolvedParameter(String parameterName, ModelNode value) throws OperationFailedException {
                    validateParameter(parameterName, value);
                }
            }).build();

    private PolicyDefinition() {
        super(JsmPolicyExtension.POLICY_PATH, JsmPolicyExtension.getResourceDescriptionResolver("policy"),
                PolicyAdd.INSTANCE, PolicyRemove.INSTANCE);
    }

    public void registerAttributes(ManagementResourceRegistration resourceRegistration) {
        resourceRegistration.registerReadWriteAttribute(FILE, null, PolicyWriteAttribute.INSTANCE);
    }

    private static void refreshIfServerRelated(OperationContext context, ModelNode operation, ModelNode resolvedValue) throws OperationFailedException {

        String newFileContentValue = resolvedValue.asString();
        String affectedPolicyName = operation.get("address").get(1).get("policy").asString();
        String thisServerName = System.getProperty("jboss.server.name");

        ModelNode address = new ModelNode();
        address.add("subsystem", "jsmpolicy");
        address.add("server", thisServerName);

        try {
            Resource resource = context.readResourceFromRoot(PathAddress.pathAddress(address));
            ModelNode policyNode = resource.getModel().get("policy");

            if (policyNode.getType() == ModelType.STRING || policyNode.getType() == ModelType.EXPRESSION) {
                if(policyNode.asString().equals(affectedPolicyName)){
                    log.info("Currently used policy " + affectedPolicyName + " changed " +
                        "- refreshing server " + thisServerName);
                    PolicyManager.INSTANCE.setPolicyFile(newFileContentValue);
                }
            }
        }
        catch (RuntimeException e) {
            if (!e.getMessage().endsWith("not found")) { // ignore if server not exist in DMR
                throw new OperationFailedException("Refreshing server " + thisServerName + ", " +
                    "because using of modified policy " + affectedPolicyName + ", failed: " + e.getMessage(), e);
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

            String deletingPolicyName = operation.get("address").get(1).get("policy").asString();
            ModelNode address = new ModelNode();
            address.add("subsystem", "jsmpolicy");

            Set<ResourceEntry> set = context.readResourceFromRoot(PathAddress.pathAddress(address), true).getChildren("server");
            Iterator<ResourceEntry> it = set.iterator();
            while (it.hasNext()) {
                ResourceEntry server = it.next();
                String serverName = server.getName();
                String serverPolicy = server.getModel().get("policy").asString();

                // for servers using deleting policy
                if (serverPolicy.equals(deletingPolicyName) && serverName.equals(System.getProperty("jboss.server.name"))) {
                    throw new OperationFailedException("Removing policy " + deletingPolicyName + " failed - policy is deployed on server " + serverName);
                }
            }

        }
    }

    static class PolicyWriteAttribute extends AbstractWriteAttributeHandler<Void> {

        public static final PolicyWriteAttribute INSTANCE = new PolicyWriteAttribute();

        private PolicyWriteAttribute() {
            super(PolicyDefinition.FILE);
        }

        protected boolean applyUpdateToRuntime(OperationContext context, ModelNode operation, String attributeName,
                ModelNode resolvedValue, ModelNode currentValue, HandbackHolder<Void> handbackHolder)
                throws OperationFailedException {

            refreshIfServerRelated(context, operation, resolvedValue);

            return false; // restart not required
        }

        protected void revertUpdateToRuntime(OperationContext context, ModelNode operation, String attributeName,
                ModelNode valueToRestore, ModelNode valueToRevert, Void handback) {
        }
    }

    static class PolicyFileUnvalidException extends OperationFailedException {
        private static final long serialVersionUID = -4334271890304426959L;

        public PolicyFileUnvalidException(String message) {
            super(message);
        }
    }
}

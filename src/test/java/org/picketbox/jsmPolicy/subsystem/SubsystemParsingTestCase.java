package org.picketbox.jsmPolicy.subsystem;


import junit.framework.Assert;

import org.jboss.as.controller.PathAddress;
import org.jboss.as.controller.PathElement;
import org.jboss.as.subsystem.test.AbstractSubsystemTest;
import org.jboss.as.subsystem.test.KernelServices;
import org.jboss.dmr.ModelNode;
import org.junit.Test;
import org.picketbox.jsmpolicy.subsystem.extension.JsmPolicyExtension;

import java.util.List;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.ADD;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.DESCRIBE;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.NAME;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP_ADDR;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OUTCOME;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.READ_ATTRIBUTE_OPERATION;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.SUBSYSTEM;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.SUCCESS;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.VALUE;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.WRITE_ATTRIBUTE_OPERATION;


/**
 * @author <a href="kabir.khan@jboss.com">Kabir Khan</a>
 */
public class SubsystemParsingTestCase extends AbstractSubsystemTest {

    public SubsystemParsingTestCase() {
        super(JsmPolicyExtension.SUBSYSTEM_NAME, new JsmPolicyExtension());
    }

    /**
     * Tests that the xml is parsed into the correct operations
     */
    
    @Test
    public void testParseSubsystem() throws Exception {
        //Parse the subsystem xml into operations
        String subsystemXml =
                "<subsystem xmlns=\"" + JsmPolicyExtension.NAMESPACE + "\">" +
                        "   <servers>" +
                        "       <server name=\"test-server\" policy=\"test.policy\"/>" +
                        "   </servers>" +
                        "</subsystem>";
        List<ModelNode> operations = super.parse(subsystemXml);

        ///Check that we have the expected number of operations
        Assert.assertEquals(2, operations.size());

        //Check that each operation has the correct content
        //The add subsystem operation will happen first
        ModelNode addSubsystem = operations.get(0);
        Assert.assertEquals(ADD, addSubsystem.get(OP).asString());
        PathAddress addr = PathAddress.pathAddress(addSubsystem.get(OP_ADDR));
        Assert.assertEquals(1, addr.size());
        PathElement element = addr.getElement(0);
        Assert.assertEquals(SUBSYSTEM, element.getKey());
        Assert.assertEquals(JsmPolicyExtension.SUBSYSTEM_NAME, element.getValue());

        //Then we will get the add type operation
        ModelNode addType = operations.get(1);
        Assert.assertEquals(ADD, addType.get(OP).asString());
        Assert.assertEquals("test.policy", addType.get("policy").asString());
        addr = PathAddress.pathAddress(addType.get(OP_ADDR));
        Assert.assertEquals(2, addr.size());
        element = addr.getElement(0);
        Assert.assertEquals(SUBSYSTEM, element.getKey());
        Assert.assertEquals(JsmPolicyExtension.SUBSYSTEM_NAME, element.getValue());
        element = addr.getElement(1);
        Assert.assertEquals("server", element.getKey());
        Assert.assertEquals("test-server", element.getValue());
    }
    

    /**
     * Test that the model created from the xml looks as expected
     */
    
    @Test
    public void testInstallIntoController() throws Exception {
        //Parse the subsystem xml and install into the controller
        String subsystemXml =
                "<subsystem xmlns=\"" + JsmPolicyExtension.NAMESPACE + "\">" +
                        "   <servers>" +
                        "       <server name=\"test-server\" policy=\"test.policy\"/>" +
                        "   </servers>" +
                        "</subsystem>";
        KernelServices services = super.installInController(subsystemXml);

        //Read the whole model and make sure it looks as expected
        ModelNode model = services.readWholeModel();
        //Useful for debugging :-)
        //System.out.println(model);
        Assert.assertTrue(model.get(SUBSYSTEM).hasDefined(JsmPolicyExtension.SUBSYSTEM_NAME));
        Assert.assertTrue(model.get(SUBSYSTEM, JsmPolicyExtension.SUBSYSTEM_NAME).hasDefined("server"));
        Assert.assertTrue(model.get(SUBSYSTEM, JsmPolicyExtension.SUBSYSTEM_NAME, "server").hasDefined("test-server"));
        Assert.assertTrue(model.get(SUBSYSTEM, JsmPolicyExtension.SUBSYSTEM_NAME, "server", "test-server").hasDefined("policy"));
        Assert.assertEquals("test.policy", model.get(SUBSYSTEM, JsmPolicyExtension.SUBSYSTEM_NAME, "server", "test-server", "policy").asString());
    }
    

    /**
     * Starts a controller with a given subsystem xml and then checks that a second
     * controller started with the xml marshalled from the first one results in the same model
     */
    
    @Test
    public void testParseAndMarshalModel() throws Exception {
        //Parse the subsystem xml and install into the first controller
        String subsystemXml =
        		"<subsystem xmlns=\"" + JsmPolicyExtension.NAMESPACE + "\">" +
                        "   <servers>" +
                        "       <server name=\"test-server\" policy=\"test.policy\"/>" +
                        "   </servers>" +
                        "</subsystem>";
        KernelServices servicesA = super.installInController(subsystemXml);
        //Get the model and the persisted xml from the first controller
        ModelNode modelA = servicesA.readWholeModel();
        String marshalled = servicesA.getPersistedSubsystemXml();

        //Install the persisted xml from the first controller into a second controller
        KernelServices servicesB = super.installInController(marshalled);
        ModelNode modelB = servicesB.readWholeModel();

        //Make sure the models from the two controllers are identical
        super.compare(modelA, modelB);
    }
    

    /**
     * Starts a controller with the given subsystem xml and then checks that a second
     * controller started with the operations from its describe action results in the same model
     */
    
    @Test
    public void testDescribeHandler() throws Exception {
        //Parse the subsystem xml and install into the first controller
        String subsystemXml =
                "<subsystem xmlns=\"" + JsmPolicyExtension.NAMESPACE + "\">" +
                        "</subsystem>";
        KernelServices servicesA = super.installInController(subsystemXml);
        //Get the model and the describe operations from the first controller
        ModelNode modelA = servicesA.readWholeModel();
        ModelNode describeOp = new ModelNode();
        describeOp.get(OP).set(DESCRIBE);
        describeOp.get(OP_ADDR).set(
                PathAddress.pathAddress(
                        PathElement.pathElement(SUBSYSTEM, JsmPolicyExtension.SUBSYSTEM_NAME)).toModelNode());
        List<ModelNode> operations = super.checkResultAndGetContents(servicesA.executeOperation(describeOp)).asList();

        //Install the describe options from the first controller into a second controller
        KernelServices servicesB = super.installInController(operations);
        ModelNode modelB = servicesB.readWholeModel();

        //Make sure the models from the two controllers are identical
        super.compare(modelA, modelB);

    }
    
    
    /**
     * Tests that the subsystem can be removed
     */
    
    @Test
    public void testSubsystemRemoval() throws Exception {
        //Parse the subsystem xml and install into the first controller
        String subsystemXml =
        		"<subsystem xmlns=\"" + JsmPolicyExtension.NAMESPACE + "\">" +
                        "   <servers>" +
                        "       <server name=\"test-server\" policy=\"test.policy\"/>" +
                        "   </servers>" +
                        "</subsystem>";
        KernelServices services = super.installInController(subsystemXml);
        
        //Checks that the subsystem was removed from the model
        super.assertRemoveSubsystemResources(services);
        
    }
    
    @Test
    public void testExecuteOperations() throws Exception {
        String subsystemXml =
        		"<subsystem xmlns=\"" + JsmPolicyExtension.NAMESPACE + "\">" +
                        "   <servers>" +
                        "       <server name=\"test-server\" policy=\"test.policy\"/>" +
                        "   </servers>" +
                        "</subsystem>";
        KernelServices services = super.installInController(subsystemXml);

        //Add new server
        PathAddress fooTypeAddr = PathAddress.pathAddress(
                PathElement.pathElement(SUBSYSTEM, JsmPolicyExtension.SUBSYSTEM_NAME),
                PathElement.pathElement("server", "foo"));
        ModelNode addOp = new ModelNode();
        addOp.get(OP).set(ADD);
        addOp.get(OP_ADDR).set(fooTypeAddr.toModelNode());
        addOp.get("policy").set("foo.policy");
        ModelNode result = services.executeOperation(addOp);
        Assert.assertEquals(SUCCESS, result.get(OUTCOME).asString());

        // Check state after add
        ModelNode model = services.readWholeModel();
        Assert.assertTrue(model.get(SUBSYSTEM).hasDefined(JsmPolicyExtension.SUBSYSTEM_NAME));
        Assert.assertTrue(model.get(SUBSYSTEM, JsmPolicyExtension.SUBSYSTEM_NAME).hasDefined("server"));
        Assert.assertTrue(model.get(SUBSYSTEM, JsmPolicyExtension.SUBSYSTEM_NAME, "server").hasDefined("test-server"));
        Assert.assertTrue(model.get(SUBSYSTEM, JsmPolicyExtension.SUBSYSTEM_NAME, "server", "test-server").hasDefined("policy"));
        Assert.assertEquals("test.policy", model.get(SUBSYSTEM, JsmPolicyExtension.SUBSYSTEM_NAME, "server", "test-server", "policy").asString()); // ????
        Assert.assertTrue(model.get(SUBSYSTEM, JsmPolicyExtension.SUBSYSTEM_NAME, "server").hasDefined("foo"));
        Assert.assertTrue(model.get(SUBSYSTEM, JsmPolicyExtension.SUBSYSTEM_NAME, "server", "foo").hasDefined("policy"));
        Assert.assertEquals("foo.policy", model.get(SUBSYSTEM, JsmPolicyExtension.SUBSYSTEM_NAME, "server", "foo", "policy").asString());
        
        //Call write-attribute
        ModelNode writeOp = new ModelNode();
        writeOp.get(OP).set(WRITE_ATTRIBUTE_OPERATION);
        writeOp.get(OP_ADDR).set(fooTypeAddr.toModelNode());
        writeOp.get(NAME).set("policy");
        writeOp.get(VALUE).set("second.policy");
        result = services.executeOperation(writeOp);
        Assert.assertEquals(SUCCESS, result.get(OUTCOME).asString());
        
        //Check that write attribute took effect, this time by calling read-attribute instead of reading the whole model
        ModelNode readOp = new ModelNode();
        readOp.get(OP).set(READ_ATTRIBUTE_OPERATION);
        readOp.get(OP_ADDR).set(fooTypeAddr.toModelNode());
        readOp.get(NAME).set("policy");
        result = services.executeOperation(readOp);
        Assert.assertEquals("second.policy", checkResultAndGetContents(result).asString());
        
        //Check that write attribute took effect using getPolicy
        //JsmPolicyService service = (JsmPolicyService) services.getContainer().getService(JsmPolicyService.createServiceName("foo")).getValue();
        //Assert.assertEquals(3456, service.getTick());
        
    }
    
}

package org.picketbox.jsmPolicy.subsystem;

import java.util.List;

import junit.framework.Assert;

import org.jboss.as.controller.PathAddress;
import org.jboss.as.controller.PathElement;
import org.jboss.as.subsystem.test.AbstractSubsystemTest;
import org.jboss.as.subsystem.test.KernelServices;
import org.jboss.dmr.ModelNode;
import org.jboss.dmr.ModelType;
import org.junit.Before;
import org.junit.Test;
import org.picketbox.jsmpolicy.subsystem.extension.JsmPolicyExtension;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.*;

/**
 * Unit test case of reading subsystem configuration from the XML
 *
 * @author <a href="xkalin03@stud.fit.vutbr.cz">Jan Kalina</a>
 * According example subsystem test by
 * @author <a href="kabir.khan@jboss.com">Kabir Khan</a>
 */
public class SubsystemParserTestCase extends AbstractSubsystemTest {

    public SubsystemParserTestCase() {
        super(JsmPolicyExtension.SUBSYSTEM_NAME, new JsmPolicyExtension());
    }

    @Before
    public void before() {
        System.setProperty("jboss.modules.policy-refreshable", "true");
    }

    @Test
    public void testParseFullSubsystem() throws Exception {

        String subsystemXml = "<subsystem xmlns=\"" + JsmPolicyExtension.NAMESPACE + "\">" + "   <servers>"
                + "       <server name=\"test-server\" policy=\"test-policy\"/>" + "   </servers>" + "   <policies>"
                + "       <policy name=\"test-policy\" file=\"grant { permission java.security.AllPermission; };\"/>"
                + "   </policies>" + "</subsystem>";

        // this test check operations created by parsing subsystem XML
        List<ModelNode> operations = super.parse(subsystemXml);

        Assert.assertEquals(3, operations.size());

        ModelNode addSubsystem = operations.get(0);
        Assert.assertEquals(ADD, addSubsystem.get(OP).asString());
        PathAddress addr = PathAddress.pathAddress(addSubsystem.get(OP_ADDR));
        Assert.assertEquals(1, addr.size());
        PathElement element = addr.getElement(0);
        Assert.assertEquals(SUBSYSTEM, element.getKey());
        Assert.assertEquals(JsmPolicyExtension.SUBSYSTEM_NAME, element.getValue());

        ModelNode addServer = operations.get(1);
        Assert.assertEquals(ADD, addServer.get(OP).asString());
        Assert.assertEquals("test-policy", addServer.get("policy").asString());
        addr = PathAddress.pathAddress(addServer.get(OP_ADDR));
        Assert.assertEquals(2, addr.size());
        element = addr.getElement(0);
        Assert.assertEquals(SUBSYSTEM, element.getKey());
        Assert.assertEquals(JsmPolicyExtension.SUBSYSTEM_NAME, element.getValue());
        element = addr.getElement(1);
        Assert.assertEquals("server", element.getKey());
        Assert.assertEquals("test-server", element.getValue());

        ModelNode addPolicy = operations.get(2);
        Assert.assertEquals(ADD, addPolicy.get(OP).asString());
        Assert.assertEquals("grant { permission java.security.AllPermission; };", addPolicy.get("file").asString());
        addr = PathAddress.pathAddress(addPolicy.get(OP_ADDR));
        Assert.assertEquals(2, addr.size());
        element = addr.getElement(0);
        Assert.assertEquals(SUBSYSTEM, element.getKey());
        Assert.assertEquals(JsmPolicyExtension.SUBSYSTEM_NAME, element.getValue());
        element = addr.getElement(1);
        Assert.assertEquals("policy", element.getKey());
        Assert.assertEquals("test-policy", element.getValue());

    }

    @Test
    public void testParseFullUndefinedSubsystem() throws Exception {

        String subsystemXml = "<subsystem xmlns=\"" + JsmPolicyExtension.NAMESPACE + "\">" + "   <servers>"
                + "       <server name=\"test-server\"/>" + "   </servers>" + "   <policies>"
                + "       <policy name=\"test-policy\"/>" + "   </policies>" + "</subsystem>";

        // this test check operations created by parsing subsystem XML
        List<ModelNode> operations = super.parse(subsystemXml);

        Assert.assertEquals(3, operations.size());

        ModelNode addSubsystem = operations.get(0);
        Assert.assertEquals(ADD, addSubsystem.get(OP).asString());
        PathAddress addr = PathAddress.pathAddress(addSubsystem.get(OP_ADDR));
        Assert.assertEquals(1, addr.size());
        PathElement element = addr.getElement(0);
        Assert.assertEquals(SUBSYSTEM, element.getKey());
        Assert.assertEquals(JsmPolicyExtension.SUBSYSTEM_NAME, element.getValue());

        ModelNode addServer = operations.get(1);
        Assert.assertEquals(ADD, addServer.get(OP).asString());
        Assert.assertEquals(ModelType.UNDEFINED, addServer.get("policy").getType());
        addr = PathAddress.pathAddress(addServer.get(OP_ADDR));
        Assert.assertEquals(2, addr.size());
        element = addr.getElement(0);
        Assert.assertEquals(SUBSYSTEM, element.getKey());
        Assert.assertEquals(JsmPolicyExtension.SUBSYSTEM_NAME, element.getValue());
        element = addr.getElement(1);
        Assert.assertEquals("server", element.getKey());
        Assert.assertEquals("test-server", element.getValue());

        ModelNode addPolicy = operations.get(2);
        Assert.assertEquals(ADD, addPolicy.get(OP).asString());
        Assert.assertEquals(ModelType.UNDEFINED, addPolicy.get("file").getType());
        addr = PathAddress.pathAddress(addPolicy.get(OP_ADDR));
        Assert.assertEquals(2, addr.size());
        element = addr.getElement(0);
        Assert.assertEquals(SUBSYSTEM, element.getKey());
        Assert.assertEquals(JsmPolicyExtension.SUBSYSTEM_NAME, element.getValue());
        element = addr.getElement(1);
        Assert.assertEquals("policy", element.getKey());
        Assert.assertEquals("test-policy", element.getValue());

    }

    @Test
    public void testParseBlankGroupsSubsystem() throws Exception {

        String subsystemXml = "<subsystem xmlns=\"" + JsmPolicyExtension.NAMESPACE + "\">" + "   <servers>" + "   </servers>"
                + "   <policies>" + "   </policies>" + "</subsystem>";

        // this test check operations created by parsing subsystem XML
        List<ModelNode> operations = super.parse(subsystemXml);

        Assert.assertEquals(1, operations.size());

        ModelNode addSubsystem = operations.get(0);
        Assert.assertEquals(ADD, addSubsystem.get(OP).asString());
        PathAddress addr = PathAddress.pathAddress(addSubsystem.get(OP_ADDR));
        Assert.assertEquals(1, addr.size());
        PathElement element = addr.getElement(0);
        Assert.assertEquals(SUBSYSTEM, element.getKey());
        Assert.assertEquals(JsmPolicyExtension.SUBSYSTEM_NAME, element.getValue());

    }

    @Test
    public void testParseBlankSubsystem() throws Exception {

        String subsystemXml = "<subsystem xmlns=\"" + JsmPolicyExtension.NAMESPACE + "\">" + "</subsystem>";

        // this test check operations created by parsing subsystem XML
        List<ModelNode> operations = super.parse(subsystemXml);

        Assert.assertEquals(1, operations.size());

        ModelNode addSubsystem = operations.get(0);
        Assert.assertEquals(ADD, addSubsystem.get(OP).asString());
        PathAddress addr = PathAddress.pathAddress(addSubsystem.get(OP_ADDR));
        Assert.assertEquals(1, addr.size());
        PathElement element = addr.getElement(0);
        Assert.assertEquals(SUBSYSTEM, element.getKey());
        Assert.assertEquals(JsmPolicyExtension.SUBSYSTEM_NAME, element.getValue());

    }

    @Test
    public void testParseMultilinePolicySubsystem() throws Exception {

        String subsystemXml = "<subsystem xmlns=\""
                + JsmPolicyExtension.NAMESPACE
                + "\">"
                + "   <policies>"
                + "       <policy name=\"test-policy\" file=\"grant {&#xa;    permission java.security.AllPermission;&#xa;};\"/>"
                + "   </policies>" + "</subsystem>";

        // this test check operations created by parsing subsystem XML
        List<ModelNode> operations = super.parse(subsystemXml);

        Assert.assertEquals(2, operations.size());

        ModelNode addPolicy = operations.get(1);
        Assert.assertEquals(ADD, addPolicy.get(OP).asString());
        Assert.assertEquals("grant {\n    permission java.security.AllPermission;\n};", addPolicy.get("file").asString());

    }

    @Test
    public void testInstallIntoController() throws Exception {
        // Parse the subsystem xml and install into the controller
        String subsystemXml = "<subsystem xmlns=\"" + JsmPolicyExtension.NAMESPACE + "\">" + "   <servers>"
                + "       <server name=\"test-server\" policy=\"test-policy\"/>" + "   </servers>" + "   <policies>"
                + "       <policy name=\"test-policy\" file=\"grant { permission java.security.AllPermission; };\"/>"
                + "   </policies>" + "</subsystem>";

        System.setProperty("jboss.server.name", "testing-server");
        KernelServices services = super.installInController(subsystemXml);

        // Read the whole model and make sure it looks as expected
        ModelNode model = services.readWholeModel();

        Assert.assertTrue(model.get(SUBSYSTEM).hasDefined(JsmPolicyExtension.SUBSYSTEM_NAME));

        Assert.assertTrue(model.get(SUBSYSTEM, JsmPolicyExtension.SUBSYSTEM_NAME).hasDefined("server"));
        Assert.assertTrue(model.get(SUBSYSTEM, JsmPolicyExtension.SUBSYSTEM_NAME, "server").hasDefined("test-server"));
        Assert.assertTrue(model.get(SUBSYSTEM, JsmPolicyExtension.SUBSYSTEM_NAME, "server", "test-server").hasDefined("policy"));
        Assert.assertEquals("test-policy",
                model.get(SUBSYSTEM, JsmPolicyExtension.SUBSYSTEM_NAME, "server", "test-server", "policy").asString());

        Assert.assertTrue(model.get(SUBSYSTEM, JsmPolicyExtension.SUBSYSTEM_NAME).hasDefined("policy"));
        Assert.assertTrue(model.get(SUBSYSTEM, JsmPolicyExtension.SUBSYSTEM_NAME, "policy").hasDefined("test-policy"));
        Assert.assertTrue(model.get(SUBSYSTEM, JsmPolicyExtension.SUBSYSTEM_NAME, "policy", "test-policy").hasDefined("file"));
        Assert.assertEquals("grant { permission java.security.AllPermission; };",
                model.get(SUBSYSTEM, JsmPolicyExtension.SUBSYSTEM_NAME, "policy", "test-policy", "file").asString());

    }

    @Test
    public void testDescribeHandler() throws Exception {
        // Parse the subsystem xml and install into the first controller
        String subsystemXml = "<subsystem xmlns=\"" + JsmPolicyExtension.NAMESPACE + "\">" + "</subsystem>";

        System.setProperty("jboss.server.name", "testing-server");
        KernelServices servicesA = super.installInController(subsystemXml);

        // Get the model and the describe operations from the first controller
        ModelNode modelA = servicesA.readWholeModel();
        ModelNode describeOp = new ModelNode();
        describeOp.get(OP).set(DESCRIBE);
        describeOp.get(OP_ADDR).set(
                PathAddress.pathAddress(PathElement.pathElement(SUBSYSTEM, JsmPolicyExtension.SUBSYSTEM_NAME)).toModelNode());

        List<ModelNode> operations = super.checkResultAndGetContents(servicesA.executeOperation(describeOp)).asList();

        // Install the describe options from the first controller into a second controller
        KernelServices servicesB = super.installInController(operations);
        ModelNode modelB = servicesB.readWholeModel();

        // Make sure the models from the two controllers are identical
        super.compare(modelA, modelB);
    }

    @Test
    public void testSubsystemRemoval() throws Exception {
        // Parse the subsystem xml and install into the first controller
        String subsystemXml = "<subsystem xmlns=\"" + JsmPolicyExtension.NAMESPACE + "\">" + "   <servers>"
                + "       <server name=\"test-server\" policy=\"test.policy\"/>" + "   </servers>" + "</subsystem>";

        System.setProperty("jboss.server.name", "testing-server");
        KernelServices services = super.installInController(subsystemXml);
        super.assertRemoveSubsystemResources(services);
    }

    @Test
    public void testExecuteOperations() throws Exception {
        String subsystemXml = "<subsystem xmlns=\"" + JsmPolicyExtension.NAMESPACE + "\">" + "   <policies>"
                + "       <policy name=\"first-policy\" file=\"grant {};\"/>" + "   </policies>" + "</subsystem>";

        System.setProperty("jboss.server.name", "testing-server");
        KernelServices services = super.installInController(subsystemXml);

        // Add new policy
        PathAddress newPolicyAddr = PathAddress.pathAddress(
                PathElement.pathElement(SUBSYSTEM, JsmPolicyExtension.SUBSYSTEM_NAME),
                PathElement.pathElement("policy", "test-policy"));
        ModelNode addNewPolicyOp = new ModelNode();
        addNewPolicyOp.get(OP).set(ADD);
        addNewPolicyOp.get(OP_ADDR).set(newPolicyAddr.toModelNode());
        addNewPolicyOp.get("file").set("grant { permission java.security.AllPermission; };");
        ModelNode resultPolicy = services.executeOperation(addNewPolicyOp);
        Assert.assertEquals(SUCCESS, resultPolicy.get(OUTCOME).asString());

        // Add new server
        PathAddress newServerAddr = PathAddress.pathAddress(
                PathElement.pathElement(SUBSYSTEM, JsmPolicyExtension.SUBSYSTEM_NAME),
                PathElement.pathElement("server", "test-server"));
        ModelNode addNewServerOp = new ModelNode();
        addNewServerOp.get(OP).set(ADD);
        addNewServerOp.get(OP_ADDR).set(newServerAddr.toModelNode());
        addNewServerOp.get("policy").set("test-policy");
        ModelNode resultServer = services.executeOperation(addNewServerOp);
        Assert.assertEquals(SUCCESS, resultServer.get(OUTCOME).asString());

        ModelNode model = services.readWholeModel();
        Assert.assertTrue(model.get(SUBSYSTEM).hasDefined(JsmPolicyExtension.SUBSYSTEM_NAME));
        Assert.assertTrue(model.get(SUBSYSTEM, JsmPolicyExtension.SUBSYSTEM_NAME).hasDefined("policy"));
        Assert.assertTrue(model.get(SUBSYSTEM, JsmPolicyExtension.SUBSYSTEM_NAME, "policy").hasDefined("test-policy"));
        Assert.assertTrue(model.get(SUBSYSTEM, JsmPolicyExtension.SUBSYSTEM_NAME, "policy", "test-policy").hasDefined("file"));
        Assert.assertEquals("grant { permission java.security.AllPermission; };",
                model.get(SUBSYSTEM, JsmPolicyExtension.SUBSYSTEM_NAME, "policy", "test-policy", "file").asString());
        Assert.assertTrue(model.get(SUBSYSTEM, JsmPolicyExtension.SUBSYSTEM_NAME).hasDefined("server"));
        Assert.assertTrue(model.get(SUBSYSTEM, JsmPolicyExtension.SUBSYSTEM_NAME, "server").hasDefined("test-server"));
        Assert.assertTrue(model.get(SUBSYSTEM, JsmPolicyExtension.SUBSYSTEM_NAME, "server", "test-server").hasDefined("policy"));
        Assert.assertEquals("test-policy",
                model.get(SUBSYSTEM, JsmPolicyExtension.SUBSYSTEM_NAME, "server", "test-server", "policy").asString());

        // Write-attribute file of policy
        ModelNode writePolicyOp = new ModelNode();
        writePolicyOp.get(OP).set(WRITE_ATTRIBUTE_OPERATION);
        writePolicyOp.get(OP_ADDR).set(newPolicyAddr.toModelNode());
        writePolicyOp.get(NAME).set("file");
        writePolicyOp.get(VALUE).set("grant {};");
        ModelNode resultPolicyWrite = services.executeOperation(writePolicyOp);
        Assert.assertEquals(SUCCESS, resultPolicyWrite.get(OUTCOME).asString());

        ModelNode readPolicyOp = new ModelNode();
        readPolicyOp.get(OP).set(READ_ATTRIBUTE_OPERATION);
        readPolicyOp.get(OP_ADDR).set(newPolicyAddr.toModelNode());
        readPolicyOp.get(NAME).set("file");
        resultPolicyWrite = services.executeOperation(readPolicyOp);
        Assert.assertEquals("grant {};", checkResultAndGetContents(resultPolicyWrite).asString());

        // Write-attribute policy of server
        ModelNode writeServerOp = new ModelNode();
        writeServerOp.get(OP).set(WRITE_ATTRIBUTE_OPERATION);
        writeServerOp.get(OP_ADDR).set(newServerAddr.toModelNode());
        writeServerOp.get(NAME).set("policy");
        writeServerOp.get(VALUE).set("first-policy");
        ModelNode resultServerWrite = services.executeOperation(writeServerOp);
        Assert.assertEquals(SUCCESS, resultServerWrite.get(OUTCOME).asString());

        ModelNode readServerOp = new ModelNode();
        readServerOp.get(OP).set(READ_ATTRIBUTE_OPERATION);
        readServerOp.get(OP_ADDR).set(newServerAddr.toModelNode());
        readServerOp.get(NAME).set("policy");
        resultServerWrite = services.executeOperation(readServerOp);
        Assert.assertEquals("first-policy", checkResultAndGetContents(resultServerWrite).asString());

    }
}

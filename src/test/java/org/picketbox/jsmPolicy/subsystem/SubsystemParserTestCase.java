package org.picketbox.jsmPolicy.subsystem;

import java.util.List;

import junit.framework.Assert;

import org.jboss.as.controller.PathAddress;
import org.jboss.as.controller.PathElement;
import org.jboss.as.subsystem.test.AbstractSubsystemTest;
import org.jboss.as.subsystem.test.KernelServices;
import org.jboss.dmr.ModelNode;
import org.jboss.dmr.ModelType;
import org.junit.Test;
import org.picketbox.jsmpolicy.subsystem.extension.JsmPolicyExtension;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.*;

/**
 * Unit test case of reading subsystem configuration from the XML
 * @author <a href="xkalin03@stud.fit.vutbr.cz">Jan Kalina</a>
 * @author <a href="kabir.khan@jboss.com">Kabir Khan</a>
 */
public class SubsystemParserTestCase extends AbstractSubsystemTest {

    public SubsystemParserTestCase() {
        super(JsmPolicyExtension.SUBSYSTEM_NAME, new JsmPolicyExtension());
    }

    @Test
    public void testParseFullSubsystem() throws Exception {

        String subsystemXml =
                "<subsystem xmlns=\"" + JsmPolicyExtension.NAMESPACE + "\">" +
                "   <servers>" +
                "       <server name=\"test-server\" policy=\"test-policy\"/>" +
                "   </servers>" +
                "   <policies>" +
                "       <policy name=\"test-policy\" file=\"grant { permission java.security.AllPermission; };\"/>" +
                "   </policies>" +
                "</subsystem>";

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

        String subsystemXml =
                "<subsystem xmlns=\"" + JsmPolicyExtension.NAMESPACE + "\">" +
                "   <servers>" +
                "       <server name=\"test-server\"/>" +
                "   </servers>" +
                "   <policies>" +
                "       <policy name=\"test-policy\"/>" +
                "   </policies>" +
                "</subsystem>";

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

        String subsystemXml =
                "<subsystem xmlns=\"" + JsmPolicyExtension.NAMESPACE + "\">" +
                "   <servers>" +
                "   </servers>" +
                "   <policies>" +
                "   </policies>" +
                "</subsystem>";

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

        String subsystemXml =
                "<subsystem xmlns=\"" + JsmPolicyExtension.NAMESPACE + "\">" +
                "</subsystem>";

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

        String subsystemXml =
                "<subsystem xmlns=\"" + JsmPolicyExtension.NAMESPACE + "\">" +
                "   <policies>" +
                "       <policy name=\"test-policy\" file=\"grant {&#xa;    permission java.security.AllPermission;&#xa;};\"/>" +
                "   </policies>" +
                "</subsystem>";

        // this test check operations created by parsing subsystem XML
        List<ModelNode> operations = super.parse(subsystemXml);

        Assert.assertEquals(2, operations.size());

        ModelNode addPolicy = operations.get(1);
        Assert.assertEquals(ADD, addPolicy.get(OP).asString());
        Assert.assertEquals("grant {\n    permission java.security.AllPermission;\n};", addPolicy.get("file").asString());

    }

    @Test
    public void testInstallIntoController() throws Exception { // TODO
        //Parse the subsystem xml and install into the controller
        String subsystemXml =
                "<subsystem xmlns=\"" + JsmPolicyExtension.NAMESPACE + "\">" +
                "   <servers>" +
                "       <server name=\"test-server\" policy=\"test-policy\"/>" +
                "   </servers>" +
                "   <policies>" +
                "       <policy name=\"test-policy\" file=\"grant { permission java.security.AllPermission; };\"/>" +
                "   </policies>" +
                "</subsystem>";

        KernelServices services = super.installInController(subsystemXml);

        //Read the whole model and make sure it looks as expected
        ModelNode model = services.readWholeModel();

        Assert.assertEquals("",model.toJSONString(true)); // DEBUG

        /*
        Assert.assertTrue(model.get(SUBSYSTEM).hasDefined(JsmPolicyExtension.SUBSYSTEM_NAME));
        Assert.assertTrue(model.get(SUBSYSTEM, JsmPolicyExtension.SUBSYSTEM_NAME).hasDefined("type"));
        Assert.assertTrue(model.get(SUBSYSTEM, JsmPolicyExtension.SUBSYSTEM_NAME, "type").hasDefined("tst"));
        Assert.assertTrue(model.get(SUBSYSTEM, JsmPolicyExtension.SUBSYSTEM_NAME, "type", "tst").hasDefined("tick"));
        Assert.assertEquals(12345, model.get(SUBSYSTEM, JsmPolicyExtension.SUBSYSTEM_NAME, "type", "tst", "tick").asLong());
        */
    }

}

package org.picketbox.jsmpolicy.subsystem.extension;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.ADD;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP_ADDR;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.SUBSYSTEM;

import java.util.Collections;
import java.util.List;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;

import org.jboss.as.controller.Extension;
import org.jboss.as.controller.ExtensionContext;
import org.jboss.as.controller.PathAddress;
import org.jboss.as.controller.PathElement;
import org.jboss.as.controller.SubsystemRegistration;
import org.jboss.as.controller.descriptions.ModelDescriptionConstants;
import org.jboss.as.controller.descriptions.StandardResourceDescriptionResolver;
import org.jboss.as.controller.parsing.ExtensionParsingContext;
import org.jboss.as.controller.parsing.ParseUtils;
import org.jboss.as.controller.persistence.SubsystemMarshallingContext;
import org.jboss.as.controller.registry.ManagementResourceRegistration;
import org.jboss.dmr.ModelNode;
import org.jboss.dmr.Property;
import org.jboss.logging.Logger;
import org.jboss.staxmapper.XMLElementReader;
import org.jboss.staxmapper.XMLElementWriter;
import org.jboss.staxmapper.XMLExtendedStreamReader;
import org.jboss.staxmapper.XMLExtendedStreamWriter;

public class JsmPolicyExtension implements Extension {

    private static final Logger log = Logger.getLogger(JsmPolicyExtension.class);

    public static final String NAMESPACE = "urn:org.picketbox.jsmpolicy:1.0";
    public static final String SUBSYSTEM_NAME = "jsmpolicy";

    private final SubsystemParser parser = new SubsystemParser();
    private static final String RESOURCE_NAME = JsmPolicyExtension.class.getPackage().getName() + ".LocalDescriptions";
    protected static final PathElement SUBSYSTEM_PATH = PathElement.pathElement(SUBSYSTEM, SUBSYSTEM_NAME);
    protected static final PathElement SERVER_PATH = PathElement.pathElement("server");
    protected static final PathElement POLICY_PATH = PathElement.pathElement("policy");

    static StandardResourceDescriptionResolver getResourceDescriptionResolver(final String keyPrefix) {
        String prefix = SUBSYSTEM_NAME + (keyPrefix == null ? "" : "." + keyPrefix);
        return new StandardResourceDescriptionResolver(prefix, RESOURCE_NAME, JsmPolicyExtension.class.getClassLoader(), true, false);
    }

    public void initializeParsers(ExtensionParsingContext context) {
        context.setSubsystemXmlMapping(SUBSYSTEM_NAME, NAMESPACE, parser);
    }

    public void initialize(ExtensionContext context) {
        final SubsystemRegistration subsystem = context.registerSubsystem(SUBSYSTEM_NAME, 1, 0);
        final ManagementResourceRegistration registration = subsystem.registerSubsystemModel(SubsystemDefinition.INSTANCE);
        registration.registerSubModel(ServerDefinition.INSTANCE);
        registration.registerSubModel(PolicyDefinition.INSTANCE);
        subsystem.registerXMLElementWriter(parser);
    }

    private static class SubsystemParser implements XMLStreamConstants, XMLElementReader<List<ModelNode>>, XMLElementWriter<SubsystemMarshallingContext> {

        public void readElement(XMLExtendedStreamReader reader, List<ModelNode> list) throws XMLStreamException {
            ParseUtils.requireNoAttributes(reader);

            final ModelNode subsystem = new ModelNode();
            subsystem.get(OP).set(ADD);
            subsystem.get(OP_ADDR).set(PathAddress.pathAddress(SUBSYSTEM_PATH).toModelNode());
            list.add(subsystem);

            // reading children of "subsystem"
            while (reader.hasNext() && reader.nextTag() != END_ELEMENT) {
                if (reader.getLocalName().equals("servers")) {
                	// reading children of "servers"
                	while (reader.hasNext() && reader.nextTag() != END_ELEMENT) {
                        if (reader.isStartElement()) {
                            readServerElement(reader, list);
                        }
                    }
                }else if (reader.getLocalName().equals("policies")) {
                    // reading children of "policies"
                    while (reader.hasNext() && reader.nextTag() != END_ELEMENT) {
                        if (reader.isStartElement()) {
                            readPolicyElement(reader, list);
                        }
                    }
                }else{
                	throw ParseUtils.unexpectedElement(reader);
                }
            }
        }

        private void readServerElement(XMLExtendedStreamReader reader, List<ModelNode> list) throws XMLStreamException {
            if (!reader.getLocalName().equals("server")) {
                throw ParseUtils.unexpectedElement(reader);
            }
            ModelNode addTypeOperation = new ModelNode();
            addTypeOperation.get(OP).set(ModelDescriptionConstants.ADD);

            String serverName = null;
            for (int i = 0; i < reader.getAttributeCount(); i++) {
                String name = reader.getAttributeLocalName(i);
                String value = reader.getAttributeValue(i);
                if (name.equals("policy")) {
                    ServerDefinition.POLICY.parseAndSetParameter(value, addTypeOperation, reader);
                } else if (name.equals("name")) {
                    serverName = value;
                } else {
                    throw ParseUtils.unexpectedAttribute(reader, i);
                }
            }
            ParseUtils.requireNoContent(reader);
            if (serverName == null) {
                throw ParseUtils.missingRequiredElement(reader, Collections.singleton("name"));
            }

            // add the "add" operation for each "server"
            PathAddress addr = PathAddress.pathAddress(SUBSYSTEM_PATH, PathElement.pathElement("server", serverName));
            addTypeOperation.get(OP_ADDR).set(addr.toModelNode());
            list.add(addTypeOperation);
        }

        private void readPolicyElement(XMLExtendedStreamReader reader, List<ModelNode> list) throws XMLStreamException {
            if (!reader.getLocalName().equals("policy")) {
                throw ParseUtils.unexpectedElement(reader);
            }
            ModelNode addTypeOperation = new ModelNode();
            addTypeOperation.get(OP).set(ModelDescriptionConstants.ADD);

            String serverName = null;
            for (int i = 0; i < reader.getAttributeCount(); i++) {
                String name = reader.getAttributeLocalName(i);
                String value = reader.getAttributeValue(i);
                if (name.equals("file")) {
                    PolicyDefinition.FILE.parseAndSetParameter(value, addTypeOperation, reader);
                } else if (name.equals("name")) {
                    serverName = value;
                } else {
                    throw ParseUtils.unexpectedAttribute(reader, i);
                }
            }
            ParseUtils.requireNoContent(reader);
            if (serverName == null) {
                throw ParseUtils.missingRequiredElement(reader, Collections.singleton("name"));
            }

            // add the "add" operation for each "policy"
            PathAddress addr = PathAddress.pathAddress(SUBSYSTEM_PATH, PathElement.pathElement("policy", serverName));
            addTypeOperation.get(OP_ADDR).set(addr.toModelNode());
            list.add(addTypeOperation);
        }

        public void writeContent(final XMLExtendedStreamWriter writer, final SubsystemMarshallingContext context)
                throws XMLStreamException {
            context.startSubsystemElement(JsmPolicyExtension.NAMESPACE, false); // goto subsystem
            {
                writer.writeStartElement("servers"); // begin servers
                ModelNode node = context.getModelNode();
                ModelNode type = node.get("server");
                for (Property property : type.asPropertyList()) {
                    writer.writeStartElement("server"); // begin server
                    writer.writeAttribute("name", property.getName());
                    ModelNode entry = property.getValue(); // get server ModelNode
                    ServerDefinition.POLICY.marshallAsAttribute(entry, true, writer); // attribute policy (entry=>writer)
                    writer.writeEndElement(); // end server
                }
                writer.writeEndElement(); // end servers
            }
            {
                writer.writeStartElement("policies"); // begin policies
                ModelNode node = context.getModelNode();
                ModelNode type = node.get("policy");
                for (Property property : type.asPropertyList()) {
                    writer.writeStartElement("policy"); // begin policy
                    writer.writeAttribute("name", property.getName());
                    ModelNode entry = property.getValue(); // get policy ModelNode
                    PolicyDefinition.FILE.marshallAsAttribute(entry, true, writer); // attribute file (entry=>writer)
                    writer.writeEndElement(); // end policy
                }
                writer.writeEndElement(); // end policies
            }
            writer.writeEndElement(); // end subsystem
        }
    }
}

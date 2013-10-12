package org.picketbox.jsmPolicy.subsystem;


import org.jboss.as.subsystem.test.AbstractSubsystemBaseTest;
import org.picketbox.jsmPolicy.subsystem.extension.JsmPolicyExtension;

import java.io.IOException;


/**
 * @author <a href="kabir.khan@jboss.com">Kabir Khan</a>
 */
public class SubsystemBaseParsingTestCase extends AbstractSubsystemBaseTest {

    public SubsystemBaseParsingTestCase() {
        super(JsmPolicyExtension.SUBSYSTEM_NAME, new JsmPolicyExtension());
    }

    @Override
    protected String getSubsystemXml() throws IOException {
    	return readResource("subsystem.xml");
    }
}

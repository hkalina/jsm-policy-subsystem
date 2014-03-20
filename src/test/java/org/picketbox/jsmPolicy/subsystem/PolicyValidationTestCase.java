package org.picketbox.jsmPolicy.subsystem;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.AccessControlException;

import org.jboss.as.controller.OperationFailedException;
import org.junit.Before;
import org.junit.Test;
import org.picketbox.jsmpolicy.subsystem.extension.PolicyManager;

public class PolicyValidationTestCase {

    @Test
    public void testValidationOfEmptyString() throws Exception {
        PolicyManager.INSTANCE.validatePolicyFile("");
    }

    @Test
    public void testValidationOfEmptyGrant() throws Exception {
        PolicyManager.INSTANCE.validatePolicyFile(
                "grant {};"
            );
    }

    @Test
    public void testValidationWithUnknown() throws Exception {
        PolicyManager.INSTANCE.validatePolicyFile(
                "grant codeBase \"file:/dir/-\"  {\n" +
                "    permission not.existing.Permission \"param\";\n" +
                "};" +
                "grant codeBase \"http://server/*\", principal \"test\" {\n" +
                "    permission really.not.existing.Permission \"param\";\n" +
                "};"
            );
    }

    @Test
    public void testValidationOfValidPermission() throws Exception {
        PolicyManager.INSTANCE.validatePolicyFile(
                "grant codeBase \"file:/home/-\" {\n" +
                "    permission java.lang.RuntimePermission \"setSecurityManager\";\n" +
                "    permission java.security.SecurityPermission \"getPolicy\";\n" +
                "};"
            );
    }

    @Test
    public void testNonValidString() throws Exception {
        boolean throwed = false;
        try{
            PolicyManager.INSTANCE.validatePolicyFile(
                    "this string is not valid policy"
                );
        }
        catch(OperationFailedException e){
            throwed = true;
        }
        assertTrue(throwed);
    }

}

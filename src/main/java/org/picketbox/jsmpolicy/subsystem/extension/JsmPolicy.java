package org.picketbox.jsmpolicy.subsystem.extension;

import java.lang.reflect.Field;
import java.security.AllPermission;
import java.security.Permission;
import java.security.PermissionCollection;
import java.security.Permissions;
import java.security.Policy;
import java.security.ProtectionDomain;

/**
 * This class should be in future used as policy class and
 * as result provide penetration of JACC policy (from DelegatingPolicy)
 * and standard policy (PolicyFile)
 */
public class JsmPolicy extends Policy {

    public Policy delegatingPolicy; // TODO: private

    private static final AllPermission ALL_PERMISSION = new AllPermission();

    public JsmPolicy(Policy delegatingPolicy){
        this.delegatingPolicy = delegatingPolicy;
    }

    public PermissionCollection getPermissions(ProtectionDomain domain){
        System.err.println("JsmPolicy.getPermission");
        final Permissions permissions = new Permissions();
        permissions.add(ALL_PERMISSION);
        return permissions;
    }

    public boolean implies(ProtectionDomain domain, Permission permission){
        System.err.println("JsmPolicy.implies("+permission.toString()+")");
        return true;
    }

    public void refresh(){
        super.refresh();
        try{
            Field delegateField = delegatingPolicy.getClass().getField("delegate");
            delegateField.setAccessible(true);
            Policy modulesPolicy = (Policy)delegateField.get(delegatingPolicy);
            modulesPolicy.refresh();
        }
        catch(Exception e){
            System.err.println("Refresh exception: ("+delegatingPolicy.getClass().getName()+") "+e.toString());
        }
    }

}

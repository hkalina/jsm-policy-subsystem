package org.picketbox.jsmpolicy.subsystem.extension;

import org.jboss.as.controller.AbstractRemoveStepHandler;

class SubsystemRemove extends AbstractRemoveStepHandler {
	
    static final SubsystemRemove INSTANCE = new SubsystemRemove();
    
    private SubsystemRemove() {}
    
}
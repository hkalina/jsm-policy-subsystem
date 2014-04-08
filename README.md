jsm-policy-subsystem
====================

This is Java Security Manager Policy subsystem for JBoss/Wildfly. It consist of following packages:

* [jsm-policy-subsystem](https://github.com/honza889/jsm-policy-subsystem) - backed subsystem (extension of JBoss)
* [jsm-policy-console](https://github.com/honza889/jsm-policy-console) - frontend (extension of JBoss Management Console)
* [jsm-policy-test](https://github.com/honza889/jsm-policy-test) - test of backend subsystem

Successful changing of policy require also this patch of WildFly:
* [security-manager](https://github.com/honza889/security-manager)


[Wildfly 8 documentation: Extending Wildfly 8](https://docs.jboss.org/author/display/WFLY8/Extending+WildFly+8)

## Installation of jsm-policy-subsystem ##

1. Install [security-manager](https://github.com/honza889/security-manager) patch into WildFly installation
2. Add into **standalone.sh** or **domain.sh** for using PolicyFile permission in `ModuleClassLoader`:

  ```
  JAVA_OPTS="$JAVA_OPTS -Djboss.modules.policy-permissions=true"
  ```
3. Do compilation: `mvn clean install`
4. Copy target to your server: `cp -r target/module/* $(JBOSS_PATH)/modules/system/layers/base/`
5. Add following into your **standalone.xml** or **domain.xml**:
  ```
  <?xml version='1.0' encoding='UTF-8'?>
  <server xmlns="urn:jboss:domain:1.4">
      <extensions>
          ...
          <extension module="org.picketbox.jsmpolicy.subsystem"/>
          ...
      </extensions>
      
      <profile>
          ...
          <subsystem xmlns="urn:org.picketbox.jsmpolicy:1.0"></subsystem>
          ...
      </profile>
  </server>
  ```

## Alternative installation ##
  ```
  cp -r target/module/* $(JBOSS_PATH)/modules/system/layers/base/
  $(JBOSS_PATH)/bin/jboss-cli.sh
  [standalone@localhost:9999 /] /extension=org.picketbox.jsmpolicy.subsystem:add
  [standalone@localhost:9999 /] /subsystem=jsmpolicy:add
  ```


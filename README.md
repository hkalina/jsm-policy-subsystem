jsm-policy-subsystem
====================

This is Java Security Manager Policy subsystem for JBoss/Wildfly. It consist of following packages:

* [jsm-policy-subsystem](https://github.com/honza889/jsm-policy-subsystem) - backed subsystem (extension of JBoss)
* [jsm-policy-console-hal](https://github.com/honza889/jsm-policy-console-hal) - frontend (fork of JBoss Management Console)
* [jsm-policy-test](https://github.com/honza889/jsm-policy-test) - test of backend subsystem

Successful changing of policy require also this patch of WildFly:
* [jboss-modules](https://github.com/honza889/jboss-modules) ([pull request](https://github.com/jboss-modules/jboss-modules/pull/48))

[Wildfly 8 documentation: Extending Wildfly 8](https://docs.jboss.org/author/display/WFLY8/Extending+WildFly+8)

## Installation of jsm-policy-subsystem ##

1. Install [jboss-modules](https://github.com/honza889/jboss-modules) patch into WildFly installation and allow it by adding of following at begining of **standalone.sh** or **domain.sh** for using dynamic permission in `ModuleClassLoader`:
  ```
  JAVA_OPTS="$JAVA_OPTS -Djboss.modules.policy-refreshable=true"
  ```
3. Do compilation: `mvn install`
4. Copy target to your server: `cp -r target/module/* $(JBOSS_PATH)/modules/system/layers/base/`
5. Do following for adding of **jsmpolicy** into WildFly. For domain and profile **full**:
  ```
  bin/jboss-cli.sh --connect
  /extension=org.picketbox.jsmpolicy.subsystem:add
  /profile=full/subsystem=jsmpolicy:add
  ```
  For standalone mode:
  ```
  bin/jboss-cli.sh --connect
  /extension=org.picketbox.jsmpolicy.subsystem:add
  /subsystem=jsmpolicy:add
  ```

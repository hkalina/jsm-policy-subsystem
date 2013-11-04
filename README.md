jsm-policy-subsystem
====================

This is Java Security Manager Policy subsystem for JBoss/Wildfly. It consist of following packages:

* [jsm-policy-subsystem](https://github.com/honza889/jsm-policy-subsystem) - backed subsystem (extension of JBoss)
* [jsm-policy-console](https://github.com/honza889/jsm-policy-console) - frontend (extension of JBoss Management Console)
* [jsm-policy-test](https://github.com/honza889/jsm-policy-test) - test of backend subsystem

Current status:
* Under JBoss EAP 6.1 with disabled JACC works good
* Under Wildfly 8 Beta1 with JACC probably works, but SecurityManager and JACC itself cause problems

## Installation of jsm-policy-subsystem ##
1. Do compilation: `mvn clean install`
2. Copy target to your server: `cp -r target/module/* $(JBOSS_PATH)/modules/system/layers/base/`
3. Add following into your **standalone.conf** or **domain.conf**:
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

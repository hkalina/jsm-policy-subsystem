jsm-policy-subsystem
====================

This is Java Security Manager Policy subsystem for JBoss/Wildfly. It consist of following packages:

* [jsm-policy-subsystem](https://github.com/honza889/jsm-policy-subsystem) - backed subsystem
* [jsm-policy-console](https://github.com/honza889/jsm-policy-console) - test of backend subsystem
* [jsm-policy-test](https://github.com/honza889/jsm-policy-test) - test of backend subsystem


## Installation of jsm-policy-subsystem ##
1. Do compilation: mvn -Dmaven.test.skip=true clean install
2. Copy target to your server: cp -r target/module/* $(JBOSS_PATH)/modules/system/layers/base/
3. Add following into your standalone.conf / domain.conf:

  <?xml version='1.0' encoding='UTF-8'?>
  <server xmlns="urn:jboss:domain:1.4">
    <extensions>
        ...
        <extension module="org.picketbox.jsmPolicy.subsystem"/>
        ...
    </extensions>

    <profile>
        ...
        <subsystem xmlns="urn:org.picketbox.jsmPolicy:1.0"></subsystem>
        ...
    </profile>
  </server>

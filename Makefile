# Path to JBoss AS / EAP / Wildfly
JBOSS_PATH = ../wildfly-target

install: compile
	cp -r target/module/* $(JBOSS_PATH)/modules/system/layers/base/

compile:
	mvn -Dmaven.test.skip=true clean install


# Path to JBoss AS / EAP / Wildfly
JBOSS_PATH = ../wildfly-8.0.0.CR1

all: compile install run

compile:
	mvn -Dmaven.test.skip=true clean install

install:
	cp -r target/module/* $(JBOSS_PATH)/modules/system/layers/base/

run:
	$(JBOSS_PATH)/bin/standalone.sh

domain:
	$(JBOSS_PATH)/bin/domain.sh

test:
	mvn clean install


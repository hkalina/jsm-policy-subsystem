# Path to JBoss AS / EAP / Wildfly
JBOSS_PATH = ../wildfly-8.0.0.CR1

all: run

compile:
	mvn -Dmaven.test.skip=true clean install

install: compile
	cp -r target/module/* $(JBOSS_PATH)/modules/system/layers/base/

run: install
	$(JBOSS_PATH)/bin/standalone.sh

domain: install
	$(JBOSS_PATH)/bin/domain.sh

test:
	mvn clean install


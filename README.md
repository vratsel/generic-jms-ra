# JBoss Generic JMS JCA Resource Adapter

This project is for the JBoss Generic JMS JCA Resource Adapter.  As the name suggests, this JCA RA provides the ability to integrate with any JMS broker which allows remote clients to look-up connection factories and destinations via JNDI.  It currently is only verified to work in JBoss AS7 and supports, for example, consuming messages with an MDB and sending messages with a JCA-base JMS connection factory to 3rd-party brokers.  It is based on the generic JMS JCA RA found in previous versions of JBoss AS (e.g. 4, 5, and 6).  However, unlike those versions this is a stand-alone project now and no longer supports internal dead-letter processing since every modern JMS broker supports this already.

## Build instructions

This project is Mavenized so you only need to execute 'mvn install' to compile it and 'mvn -Prelease install' to generate the full, deployable resource adapter.

## JBoss AS7 Deployment notes

Since this is a generic JCA RA, the user must supply it with the proper client classes to actually make a physical connection to a 3rd party JMS broker.  Since AS7 uses a modular classload this requires the user create a module with the proper classes and then modify the manifest.mf of the Resource Adapter to use that module.

For example...

### Example deployment descriptor

TODO

## Example MDB

TODO

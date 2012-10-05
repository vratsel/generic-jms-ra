# JBoss Generic JMS JCA Resource Adapter

This project is for the JBoss Generic JMS JCA Resource Adapter.  As the name suggests, this JCA RA provides the ability to integrate with any JMS broker which allows remote clients to look-up connection factories and destinations via JNDI.  It currently is only verified to work in JBoss AS7 and supports, for example, consuming messages with an MDB and sending messages with a JCA-base JMS connection factory to 3rd-party brokers.  It is based on the generic JMS JCA RA found in previous versions of JBoss AS (e.g. 4, 5, and 6).  However, unlike those versions this is a stand-alone project now and no longer supports internal dead-letter processing since every modern JMS broker supports this already.

## Build instructions

This project is Mavenized so you only need to execute 'mvn install' to compile it and 'mvn -Prelease install' to generate the full, deployable resource adapter.

## JBoss AS7 Deployment notes

Since this is a <em>generic</em> JMS JCA RA, the user must supply it with the proper client classes to actually make a physical connection to a 3rd party JMS broker.  Since AS7 uses a modular classload this requires the user to:

1. Create a module with the proper integration classes 
2. Modify the manifest.mf of the RAR to use the aforementioned module

For example, to integrate with JBoss Messaging running in JBoss AS 5 create a module.xml like this (jar files copied from <JBOSS_5_HOME>/client):

	<module xmlns="urn:jboss:module:1.1" name="org.jboss.jboss-5-client">
	    <resources>
	        <resource-root path="concurrent.jar"/>
	        <resource-root path="javassist.jar"/>
	        <resource-root path="jboss-aop-client.jar"/>
	        <resource-root path="jboss-common-core.jar"/>
	        <resource-root path="jboss-logging-log4j.jar"/>
	        <resource-root path="jboss-logging-spi.jar"/>
	        <resource-root path="jboss-mdr.jar"/>
	        <resource-root path="jboss-messaging-client.jar"/>
	        <resource-root path="jboss-remoting.jar"/>
	        <resource-root path="jboss-serialization.jar"/>
	        <resource-root path="jnp-client.jar"/>
	        <resource-root path="log4j.jar"/>
	        <resource-root path="trove.jar"/>
	    </resources>

	    <dependencies>
	        <module name="javax.api"/>
	        <module name="javax.jms.api"/>
	    </dependencies>
	</module>

Of course, the module.xml and all the related jar files would need to be placed in <JBOSS_7_HOME>/modules/org/jboss/jboss-5-client/main.

The next step is to modify the generic JMS JCA RA to use this module so it has access to all the proper integration classes when it interacts with the remote JBoss Messaging broker.  To do this, simply add this line to the generic-jms-rar.rar/META-INF/manifest.mf:

	Dependencies: org.jboss.jboss-5-client

### Example deployment descriptor

To create an outbound connection factory, use a deployment descriptor like this in your standalone*.xml.

     <subsystem xmlns="urn:jboss:domain:resource-adapters:1.0">
         <resource-adapters>
             <resource-adapter>
                 <archive>
                     generic-jms-rar.rar
                 </archive>
                 <transaction-support>XATransaction</transaction-support>
                 <connection-definitions>
                     <connection-definition class-name="org.jboss.resource.adapter.jms.JmsManagedConnectionFactory" jndi-name="java:/GenericJmsXA" enabled="true" use-java-context="true" pool-name="GenericJmsXA" use-ccm="true">
                         <config-property name="JndiParameters">
                             java.naming.factory.initial=org.jnp.interfaces.NamingContextFactory;java.naming.provider.url=JBM_HOST:1099;java.naming.factory.url.pkgs=org.jboss.naming:org.jnp.interfaces
                         </config-property>
                         <config-property name="ConnectionFactory">
                             XAConnectionFactory
                         </config-property>
                         <xa-pool>
                             <min-pool-size>0</min-pool-size>
                             <max-pool-size>10</max-pool-size>
                             <prefill>false</prefill>
                             <use-strict-min>false</use-strict-min>
                             <flush-strategy>FailingConnectionOnly</flush-strategy>
                             <pad-xid>false</pad-xid>
                             <wrap-xa-resource>true</wrap-xa-resource>
                         </xa-pool>
                         <security>
                             <application/>
                         </security>
                     </connection-definition>
                 </connection-definitions>
             </resource-adapter>
         </resource-adapters>
     </subsystem>

This particular configuration binds a JMS connection factory to "java:/GenericJmsXA".  Under the covers it looks up the "XAConnectionFactory" via JNDI from JBM_HOST.

## Example MDB

This MDB will connect to JBM_HOST using the "XAConnectionFactory" and consume messages from the "queue/source" destination.  It's important to note that the RA will use the "jndiParameters" activation configuration property to lookup the "connectionFactory" and the "destination."

Once a message is received the MDB will use the "java:/GenericJmsXA" connection factory (defined above) to send a message to the "target" destination hosted on JBM_HOST.  Notice here that the GenericJmsXA connection factory is looked up via JNDI, but the "target" destination is not looked up via JNDI but rather instantiated with javax.jms.Session.createQueue(String) where the Sting parameter is the actual name of the destination (i.e. not necessarily where it is bound in JNDI).  This is done because we want to avoid a full JNDI look-up of the destination on the remote server, and because there is currently no way to make a local JNDI look-up go to a remote server (a la the ExternalContext MBean from JBoss AS 4, 5, and 6).  The reason it is typically good to avoid a full JNDI lookup of the destination on the remote server is because it saves the developer from having to specify the same JNDI lookup parameters both in the code and the activation configuration.

The consumption and production will be done atomically because the underlying connection factories used to consume and produce the messages support XA and also because the way the MDB is coded to rollback the transaction when production fails for any reason.

	import javax.ejb.ActivationConfigProperty;
	import javax.ejb.MessageDriven;
	import javax.jms.Connection;
	import javax.jms.ConnectionFactory;
	import javax.jms.Destination;
	import javax.jms.Message;
	import javax.jms.MessageListener;
	import javax.jms.MessageProducer;
	import javax.jms.Session;
	import javax.naming.Context;
	import javax.naming.InitialContext;
	
	import org.jboss.ejb3.annotation.ResourceAdapter;
	
	@MessageDriven(activationConfig = {
	      @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue"),
	      @ActivationConfigProperty(propertyName = "destination", propertyValue = "/queue/source"),
	      @ActivationConfigProperty(propertyName = "jndiParameters", propertyValue = "java.naming.factory.initial=org.jnp.interfaces.NamingContextFactory;java.naming.provider.url=JBM_HOST:1099;java.naming.factory.url.pkgs=org.jboss.naming:org.jnp.interfaces"),
	      @ActivationConfigProperty(propertyName = "connectionFactory", propertyValue = "XAConnectionFactory")
	})
	@ResourceAdapter("generic-jms-rar.rar")
	public class ExampleMDB implements MessageListener
	{

	   @Resource
	   private MessageDrivenContext context;

	   public void onMessage(final Message message)
	   {
	      Connection connection = null;

	      try
	      {
	         Context context = new InitialContext();
	         ConnectionFactory cf = (ConnectionFactory) context.lookup("java:/GenericJmsXA");
	         context.close();
	         connection = cf.createConnection();
	         Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
	         Destination destination = session.createQueue("target");
	         MessageProducer producer = session.createProducer(destination);
	         Message msg = session.createTextMessage("example text");
	         producer.send(msg);
	      }
	      catch (Exception e)
	      {
	         context.setRollbackOnly();
	      }
	      finally
	      {
	         if (connection != null)
	         {
	            connection.close();
	         }
	      }
	   }
	}

When deploying an MDB which depends on a non-default RA it is customary to modify the MDB's deployment so that it is not deployed until the RA it needs has been deployed.  To do this in JBoss AS7 simply add this line to the META-INF/manifest.mf of your deployment:

	Dependencies: deployment.generic-jms-rar.rar

### Activation Configuration Properties

#### Most commonly used activation configuration properties
destination - the JNDI name of JMS destination from which the MDB will consume messages
destinationType - the type of JMS destination from which to consume messages (e.g. javax.jms.Queue or javax.jms.Topic)
jndiParameters - the JNDI parameters to use to perform the lookup of the destination and the connectionFactory
connectionFactory - the JNDI name of connection factory which the RA will use to consume the messages; this is normally a connection factory which supports XA

#### Less commonly used activation configuration properties
messageSelector
acknowledgeMode
subscriptionDurability
clientId
subscriptionName
reconnectInterval - value is measured in seconds; default is -1 (i.e. infinite retries)
reconnectAttempts - default is 5
user
pass
minSession - default is 1
maxSession - default is 15

#### Rarely used activation configuration properties
maxMessages - default is 1
sessionTransacted - default is true
redeliverUnspecified - default is true
transactionTimeout
isSameRMOverrideValue
forceClearOnShutdown - default is false
forceClearOnShutdownInterval - value is measured in milliseconds; default is 1000
forceClearAttempts - default is 0
forceTransacted - default is false

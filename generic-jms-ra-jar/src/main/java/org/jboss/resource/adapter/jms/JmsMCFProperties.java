/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.resource.adapter.jms;

import javax.jms.Queue;
import javax.jms.Topic;
import javax.resource.ResourceException;

import org.jboss.resource.adapter.jms.util.Strings;

/**
 * The MCF default properties, settable in ra.xml or in deployer.
 *
 * @author Peter Antman
 * @author <a href="mailto:adrian@jboss.com">Adrian Brock</a>
 */
public class JmsMCFProperties implements java.io.Serializable {
    static final long serialVersionUID = -7997396849692340121L;

    public static final String QUEUE_TYPE = Queue.class.getName();
    public static final String TOPIC_TYPE = Topic.class.getName();

    String userName;
    String password;
    String clientID;
    String jndiParameters;
    String connectionFactory;
    int type = JmsConnectionFactory.AGNOSTIC;

    public JmsMCFProperties() {
        // empty
    }

    /**
     * Set userName, null by default.
     */
    public void setUserName(final String userName) {
        this.userName = userName;
    }

    /**
     * Get userName, may be null.
     */
    public String getUserName() {
        return userName;
    }

    /**
     * Set password, null by default.
     */
    public void setPassword(final String password) {
        this.password = password;
    }

    /**
     * Get password, may be null.
     */
    public String getPassword() {
        return password;
    }

    /**
     * Get client id, may be null.
     */
    public String getClientID() {
        return clientID;
    }

    /**
     * Set client id, null by default.
     */
    public void setClientID(final String clientID) {
        this.clientID = clientID;
    }

    /**
     * Type of the JMS Session.
     */
    public int getType() {
        return type;
    }

    /**
     * Set the default session type.
     */
    public void setType(int type) {
        this.type = type;
    }

    public String getConnectionFactory() {
        return connectionFactory;
    }

    public void setConnectionFactory(String connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    public String getJndiParameters() {
        return jndiParameters;
    }

    public void setJndiParameters(String jndiParameters) {
        this.jndiParameters = jndiParameters;
    }

    /**
     * Helper method to set the default session type.
     *
     * @param type either javax.jms.Topic or javax.jms.Queue
     * @throws ResourceException if type was not a valid type.
     */
    public void setSessionDefaultType(String type) throws ResourceException {
        if (type.equals(QUEUE_TYPE))
            this.type = JmsConnectionFactory.QUEUE;
        else if (type.equals(TOPIC_TYPE))
            this.type = JmsConnectionFactory.TOPIC;
        else
            this.type = JmsConnectionFactory.AGNOSTIC;
    }

    public String getSessionDefaultType() {
        if (type == JmsConnectionFactory.AGNOSTIC)
            return "agnostic";
        else if (type == JmsConnectionFactory.QUEUE)
            return QUEUE_TYPE;
        else
            return TOPIC_TYPE;
    }

    /**
     * Test for equality.
     */
    public boolean equals(Object obj) {
        if (obj == null) return false;

        if (obj instanceof JmsMCFProperties) {
            JmsMCFProperties you = (JmsMCFProperties) obj;
            return (Strings.compare(userName, you.getUserName()) &&
                    Strings.compare(password, you.getPassword()) &&
                    this.type == you.type);
        }

        return false;
    }

    /**
     * Simple hashCode of all attributes.
     */
    public int hashCode() {
        // FIXME
        String result = "" + userName + password + type;
        return result.hashCode();
    }
}

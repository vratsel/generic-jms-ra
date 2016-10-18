/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2014, Red Hat, Inc., and individual contributors
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

import java.security.AccessController;
import java.security.PrivilegedAction;

/**
 * Security Actions.
 *
 * @author <a href="http://jmesnil.net/">Jeff Mesnil</a> (c) 2014 Red Hat inc.
 */
public class SecurityActions {

    public static ClassLoader getThreadContextClassLoader() {
        if (System.getSecurityManager() == null) {
            return Thread.currentThread().getContextClassLoader();
        } else {
            return AccessController.doPrivileged(new PrivilegedAction<ClassLoader>() {
                @Override
                public ClassLoader run() {
                    return Thread.currentThread().getContextClassLoader();
                }
            });
        }
    }

    /**
     * Set the context classloader.
     *
     * @param cl classloader
     */
    public static void setThreadContextClassLoader(final ClassLoader cl) {
        if (System.getSecurityManager() == null) {
            Thread.currentThread().setContextClassLoader(cl);
        } else {
            AccessController.doPrivileged(new PrivilegedAction<Object>() {
                public Object run() {
                    Thread.currentThread().setContextClassLoader(cl);

                    return null;
                }
            });
        }
    }

    /**
     * Get a classLoader
     *
     * @param clazz The class name
     * @return the ClassLoader
     */
    static ClassLoader getClassLoader(final Class<?> clazz) {
        if (System.getSecurityManager() == null) {
            return clazz.getClassLoader();
        }
        return AccessController.doPrivileged(new PrivilegedAction<ClassLoader>() {
            public ClassLoader run() {
                return clazz.getClassLoader();
            }
        });
    }

}

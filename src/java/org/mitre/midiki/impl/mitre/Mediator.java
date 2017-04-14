/****************************************************************************
 *
 * Copyright (C) 2004. The MITRE Corporation (http://www.mitre.org/). All Rights Reserved.
 * Consult the LICENSE file in the root of the distribution for terms and restrictions.
 *
 *       Release: 1.0
 *       Date: 24-August-2004
 *       Author: Carl Burke
 *
 *****************************************************************************/
package org.mitre.midiki.impl.mitre;

/**
 * Controls the medium by which information
 * is exchanged outside the bounds of a single DM process.
 * This design makes the assumption that the DM code might be
 * factored in any of several ways, and that the underlying
 * communication method might not support a granularity any finer
 * than a single executable process (but does support at least
 * that fine a distinction).<p>
 *
 * @author <a href="mailto:cburke@mitre.org">Carl Burke</a>
 * @version 1.0
 * @since 1.0
 */
public interface Mediator
{
    /**
     * Describe <code>publicName</code> method here.
     *
     */
    public String publicName();
    /**
     * Describe <code>agentName</code> method here.
     *
     */
    public String agentName();
    /**
     * Set configuration data for this mediator from properties.
     *
     * @param props a <code>PropertyTree</code> value
     * @return a <code>boolean</code> value
     */
    public boolean configure(PropertyTree props);
    /**
     * Returns <code>true</code> if this mediator follows Midiki
     * protocols for data exchange, or <code>false</code> if only
     * the underlying framework is to be used. Enables/disables
     * additional processing in cells.
     *
     * @return a <code>boolean</code> value
     */
    public boolean usesMidikiProtocol();
    /**
     * Describe <code>register</code> method here.
     *
     */
    public boolean register(String asName);
    /**
     * Describe <code>declareServices</code> method here.
     *
     */
    public boolean declareServices(Object services, Object requests);
    /**
     * Describe <code>appendServiceDeclaration</code> method here.
     *
     */
    public Object appendServiceDeclaration(String name,
                                           Object parameters,
                                           Object serviceList);
    /**
     * Describe <code>appendQueryDeclaration</code> method here.
     *
     */
    public Object appendQueryDeclaration(String cell,
                                         String query,
                                         boolean isnative,
                                         Object parameters,
                                         Object serviceList);
    /**
     * Describe <code>appendActionDeclaration</code> method here.
     *
     */
    public Object appendActionDeclaration(String cell,
                                          String action,
                                          boolean isnative,
                                          Object parameters,
                                          Object serviceList);
    /**
     * Describe <code>appendServiceSpecification</code> method here.
     *
     */
    public Object appendServiceSpecification(String name,
                                             Object parameters,
                                             Object serviceList);
    /**
     * Describe <code>appendQuerySpecification</code> method here.
     *
     */
    public Object appendQuerySpecification(String cell,
                                           String query,
                                           boolean isnative,
                                           Object parameters,
                                           Object serviceList);
    /**
     * Describe <code>appendActionSpecification</code> method here.
     *
     */
    public Object appendActionSpecification(String cell,
                                            String action,
                                            boolean isnative,
                                            Object parameters,
                                            Object serviceList);
    /**
     * Describe <code>registerServiceHandler</code> method here.
     *
     */
    public void registerServiceHandler(Object service, 
                                       ServiceHandler handler);
    /**
     * Describe <code>assertReadiness</code> method here.
     *
     */
    public void assertReadiness();
    /**
     * Use the specified service, waiting until complete.
     * Returns zero or more sets of values for the service parameters
     * in <code>results</code>.
     *
     */
    public boolean useService(Object request,
                              Object parameters,
                              Object results);
    /**
     * Describe <code>requestService</code> method here.
     *
     */
    public boolean requestService(Object request,
                                  Object parameters,
                                  Object results);
    /**
     * Describe <code>broadcastServiceRequest</code> method here.
     *
     */
    public boolean broadcastServiceRequest(Object request,
                                           Object parameters,
                                           Object results);
    /**
     * Fetch all available data for the specified instance of the cell.
     * Each result is a <code>Collection</code> of attribute values,
     * compatible with the contents of a <code>CellInstance</code>.
     * If no result is found for this cell instance, the list will be empty.
     *
     */
    public boolean getData(Object cellName,
                           Object cellInstance,
                           Object results);
    /**
     * Describe <code>putData</code> method here.
     *
     */
    public boolean putData(Object request);
    /**
     * Describe <code>replaceData</code> method here.
     *
     */
    public boolean replaceData(Object oldData, Object newData);
    /**
     * Describe <code>pauseInteraction</code> method here.
     *
     */
    public Object pauseInteraction(String key, Object param);
    /**
     * Describe <code>resumeInteraction</code> method here.
     *
     */
    public Object resumeInteraction(String key, Object param);
}

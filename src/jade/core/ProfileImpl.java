/**
 * ***************************************************************
 * JADE - Java Agent DEvelopment Framework is a framework to develop
 * multi-agent systems in compliance with the FIPA specifications.
 * Copyright (C) 2000 CSELT S.p.A.
 * 
 * GNU Lesser General Public License
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation,
 * version 2.1 of the License.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the
 * Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 * Boston, MA  02111-1307, USA.
 * **************************************************************
 */

package jade.core;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Vector;

import jade.util.leap.ArrayList;
import jade.util.leap.List;
import jade.util.leap.Properties;

/**
 * This class allows the JADE core to retrieve configuration-dependent classes
 * and boot parameters.
 * <p>
 * Take care of using different instances of this class when launching different
 * containers/main-containers on the same JVM otherwise they would conflict!
 * 
 * @author Federico Bergenti
 * @author Giovanni Rimassa - Universita' di Parma
 * @author Giovanni Caire - TILAB
 * 
 * @version 1.0, 22/11/00
 * 
 */
public class ProfileImpl extends Profile {

	private Properties props = null;

	// Keys to retrieve the implementation classes for configurable
	// functionalities among the bootstrap properties.
	private static final String RESOURCE = "resource";

	// #APIDOC_EXCLUDE_BEGIN
	public static final int DEFAULT_PORT = 1099;
	// #APIDOC_EXCLUDE_END

	// #ALL_EXCLUDE_BEGIN
	private static final String DEFAULT_IMTPMANAGER_CLASS = "jade.imtp.rmi.RMIIMTPManager";
	// #ALL_EXCLUDE_END
	/*
	 * #ALL_INCLUDE_BEGIN private static final String DEFAULT_IMTPMANAGER_CLASS =
	 * "jade.imtp.leap.LEAPIMTPManager"; #ALL_INCLUDE_END
	 */

	// #MIDP_EXCLUDE_BEGIN
	private MainContainerImpl myMain = null;
	// #MIDP_EXCLUDE_END

	private PlatformManager myPlatformManager = null;
	private ServiceManager myServiceManager = null;
	private CommandProcessor myCommandProcessor = null;
	private IMTPManager myIMTPManager = null;
	private ResourceManager myResourceManager = null;

	private boolean udpMonitoring;
	private int udpMonitoringPort;
	private int udpMonitoringPingDelay;
	private int udpMonitoringTimeLimit;

	/**
	 * Creates a Profile implementation using the given properties to configure the
	 * platform startup process.
	 * 
	 * @param aProp The names and values of the configuration properties to use.
	 */
	public ProfileImpl(Properties aProp) {
		props = aProp;
		init();
	}

	/**
	 * Creates a Profile implementation with the following default configuration:
	 * <br>
	 * if isMain is true, then the profile is configured to launch a main-container
	 * on the localhost, RMI internal Message Transport Protocol, port number 1099,
	 * HTTP MTP. <br>
	 * if isMain is false, then the profile is configured to launch a remote
	 * container on the localhost, connecting to the main-container on the localhost
	 * through RMI internal Message Transport Protocol, port number 1099.
	 */
	public ProfileImpl(boolean isMain) {

		// #ALL_EXCLUDE_BEGIN
		props = new jade.util.BasicProperties();
		// #ALL_EXCLUDE_END

		/*
		 * #ALL_INCLUDE_BEGIN props = new Properties(); #ALL_INCLUDE_END
		 */
		props.setProperty(Profile.MAIN, (new Boolean(isMain)).toString()); // set to a main/non-main container
		init();
	}

	/**
	 * This is equivalent to <code>ProfileImpl(true)</code>
	 */
	public ProfileImpl() {
		this(true);
	}

	/**
	 * Create a Profile object initialized with the settings specified in a given
	 * property file
	 */
	public ProfileImpl(String fileName) throws ProfileException {
		props = new Properties();
		if (fileName != null) {
			try {
				props.load(fileName);
			} catch (IOException ioe) {
				throw new ProfileException("Can't load properties: " + ioe.getMessage());
			}
		}
		init();
	}

	/**
	 * This constructor creates a default Profile for launching a platform.
	 * 
	 * @param host       is the name of the host where the main-container should be
	 *                   listen to. A null value means use the default (i.e.
	 *                   localhost)
	 * @param port       is the port number where the main-container should be
	 *                   listen for other containers. A negative value should be
	 *                   used for using the default port number.
	 * @param platformID is the synbolic name of the platform, if different from
	 *                   default. A null value means use the default (i.e.
	 *                   localhost)
	 **/
	public ProfileImpl(String host, int port, String platformID) {
		// create the object props
		// #ALL_EXCLUDE_BEGIN
		props = new jade.util.BasicProperties();
		// #ALL_EXCLUDE_END
		/*
		 * #ALL_INCLUDE_BEGIN props = new Properties(); #ALL_INCLUDE_END
		 */
		// set the passed properties
		if (host != null)
			props.setProperty(MAIN_HOST, host);
		if (port > 0)
			setIntProperty(MAIN_PORT, port);
		if (platformID != null)
			props.setProperty(PLATFORM_ID, platformID);
		// calls the method init to adjust all the other parameters
		init();
	}

	private void init() {

		// Set JVM parameter if not set
		if (props.getProperty(JVM) == null) {
			// #PJAVA_EXCLUDE_BEGIN
			props.setProperty(JVM, J2SE);
			// #PJAVA_EXCLUDE_END
			/*
			 * #PJAVA_INCLUDE_BEGIN props.setProperty(JVM, PJAVA); #PJAVA_INCLUDE_END
			 */
			/*
			 * #MIDP_INCLUDE_BEGIN props.setProperty(JVM, MIDP); props.setProperty(MAIN,
			 * "false"); #MIDP_INCLUDE_END
			 */
		}

		// Set default values

		setPropertyIfNot(MAIN, "true");

		String host = props.getProperty(MAIN_HOST);
		if (host == null) {
			host = getDefaultNetworkName();
			props.setProperty(MAIN_HOST, host);
		}

		String p = props.getProperty(MAIN_PORT);
		if (p == null) {
			String localPort = props.getProperty(LOCAL_PORT);

			// Default for a sole main container: use the local port, or
			// the default port if also the local port is null.
			if (isFirstMain()) {

				if (localPort != null) {
					p = localPort;
				} else {
					p = Integer.toString(DEFAULT_PORT);
				}
			} else {
				// All other cases: use the default port.
				p = Integer.toString(DEFAULT_PORT);
			}
			props.setProperty(MAIN_PORT, p);
		}

		String localHost = props.getProperty(LOCAL_HOST);
		if (localHost == null) {

			if (isFirstMain()) {
				// Default for a sole main container: use the MAIN_HOST property
				localHost = host;
			} else {
				// Default for a peripheral container or an added main container: use the local
				// host
				localHost = getDefaultNetworkName();
			}

			props.setProperty(LOCAL_HOST, localHost);
		}

		String lp = props.getProperty(LOCAL_PORT);
		if (lp == null) {
			if (isFirstMain()) {
				// Default for a sole main container: use the MAIN_PORT property
				lp = p;
			} else {
				// Default for a peripheral container or an added main container: use the
				// default port
				lp = Integer.toString(DEFAULT_PORT);
			}
			props.setProperty(LOCAL_PORT, lp);

		}

		setPropertyIfNot(SERVICES, DEFAULT_SERVICES);

		// #MIDP_EXCLUDE_BEGIN
		// Set agents as a list to handle the "gui" option
		try {
			List agents = getSpecifiers(AGENTS);
			String isGui = props.getProperty("gui");

			if (isGui != null && CaseInsensitiveString.equalsIgnoreCase(isGui, "true")) {
				Specifier s = new Specifier();

				s.setName("rma");
				s.setClassName("jade.tools.rma.rma");
				agents.add(0, s);
				props.put(AGENTS, agents);
			}
		} catch (ProfileException pe) {
			// FIXME: Should throw?
			pe.printStackTrace();
		}
		// #MIDP_EXCLUDE_END

		// #J2ME_EXCLUDE_BEGIN

		// If this is a Main Container and the '-nomtp' option is not
		// given, activate the default HTTP MTP (unless some MTPs have
		// been directly provided).
		if (isMain() && !getBooleanProperty("nomtp", false) && (props.getProperty(MTPS) == null)) {
			Specifier s = new Specifier();
			s.setClassName("jade.mtp.http.MessageTransportProtocol");
			List l = new ArrayList(1);
			l.add(s);
			props.put(MTPS, l);
		}
		// #J2ME_EXCLUDE_END

	}

	/**
	 * Return the underlying properties collection.
	 * 
	 * @return Properties The properties collection.
	 */
	public Properties getProperties() {
		return props;
	}

	/**
	 * Assign the given value to the given property name.
	 *
	 * @param key   is the property name
	 * @param value is the property value
	 */
	public void setParameter(String key, String value) {
		props.setProperty(key, value);
	}

	/**
	 * Assign the given property value to the given property name
	 *
	 * @param key   is the property name
	 * @param value is the property value
	 */
	public void setSpecifiers(String key, List value) {
		// #MIDP_EXCLUDE_BEGIN
		props.put(key, value);
		// #MIDP_EXCLUDE_END
	}

	/**
	 * Retrieve a String value from the configuration properties. If no parameter
	 * corresponding to the specified key is found, <code>aDefault</code> is
	 * returned.
	 * 
	 * @param key      The key identifying the parameter to be retrieved among the
	 *                 configuration properties.
	 * @param aDefault The value that is returned if the specified key is not found
	 */
	public String getParameter(String key, String aDefault) {
		String v = props.getProperty(key);
		if (v == null) {
			String dottedKey = key.replace('_', '.');
			v = props.getProperty(dottedKey);
		}
		if (v == null) {
			String underscoredKey = key.replace('.', '_');
			v = props.getProperty(underscoredKey);
		}
		return (v != null ? v.trim() : aDefault);
	}

	/**
	 * Retrieve a boolean value for a configuration property. If no corresponding
	 * property is found or if its string value cannot be converted to a boolean
	 * one, a default value is returned.
	 * 
	 * @param key      The key identifying the parameter to be retrieved among the
	 *                 configuration properties.
	 * @param aDefault The value to return when there is no property set for the
	 *                 given key, or its value cannot be converted to a boolean
	 *                 value.
	 *
	 *                 public boolean getParameter(String key, boolean aDefault) {
	 *                 String v = props.getProperty(key); if(v == null) { return
	 *                 aDefault; } else {
	 *                 if(CaseInsensitiveString.equalsIgnoreCase(v, "true")) {
	 *                 return true; } else
	 *                 if(CaseInsensitiveString.equalsIgnoreCase(v, "false")) {
	 *                 return false; } else { return aDefault; } } }
	 */

	/**
	 * Retrieve a list of Specifiers from the configuration properties. Agents, MTPs
	 * and other items are specified among the configuration properties in this way.
	 * If no list of Specifiers corresponding to the specified key is found, an
	 * empty list is returned.
	 * 
	 * @param key The key identifying the list of Specifires to be retrieved among
	 *            the configuration properties.
	 */
	public List getSpecifiers(String key) throws ProfileException {
		// #MIDP_EXCLUDE_BEGIN
		// Check if the list of specs is already in the properties as a list
		List l = null;
		try {
			l = (List) props.get(key);
		} catch (ClassCastException cce) {
		}
		if (l != null) {
			return l;
		}
		// #MIDP_EXCLUDE_END

		// The list should be present as a string --> parse it
		String specsLine = getParameter(key, null);

		try {
			Vector v = Specifier.parseSpecifierList(specsLine);
			// convert the vector into an arraylist (notice that using the vector allows to
			// avoid class loading of ArrayList)
			List l1 = new ArrayList(v.size());
			for (int i = 0; i < v.size(); i++) {
				l1.add(v.elementAt(i));
			}
			return l1;
		} catch (Exception e) {
			throw new ProfileException("Error parsing specifier list " + specsLine + ".", e);
		}
	}

	/**
	 * Retrieve a boolean value for a configuration property. If no corresponding
	 * property is found or if its string value cannot be converted to a boolean
	 * one, a default value is returned.
	 * 
	 * @param key      The key identifying the parameter to be retrieved among the
	 *                 configuration properties.
	 * @param aDefault The value to return when there is no property set for the
	 *                 given key, or its value cannot be converted to a boolean
	 *                 value.
	 */
	public boolean getBooleanProperty(String aKey, boolean aDefault) {
		String v = props.getProperty(aKey);
		if (v == null) {
			return aDefault;
		} else {
			if (CaseInsensitiveString.equalsIgnoreCase(v, "true")) {
				return true;
			} else if (CaseInsensitiveString.equalsIgnoreCase(v, "false")) {
				return false;
			} else {
				return aDefault;
			}
		}
	}

	/**
	 * Creates a string representation of this profile. The returned string has the
	 * format
	 * <p>
	 * <code>(profile name1=value1 name2=value2 ... )</code>
	 * </p>
	 * 
	 * @return A string containing a readable representation of this profile object.
	 */
	public String toString() {
		StringBuffer str = new StringBuffer("(Profile");
		String[] properties = propsToStringArray();
		if (properties != null)
			for (int i = 0; i < properties.length; i++)
				str.append(" " + properties[i]);
		str.append(")");
		return str.toString();
	}

	// #APIDOC_EXCLUDE_BEGIN

	protected PlatformManager getPlatformManager() throws ProfileException {
		if (myPlatformManager == null) {
			createPlatformManager();
		}
		return myPlatformManager;
	}

	/**
	 * Access the platform service manager.
	 * 
	 * @return The platform service manager, either the real implementation or a
	 *         remote proxy object.
	 * @throws ProfileException If some needed information is wrong or missing from
	 *                          the profile.
	 */
	protected ServiceManager getServiceManager() throws ProfileException {
		if (myServiceManager == null) {
			myServiceManager = new ServiceManagerImpl(this, getPlatformManager());
		}
		return myServiceManager;
	}

	/**
	 * Access the platform service finder.
	 * 
	 * @return The platform service finder, either the real implementation or a
	 *         remote proxy object.
	 * @throws ProfileException If some needed information is wrong or missing from
	 *                          the profile.
	 */
	protected ServiceFinder getServiceFinder() throws ProfileException {
		return (ServiceFinder) getServiceManager();
	}

	protected CommandProcessor getCommandProcessor() throws ProfileException {
		if (myCommandProcessor == null) {
			myCommandProcessor = new CommandProcessor();
		}
		return myCommandProcessor;
	}

	// #MIDP_EXCLUDE_BEGIN
	protected MainContainerImpl getMain() throws ProfileException {
		return myMain;
	}
	// #MIDP_EXCLUDE_END

	/**
	 */
	protected IMTPManager getIMTPManager() throws ProfileException {
		if (myIMTPManager == null) {
			createIMTPManager();
		}
		return myIMTPManager;
	}

	/**
	 */
	public ResourceManager getResourceManager() throws ProfileException {
		if (myResourceManager == null) {
			createResourceManager();
		}
		return myResourceManager;
	}

	// #APIDOC_EXCLUDE_END

	private void createPlatformManager() throws ProfileException {
		try {
			myIMTPManager = getIMTPManager();

			// #MIDP_EXCLUDE_BEGIN
			if (isMain()) {
				// This is a main container: create a real PlatformManager,
				// export it and get the MainContainer.
				PlatformManagerImpl pm = new PlatformManagerImpl(this);
				myIMTPManager.exportPlatformManager(pm);
				myMain = pm.getMain();
				myPlatformManager = pm;
				return;
			}
			// #MIDP_EXCLUDE_END

			// This is a peripheral container: create a proxy to the PlatformManager
			myPlatformManager = myIMTPManager.getPlatformManagerProxy();
		} catch (IMTPException imtpe) {
			throw new ProfileException("Can't get a proxy to the Platform Manager", imtpe);
		}
	}

	private void createIMTPManager() throws ProfileException {
		String className = getParameter(IMTP, DEFAULT_IMTPMANAGER_CLASS);
		try {
			myIMTPManager = (IMTPManager) Class.forName(className).newInstance();
		} catch (Exception e) {
			throw new ProfileException("Error loading IMTPManager class " + className);
		}
	}

	private void createResourceManager() throws ProfileException {
		// #J2ME_EXCLUDE_BEGIN
		String className = getParameter(RESOURCE, "jade.core.FullResourceManager");
		// #J2ME_EXCLUDE_END
		/*
		 * #J2ME_INCLUDE_BEGIN String className = getParameter(RESOURCE,
		 * "jade.core.LightResourceManager"); #J2ME_INCLUDE_END
		 */

		try {
			myResourceManager = (ResourceManager) Class.forName(className).newInstance();
		} catch (Exception e) {
			throw new ProfileException("Error loading ResourceManager class " + className);
		}
	}

	private void setPropertyIfNot(String key, String value) {
		String old = props.getProperty(key);
		if (old == null) {
			props.setProperty(key, value);
		}
	}

	private void setIntProperty(String aKey, int aValue) {
		props.setProperty(aKey, Integer.toString(aValue));
	}

	private String[] propsToStringArray() {
		String[] result = new String[props.size()];
		int i = 0;
		for (Enumeration e = props.keys(); e.hasMoreElements();) {
			String key = (String) e.nextElement();
			String value = props.getProperty(key);
			if (value != null) {
				result[i++] = key + "=" + value;
			} else {
				result[i++] = key + "=";
			}
		}
		return result;
	}

	// #APIDOC_EXCLUDE_BEGIN

	protected boolean isMain() {
		return getBooleanProperty(MAIN, false);
	}

	// True if this is a Main Container and the LOCAL_SERVICE_MANAGER
	// option is set to false or not set
	protected boolean isFirstMain() {
		return isMain() && !getBooleanProperty(LOCAL_SERVICE_MANAGER, false);
	}

	// #APIDOC_EXCLUDE_END

}

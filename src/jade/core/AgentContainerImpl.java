/*****************************************************************
JADE - Java Agent DEvelopment Framework is a framework to develop
multi-agent systems in compliance with the FIPA specifications.
Copyright (C) 2000 CSELT S.p.A.

GNU Lesser General Public License

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation,
version 2.1 of the License.

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public
License along with this library; if not, write to the
Free Software Foundation, Inc., 59 Temple Place - Suite 330,
Boston, MA  02111-1307, USA.
 *****************************************************************/

package jade.core;

import java.util.Iterator;
import java.util.Vector;

import jade.core.behaviours.Behaviour;
import jade.core.messaging.GenericMessage;
import jade.lang.acl.ACLMessage;
import jade.mtp.MTPDescriptor;
import jade.mtp.TransportAddress;
import jade.security.Credentials;
import jade.security.CredentialsHelper;
import jade.security.JADEPrincipal;
import jade.security.JADESecurityException;
import jade.util.Logger;
import jade.util.leap.ArrayList;
import jade.util.leap.List;

/**
 * This class is a concrete implementation of the JADE agent container,
 * providing runtime support to JADE agents.
 * 
 * This class cannot be instantiated from applications. Instead, the
 * <code>Runtime.createAgentContainer(Profile p)</code> method must be called.
 * 
 * @see Runtime#createAgentContainer(Profile)
 * 
 * @author Giovanni Rimassa - Universita' di Parma
 * @author Jerome Picault - Motorola Labs
 * @author Giovanni Caire - TILAB
 * @version $Date: 2005/02/24 08:43:05 $ $Revision: 2.201 $
 * 
 */
class AgentContainerImpl implements AgentContainer, AgentToolkit {

	private Logger myLogger = Logger.getMyLogger(this.getClass().getName());

	// Local agents, indexed by agent name
	protected LADT localAgents;

	// The Profile defining the configuration of this Container
	protected Profile myProfile;

	// The Command Processor through which all the vertical commands in this
	// container will pass
	protected CommandProcessor myCommandProcessor;

	// #MIDP_EXCLUDE_BEGIN
	// The agent platform this container belongs to
	protected MainContainerImpl myMainContainer; // FIXME: It should go away
	// #MIDP_EXCLUDE_END

	// The IMTP manager, used to access IMTP-dependent functionalities
	protected IMTPManager myIMTPManager;

	// The platform Service Manager
	private ServiceManager myServiceManager;

	// The platform Service Finder
	private ServiceFinder myServiceFinder;

	// The Object managing Thread resources in this container
	private ResourceManager myResourceManager;

	protected ContainerID myID;
	protected NodeDescriptor myNodeDescriptor;

	// These are only used at bootstrap-time to initialize the local
	// NodeDescriptor. Further modifications take no effect
	protected JADEPrincipal ownerPrincipal;
	protected Credentials ownerCredentials;

	private AID theAMS;
	private AID theDefaultDF;

	// Default constructor
	AgentContainerImpl() {
	}

	// Package scoped constructor, so that only the Runtime
	// class can actually create a new Agent Container.
	AgentContainerImpl(Profile p) {
		myProfile = p;
		localAgents = new LADT(16);
	}

	// #MIDP_EXCLUDE_BEGIN
	/////////////////////////////////////////////////
	// Support for the in-process interface section
	/////////////////////////////////////////////////
	jade.wrapper.AgentContainer getContainerController() {
		return getContainerController(myNodeDescriptor.getOwnerPrincipal(), myNodeDescriptor.getOwnerCredentials());
	}

	public jade.wrapper.AgentContainer getContainerController(JADEPrincipal principal, Credentials credentials) {
		return new jade.wrapper.AgentContainer(getContainerProxy(principal, credentials), this, getPlatformID());
	}

	/**
	 * Return a proxy that allows making requests to the local container as if they
	 * were received from the main. This allows dealing uniformly with local and
	 * remote requests. Local requests occurs at bootstrap and following calls to
	 * the in-process interface.
	 */
	private jade.wrapper.ContainerProxy getContainerProxy(final JADEPrincipal principal,
			final Credentials credentials) {
		return new jade.wrapper.ContainerProxy() {
			GenericCommand dummyCmd = new GenericCommand(null, null, null);

			{
				dummyCmd.setPrincipal(principal);
				dummyCmd.setCredentials(credentials);
			}

			public void createAgent(AID id, String className, Object[] args) throws Throwable {
				// Do as if it was a remote call from the main to allows
				// security checks to take place if needed
				jade.core.management.AgentManagementSlice target = (jade.core.management.AgentManagementSlice) getProxyToLocalSlice(
						jade.core.management.AgentManagementSlice.NAME);
				// roberta: a criacao do agente ? aqui!!!
				target.createAgent(id, className, args, principal, null, target.CREATE_ONLY, dummyCmd);
			}

			public void killContainer() throws Throwable {
				// Do as if it was a remote call from the main to allows
				// security checks to take place if needed
				jade.core.management.AgentManagementSlice target = (jade.core.management.AgentManagementSlice) getProxyToLocalSlice(
						jade.core.management.AgentManagementSlice.NAME);
				// FIXME: set Principal and Credentials
				target.exitContainer();
			}

			public MTPDescriptor installMTP(String address, String className) throws Throwable {
				// Do as if it was a remote call from the main to allows
				// security checks to take place if needed
				jade.core.messaging.MessagingSlice target = (jade.core.messaging.MessagingSlice) getProxyToLocalSlice(
						jade.core.messaging.MessagingSlice.NAME);
				// FIXME: set Principal and Credentials
				return target.installMTP(address, className);
			}

			public void uninstallMTP(String address) throws Throwable {
				// Do as if it was a remote call from the main to allows
				// security checks to take place if needed
				jade.core.messaging.MessagingSlice target = (jade.core.messaging.MessagingSlice) getProxyToLocalSlice(
						jade.core.messaging.MessagingSlice.NAME);
				// FIXME: set Principal and Credentials
				target.uninstallMTP(address);
			}

			public void suspendAgent(AID id) throws Throwable {
				// Do as if it was a remote call from the main to allows
				// security checks to take place if needed
				jade.core.management.AgentManagementSlice target = (jade.core.management.AgentManagementSlice) getProxyToLocalSlice(
						jade.core.management.AgentManagementSlice.NAME);
				// FIXME: set Principal and Credentials
				target.changeAgentState(id, Agent.AP_SUSPENDED);
			}

			public void activateAgent(AID id) throws Throwable {
				// Do as if it was a remote call from the main to allows
				// security checks to take place if needed
				jade.core.management.AgentManagementSlice target = (jade.core.management.AgentManagementSlice) getProxyToLocalSlice(
						jade.core.management.AgentManagementSlice.NAME);
				// FIXME: set Principal and Credentials
				target.changeAgentState(id, Agent.AP_ACTIVE);
			}

			public void killAgent(AID id) throws Throwable {
				// Do as if it was a remote call from the main to allows
				// security checks to take place if needed
				jade.core.management.AgentManagementSlice target = (jade.core.management.AgentManagementSlice) getProxyToLocalSlice(
						jade.core.management.AgentManagementSlice.NAME);
				target.killAgent(id, dummyCmd);
			}

			public void moveAgent(AID id, Location where) throws Throwable {
				// Do as if it was a remote call from the main to allows
				// security checks to take place if needed
				jade.core.mobility.AgentMobilitySlice target = (jade.core.mobility.AgentMobilitySlice) getProxyToLocalSlice(
						jade.core.mobility.AgentMobilitySlice.NAME);
				// FIXME: set Principal and Credentials
				target.moveAgent(id, where);
			}

			public void cloneAgent(AID id, Location where, String newName) throws Throwable {
				// Do as if it was a remote call from the main to allows
				// security checks to take place if needed
				jade.core.mobility.AgentMobilitySlice target = (jade.core.mobility.AgentMobilitySlice) getProxyToLocalSlice(
						jade.core.mobility.AgentMobilitySlice.NAME);
				// FIXME: set Principal and Credentials
				target.copyAgent(id, where, newName);
			}

			private Service.SliceProxy getProxyToLocalSlice(String serviceName) throws Throwable {
				Service svc = myServiceFinder.findService(serviceName);
				return (Service.SliceProxy) myIMTPManager.createSliceProxy(serviceName, svc.getHorizontalInterface(),
						myIMTPManager.getLocalNode());
			}
		};
	}
	////////////////////////////////////////////////////////
	// END of support for the in-process interface section
	////////////////////////////////////////////////////////
	// #MIDP_EXCLUDE_END

	/**
	 * Issue an INFORM_CREATED vertical command. Note that the Principal and
	 * Credentials if any are those of the owner of the newly born agent. The
	 * SecurityService, if active, creates the Principal and credentials of the
	 * agent and sets them.
	 */
	public void initAgent(AID agentID, Agent instance, JADEPrincipal ownerPrincipal, Credentials initialCredentials)
			throws NameClashException, IMTPException, NotFoundException, JADESecurityException {

		// Setting the AID and toolkit here is redundant, but
		// allows services to retrieve their agent helper correctly
		// when processing the INFORM_CREATED command.
		instance.setAID(agentID);
		instance.setToolkit(this);

		GenericCommand cmd = new GenericCommand(jade.core.management.AgentManagementSlice.INFORM_CREATED,
				jade.core.management.AgentManagementSlice.NAME, null);
		cmd.addParam(agentID);
		cmd.addParam(instance);
		cmd.addParam(ownerPrincipal);
		cmd.addParam(initialCredentials);

		Object ret = myCommandProcessor.processOutgoing(cmd);
		if (ret != null) {
			if (ret instanceof NameClashException) {
				throw ((NameClashException) ret);
			} else if (ret instanceof IMTPException) {
				throw ((IMTPException) ret);
			} else if (ret instanceof NotFoundException) {
				throw ((NotFoundException) ret);
			} else if (ret instanceof JADESecurityException) {
				throw ((JADESecurityException) ret);
			} else if (ret instanceof Throwable) {
				((Throwable) ret).printStackTrace();
			}
		}
	}

	public NodeDescriptor getNodeDescriptor() {
		return myNodeDescriptor;
	}

	protected void init() throws IMTPException, ProfileException {
		myCommandProcessor = myProfile.getCommandProcessor();

		// Create and initialize the IMTPManager
		myIMTPManager = myProfile.getIMTPManager();
		myIMTPManager.initialize(myProfile);

		// Get the Service Manager and the Service Finder
		myServiceManager = myProfile.getServiceManager();
		myServiceFinder = myProfile.getServiceFinder();

		// Attach CommandProcessor and ServiceManager to the local node
		BaseNode localNode = (BaseNode) myIMTPManager.getLocalNode();
		localNode.setCommandProcessor(myCommandProcessor);
		localNode.setServiceManager(myServiceManager);

		// #MIDP_EXCLUDE_BEGIN
		myMainContainer = myProfile.getMain();
		// #MIDP_EXCLUDE_END

		// This string will be used to build the GUID for every agent on
		// this platform.
		AID.setPlatformID(myServiceManager.getPlatformName());

		// Build the Agent IDs for the AMS and for the Default DF.
		theAMS = new AID("ams", AID.ISLOCALNAME);
		theDefaultDF = new AID("df", AID.ISLOCALNAME);

		// Create the ResourceManager
		myResourceManager = myProfile.getResourceManager();

		// Initialize the Container ID
		TransportAddress addr = (TransportAddress) myIMTPManager.getLocalAddresses().get(0);
		myID = new ContainerID(myProfile.getParameter(Profile.CONTAINER_NAME, PlatformManager.NO_NAME), addr);
		myNodeDescriptor = new NodeDescriptor(myID, myIMTPManager.getLocalNode());
	}

	/**
	 * Add the node to the platform with the basic services
	 */
	protected void startNode()
			throws IMTPException, ProfileException, ServiceException, JADESecurityException, NotFoundException {
		// Start all container fundamental services (without activating them)
		List basicServices = new ArrayList();
		ServiceDescriptor dsc = startService("jade.core.management.AgentManagementService", false);
		basicServices.add(dsc);
		// #MIDP_EXCLUDE_BEGIN
		dsc = startService("jade.core.messaging.MessagingService", false);
		basicServices.add(dsc);
		// #MIDP_EXCLUDE_END
		/*
		 * #MIDP_INCLUDE_BEGIN dsc =
		 * startService("jade.core.messaging.LightMessagingService", false);
		 * basicServices.add(dsc); #MIDP_INCLUDE_END
		 */
		List l = myProfile.getSpecifiers(Profile.SERVICES);
		myProfile.setSpecifiers(Profile.SERVICES, l); // Avoid parsing services twice
		Iterator serviceSpecifiers = l.iterator();
		while (serviceSpecifiers.hasNext()) {
			Specifier s = (Specifier) serviceSpecifiers.next();
			String serviceClass = s.getClassName();
			if (serviceClass.equals("jade.core.security.SecurityService")
					|| serviceClass.equals("jade.core.security.permission.PermissionService")) {
				dsc = startService(serviceClass, false);
				basicServices.add(dsc);
			}
		}

		// Register with the platform
		ServiceDescriptor[] descriptors = new ServiceDescriptor[basicServices.size()];
		for (int i = 0; i < descriptors.length; ++i) {
			descriptors[i] = (ServiceDescriptor) basicServices.get(i);
		}
		// This call can modify the name of this container
		myServiceManager.addNode(myNodeDescriptor, descriptors);

		// Boot all basic services
		for (int i = 0; i < descriptors.length; ++i) {
			descriptors[i].getService().boot(myProfile);
		}

		// #MIDP_EXCLUDE_BEGIN
		// If we are the master main container --> start the AMS and DF
		if (myMainContainer != null) {
			boolean startThem = (myProfile.getParameter(Profile.LOCAL_SERVICE_MANAGER, null) == null);
			myMainContainer.initSystemAgents(this, startThem);
		}
		// #MIDP_EXCLUDE_END
	}

	protected void startAdditionalServices() {
		// Start all the additional services mentioned in the profile
		try {
			List l = myProfile.getSpecifiers(Profile.SERVICES);
			Iterator serviceSpecifiers = l.iterator();
			while (serviceSpecifiers.hasNext()) {
				Specifier s = null;
				try {
					s = (Specifier) serviceSpecifiers.next();
					String serviceClass = s.getClassName();
					if (!(serviceClass.equals("jade.core.security.SecurityService")
							|| serviceClass.equals("jade.core.security.permission.PermissionService"))) {
						startService(serviceClass, true);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		} catch (ProfileException pe) {
			pe.printStackTrace();
		}
	}

	boolean joinPlatform() {
		// #J2ME_EXCLUDE_BEGIN
		// Redirect output if the -output option is specified
		String output = myProfile.getParameter("output", null);
		if (output != null) {

			try {
				jade.util.PerDayFileLogger fl = new jade.util.PerDayFileLogger(output);
				jade.util.PrintStreamSplitter pss = new jade.util.PrintStreamSplitter(System.out, fl);
				System.setOut(pss);
				System.setErr(pss);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {

		}
		// #J2ME_EXCLUDE_END

		try {
			// Perform the initial setup from the profile
			init();

			// Connect the local node to the platform and activate the basic services
			startNode();
		} catch (IMTPException imtpe) {
			myLogger.log(Logger.SEVERE, "Communication failure while joining agent platform: " + imtpe.getMessage());
			imtpe.printStackTrace();
			endContainer();
			return false;
		} catch (JADESecurityException ae) {
			myLogger.log(Logger.SEVERE, "Authentication or authorization failure while joining agent platform.");
			ae.printStackTrace();
			endContainer();
			return false;
		} catch (Exception e) {
			myLogger.log(Logger.SEVERE, "Some problem occurred while joining agent platform.");
			e.printStackTrace();
			endContainer();
			return false;
		}

		// Start additional services as specified in the profile
		startAdditionalServices();

		// Create and activate agents that must be launched at bootstrap
		startBootstrapAgents();

		myLogger.log(Logger.INFO, "--------------------------------------\nAgent container " + myID
				+ " is ready.\n--------------------------------------------");
		return true;
	}

	private void startBootstrapAgents() {
		try {
			List l = myProfile.getSpecifiers(Profile.AGENTS);
			Iterator agentSpecifiers = l.iterator();
			while (agentSpecifiers.hasNext()) {
				Specifier s = (Specifier) agentSpecifiers.next();
				if (s.getName() != null) {

					AID agentID = new AID(s.getName(), AID.ISLOCALNAME);

					try {
						// #MIDP_EXCLUDE_BEGING
						getContainerProxy(myNodeDescriptor.getOwnerPrincipal(), myNodeDescriptor.getOwnerCredentials())
								.createAgent(agentID, s.getClassName(), s.getArgs());
						// #MIDP_EXCLUDE_END
						/*
						 * #MIDP_INCLUDE_BEGIN String serviceName =
						 * jade.core.management.AgentManagementSlice.NAME; Service svc =
						 * myServiceFinder.findService(serviceName);
						 * jade.core.management.AgentManagementSlice target =
						 * (jade.core.management.AgentManagementSlice)
						 * myIMTPManager.createSliceProxy(serviceName, svc.getHorizontalInterface(),
						 * myIMTPManager.getLocalNode()); GenericCommand dummyCmd = new
						 * GenericCommand(null, null, null);
						 * dummyCmd.setPrincipal(myNodeDescriptor.getOwnerPrincipal());
						 * dummyCmd.setCredentials(myNodeDescriptor.getOwnerCredentials());
						 * target.createAgent(agentID, s.getClassName(), s.getArgs(),
						 * myNodeDescriptor.getOwnerPrincipal(), null, target.CREATE_ONLY, dummyCmd);
						 * #MIDP_INCLUDE_END
						 */
					} catch (Throwable t) {
						if (myLogger.isLoggable(Logger.SEVERE))
							myLogger.log(Logger.SEVERE, "Cannot create agent " + s.getName() + ": " + t.getMessage());
					}
				} else {
					myLogger.log(Logger.WARNING, "Cannot create an agent with no name");
				}
			}

			// Now activate all agents (this call starts their embedded threads)
			AID[] allLocalNames = localAgents.keys();
			for (int i = 0; i < allLocalNames.length; i++) {
				AID id = allLocalNames[i];

				if (!id.equals(theAMS) && !id.equals(theDefaultDF)) {
					try {
						powerUpLocalAgent(id);
					} catch (NotFoundException nfe) {
						// Should never happen
						nfe.printStackTrace();
					}
				}
			}
		} catch (ProfileException pe) {
			if (myLogger.isLoggable(Logger.SEVERE))
				myLogger.log(Logger.SEVERE, "Warning: error reading initial agents");
		}
	}

	// Incluido por Roberta:
	// recebe uma lista de Specifiers
	public void startAgents(List l) {

		Iterator agentSpecifiers = l.iterator();
		while (agentSpecifiers.hasNext()) {
			Specifier s = (Specifier) agentSpecifiers.next();
			if (s.getName() != null) {

				AID agentID = new AID(s.getName(), AID.ISLOCALNAME);

				try {
					// #MIDP_EXCLUDE_BEGING

					getContainerProxy(myNodeDescriptor.getOwnerPrincipal(), myNodeDescriptor.getOwnerCredentials())
							.createAgent(agentID, s.getClassName(), s.getArgs());
					powerUpLocalAgent(agentID);

				} catch (Throwable t) {
					if (myLogger.isLoggable(Logger.SEVERE))
						myLogger.log(Logger.SEVERE, "Cannot create agent " + s.getName() + ": " + t.getMessage());
				}
			} else {
				myLogger.log(Logger.WARNING, "Cannot create an agent with no name");
			}
		}

	}// method

	public void shutDown() {
		// Remove all non-system agents
		Agent[] allLocalAgents = localAgents.values();

		for (int i = 0; i < allLocalAgents.length; i++) {
			// Kill agent and wait for its termination
			Agent a = allLocalAgents[i];

			// Skip the Default DF and the AMS
			AID id = a.getAID();

			if (id.equals(getAMS()) || id.equals(getDefaultDF()))
				continue;

			// System.out.println("Killing agent "+a.getLocalName());
			// System.out.flush();
			a.doDelete();
			// System.out.println("Done. Waiting for its termination...");
			// System.out.flush();
			a.join();
			// System.out.println("Agent "+a.getLocalName()+" terminated");
			// System.out.flush();
			a.resetToolkit();
		}

		try {

			myServiceManager.removeNode(myNodeDescriptor);
			myIMTPManager.shutDown();

		} catch (IMTPException imtpe) {
			imtpe.printStackTrace();
		} catch (ServiceException se) {
			se.printStackTrace();
		}

		// Releases Thread resources
		myResourceManager.releaseResources();

		// Notify the JADE Runtime that the container has terminated execution
		endContainer();

	}

//	Roberta Selective shutdown  
	public void selectiveShutDown(Vector survivers) {
		// Remove all non-system agents
		Agent[] allLocalAgents = localAgents.values();
		String localName = null;
		String single = null;

		if (survivers != null && (survivers.size() == 1)) {
			single = (String) survivers.firstElement();
		}

		for (int i = 0; i < allLocalAgents.length; i++) {
			// Kill agent and wait for its termination
			Agent a = allLocalAgents[i];
			// Skip the Default DF and the AMS
			AID id = a.getAID();
			localName = id.getLocalName();

			if (id.equals(getAMS()) || id.equals(getDefaultDF())
					|| ((single != null && single.equals(localName)) || survivers.contains(id.getLocalName())))
				continue;

			// System.out.println("Killing agent "+a.getLocalName());
			// System.out.flush();
			a.doDelete();
			// System.out.println("Done. Waiting for its termination...");
			// System.out.flush();
			a.join();
			// System.out.println("Agent "+a.getLocalName()+" terminated");
			// System.out.flush();
			a.resetToolkit();
		}

		try {

			myServiceManager.removeNode(myNodeDescriptor);
			myIMTPManager.shutDown();

		} catch (IMTPException imtpe) {
			imtpe.printStackTrace();
		} catch (ServiceException se) {
			se.printStackTrace();
		}

		// Releases Thread resources
		myResourceManager.releaseResources();

		// Notify the JADE Runtime that the container has terminated execution
		endContainer();

	}

	// calls Runtime.instance().endContainer()
	// with the security priviledges of AgentContainerImpl
	// no matter priviledges of who originaltely triggered this action
	private void endContainer() {
		try {
			Runtime.instance().endContainer();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	////////////////////////////////////////////
	// AgentToolkit interface implementation
	////////////////////////////////////////////

	public Location here() {
		return myID;
	}

	/**
	 * Issue a SEND_MESSAGE VerticalCommand for each receiver
	 */
	public void handleSend(ACLMessage msg, AID sender, boolean needClone) {
		Iterator it = msg.getAllIntendedReceiver();
		// If there are multiple receivers the message must always be cloned
		// since the MessageManager will modify it. If there is a single
		// receiver we clone it or not depending on the needClone parameter
		boolean isFirst = true;
		while (it.hasNext()) {
			AID receiver = (AID) it.next();
			if (isFirst) {
				needClone = needClone || it.hasNext();
				isFirst = false;
			}
			GenericCommand cmd = new GenericCommand(jade.core.messaging.MessagingSlice.SEND_MESSAGE,
					jade.core.messaging.MessagingSlice.NAME, null);
			cmd.addParam(sender);
			ACLMessage toBeSent = null;
			if (needClone) {
				toBeSent = (ACLMessage) msg.clone();
			} else {
				toBeSent = msg;
			}
			isFirst = false;
			GenericMessage gmsg = new GenericMessage(toBeSent);
			cmd.addParam(gmsg);
			cmd.addParam(receiver);
			// Set the credentials of the sender
			initCredentials(cmd, sender);
			Object ret = myCommandProcessor.processOutgoing(cmd);
			if (ret != null) {
				if (ret instanceof Throwable) {
					// The SEND_MESSAGE VerticalCommand was blocked by some Filter
					// before reaching the Messaging Souce Sink --> Issue
					// a NOTIFY_FAILURE VerticalCommand to notify the sender
					cmd = new GenericCommand(jade.core.messaging.MessagingSlice.NOTIFY_FAILURE,
							jade.core.messaging.MessagingSlice.NAME, null);
					cmd.addParam(gmsg);
					cmd.addParam(receiver);
					cmd.addParam(new InternalError("Message blocked: " + ret));
					ret = myCommandProcessor.processOutgoing(cmd);
					if (ret != null) {
						if (ret instanceof Throwable) {
							((Throwable) ret).printStackTrace();
						}
					}
				}
			}
		}

	}

	// #MIDP_EXCLUDE_BEGIN
	// FIXME: to be removed
	public void handlePosted(AID agentID, ACLMessage msg) {
		GenericCommand cmd = new GenericCommand(jade.core.event.NotificationSlice.NOTIFY_POSTED,
				jade.core.event.NotificationSlice.NAME, null);
		cmd.addParam(msg);
		cmd.addParam(agentID);

		Object ret = myCommandProcessor.processOutgoing(cmd);
		if (ret != null) {
			if (ret instanceof Throwable) {
				((Throwable) ret).printStackTrace();
			}
		}
	}
	// #MIDP_EXCLUDE_END

	// #MIDP_EXCLUDE_BEGIN
	/**
	 * Issue a NOTIFY_RECEIVED VerticalCommand
	 */
	public void handleReceived(AID agentID, ACLMessage msg) {
		GenericCommand cmd = new GenericCommand(jade.core.event.NotificationSlice.NOTIFY_RECEIVED,
				jade.core.event.NotificationSlice.NAME, null);
		cmd.addParam(msg);
		cmd.addParam(agentID);
		// No security check is meaningful on this action --> don't even set the
		// Credentials

		Object ret = myCommandProcessor.processOutgoing(cmd);
		if (ret != null) {
			if (ret instanceof Throwable) {
				((Throwable) ret).printStackTrace();
			}
		}

	}
	// #MIDP_EXCLUDE_END

	// #MIDP_EXCLUDE_BEGIN
	public void handleBehaviourAdded(AID agentID, Behaviour b) {
		GenericCommand cmd = new GenericCommand(jade.core.event.NotificationSlice.NOTIFY_BEHAVIOUR_ADDED,
				jade.core.event.NotificationSlice.NAME, null);
		cmd.addParam(agentID);
		cmd.addParam(b);
		// No security check is meaningful on this action --> don't even set the
		// Credentials

		Object ret = myCommandProcessor.processOutgoing(cmd);
		if (ret != null) {
			if (ret instanceof Throwable) {
				((Throwable) ret).printStackTrace();
			}
		}
	}
	// #MIDP_EXCLUDE_END

	// #MIDP_EXCLUDE_BEGIN
	public void handleBehaviourRemoved(AID agentID, Behaviour b) {
		GenericCommand cmd = new GenericCommand(jade.core.event.NotificationSlice.NOTIFY_BEHAVIOUR_REMOVED,
				jade.core.event.NotificationSlice.NAME, null);
		cmd.addParam(agentID);
		cmd.addParam(b);
		// No security check is meaningful on this action --> don't even set the
		// Credentials

		Object ret = myCommandProcessor.processOutgoing(cmd);
		if (ret != null) {
			if (ret instanceof Throwable) {
				((Throwable) ret).printStackTrace();
			}
		}
	}
	// #MIDP_EXCLUDE_END

	// #MIDP_EXCLUDE_BEGIN
	public void handleChangeBehaviourState(AID agentID, Behaviour b, String from, String to) {
		GenericCommand cmd = new GenericCommand(jade.core.event.NotificationSlice.NOTIFY_CHANGED_BEHAVIOUR_STATE,
				jade.core.event.NotificationSlice.NAME, null);
		cmd.addParam(agentID);
		cmd.addParam(b);
		cmd.addParam(from);
		cmd.addParam(to);
		// No security check is meaningful on this action --> don't even set the
		// Credentials

		Object ret = myCommandProcessor.processOutgoing(cmd);
		if (ret != null) {
			if (ret instanceof Throwable) {
				((Throwable) ret).printStackTrace();
			}
		}
	}
	// #MIDP_EXCLUDE_END

	// #MIDP_EXCLUDE_BEGIN
	// FIXME: to be removed
	public void handleChangedAgentPrincipal(AID agentID, JADEPrincipal oldPrincipal, Credentials creds) {

		/***
		 * 
		 * myNotificationManager.fireEvent(NotificationManager.CHANGED_AGENT_PRINCIPAL,
		 * new Object[]{agentID, oldPrincipal,
		 * (AgentPrincipal)certs.getIdentityCertificate().getSubject()}); try {
		 * myPlatform.changedAgentPrincipal(agentID, certs); } catch (IMTPException re)
		 * { re.printStackTrace(); } catch (NotFoundException nfe) {
		 * nfe.printStackTrace(); }
		 * 
		 ***/
	}
	// #MIDP_EXCLUDE_END

	public void handleChangedAgentState(AID agentID, int oldState, int newState) {
		AgentState from = AgentState.getInstance(oldState);
		AgentState to = AgentState.getInstance(newState);

		GenericCommand cmd = new GenericCommand(jade.core.management.AgentManagementSlice.INFORM_STATE_CHANGED,
				jade.core.management.AgentManagementSlice.NAME, null);
		cmd.addParam(agentID);
		cmd.addParam(from);
		cmd.addParam(to);
		// No security check is meaningful on this action --> don't even set the
		// Credentials

		Object ret = myCommandProcessor.processOutgoing(cmd);
		if (ret != null) {
			if (ret instanceof Throwable) {
				((Throwable) ret).printStackTrace();
			}
		}
	}

	public void handleEnd(AID agentID) {
		GenericCommand cmd = new GenericCommand(jade.core.management.AgentManagementSlice.INFORM_KILLED,
				jade.core.management.AgentManagementSlice.NAME, null);
		cmd.addParam(agentID);
		// Set the credentials of the terminating agent
		initCredentials(cmd, agentID);

		Object ret = myCommandProcessor.processOutgoing(cmd);
		if (ret != null) {
			if (ret instanceof Throwable) {
				((Throwable) ret).printStackTrace();
			}
		}
	}

	/*
	 * FIXME: Needed due to the Persistence Service being an add-on public void
	 * handleSave(AID agentID, String repository) throws ServiceException,
	 * NotFoundException, IMTPException { GenericCommand cmd = new
	 * GenericCommand("Save-Myself", "jade.core.persistence.Persistence", null);
	 * cmd.addParam(agentID); cmd.addParam(repository); // Set the credentials of
	 * the agent to be saved initCredentials(cmd, agentID);
	 * 
	 * Object ret = myCommandProcessor.processOutgoing(cmd); if (ret != null) { if
	 * (ret instanceof ServiceException) { throw ((ServiceException) ret); } else if
	 * (ret instanceof NotFoundException) { throw ((NotFoundException) ret); } else
	 * if (ret instanceof IMTPException) { throw ((IMTPException) ret); } else if
	 * (ret instanceof Throwable) { ((Throwable) ret).printStackTrace(); } } }
	 */

	// FIXME: Needed due to the Persistence Service being an add-on
	/*
	 * public void handleReload(AID agentID, String repository) throws
	 * ServiceException, NotFoundException, IMTPException { GenericCommand cmd = new
	 * GenericCommand("Reload-Myself", "jade.core.persistence.Persistence", null);
	 * cmd.addParam(agentID); cmd.addParam(repository); // Set the credentials of
	 * the agent to be reloaded initCredentials(cmd, agentID);
	 * 
	 * Object ret = myCommandProcessor.processOutgoing(cmd); if (ret != null) { if
	 * (ret instanceof ServiceException) { throw ((ServiceException) ret); } else if
	 * (ret instanceof NotFoundException) { throw ((NotFoundException) ret); } else
	 * if (ret instanceof IMTPException) { throw ((IMTPException) ret); } else if
	 * (ret instanceof Throwable) { ((Throwable) ret).printStackTrace(); } } }
	 */

	// FIXME: Needed due to the Persistence Service being an add-on
	/*
	 * public void handleFreeze(AID agentID, String repository, ContainerID
	 * bufferContainer) throws ServiceException, NotFoundException, IMTPException {
	 * GenericCommand cmd = new GenericCommand("Freeze-Myself",
	 * "jade.core.persistence.Persistence", null); cmd.addParam(agentID);
	 * cmd.addParam(repository); cmd.addParam(bufferContainer); // Set the
	 * credentials of the agent to be frozen initCredentials(cmd, agentID);
	 * 
	 * Object ret = myCommandProcessor.processOutgoing(cmd); if (ret != null) { if
	 * (ret instanceof ServiceException) { throw ((ServiceException) ret); } else if
	 * (ret instanceof NotFoundException) { throw ((NotFoundException) ret); } else
	 * if (ret instanceof IMTPException) { throw ((IMTPException) ret); } else if
	 * (ret instanceof Throwable) { ((Throwable) ret).printStackTrace(); } } }
	 */

	public void setPlatformAddresses(AID id) {
		GenericCommand cmd = new GenericCommand(jade.core.messaging.MessagingSlice.SET_PLATFORM_ADDRESSES,
				jade.core.messaging.MessagingSlice.NAME, null);
		cmd.addParam(id);
		// No security check is meaningful on this action --> don't even set the
		// Credentials

		Object ret = myCommandProcessor.processOutgoing(cmd);
		if (ret != null) {
			if (ret instanceof Throwable) {
				((Throwable) ret).printStackTrace();
			}
		}
	}

	public AID getAMS() {
		return (AID) theAMS.clone();
	}

	public AID getDefaultDF() {
		return (AID) theDefaultDF.clone();
	}

	public String getProperty(String key, String aDefault) {
		return myProfile.getParameter(key, aDefault);
	}

	public ServiceHelper getHelper(Agent a, String serviceName) throws ServiceException {
		try {

			// Retrieve the service
			Service s = myServiceFinder.findService(serviceName);
			if (s == null) {
				throw new ServiceNotActiveException(serviceName);
			}

			return s.getHelper(a);
		} catch (IMTPException imtpe) {
			throw new ServiceException(" ServiceHelper could not be created for: " + serviceName, imtpe);
		}
	}

	// Private and package scoped methods

	/**
	 */
	public String getPlatformID() {
		return AID.getPlatformID();
	}

	public Agent addLocalAgent(AID id, Agent a) {

		a.setToolkit(this);
		return localAgents.put(id, a);
	}

	public void powerUpLocalAgent(AID agentID) throws NotFoundException {
		Agent instance = localAgents.acquire(agentID);
		if (instance == null) {
			throw new NotFoundException("powerUpLocalAgent() failed to find agent " + agentID.getName());
		}
		Thread t = myResourceManager.getThread(ResourceManager.USER_AGENTS, agentID.getLocalName(), instance);
		instance.powerUp(agentID, t);
		localAgents.release(agentID);
	}

	public void removeLocalAgent(AID id) {
		localAgents.remove(id);
	}

	public Agent acquireLocalAgent(AID id) {
		return localAgents.acquire(id);
	}

	public void releaseLocalAgent(AID id) {
		localAgents.release(id);
	}

	// #MIDP_EXCLUDE_BEGIN
	public void fillListFromMessageQueue(List messages, Agent a) {
		MessageQueue mq = a.getMessageQueue();

		synchronized (mq) {
			Iterator i = mq.iterator();
			while (i.hasNext()) {
				messages.add(i.next());
			}
		}
	}
	// #MIDP_EXCLUDE_END

	// #MIDP_EXCLUDE_BEGIN
	public void fillListFromReadyBehaviours(List behaviours, Agent a) {

		Scheduler s = a.getScheduler();

		// (Mutual exclusion with Scheduler.add(), remove()...)
		synchronized (s) {
			Iterator it = s.readyBehaviours.iterator();
			while (it.hasNext()) {
				Behaviour b = (Behaviour) it.next();
				behaviours.add(new BehaviourID(b));
			}

		}
	}
	// #MIDP_EXCLUDE_END

	// #MIDP_EXCLUDE_BEGIN
	public void fillListFromBlockedBehaviours(List behaviours, Agent a) {

		Scheduler s = a.getScheduler();

		// (Mutual exclusion with Scheduler.add(), remove()...)
		synchronized (s) {
			Iterator it = s.blockedBehaviours.iterator();
			while (it.hasNext()) {
				Behaviour b = (Behaviour) it.next();
				behaviours.add(new BehaviourID(b));
			}
		}
	}
	// #MIDP_EXCLUDE_END

	// #MIDP_EXCLUDE_BEGIN
	/*
	 * public void commitMigration(Agent instance) { instance.doGone();
	 * localAgents.remove(instance.getAID()); }
	 */
	// #MIDP_EXCLUDE_END

	// #MIDP_EXCLUDE_BEGIN
	/*
	 * public void abortMigration(Agent instance) { instance.doExecute(); }
	 */
	// #MIDP_EXCLUDE_END

	public void addAddressToLocalAgents(String address) {
		Agent[] allLocalAgents = localAgents.values();

		// Add the address to the AIDs of all local agents
		for (int j = 0; j < allLocalAgents.length; j++) {
			allLocalAgents[j].addPlatformAddress(address);
		}

		// Add the new addresses to the AMS and Default DF AIDs
		theAMS.addAddresses(address);
		theDefaultDF.addAddresses(address);
	}

	public void removeAddressFromLocalAgents(String address) {
		Agent[] allLocalAgents = localAgents.values();

		// Remove the address from the AIDs of all local agents
		for (int j = 0; j < allLocalAgents.length; j++) {
			allLocalAgents[j].removePlatformAddress(address);
		}

		// Remove the address from the AIDs of the AMS and the Default DF
		theAMS.removeAddresses(address);
		theDefaultDF.removeAddresses(address);
	}

	public boolean postMessageToLocalAgent(ACLMessage msg, AID receiverID) {
		Agent receiver = localAgents.acquire(receiverID);
		if (receiver == null) {
			return false;
		}
		receiver.postMessage(msg);
		localAgents.release(receiverID);

		return true;
	}

	// Tells whether the given AID refers to an agent of this platform
	// or not.
	public boolean livesHere(AID id) {
		String hap = id.getHap();
		return CaseInsensitiveString.equalsIgnoreCase(hap, AID.getPlatformID());
	}

	public ContainerID getID() {
		return myID;
	}

	public MainContainer getMain() {
		// #MIDP_EXCLUDE_BEGIN
		return myMainContainer;
		// #MIDP_EXCLUDE_END
		/*
		 * #MIDP_INCLUDE_BEGIN return null; #MIDP_INCLUDE_END
		 */
	}

	public ServiceManager getServiceManager() {
		return myServiceManager;
	}

	public ServiceFinder getServiceFinder() {
		return myServiceFinder;
	}

	// Utility method to start a kernel service
	protected ServiceDescriptor startService(String name, boolean activateIt) throws ServiceException {

		try {
			Class svcClass = Class.forName(name);
			Service svc = (Service) svcClass.newInstance();
			svc.init(this, myProfile);
			ServiceDescriptor dsc = new ServiceDescriptor(svc.getName(), svc);

			if (activateIt) {
				myServiceManager.activateService(dsc);
				svc.boot(myProfile);
			}
			return dsc;
		} catch (ServiceException se) {
			// Let it through
			throw se;
		} catch (Throwable t) {
			throw new ServiceException("An error occurred during service activation", t);
		}
	}

	public void becomeLeader() {
		// #MIDP_EXCLUDE_BEGIN
		try {
			myMainContainer.startSystemAgents(this);
		} catch (Exception e) {
			e.printStackTrace();
		}
		// #MIDP_EXCLUDE_END
	}

//	#ALL_EXCLUDE_BEGIN
	// FIXME: These methods have been added to support
	// PlatformListener registration from the In-process-interface
	// with minimum effort. They will possibly be removed in a
	// future (more general) implementation
	public void addPlatformListener(AgentManager.Listener l) throws ClassCastException {
		AgentManager m = (AgentManager) myMainContainer;
		m.addListener(l);
	}

	public void removePlatformListener(AgentManager.Listener l) throws ClassCastException {
		AgentManager m = (AgentManager) myMainContainer;
		m.removeListener(l);
	}
//	#ALL_EXCLUDE_END

	private void initCredentials(Command cmd, AID id) {
		// #MIDP_EXCLUDE_BEGIN
		Agent agent = localAgents.acquire(id);
		if (agent != null) {
			try {
				CredentialsHelper ch = (CredentialsHelper) agent.getHelper("jade.core.security.Security");
				cmd.setPrincipal(ch.getPrincipal());
				cmd.setCredentials(ch.getCredentials());
			} catch (ServiceException se) {
				// The security plug-in is not there. Just ignore it
			}
		}
		localAgents.release(id);
		// #MIDP_EXCLUDE_END
	}

}

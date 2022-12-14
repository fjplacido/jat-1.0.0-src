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

import jade.util.Logger;
import jade.util.leap.LinkedList;

/**
 * The singleton instance (accessible through the <code>instance()</code> static
 * method) of this class allows controlling the JADE runtime system from an
 * external application. Two different modalities of controlling the JADE
 * runtime system exist:
 * <ul>
 * <li>Multiple-container: Several containers (belonging to the same platform)
 * can be executed in the local JVM. This modality is activated by means of the
 * <code>createAgentContainer()</code> and <code>createMainContainer()</code>
 * methods plus the classes included in the <code>jade.wrapper</code>
 * package.</li>
 * <li>Single-container: Only one container can be executed in the local JVM.
 * This modality is activated by means of the <code>startUp()</code> and
 * <code>shutDown()</code> methods</li>
 * </ul>
 * Once a modality has been activated (by calling one of the above methods)
 * calling one of the methods for the other modality cause an
 * <code>IllegalStateException</code> to be thrown.
 * <p>
 * It should be noted that the Single-container modality only provides a limited
 * control of the JADE runtime system (e.g. it does not allow creating and
 * killing agents), but is the only one supported both in J2SE, PersonalJava and
 * MIDP when using the LEAP add-on.
 * 
 * @author Giovanni Rimassa - Universita' di Parma
 * @author Giovanni Caire - TILAB
 */
public class Runtime {
	// JADE runtime execution modes:
	// MULTIPLE --> Several containers can be activated in a JVM
	private static final int MULTIPLE_MODE = 0;
	// SINGLE --> Only one container can be activated in a JVM
	private static final int SINGLE_MODE = 1;
	// UNKNOWN --> Mode not yet set
	private static final int UNKNOWN_MODE = 2;

	private static Runtime theInstance;

	static {
		theInstance = new Runtime();
	}

	// #MIDP_EXCLUDE_BEGIN
	private ThreadGroup criticalThreads;
	// #MIDP_EXCLUDE_END
	private int activeContainers = 0;
	private LinkedList terminators = new LinkedList();
	private AgentContainerImpl theContainer = null;
	private int mode = UNKNOWN_MODE;

	private Logger myLogger = Logger.getMyLogger(getClass().getName());

	// Private constructor to forbid instantiation outside the class.
	private Runtime() {
		// Do nothing
	}

	/**
	 * This method returns the singleton instance of this class that should be then
	 * used to create agent containers.
	 **/
	public static Runtime instance() {
		return theInstance;
	}

	// #MIDP_EXCLUDE_BEGIN
	/**
	 * Creates a new agent container in the current JVM, providing access through a
	 * proxy object. <br>
	 * <b>NOT available in MIDP</b> <br>
	 * 
	 * @param p the profile containing boostrap and configuration data for this
	 *          container
	 * @return A proxy object, through which services can be requested from the real
	 *         JADE container.
	 * @exception IllegalStateException if the Single-container modality was
	 *                                  previously activated by calling the
	 *                                  <code>startUp()</code> method.
	 */
	public jade.wrapper.AgentContainer createAgentContainer(Profile p) {
		if (mode == UNKNOWN_MODE || mode == MULTIPLE_MODE) {
			mode = MULTIPLE_MODE;
			p.setParameter(Profile.MAIN, "false"); // set to an agent container
			AgentContainerImpl impl = new AgentContainerImpl(p);
			beginContainer();
			if (impl.joinPlatform()) {
				return impl.getContainerController();
			} else {
				return null;
			}
		} else {
			throw new IllegalStateException("Single-container modality already activated");
		}
	}

	/**
	 * Creates a new main container in the current JVM, providing access through a
	 * proxy object. <br>
	 * <b>NOT available in MIDP</b> <br>
	 * 
	 * @param p the profile containing boostrap and configuration data for this
	 *          container
	 * @return A proxy object, through which services can be requested from the real
	 *         JADE main container.
	 * @exception IllegalStateException if the Single-container modality was
	 *                                  previously activated by calling the
	 *                                  <code>startUp()</code> method.
	 */
	public jade.wrapper.AgentContainer createMainContainer(Profile p) {
		if (mode == UNKNOWN_MODE || mode == MULTIPLE_MODE) {
			mode = MULTIPLE_MODE;
			p.setParameter(Profile.MAIN, "true"); // set to a main container
			AgentContainerImpl impl = new AgentContainerImpl(p);
			beginContainer();
			if (impl.joinPlatform()) {
				return impl.getContainerController();
			} else {
				return null;
			}
		} else {
			throw new IllegalStateException("Single-container modality already activated");
		}
	}

	/**
	 * Causes the local JVM to be closed when the last container in this JVM
	 * terminates. <br>
	 * <b>NOT available in MIDP</b> <br>
	 */
	public void setCloseVM(boolean flag) {
		if (flag) {
			terminators.addLast(new Runnable() {
				public void run() {
					// Give one more chance to other threads to complete
					Thread.yield();
					myLogger.log(Logger.INFO, "JADE is closing down now.");
					System.exit(0);
				}
			});
		}
	}
	// #MIDP_EXCLUDE_END

	/**
	 * Starts a JADE container in the Single-container modality. Successive calls to
	 * this method will take no effect.
	 * 
	 * @param p the profile containing boostrap and configuration data for this
	 *          container
	 * @exception IllegalStateException if the Multiple-container modality was
	 *                                  previously activated by calling the
	 *                                  <code>createAgentContainer()</code> or
	 *                                  <code>createMainContainer()</code> methods.
	 */
	public void startUp(Profile p) {
		if (mode == MULTIPLE_MODE) {
			throw new IllegalStateException("Multiple-container modality already activated");
		}
		if (mode == UNKNOWN_MODE) {
			mode = SINGLE_MODE;
			theContainer = new AgentContainerImpl(p);
			beginContainer();
			theContainer.joinPlatform();
		}
	}

	/**
	 * Stops the JADE container running in the Single-container modality.
	 */
	public void shutDown() {
		if (theContainer != null) {
			theContainer.shutDown();
		}
	}

	/**
	 * Allows setting a <code>Runnable</code> that is executed when the last
	 * container in this JVM terminates.
	 */
	public void invokeOnTermination(Runnable r) {
		terminators.addFirst(r);
	}

	// Called by a starting up container.
	void beginContainer() {
		// myLogger.log(Logger.INFO,
		// "----------------------------------\n"+getCopyrightNotice()+"----------------------------------------");
		if (activeContainers == 0) {
			// Initialize and start up the timer dispatcher
			TimerDispatcher theDispatcher = new TimerDispatcher();

			// #MIDP_EXCLUDE_BEGIN
			// Set up group and attributes for time critical threads
			criticalThreads = new ThreadGroup("JADE time-critical threads");
			criticalThreads.setMaxPriority(Thread.MAX_PRIORITY);
			Thread t = new Thread(criticalThreads, theDispatcher);
			t.setPriority(criticalThreads.getMaxPriority());
			t.setName("JADE Timer dispatcher");
			// #MIDP_EXCLUDE_END
			/*
			 * #MIDP_INCLUDE_BEGIN Thread t = new Thread(theDispatcher); #MIDP_INCLUDE_END
			 */
			theDispatcher.setThread(t);
			TimerDispatcher.setTimerDispatcher(theDispatcher);
			theDispatcher.start();
		}
		++activeContainers;
	}

	// Called by a terminating container.
	void endContainer() {
		--activeContainers;
		if (activeContainers == 0) {
			// Start a new Thread that calls all terminators one after
			// the other
			Thread t = new Thread(new Runnable() {
				public void run() {
					for (int i = 0; i < terminators.size(); ++i) {
						Runnable r = (Runnable) terminators.get(i);
						r.run();
					}
				}
			});
			// #MIDP_EXCLUDE_BEGIN
			t.setDaemon(false);
			// #MIDP_EXCLUDE_END

			// Terminate the TimerDispatcher and release its resources
			TimerDispatcher.getTimerDispatcher().stop();

			// Reset mode
			mode = UNKNOWN_MODE;
			theContainer = null;

			// #MIDP_EXCLUDE_BEGIN
			try {
				criticalThreads.destroy();
			} catch (IllegalThreadStateException itse) {
				myLogger.log(Logger.WARNING, "Time-critical threads still active: ");
				criticalThreads.list();
			} finally {
				criticalThreads = null;
			}
			// #MIDP_EXCLUDE_END
			t.start();
		}
	}

	// #APIDOC_EXCLUDE_BEGIN
	public TimerDispatcher getTimerDispatcher() {
		return TimerDispatcher.getTimerDispatcher();
	}
	// #APIDOC_EXCLUDE_END

	// #APIDOC_EXCLUDE_BEGIN
	/**
	 * Return a String with copyright Notice, Name and Version of this version of
	 * JADE
	 */
	public static String getCopyrightNotice() {
		return ("    This is " + instance().getVersionInfo()
				+ "\n    downloaded in Open Source, under LGPL restrictions,\n    at http://jade.cselt.it/\n");
	}
	// #APIDOC_EXCLUDE_END

	/**
	 * Return the version number and date of this JADE Runtime.
	 */
	public String getVersionInfo() {
		String CVSname = "$Name: JADE-3_3 $";
		String CVSdate = "$Date: 2005/03/02 16:11:05 $";
		int colonPos = CVSname.indexOf(":");
		int dollarPos = CVSname.lastIndexOf('$');
		String name = CVSname.substring(colonPos + 1, dollarPos);
		if (name.indexOf("JADE") == -1)
			name = "JADE snapshot";
		else {
			name = name.replace('-', ' ');
			name = name.replace('_', '.');
			name = name.trim();
		}
		colonPos = CVSdate.indexOf(':');
		dollarPos = CVSdate.lastIndexOf('$');
		String date = CVSdate.substring(colonPos + 1, dollarPos);
		date = date.trim();
		return name + " - " + date;
	}
}

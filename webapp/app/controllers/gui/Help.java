package controllers.gui;

import play.mvc.Controller;
import play.mvc.With;
import gov.nrel.util.Utility;
import models.EntityUser;

public class Help extends Controller {
	
	public static void myHelp() {
		EntityUser user = Utility.getCurrentUser(session);
		render(user);
	}
	
	public static void myHelpAccess() {
		EntityUser user = Utility.getCurrentUser(session);
		render(user);
	}
	
	public static void myHelpGroups() {
		EntityUser user = Utility.getCurrentUser(session);
		render(user);
	}
	
	public static void myHelpRobots() {
		EntityUser user = Utility.getCurrentUser(session);
		render(user);
	}
	
	public static void myHelpProvisioning() {
		EntityUser user = Utility.getCurrentUser(session);
		render(user);
	}

	public static void myHelpModules() {
		EntityUser user = Utility.getCurrentUser(session);
		render(user);
	}
	
	public static void myHelpUpload() {
		EntityUser user = Utility.getCurrentUser(session);
		render(user);
	}

	public static void myHelpDownload() {
		EntityUser user = Utility.getCurrentUser(session);
		render(user);
	}

	public static void firstTimeUser() {
		render();
	}

	/**
	 * Twitter Bootstrap Prototypes
	 */
	public static void myHelpBootstrapProto1() {
		EntityUser user = Utility.getCurrentUser(session);
		render(user);
	}
	
	
}

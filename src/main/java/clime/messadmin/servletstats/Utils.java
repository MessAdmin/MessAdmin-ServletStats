/**
 *
 */
package clime.messadmin.servletstats;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import clime.messadmin.model.Application;
import clime.messadmin.model.Session;
import clime.messadmin.providers.lifecycle.ServletStatsGatherer;

/**
 * @author C&eacute;drik LIME
 */
public class Utils {
	private static final String USER_DATA_KEY = ServletStatsGatherer.class.getName();

	private Utils() {
	}

	public static Map getPluginData(Application application) {
		Map result = (Map) application.getUserData().get(USER_DATA_KEY);
		if (result == null) {
			result = new ConcurrentHashMap();
			application.getUserData().put(USER_DATA_KEY, result);
		}
		return result;
	}
	public static Map getPluginData(Session session) {
		Map result = (Map) session.getUserData().get(USER_DATA_KEY);
		if (result == null) {
			result = new ConcurrentHashMap();
			session.getUserData().put(USER_DATA_KEY, result);
		}
		return result;
	}
}

/**
 *
 */
package clime.messadmin.servletstats;

import java.io.Serializable;
import java.util.Comparator;

import clime.messadmin.model.RequestInfo;

/**
 * @author C&eacute;drik LIME
 */
public class RequestInfoTimeComparator implements Comparator<RequestInfo>, Serializable {

	/**
	 *
	 */
	public RequestInfoTimeComparator() {
		super();
	}

	/**
	 * {@inheritDoc}
	 */
	public int compare(RequestInfo o1, RequestInfo o2) {
		return (int) (o2.getTotalUsedTime() - o1.getTotalUsedTime());
	}

}

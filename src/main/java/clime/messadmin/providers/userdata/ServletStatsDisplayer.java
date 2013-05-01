/**
 *
 */
package clime.messadmin.providers.userdata;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;

import clime.messadmin.i18n.I18NSupport;
import clime.messadmin.model.Application;
import clime.messadmin.model.RequestInfo;
import clime.messadmin.model.Server;
import clime.messadmin.model.Session;
import clime.messadmin.providers.spi.ApplicationDataProvider;
import clime.messadmin.providers.spi.BaseTabularDataProvider;
import clime.messadmin.providers.spi.SessionDataProvider;
import clime.messadmin.providers.spi.SizeOfProvider;
import clime.messadmin.servletstats.RequestInfoTimeComparator;
import clime.messadmin.servletstats.Utils;
import clime.messadmin.utils.BytesFormat;
import clime.messadmin.utils.StringUtils;

/**
 * @author C&eacute;drik LIME
 */
public class ServletStatsDisplayer extends BaseTabularDataProvider
		implements ApplicationDataProvider, SessionDataProvider {
	private static final String BUNDLE_NAME = ServletStatsDisplayer.class.getName();

	/**
	 *
	 */
	public ServletStatsDisplayer() {
		super();
	}

	/****************************************************************/
	/*							Common methods						*/
	/****************************************************************/

	public String[] getTabularDataLabels(ClassLoader cl) {
		return new String[] {
				I18NSupport.getLocalizedMessage(BUNDLE_NAME, cl, "label.url"),//$NON-NLS-1$
				I18NSupport.getLocalizedMessage(BUNDLE_NAME, cl, "label.n_errors"),//$NON-NLS-1$
				I18NSupport.getLocalizedMessage(BUNDLE_NAME, cl, "label.n_hits"),//$NON-NLS-1$
				I18NSupport.getLocalizedMessage(BUNDLE_NAME, cl, "label.used_time.mean"),//$NON-NLS-1$
				I18NSupport.getLocalizedMessage(BUNDLE_NAME, cl, "label.used_time.std_dev"),//$NON-NLS-1$
				I18NSupport.getLocalizedMessage(BUNDLE_NAME, cl, "label.used_time.total")//$NON-NLS-1$
			};
	}

	protected String getTableCaption(String[] labels, Object[][] values, ClassLoader cl) {
		NumberFormat numberFormatter = NumberFormat.getNumberInstance(I18NSupport.getAdminLocale());
		String caption = I18NSupport.getLocalizedMessage(BUNDLE_NAME, cl, "table.caption", new Object[] {numberFormatter.format(values.length)});//$NON-NLS-1$
		return caption;
	}

	protected String getDataTitle(Map data, ClassLoader cl) {
		NumberFormat bytesFormatter = BytesFormat.getBytesInstance(I18NSupport.getAdminLocale(), true);
        long currentItemSize = SizeOfProvider.Util.getObjectSize(data, cl);
		String result = I18NSupport.getLocalizedMessage(BUNDLE_NAME, cl, "title", new Object[] {bytesFormatter.format(currentItemSize)});//$NON-NLS-1$
		return result;
	}

	/** {@inheritDoc} */
	protected String getCellClass(int cellNumber, Object value) {
		return cellNumber > 0 ? "number" : super.getCellClass(cellNumber, value);
	}

	protected Object[][] getTabularData(Map data) {
		NumberFormat numberFormatter = NumberFormat.getNumberInstance(I18NSupport.getAdminLocale());
		numberFormatter.setMaximumFractionDigits(1);
		List inputList = new ArrayList(data.values());
		List resultList = new ArrayList(data.size());
		// sort data by total used time
		Collections.sort(inputList, new RequestInfoTimeComparator());
		Iterator iter = inputList.iterator();
		while (iter.hasNext()) {
			RequestInfo request = (RequestInfo) iter.next();
			resultList.add(new Object[] {
				StringUtils.escapeXml(request.getURL()),
				request.getNErrors()>0 ?
						"<span style=\"color: red; font-weight: bolder;\">" + numberFormatter.format(request.getNErrors()) +  "</span>"//$NON-NLS-1$//$NON-NLS-2$
						: numberFormatter.format(request.getNErrors()),
				numberFormatter.format(request.getHits()),
				numberFormatter.format(request.getMeanUsedTime()) + " ms",
				numberFormatter.format(request.getStdDevUsedTime()) + " ms",
				numberFormatter.format(request.getTotalUsedTime()) + " ms"
			});
		}
		Object[][] result = new Object[resultList.size()][];
		return (Object[][]) resultList.toArray(result);
	}

	/**
	 * {@inheritDoc}
	 */
	public int getPriority() {
		return 500;
	}

	/****************************************************************/
	/*							Session methods						*/
	/****************************************************************/

	public Object[][] getSessionTabularData(HttpSession httpSession) {
		Session session = Server.getInstance().getSession(httpSession);
		Map data = new HashMap(Utils.getPluginData(session));
		Object[][] result = getTabularData(data);
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getSessionDataTitle(HttpSession httpSession) {
		Application application = Server.getInstance().getApplication(httpSession.getServletContext());
		Session session = application.getSession(httpSession);
		Map data = new HashMap(Utils.getPluginData(session));
		String result = getDataTitle(data, application.getApplicationInfo().getClassLoader());
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getXHTMLSessionData(HttpSession session) {
		try {
			ClassLoader cl = getClassLoader(session);
			String[] labels = getTabularDataLabels(cl);
			Object[][] values = getSessionTabularData(session);
			return buildXHTML(labels, values, "extraSessionAttributesTable-"+getClass().getName(), getTableCaption(labels, values, cl));//$NON-NLS-1$
		} catch (RuntimeException rte) {
			return "Error in " + this.getClass().getName() + ": " + rte;
		}
	}

	/****************************************************************/
	/*						Application methods						*/
	/****************************************************************/

	public Object[][] getApplicationTabularData(ServletContext context) {
		Application application = Server.getInstance().getApplication(context);
		Map data = new HashMap(Utils.getPluginData(application));
		Object[][] result = getTabularData(data);
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getApplicationDataTitle(ServletContext context) {
		Application application = Server.getInstance().getApplication(context);
		Map data = new HashMap(Utils.getPluginData(application));
		String result = getDataTitle(data, application.getApplicationInfo().getClassLoader());
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getXHTMLApplicationData(ServletContext context) {
		try {
			ClassLoader cl = getClassLoader(context);
			String[] labels = getTabularDataLabels(cl);
			Object[][] values = getApplicationTabularData(context);
			return buildXHTML(labels, values, "extraApplicationAttributesTable-"+getClass().getName(), getTableCaption(labels, values, cl));//$NON-NLS-1$
		} catch (RuntimeException rte) {
			return "Error in " + this.getClass().getName() + ": " + rte;
		}
	}

}

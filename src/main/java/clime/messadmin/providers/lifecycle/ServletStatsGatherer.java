/**
 *
 */
package clime.messadmin.providers.lifecycle;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import clime.messadmin.model.Application;
import clime.messadmin.model.Request;
import clime.messadmin.model.RequestInfo;
import clime.messadmin.model.Server;
import clime.messadmin.model.Session;
import clime.messadmin.providers.spi.RequestExceptionProvider;
import clime.messadmin.providers.spi.RequestLifeCycleProvider;
import clime.messadmin.servletstats.Utils;

/**
 * Collects statistics on Servlets
 * @author C&eacute;drik LIME
 */
public class ServletStatsGatherer implements RequestLifeCycleProvider, RequestExceptionProvider {
	static final Request request = new Request(null);

	/**
	 *
	 */
	public ServletStatsGatherer() {
		super();
	}

	/**
	 * {@inheritDoc}
	 */
	public void requestInitialized(HttpServletRequest httpRequest,
			HttpServletResponse httpResponse, ServletContext servletContext) {
		handleRequest(null, RequestHandler.INITIALIZED, httpRequest, httpResponse, servletContext);
	}

	/**
	 * {@inheritDoc}
	 */
	public void requestDestroyed(HttpServletRequest httpRequest,
			HttpServletResponse httpResponse, ServletContext servletContext) {
		handleRequest(null, RequestHandler.DESTROYED, httpRequest, httpResponse, servletContext);
	}

	/**
	 * {@inheritDoc}
	 */
	public void requestException(Exception e, HttpServletRequest httpRequest, HttpServletResponse httpResponse, ServletContext servletContext) {
		requestDestroyed(httpRequest, httpResponse, servletContext);
		handleRequest(e, RequestHandler.EXCEPTION, httpRequest, httpResponse, servletContext);
	}

	private void handleRequest(Exception e, RequestHandler handler, HttpServletRequest httpRequest,
			HttpServletResponse httpResponse, ServletContext servletContext) {
		// Application
		Application application = Server.getInstance().getApplication(servletContext);
		final String servletPath = httpRequest.getContextPath() + httpRequest.getServletPath();
		RequestInfo requestInfo = (RequestInfo) Utils.getPluginData(application).get(servletPath);
		if (requestInfo == null) {
			requestInfo = new RequestInfo(servletPath);
			Utils.getPluginData(application).put(servletPath, requestInfo);
			handler.handleMissedRequestInfo(requestInfo, httpRequest, httpResponse, servletContext);
		}
		handler.handleRequestInfo(e, requestInfo, httpRequest, httpResponse, servletContext);
		// Session
		if (httpRequest.getSession(false) != null) {
			Session session = application.getSession(httpRequest.getSession(false));
			requestInfo = (RequestInfo) Utils.getPluginData(session).get(servletPath);
			if (requestInfo == null) {
				// session was created with this hit
				requestInfo = new RequestInfo(servletPath);
				Utils.getPluginData(session).put(servletPath, requestInfo);
				handler.handleMissedRequestInfo(requestInfo, httpRequest, httpResponse, servletContext);
			}
			handler.handleRequestInfo(e, requestInfo, httpRequest, httpResponse, servletContext);
		}
	}

	private static interface RequestHandler {
		void handleMissedRequestInfo(RequestInfo requestInfo, HttpServletRequest httpRequest,
				HttpServletResponse httpResponse, ServletContext servletContext);
		void handleRequestInfo(Exception e, RequestInfo requestInfo, HttpServletRequest httpRequest,
				HttpServletResponse httpResponse, ServletContext servletContext);

		public static final RequestHandler INITIALIZED = new RequestHandler() {
			public void handleMissedRequestInfo(RequestInfo requestInfo,
					HttpServletRequest httpRequest, HttpServletResponse httpResponse, ServletContext servletContext) {
				//noop
			}
			public void handleRequestInfo(Exception e, RequestInfo requestInfo,
					HttpServletRequest httpRequest, HttpServletResponse httpResponse, ServletContext servletContext) {
				request.requestInitialized(requestInfo, httpRequest, servletContext);
			}
		};
		public static final RequestHandler DESTROYED = new RequestHandler() {
			public void handleMissedRequestInfo(RequestInfo requestInfo,
					HttpServletRequest httpRequest, HttpServletResponse httpResponse, ServletContext servletContext) {
				request.requestInitialized(requestInfo, httpRequest, servletContext);
			}
			public void handleRequestInfo(Exception e, RequestInfo requestInfo,
					HttpServletRequest httpRequest, HttpServletResponse httpResponse, ServletContext servletContext) {
				request.requestDestroyed(requestInfo, httpRequest, httpResponse, servletContext);
			}
		};
		public static final RequestHandler EXCEPTION = new RequestHandler() {
			public void handleMissedRequestInfo(RequestInfo requestInfo,
					HttpServletRequest httpRequest, HttpServletResponse httpResponse, ServletContext servletContext) {
				request.requestInitialized(requestInfo, httpRequest, servletContext);
			}
			public void handleRequestInfo(Exception e, RequestInfo requestInfo,
					HttpServletRequest httpRequest, HttpServletResponse httpResponse, ServletContext servletContext) {
				request.requestException(requestInfo, e, httpRequest, httpResponse, servletContext);
			}
		};
	}

	/**
	 * {@inheritDoc}
	 */
	public int getPriority() {
		// no need for a priority, really
		return 0;
	}

}

package fi.kotipalo.h.CryptoID;

import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

public class SessionJanitor implements HttpSessionListener {
	private int sessionCount = 0;

	@Override
	public void sessionCreated(HttpSessionEvent arg0) {
		synchronized (this) {
			sessionCount++;
		}
		String sessionid = arg0.getSession().getId();
		arg0.getSession().getServletContext().log("Created session: "+sessionid +"("+Integer.toString(sessionCount)+")");
	}

	@Override
	public void sessionDestroyed(HttpSessionEvent arg0) {
		synchronized (this) {
			sessionCount--;
		}
		ServletContext servletContext = arg0.getSession().getServletContext();
		String sessionid = arg0.getSession().getId();
		arg0.getSession().getServletContext().log("Cleaning session: "+sessionid +"("+Integer.toString(sessionCount)+")");
		Utils.clearContextWideSessionAttributes(servletContext,sessionid);
	}

}

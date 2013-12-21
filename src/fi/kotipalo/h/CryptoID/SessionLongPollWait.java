package fi.kotipalo.h.CryptoID;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * Servlet implementation class SessionLongPollWait
 */
@WebServlet(description = "Waits for the session data to be updated", urlPatterns = { "/SessionLongPollWait" })
public class SessionLongPollWait extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public SessionLongPollWait() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		HttpSession session = request.getSession(true);
		//ServletContext context = getServletContext();
		String sessionId = session.getId();
		Map<String, Object> contextWideSessionAttributes = null;
		int delayseconds=0;
		String uname = null;
		String trigger = request.getParameter("trigger");
		if (trigger==null) trigger = "error";
		boolean trig = false;
		while (delayseconds < 12) {
			/*
			//contextWideSessionAttributes = (HashMap<String, Object>)getServletContext().getAttribute(sessionId);
			if (contextWideSessionAttributes == null) {
				contextWideSessionAttributes = Utils.getContextWideSessionAttributes(getServletContext(), sessionId);
			}
			if (contextWideSessionAttributes != null) { 
				triggerValue = (String)contextWideSessionAttributes.get(trigger);
				uname = (String)contextWideSessionAttributes.get("uname");
			}
			//uname = (String) getServletContext().getAttribute("when");

			//if (uname != null) { // or check hash etc.
			 * 
			 */
			//if ("true".equals(triggerValue)) {
			trig = Utils.isTriggered(getServletContext(), sessionId, trigger);
			if (trig) {
			//if (triggerValue != null) {
				// log poll wait ended!
				break;
			} else {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					//e.printStackTrace();
				}
				delayseconds++;
			}
		}
		// debug:
		// contextWideSessionAttributes = new HashMap<String, Object>();
		// contextWideSessionAttributes.put("uname", "Uuno");
		if (trig) {
		//if (triggerValue != null) {
			contextWideSessionAttributes = Utils.getContextWideSessionAttributes(getServletContext(), sessionId);
			if (contextWideSessionAttributes != null) { 
				uname = (String)contextWideSessionAttributes.get("uname");
			}
			if (uname != null)
				session.setAttribute("uname", uname);
			response.setContentType("text/plain");
			PrintWriter pw = response.getWriter();
			pw.println("User " + uname + " is "+trigger + " by CryptoID.");
			pw.close();
		} else {
			response.sendError(404, "Longpoll timeout:"+ sessionId);
		}
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request,response);
	}

}

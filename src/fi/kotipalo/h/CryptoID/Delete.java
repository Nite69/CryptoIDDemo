package fi.kotipalo.h.CryptoID;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.MessageFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * Servlet implementation class Delete
 */
@WebServlet(description = "Delete a user", urlPatterns = { "/delete" })
public class Delete extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public Delete() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		HttpSession session = request.getSession(true);
		// if ! logged in......
		String sid = session.getId();
		String sslId = (String) request.getAttribute(
	                "javax.servlet.request.ssl_session");

		// prevent session hijack
		if (Utils.isHijackedSession(request,session)) {
			// hijacked session!!
			PrintWriter pw = response.getWriter();
			Utils.printHijackedSession(pw);
			return;
		}
		
		String uri = request.getRequestURI();
		Map<String, Object> secrets;
		Object secret = null;
		Object notSecret;
		Date d = new Date();

		notSecret = session.getAttribute("unprotected");
		if (notSecret == null) {
			notSecret = "unprotected: " + d.getTime();
			session.setAttribute("unprotected", notSecret);
		}
		secrets = (Map<String, Object>) session.getAttribute("protected");
		if (secrets == null) {
			secrets = new HashMap<String, Object>();
			session.setAttribute("protected", secrets);
		}
		if (sslId != null) {
			if (secrets.containsKey(sslId))
				secret = secrets.get(sslId);
			else {
				secret = "protected: " + d.getTime();
				secrets.put(sslId, secret);
			}
		}
		// you only should have public key online!!!!
		BitIdentity serverKeys = Utils.getMasterServerKeys(getServletContext());
/*		BitIdentity serverKeys = (BitIdentity)getServletContext().getAttribute("serverKeys");
		if (serverKeys == null) {
			serverKeys = new BitIdentity();
			serverKeys.setUname(Utils.myServerAddress);
			getServletContext().setAttribute("serverKeys", serverKeys);
		}*/


		Map<String, Object> contextWideSessionAttributes = Utils.getContextWideSessionAttributes(getServletContext(), sid);
/*		Map<String, Object> contextWideSessionAttributes = null;
		contextWideSessionAttributes = (Map<String, Object>)getServletContext().getAttribute(sid);
		if (contextWideSessionAttributes == null) { // or check hash etc.
			contextWideSessionAttributes = new HashMap<String, Object>();
			getServletContext().setAttribute(sid, contextWideSessionAttributes);
		}*/
		User user = (User)contextWideSessionAttributes.get("user");
		if (user == null) {
			
		} else {

			response.setContentType("text/html");
			PrintWriter pw = response.getWriter();
			pw.println("<!DOCTYPE html>" );
			pw.println("<html>" );
			pw.println("<head>" );
			pw.println("<script type=\"text/javascript\" src=\"LongPoll.js\" ></script>" );
			pw.println("</head>" );
			pw.println("<body onload=\"trigger = 'deleted'; forwardPage = 'login'; pollRefresh()\">" );
			pw.println(MessageFormat.format("<p>URI: {0}</p>", new Object[] { uri }));
			pw.println(MessageFormat.format("<p>SID: {0}</p>", new Object[] { sid }));
			pw.println(MessageFormat.format("<p>SSLID: {0}</p>", new Object[] { sslId }));
	/*		pw.println(MessageFormat.format("<p>Info: {0}</p>", new Object[] { notSecret }));
			pw.println(MessageFormat.format("<p>Secret: {0}</p>", new Object[] { secret }));
			pw.println(MessageFormat.format("<p>Date: {0}</p>", new Object[] { d }));*/
			pw.println("<img src=\"" + "GetQRCode?f=delete" + /*"&uname="+request.getParameter("uname") +*/ "\">" );
			pw.println("<p>Message <b>"+Utils.getCodeMessage("delete",sid,user.getUname(), serverKeys) + "</b></p>");

			// Display response for simulated user
			Map<String, BitIdentity> remoteUsers = Utils.getRemoteUsers(getServletContext());
			/*Map<String, BitIdentity> remoteUsers = (Map<String, BitIdentity>)getServletContext().getAttribute("remoteUsers");
			if (remoteUsers == null) {
				remoteUsers = new HashMap<String, BitIdentity>();
				getServletContext().setAttribute("remoteUsers", remoteUsers);
			}*/
			BitIdentity delUser = remoteUsers.get(user.getPubkey());
			if (delUser != null) {
				String message = "d~"+sid+"~"+user.getUname();
				String signature = delUser.generateSignaTure(message);
				pw.println("<p>Response <b>"+ Utils.getCodeResponse("id="+message,signature, serverKeys)+ "</b></p>");
			}
			pw.println("</body>" );
			pw.println("</html>" );
			pw.close();	
			// else (logged in)
		}
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doPost(request,response);
	}

}

package fi.kotipalo.h.CryptoID;

import java.io.IOException;
import java.io.OutputStream;
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
 * Servlet implementation class create
 */
@WebServlet(description = "Register a new account", urlPatterns = { "/create" })
public class Create extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final String serverAddress = "13zQ3gA93iWriYqTLBfENKtuLA1iaMwZVV";
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public Create() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		HttpSession session = request.getSession(true);
		// Redirect, if we have logged in meanwhile
		if (Utils.isTriggered(getServletContext(), session.getId(), "created")) {
			response.sendRedirect("wellcome");
			return;
		}
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
		
		String uri = request.getRequestURI();
		String uname = request.getParameter("uname");
		Map<String, Object> contextWideSessionAttributes = Utils.getContextWideSessionAttributes(getServletContext(), sid);
/*		contextWideSessionAttributes = (Map<String, Object>)getServletContext().getAttribute(sid);
		if (contextWideSessionAttributes == null) { // or check hash etc.
			contextWideSessionAttributes = new HashMap<String, Object>();
			getServletContext().setAttribute(sid, contextWideSessionAttributes);
		}*/
		contextWideSessionAttributes.put("uname", uname);
		// userBase is our user database, should be on real database...
		//Map<String, User> userBase = Utils.getUserBase(getServletContext());
				/*(Map<String, User>)getServletContext().getAttribute("userBase");
		if (userBase == null) {
			userBase = new HashMap<String, User>();
		}*/
		// TODO: should check for duplicate unames
		//
		// remoteUser is for simulating only
		//Map<String, BitIdentity> remoteUsers = Utils.getRemoteUsers(getServletContext());
				/*(Map<String, BitIdentity>)getServletContext().getAttribute("remoteUsers");
		if (remoteUsers == null) {
			remoteUsers = new HashMap<String, BitIdentity>();
			getServletContext().setAttribute("remoteUsers", remoteUsers);
		}*/
		// you only should have public key online!!!!
		BitIdentity serverKeys = Utils.getMasterServerKeys(getServletContext());
		/*(BitIdentity)getServletContext().getAttribute("serverKeys");
		if (serverKeys == null) {
			serverKeys = new BitIdentity();
			serverKeys.setUname(Utils.myServerAddress);
			getServletContext().setAttribute("serverKeys", serverKeys);
		}*/

		response.setContentType("text/html");
		PrintWriter pw = response.getWriter();
		pw.println("<!DOCTYPE html>" );
		pw.println("<html>" );
		pw.println("<head>" );
		pw.println("<script type=\"text/javascript\" src=\"LongPoll.js\" ></script>" );
		pw.println("</head>" );
		pw.println("<body onload=\"trigger = 'created'; forwardPage = 'wellcome'; pollRefresh()\">" );
		pw.println(MessageFormat.format("<p>URI: {0}</p>", new Object[] { uri }));
		pw.println(MessageFormat.format("<p>SID: {0}</p>", new Object[] { sid }));
		pw.println(MessageFormat.format("<p>SSLID: {0}</p>", new Object[] { sslId }));
/*		pw.println(MessageFormat.format("<p>Info: {0}</p>", new Object[] { notSecret }));
		pw.println(MessageFormat.format("<p>Secret: {0}</p>", new Object[] { secret }));
		pw.println(MessageFormat.format("<p>Date: {0}</p>", new Object[] { d }));*/
		session.setAttribute("uname", uname);
		pw.println("<img src=\"" + "GetQRCode?f=create" + /*"&uname="+request.getParameter("uname") +*/ "\">" );
		pw.println("<p>Message <b>"+Utils.getCodeMessage("create",sid,uname,serverKeys) + "</b></p>");
		
		// With this you can simulate android response:
		Map<String, BitIdentity> remoteUsers = Utils.getRemoteUsers(getServletContext());
		if (remoteUsers.size()<3) {
			String message = "c~"+sid+"~"+uname;
			BitIdentity newUser = new BitIdentity();
			newUser.setUname(uname);
			remoteUsers.put(newUser.getAddress(), newUser);
			String signature = newUser.generateSignaTure(message);
			pw.println("<p>Confirm your registration for your personal usage with your android application.</p>");
			pw.println("<p>To create a test user without android client, ");
			pw.println("just copypaste the bolded text to any browser:</p>");
			pw.println("<p>Response <b>"+ Utils.getCodeResponse("id="+message,signature,serverKeys)+ "</b></p>");
		} else {
			pw.println("<p>Confirm your registration for your personal usage with your android application</p>");
		}
		pw.println("</body>" );
		pw.println("</html>" );
		pw.close();
		// else (logged in)
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doPost(request,response);
	}

}

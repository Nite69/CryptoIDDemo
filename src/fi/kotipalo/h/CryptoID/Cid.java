package fi.kotipalo.h.CryptoID;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet implementation class Cid
 */
@WebServlet(description = "This servlet parses the mobile client server side functions (login, create, del)", urlPatterns = { "/cid" })
public class Cid extends HttpServlet {
	private static final long serialVersionUID = 1L;
	static final long ONE_MINUTE_IN_MILLIS=60000;//millisecs
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public Cid() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		final int COMMAND=0;
		final int SESSIONID=1;
		// TODO Auto-generated method stub
		String message = request.getParameter("id");
		String signature = request.getParameter("sig");
		if (signature==null)
			signature = request.getParameter("signature");
		// TODO: check if signature matches message, get user's public key from signature
		// TODO: clean up servletcontext after user logs out and periodically
		String arg[] = message.split("~");
		String command = arg[COMMAND].toLowerCase();
		if (command==null) command = "error";
		String sessionid = arg[SESSIONID];
		Map<String, Object> contextWideSessionAttributes = Utils.getContextWideSessionAttributes(getServletContext(), sessionid); 
/*		contextWideSessionAttributes = (Map<String, Object>)getServletContext().getAttribute(sessionid);
		if (contextWideSessionAttributes == null) { // or check hash etc.
			contextWideSessionAttributes = new HashMap<String, Object>();
			getServletContext().setAttribute(sessionid, contextWideSessionAttributes);
		}*/
		// userBase is our user database, should be on real database...
		Map<String, User> userBase = Utils.getUserBase(getServletContext());
				/* (Map<String, User>)getServletContext().getAttribute("userBase");
		if (userBase == null) {
			userBase = new HashMap<String, User>();
			getServletContext().setAttribute("userBase",userBase);
		}*/
		
		if (command.startsWith("l")) {
			String userPubKey = BitIdentity.getMessageSigner(message,signature);
			if (userPubKey.isEmpty()) {
				response.setContentType("text/plain");
				PrintWriter pw = response.getWriter();
				pw.println("Signature failed, DID NOT log in: " + signature);		
				pw.close();	
			} else {
				User user = userBase.get(userPubKey);
				if (user == null) {
					response.setContentType("text/plain");
					PrintWriter pw = response.getWriter();
					pw.println("Signature ok, but did not find user for " + userPubKey);		
					pw.close();	
				} else if (user.isDeleted()) {
					response.setContentType("text/plain");
					PrintWriter pw = response.getWriter();
					pw.println("Signature ok, but user is deleted.");		
					pw.close();	
				} else {
					response.setContentType("text/plain");
					PrintWriter pw = response.getWriter();
					pw.println(confirmFunction(user.getUname()+"~"+sessionid));
					pw.println("Logged in : " + user.getUname() +":"+userPubKey);		
					pw.println("Setting sessionAttribute: " + sessionid);
					pw.close();	
					contextWideSessionAttributes.put("uname", user.getUname()); 
					contextWideSessionAttributes.put("user", user); 
					contextWideSessionAttributes.put("logged",true);
				}
			}
		} else if (command.startsWith("c")) {
			String uname = null; //arg[UNAME];
			//contextWideSessionAttributes.put("uname", uname); // sign is for debuggig
			uname = (String)contextWideSessionAttributes.get("uname");
			String userPubKey = BitIdentity.getMessageSigner(message,signature);
			if (userPubKey.isEmpty()) {
				response.setContentType("text/plain");
				PrintWriter pw = response.getWriter();
				pw.println("Signature failed, DID NOT Create user : " + uname+":"+signature);		
				pw.println("Setting sessionAttribute: " + sessionid);
				pw.close();	
			} else {
				User user = new User();
				user.setPubkey(userPubKey);
				user.setUname(uname);
				userBase.put(userPubKey, user);
				response.setContentType("text/plain");
				PrintWriter pw = response.getWriter();
				pw.println(confirmFunction(user.getUname()+"~"+sessionid));
				pw.println("Created user : " + uname+":"+userPubKey);		
				pw.println("Setting sessionAttribute: " + sessionid);
				pw.close();	
				contextWideSessionAttributes.put("user", user); 
				contextWideSessionAttributes.put("created",true);
			}
		} else if (command.startsWith("d")) {
			String uname = null; //arg[UNAME];
			//contextWideSessionAttributes.put("uname", uname+signature); // sign is for debuggig
			uname = (String)contextWideSessionAttributes.get("uname");
			User user = (User)contextWideSessionAttributes.get("user");
			if (user != null) {
				Map<String, BitIdentity> remoteUsers = Utils.getRemoteUsers(getServletContext());
				if (remoteUsers.containsKey(user.getPubkey())) {
					remoteUsers.remove(user.getPubkey());
				}
				user.deleteUser();
			}
			
			response.setContentType("text/plain");
			PrintWriter pw = response.getWriter();
			pw.println(confirmFunction(uname+"~"+sessionid));
			pw.println("Deleted user : " + uname+":"+signature);		
			pw.println("Setting sessionAttribute: " + sessionid);
			pw.close();	
			contextWideSessionAttributes.put("deleted",true);

		} else {
			// Error
			response.setContentType("text/plain");
			PrintWriter pw = response.getWriter();
			pw.println("Error");
			pw.println("Session: " + sessionid);
			pw.close();	
		}
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request,response);
		// TODO Auto-generated method stub
	}

	String confirmFunction(String message) {
		BitIdentity onlineKeys = Utils.getOnlineServerKeys(getServletContext());
		DateFormat df = new SimpleDateFormat("yyyyMMddHHmmssZ");			
		Date now = new Date();
		String datedMessage = df.format(now)+"~"+message;
		String signature = onlineKeys.generateSignaTure(datedMessage );
		String confirmMessage = "{\"message\":{\""+datedMessage +"\"},\"signature\":\""+signature+"\"},"+getServletContext().getAttribute("myMicroCert");
		return confirmMessage;
	}
}

package fi.kotipalo.h.CryptoID;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * Servlet implementation class login
 */
@WebServlet("/login")
public class Login extends HttpServlet {
	private static final long serialVersionUID = 1L;

    /**
     * Default constructor. 
     */
    public Login() {
        // TODO Auto-generated constructor stub
    }

	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config); // always!
		getServletContext().log("Initializing...");
		BitIdentity serverKeys = null;
		DataInputStream in = null;
		try {
			File settingFile = new File("/opt/CryptoIDDemo/config.txt");
			if (settingFile.exists()) {
				if (parseConfig(settingFile)) {
					getServletContext().log("Got server keys...");
				} else {
					// saveconfig
					getServletContext().log("error reading server keys, generating temp keys...");
					serverKeys = Utils.getMasterServerKeys(getServletContext());
					// Write to config;
				}
			} else {
				// saveconfig
				getServletContext().log("No server keys found, generating new keys...");
				serverKeys = Utils.getMasterServerKeys(getServletContext());
				// Write to config;
				settingFile.createNewFile();

				FileWriter fw = new FileWriter(settingFile.getAbsoluteFile());
				BufferedWriter bw = new BufferedWriter(fw);
				bw.write(serverKeys.getSerialized());
				bw.close();				
			}
		} catch (Exception e) {
			getServletContext().log("Exception while Initializing..."+e.getMessage());
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					// ignored
				}
			}
		}
		getServletContext().log("Done.");
	}

	private boolean parseConfig(File file ) {
		{
		    BufferedReader fr;
			try {
				fr = new BufferedReader(new FileReader(file));
				String nextLine;
				List<String> lines = new ArrayList<String>();
				do {
					nextLine=fr.readLine(); // = fr.readLine().trim() -> null pointer!!!
					if (nextLine != null) nextLine = nextLine.trim();
					if ((nextLine != null) && (!nextLine.startsWith("["))) {
						lines.add(nextLine);
					} else {
						BitIdentity id = new BitIdentity(lines);
						if (id.getUname() != null) {
							getServletContext().setAttribute("masterServerKeys", id);
							return true;
						}
						lines.clear();
						if (nextLine != null)
							lines.add(nextLine);
					}
				} while (nextLine != null);
		        //fr.write(identity.getSerialized());
		        fr.close();			    
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return false;
	}
	
	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		HttpSession session = request.getSession(false);
		if (session != null) {
			// Redirect, if we have logged in meanwhile
			if (Utils.isTriggered(getServletContext(), session.getId(), "logged")) {
				response.sendRedirect("wellcome");
				return;
			}
			// dump old session and generate a new every time we get on this page
			Map<String, Object> contextWideSessionAttributes = null;
			String sessionId = session.getId();
			contextWideSessionAttributes = (Map<String, Object>)getServletContext().getAttribute(sessionId);
			if (contextWideSessionAttributes != null) { 
				contextWideSessionAttributes.put("uname", null);
			}
			// this should be done allways when useer logs out
			// TODO: make a sessionlistener to clean up
			getServletContext().setAttribute(sessionId, null); 
			session.invalidate();
		} 
		session = request.getSession(true);
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

		/*(Map<String, BitIdentity>)getServletContext().getAttribute("remoteUsers");
		if (remoteUsers == null) {
			remoteUsers = new HashMap<String, BitIdentity>();
			getServletContext().setAttribute("remoteUsers", remoteUsers);
		}*/
		// you should only have public key on the server
		// It is used to sign online key
		BitIdentity serverKeys = Utils.getMasterServerKeys(getServletContext());
		/*(BitIdentity)getServletContext().getAttribute("serverKeys");
		if (serverKeys == null) {
			serverKeys = new BitIdentity();
			serverKeys.setUname(Utils.myServerAddress);
			getServletContext().setAttribute("serverKeys", serverKeys);
		}*/
		//BitIdentity onlineKeys = Utils.getOnlineServerKeys(getServletContext());
		/*		(BitIdentity)getServletContext().getAttribute("onlineKeys");
		if (onlineKeys == null) {
			onlineKeys = new BitIdentity();
			long t=new Date().getTime();
			Date validUntil=new Date(t + (10 * ONE_MINUTE_IN_MILLIS));	
			DateFormat df = new SimpleDateFormat("yyyyMMdd-HH:mm:ss");			
			onlineKeys.setUname(df.format(validUntil));
			getServletContext().setAttribute("onlineKeys", onlineKeys);
			String myMicroCertReq="\"key\":\""+onlineKeys.getAddress()+"\",\"expires\":\""+onlineKeys.getUname()+"\"";
			String myMicroCertSign=serverKeys.generateSignaTure(myMicroCertReq);
			String myMicroCert = "{\"certificate\":{"+myMicroCertReq+"},\"signature\":\""+myMicroCertSign+"\"}";
			getServletContext().setAttribute("myMicroCert", myMicroCert);
		}*/

		response.setContentType("text/html");
		PrintWriter pw = response.getWriter();
		pw.println("<!DOCTYPE html>" );
		pw.println("<html>" );
		pw.println("<head>" );
		pw.println("<script type=\"text/javascript\" src=\"LongPoll.js\" ></script>" );
		pw.println("</head>" );
		pw.println("<body onload=\"trigger = 'logged'; forwardPage = 'wellcome'; pollRefresh()\">" );
		pw.println("<p>CryptoID QR code identification demo application</p>");
		pw.println("<p>This server has not even a single user passord for the hackers to steal.<br>");
		pw.println("<p>Identification is based on Bitcoin cryptography (protecting over 10 million bitcoins worth 10 billion US $)<br>");
		pw.println("If you have already registered, you can log in simply by reading the QR code below<br>");
		pw.println("If you want to register, just click 'Register' and enter the username you want. <br>");
		pw.println("After that you can confirm your registration simply by reading the generated QR code</p>");
		pw.println("<p>Note: identities are not permanently saved</p>");
		pw.println("<p>Note: Enable javascript for longpoll to work or refresh this page after 2D barcode read.</p>");
		pw.println(MessageFormat.format("URI: {0}<br>", new Object[] { uri }));
		pw.println(MessageFormat.format("SID: {0}<br>", new Object[] { sid }));
		pw.println(MessageFormat.format("SSLID: {0}<br>", new Object[] { sslId }));
/*		pw.println(MessageFormat.format("<p>Info: {0}</p>", new Object[] { notSecret }));
		pw.println(MessageFormat.format("<p>Secret: {0}</p>", new Object[] { secret }));
		pw.println(MessageFormat.format("<p>Date: {0}</p>", new Object[] { d }));*/
		//pw.println("Total number of visits: " + totalTimes + "<br/>");

		pw.println("<form method=\"get\" action=\"Register.html\"><button type=\"submit\">Register</button></form>");
		pw.println("<p>Download Android BitLogin application <A href=\"download/BitLogin.apk\">BitLogin.apk</A></p>");
		pw.println("<img src=\"" + "GetQRCode?f=login" + "\">" );
		pw.println("<p>Message inside the QR code:<b>"+Utils.getCodeMessage("login",sid,"",serverKeys) + "</b></p>");
		// remoteUser is for simulating only
		Map<String, BitIdentity> remoteUsers = Utils.getRemoteUsers(getServletContext());
		if (!remoteUsers.isEmpty()) {
			pw.println("<p>You can test with these identities.<br>");
			pw.println("Just copypaste the bolded text to any browser:</p>");
		} else {
			pw.println("<p>Click 'Register' button to create an identity for testing</p>");
		}
		for (Entry<String, BitIdentity> e : remoteUsers.entrySet()) {
		    //System.out.println(e.getKey() + ": " + e.getValue());
		    BitIdentity user = e.getValue();
		    String message = "l~"+sid;
		    String signature = user.generateSignaTure(message);
		    pw.println("<p>"+user.getUname()+" <b>"+ Utils.getCodeResponse("id="+message,signature,serverKeys)+ "</b></p>");
		}
		pw.println("</body>" );		
		pw.println("</html>" );		
		pw.close();	
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doPost(request,response);
	}

}

package fi.kotipalo.h.CryptoID;

import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

public class Utils {
	// Get this from database:
	public static String myServerAddress = "Enter_address_to_Utils.java";
	static final long ONE_MINUTE_IN_MILLIS=60000;

	public static String getCodeMessage(String function, String sid, String uname, BitIdentity serverKeys ) {
		  String qrText;
		  if ("login".equals(function)) {
			  qrText = "bitid:"+serverKeys.getUname()+"/cid?id=l~"+sid+"~"+serverKeys.getAddress();
			  //String message = "bitid:"+serverKeys.getUname()+"/cid?id=l~"+sid;
			  //qrText = message+"&sign="+serverKeys.generateSignaTure(message);
			  
		  } else if ("create".equals(function)) {
			  qrText = "bitid:"+serverKeys.getUname()+"/cid?id=c~"+sid+"~"+uname+"~"+serverKeys.getAddress();
			  //String message = "bitid:"+serverKeys.getUname()+"/cid?id=c~"+sid+"~"+uname;
			  //qrText = message+"&sign="+serverKeys.generateSignaTure(message);
		  } else if ("delete".equals(function)) {
			  qrText = "bitid:"+serverKeys.getUname()+"/cid?id=d~"+sid+"~"+uname+"~"+serverKeys.getAddress();
			  //String message = "bitid:"+serverKeys.getUname()+"/cid?id=d~"+sid+"~"+uname;
			  //qrText = message  + "&sign="+serverKeys.generateSignaTure(message);
		  } else {
			  qrText = "bitid.error:unknown_function:"+function;
		  }
		  return qrText;
	}

	// for debugging
	public static String getCodeResponse(String message, String signature, BitIdentity serverKeys ) {
		  String qrText;
		  qrText = serverKeys.getUname()+"/cid?"+message+ "&signature=" + signature; //"id=l~"+sid+"&signature=" + signature;
		  return qrText;
	}
	
	public static boolean isHijackedSession(HttpServletRequest request, HttpSession session) {
		String sid = session.getId();
		String sslId = (String) request.getAttribute(
	                "javax.servlet.request.ssl_session");
		// prevent session hijack
		String oldSslId = (String)session.getAttribute("sslId");
		if (oldSslId != null) {
			if (oldSslId.equals(sslId)) {
				// not hijacked
			} else {
				// hijacked session!!
				return true;
			}
		} else {
			// new session
			if (sslId != null) {
				// and this is https
				session.setAttribute("sslId", sslId);
			} else {
			// well, door is open for hijack... maybe forward to https?!
			}
		}
		return false;
	}
	
	static void printHijackedSession(PrintWriter pw ) {
		pw.println("<!DOCTYPE html>" );
		pw.println("<html>" );
		pw.println("<head>" );
		pw.println("</head>" );
		pw.println("<body>" );
		pw.println("<p><b>Sorry, this session is hijacked, try again!</b></p>");
		pw.println("</body>" );		
		pw.println("</html>" );		
		pw.close();	
		return;
	}
	
	// These are ust for debugging without android device
	static Map<String, BitIdentity> getRemoteUsers(ServletContext servletContext) {
		Map<String, BitIdentity> remoteUsers = (Map<String, BitIdentity>)servletContext.getAttribute("remoteUsers");
		if (remoteUsers == null) {
			remoteUsers = new HashMap<String, BitIdentity>();
			servletContext.setAttribute("remoteUsers", remoteUsers);
		}
		// Clean up old non-activated users
		Map<String, User> userBase = getUserBase(servletContext);
		Date now = new Date();
		Iterator<Map.Entry<String, BitIdentity>> iter = remoteUsers.entrySet().iterator();
		while (iter.hasNext()) {
		    Map.Entry<String,BitIdentity> entry = iter.next();
		    BitIdentity identity = entry.getValue();
			User user = userBase.get(identity.getAddress());
			long userAgeInMinutes = (now.getTime() - identity.getCreateTime().getTime()) / ONE_MINUTE_IN_MILLIS;
		    if ((user==null) && (userAgeInMinutes>10)) {
		        iter.remove();
		    }
		}		
		return remoteUsers;
	}
	
	// of course, only public key should be online 
	static BitIdentity  getMasterServerKeys(ServletContext servletContext) {
		BitIdentity serverKeys = (BitIdentity)servletContext.getAttribute("masterServerKeys");
		if (serverKeys == null) {
			serverKeys = new BitIdentity();
			serverKeys.setUname(Utils.myServerAddress);
			servletContext.setAttribute("masterServerKeys", serverKeys);
		}
		return serverKeys;
	}
	
	static BitIdentity  getOnlineServerKeys(ServletContext servletContext) {
		BitIdentity onlineKeys = (BitIdentity)servletContext.getAttribute("onlineServerKeys");
		Date onLineKeyExpireTime = null;
		DateFormat df = new SimpleDateFormat("yyyyMMddHHmmssZ");			
		if (onlineKeys == null) {
			onLineKeyExpireTime = new Date(Long.MIN_VALUE);
		} else {
			try {
				onLineKeyExpireTime = df.parse(onlineKeys.getUname());
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		Date now = new Date();
		if (now.after(onLineKeyExpireTime)) {
			// server master keys should be offline  
			// and uCertificate should be get manually or
			// by unidirectional link from offline computer
			// unidirectional link might be rs232 cabel with cutted rx
			// or, why not, QR code read by web server webcam ;-)
			BitIdentity serverKeys = getMasterServerKeys(servletContext);
			onlineKeys = new BitIdentity();
			long t=new Date().getTime();
			Date validUntil=new Date(t + (10 * ONE_MINUTE_IN_MILLIS));	
			onlineKeys.setUname(df.format(validUntil));
			servletContext.setAttribute("onlineServerKeys", onlineKeys);
			String myMicroCertReq="\"key\":\""+onlineKeys.getAddress()+"\",\"expires\":\""+onlineKeys.getUname()+"\"";
			String myMicroCertSign=serverKeys.generateSignaTure(myMicroCertReq);
			String myMicroCert = "{\"uCert\":{"+myMicroCertReq+"},\"signature\":\""+myMicroCertSign+"\"}";
			servletContext.setAttribute("myMicroCert", myMicroCert);
		}
		return onlineKeys;
	}
	
	static Map<String, Object> getContextWideSessionAttributes(ServletContext servletContext, String sessionid) {
		Map<String, Object> contextWideSessionAttributes = (Map<String, Object>)servletContext.getAttribute(sessionid);
		if (contextWideSessionAttributes == null) { // or check hash etc.
			contextWideSessionAttributes = new HashMap<String, Object>();
			servletContext.setAttribute(sessionid, contextWideSessionAttributes);
		}
		return contextWideSessionAttributes;
	}

	static void clearContextWideSessionAttributes(ServletContext servletContext, String sessionid) {
		servletContext.setAttribute(sessionid, null);
	}
	
	static Map<String, User> getUserBase(ServletContext servletContext) {
		Map<String, User> userBase = (Map<String, User>)servletContext.getAttribute("userBase");
		if (userBase == null) {
			userBase = new HashMap<String, User>();
			servletContext.setAttribute("userBase",userBase);
		}
		return userBase;
	}
	
	static boolean isTriggered(ServletContext servletContext, String sessionId, String trigger) {
		Boolean triggerValue;
		Map<String, Object> contextWideSessionAttributes = getContextWideSessionAttributes(servletContext, sessionId);
		if (contextWideSessionAttributes != null) { 
			triggerValue = (Boolean)contextWideSessionAttributes.get(trigger);
			// Could be null
			if (Boolean.TRUE.equals(triggerValue)) return true;
		}
		return false;
	}
}

package fi.kotipalo.h.CryptoID;

import java.io.IOException;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import net.glxn.qrgen.QRCode;
import net.glxn.qrgen.image.ImageType;

/**
 * Servlet implementation class GetQRCode
 */
@WebServlet("/GetQRCode")
public class GetQRCode extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public GetQRCode() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		  HttpSession session = request.getSession(true);
		  String sid = session.getId(); // maybe hashed?
		  String function = request.getParameter("f");
		  String uname = (String)session.getAttribute("uname");
		  BitIdentity serverKeys = Utils.getMasterServerKeys(getServletContext());
		  /*BitIdentity serverKeys = (BitIdentity)getServletContext().getAttribute("serverKeys");
		  if (serverKeys == null) {
			serverKeys = new BitIdentity();
			serverKeys.setUname(Utils.myServerAddress);
			getServletContext().setAttribute("serverKeys", serverKeys);
		  }*/
		  String qrText = Utils.getCodeMessage(function,sid,uname,serverKeys);
		  ByteArrayOutputStream out = QRCode.from(qrText).to(ImageType.PNG).withSize(300, 300).stream();
		         
		        response.setContentType("image/png");
		        response.setContentLength(out.size());
		         
		        OutputStream os = response.getOutputStream();
		        os.write(out.toByteArray());
		        
		        os.flush();
		        os.close();
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request,response);
		// TODO Auto-generated method stub
	}

}

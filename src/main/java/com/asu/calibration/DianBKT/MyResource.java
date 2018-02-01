package com.asu.calibration.DianBKT;

import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

import com.asu.calibration.DianBKT.models.seatr_message;
import com.asu.dinabkt.database.SessionFactoryUtil;

/**
 * Root resource (exposed at "myresource" path)
 */
@Path("myresource")
public class MyResource {

    /**
     * Method handling HTTP GET requests. The returned object will be sent
     * to the client as "text/plain" media type.
     *
     * @return String that will be returned as a text/plain response.
     */
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String getIt() {
    	System.out.println("HITTING");
    	
    	SessionFactory sf_OPE_Class_25 = SessionFactoryUtil.getSessionFactory();
		Session session25 = sf_OPE_Class_25.openSession();
		
		String seatrMsg_hql = "FROM seatr_message";
		Query seatrMSGquery = session25.createQuery(seatrMsg_hql);
		List<seatr_message> qResult = seatrMSGquery.list();
		for(seatr_message m:qResult){
			System.out.println("Student_id:"+m.getStudent_id()+"  Quetion_id:"+m.getQuestion_id()+"  format_id:"+m.getFormat_id()+" correct:"+m.getCorrect()+" timestamp:"+m.getTimestamp());
		}
    	
		session25.disconnect();
		session25.close();
    	Calibration.START();
    	return "Got it!";
    }
}

package com.asu.dinabkt.database;

import org.hibernate.SessionFactory;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistry;

import com.asu.dinabkt.utils.GlobalConstants;

/**
 * 
 * @author Lakshmisagar Kusnoor
 *
 */
public class SessionFactoryUtil {

	private static SessionFactory sesFactory;
    private static ServiceRegistry sesRegistry;
    static Configuration cfg;
    public static SessionFactory initSessionFactory(){
        try{

        	/*cfg= new Configuration().configure("/hibernatetest.cfg.xml");
            sesRegistry = new StandardServiceRegistryBuilder().applySettings(cfg.getProperties()).build();
            sesFactory=cfg.buildSessionFactory(sesRegistry);*/
        	StandardServiceRegistry standardRegistry = new StandardServiceRegistryBuilder() .configure(GlobalConstants.OPE_Class_25).build();
            Metadata metadata = new MetadataSources(standardRegistry).getMetadataBuilder().build();
            
            try{

                //Session session = getSessionFactory().openSession();
                //Transaction tx = session.beginTransaction();
                System.out.println("Connected to Master Database Server");
                return metadata.getSessionFactoryBuilder().build();
            }                   
            catch(Throwable ex){
                cfg= new Configuration().configure("lib/hibernatetest.cfg.xml"); 
                sesRegistry = new StandardServiceRegistryBuilder().applySettings(cfg.getProperties()).build();
                sesFactory=cfg.buildSessionFactory(sesRegistry);
                System.out.println("Connected to Slave Database Server");
                return null;
            }
        }
        catch(Throwable ex){
            System.out.println("Master & Slave Database Error.");
            System.err.println("Initial SessionFactory Creation Failed"+ex);
            throw new ExceptionInInitializerError(ex);
        }
    }   
    public  static SessionFactory getSessionFactory() {
    	if(sesFactory == null)
    	{
    		sesFactory = initSessionFactory();
    	}
            return sesFactory;
    }
}

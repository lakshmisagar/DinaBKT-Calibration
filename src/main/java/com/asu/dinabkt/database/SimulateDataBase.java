package com.asu.dinabkt.database;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;

import com.asu.calibration.DianBKT.models.seatr_message;
import com.asu.dinabkt.utils.GlobalConstants;
import com.asu.dinabkt.utils.Operations;
import com.asu.dinabkt.utils.Utils;

public class SimulateDataBase {
	static Double fillrandom_initalMastery[];
	static Double fillrandom_Learn[];
	static Double fillrandom_slip[];
	static Double fillrandom_guess[];
	static SessionFactory sf_OPE_Class_25;
	static Session session25;
	public static void setAllStudentsData() {
		GlobalConstants.total_KCs = 30;
		GlobalConstants.total_Questions = 50;
		GlobalConstants.total_Students = 100;
		GlobalConstants.total_Formats = 6;
		GlobalConstants.total_Threshold = 25;
		GlobalConstants.total_attempts_per_student = (2*GlobalConstants.total_Questions);
		setQMatrix();
		fillRandomParameters();
		setCompetence();
		generateMessagesToSeatr();
	}

	private static void setQMatrix() {
		Random r = new Random();
		for (int q = 0; q < GlobalConstants.total_Questions; q++) {
			Utils.setQuestion(q, q);
			Utils.setQuestionMap(q);
			Utils.setClassIdQuestion(q, q);
			int n_KCs = r.nextInt((GlobalConstants.total_KCs - 1) + 1) + 1;
			for (int j = 0; j < n_KCs; j++) {
				int kc = r.nextInt(((GlobalConstants.total_KCs - 1) - 0) + 1) + 0;
				Utils.setQuestionMatrix(q, kc);
			}
		}
	}
	
	public static void fillRandomParameters() {
		Random r = new Random();
		for (int KcIndex = 0; KcIndex < GlobalConstants.total_KCs; KcIndex++) {
			double r_initalMaster = 0.05 + r.nextDouble() * (0.95 - 0.05);
			double r_Learn = 0.05 + r.nextDouble() * (0.5 - 0.05);
			int Kc = Utils.getKc(KcIndex);
			Utils.setInitialMasteryMap(Kc, Double.valueOf(r_initalMaster));
			Utils.setLearnMap(Kc, Double.valueOf(r_Learn));
			fillrandom_initalMastery[KcIndex] = Utils.getInitialMasteryMap(Kc);
			fillrandom_Learn[KcIndex] = Utils.getLearnMap(Kc);
		}
		for (int F = 0; F <GlobalConstants.total_Formats ; F++) {
			double r_slip = 0.05 + r.nextDouble() * (0.45 - 0.05);
			double r_guess = 0.01 + r.nextDouble() * (0.5 - 0.01);
			Utils.setSlipMap(F, Double.valueOf(r_slip));
			Utils.setGuessMap(F, Double.valueOf(r_guess));
			fillrandom_slip[F] = Utils.getSlipMap(F);
			fillrandom_guess[F] = Utils.getGuessMap(F);
		}
	}
	
	private static void setCompetence() {
		Random r = new Random();
		for (int St = 0; St < GlobalConstants.total_Students; St++) {
			int S = Utils.getStudent(St);
			HashMap<Integer, Double> inner_KcV_Map = new HashMap<Integer, Double>();
			for (int K = 0; K < GlobalConstants.total_KCs; K++) {
				int Kc = Utils.getKc(K);
				double compVal = 0.001 + r.nextDouble() * (0.999 - 0.001);
				inner_KcV_Map.put(Kc, compVal);
			}
			Utils.initalizeCompetence(S, inner_KcV_Map);
		}
	}
	private static void generateMessagesToSeatr() {
		Random r = new Random();
		sf_OPE_Class_25 = new Configuration().configure(GlobalConstants.OPE_Class_25).buildSessionFactory();
		Session session25 = sf_OPE_Class_25.openSession();
		for(int A=0;A<GlobalConstants.total_attempts_per_student;A++){
			for (int St = 0; St < GlobalConstants.total_Students; St++) {
				int S = Utils.getStudent(St);
				int Q = 0 + r.nextInt() * (GlobalConstants.total_Questions - 0); 
				int F = 0 + r.nextInt() * (GlobalConstants.total_Formats - 0); 
				double Applied = 1.0;
				ArrayList<Integer> KCs = Utils.getQuestionMatrix(Q);
				for (int list_K = 0; list_K < KCs.size(); list_K++) {
					Applied = Operations.multiplyDouble(Applied, Utils.getCompetence(S, list_K));
					double one_minus_comp = Operations.substractDouble(1.0, Utils.getCompetence(S, list_K));
					double learn_mul_one_minus_comp = Operations.multiplyDouble(Utils.getLearnMap(list_K), one_minus_comp);
					double CompetenceValue = Operations.addDouble(Utils.getCompetence(S, list_K),learn_mul_one_minus_comp); 
					Utils.setCompetence(S, list_K, CompetenceValue);
				}
				double one_minus_applied = Operations.substractDouble((double)1, Applied);
				double guess_mul_one_minus_applied = Operations.multiplyDouble(Utils.getGuessMap(F), one_minus_applied);
				double one_minus_slip = Operations.substractDouble((double)1, Utils.getSlipMap(F));
				double one_minus_slip_mul_applied = Operations.multiplyDouble(one_minus_slip, Applied);
				double P = Operations.addDouble(one_minus_slip_mul_applied, guess_mul_one_minus_applied);
				
				double T = 0.0 + r.nextDouble() * (1.0 - 0.0); 
				int Answer = 0;
				if(T < P){
					Answer = 1;
				}
				String timeStamp = new Timestamp(System.currentTimeMillis()).toString();
				sendMessageToSeatrTable(S,Q,F,Answer,timeStamp);
			}
		}
		session25.disconnect();
		session25.close();
	}

	private static void sendMessageToSeatrTable(int s, int q, int f, int answer, String timeStamp) {
		Transaction tx = session25.beginTransaction();
		seatr_message message = new seatr_message(s,q,f,answer,timeStamp);
		session25.save(message);
		tx.commit();
	}
	
	
		
}

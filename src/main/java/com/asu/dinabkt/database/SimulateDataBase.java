package com.asu.dinabkt.database;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Random;
import java.util.Set;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

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
		GlobalConstants.total_KCs = 3;
		GlobalConstants.total_Questions = 5;
		GlobalConstants.total_Students = 10;
		GlobalConstants.total_Formats = 6;
		GlobalConstants.total_Threshold = 25;
		GlobalConstants.total_attempts_per_student = (2*GlobalConstants.total_Questions);
	
		fillrandom_initalMastery = new Double[GlobalConstants.total_KCs + 1];
		fillrandom_Learn = new Double[GlobalConstants.total_KCs + 1];
		fillrandom_slip = new Double[GlobalConstants.total_Formats + 1];
		fillrandom_guess = new Double[GlobalConstants.total_Formats + 1];
		
		setKcMap();
		setStudentList();
		setQMatrix();
		setSQMap();
		setFormatMap();
		fillRandomParameters();
		setCompetence();
		initialize4ParamsCount();
		initialize4ParamsOpportunities();
		initialize4ParamsEstimate();
		initializeLastS();
		generateMessagesToSeatr();
		
	}
	
	private static void setFormatMap() {
		for (int F = 0; F < GlobalConstants.total_Formats; F++) {
			Utils.setFormatMap(F);
		}
	}

	//initializations of Data Structures
private static void initializeLastS() {
		
		for(int S = 0; S < GlobalConstants.total_Students; S++){
			int st = Utils.getStudent(S);
			Utils.setLast(st, 0);
		}
	}

	private static void initialize4ParamsEstimate() {

		for (int F = 0; F < GlobalConstants.total_Formats; F++) {
			
			Utils.setSlipEstimateMap(F, 0.0);
			Utils.setGuessEstimateMap(F, 0.0);
		}
		for (int K = 0; K < GlobalConstants.total_KCs; K++) {
			int kc = Utils.getKc(K);
			Utils.setLearnEstimateMap(kc, 0.0);
			Utils.setInitialMasteryEstimateMap(kc, 0.0);
		}
		
	}

	private static void initialize4ParamsOpportunities() {

		for (int F = 0; F < GlobalConstants.total_Formats; F++) {
			
			Utils.setSlipOpportunitiesMap(F, 0.0);
			Utils.setGuessOpportunitiesMap(F, 0.0);
		}
		for (int K = 0; K < GlobalConstants.total_KCs; K++) {
			int kc = Utils.getKc(K);
			Utils.setLearnOpportunitiesMap(kc, 0.0);
			Utils.setInitialMasteryOpportunitiesMap(kc, 0.0);
		}
	}

	private static void initialize4ParamsCount() {
		for (int F = 0; F < GlobalConstants.total_Formats; F++) {
			
			Utils.setSlipCountMap(F, 0.0);
			Utils.setGuessCountMap(F, 0.0);
		}
		for (int K = 0; K < GlobalConstants.total_KCs; K++) {
			int kc = Utils.getKc(K);
			Utils.setLearnCountMap(kc, 0.0);
			Utils.setInitialMasteryCountMap(kc, 0.0);
		}
	}
	
	//KC
	private static void setKcMap() {
		for (int i = 0; i < GlobalConstants.total_KCs; i++) {
			int kcValue = i;
			Utils.setKc(i, kcValue);
			Utils.setKcMap(kcValue);
		}
	}
	
	//S
	private static void setStudentList() {
		int[] ids = new int[GlobalConstants.total_Students];
		for (int i = 0; i < GlobalConstants.total_Students; i++) {
			ids[i] = i;
		}
		Utils.setStudentsList(ids);
	}
	
	private static void setQMatrix() {
		Random r = new Random();
		for (int q = 0; q < GlobalConstants.total_Questions; q++) {
			Utils.setQuestion(q, q);
			//Utils.setQuestionMap(q);
			Utils.setClassIdQuestion(q, q);
			int n_KCs = r.nextInt((GlobalConstants.total_KCs - 1) + 1) + 1;
			Set<Integer> generated = new LinkedHashSet<Integer>();
			while(generated.size()<n_KCs){
				int kc = r.nextInt(((GlobalConstants.total_KCs - 1) - 0) + 1) + 0;
				generated.add(kc);
			}
			Iterator<Integer> itr = generated.iterator();
			while(itr.hasNext()){
				Utils.setQuestionMatrix(q, itr.next());
			}
		}
	}
	
	// set of Q answered by student
		private static void setSQMap() {
			Random r = new Random();
			for (int st = 0; st < GlobalConstants.total_Students; st++) {
				int id = st;
				HashMap<Integer, Integer> question_AQ_Map = new HashMap<Integer, Integer>();
				HashMap<Integer, Integer> answer_AC_Map = new HashMap<Integer, Integer>();
				HashMap<Integer, Integer> inner_setAnswer = new HashMap<Integer, Integer>();
				int numberOfQuestionsAttempted =/* r.nextInt((*/GlobalConstants.total_Questions /*- 1) + 1) + 1*/;
				Set<Integer> generated = new LinkedHashSet<Integer>();
				while(generated.size()<numberOfQuestionsAttempted){
					int Q = r.nextInt(((GlobalConstants.total_Questions - 1) - 0) + 1) + 0;
					generated.add(Q);
				}
				for(Integer Q : generated){
					inner_setAnswer.put(Q, 0);
	 				int correct = (Math.random() < 0.5) ? 0 :1;
					//int correct = 0;
					int count = Utils.getLast(id);
					count++;
					Utils.setLast(id, count);
					int A = count;
					/*if(A==1)correct=1;
					if(A==2)correct=0;
					if(A==3)correct=0;
					if(A==4)correct=1;*/
					answer_AC_Map.put(A, correct);
					Utils.setAnswer(st, answer_AC_Map);
					question_AQ_Map.put(A, Q);
					//System.out.println("setQuestion "+st+" "+A+" "+Q);
					Utils.setQuestionSAMap(st, question_AQ_Map);
					//System.out.println("getQuestion ("+st+","+A+") "+Utils.getQuestion(st, A));
					//System.out.println(st+" "+A+" "+Q+" = "+correct);
					//System.out.println("1 SAGAR getAnswer_S_A_Q("+st+","+A+","+Utils.getQuestion(st, A)+")"+Utils.getAnswer(st, A));
				}
				//Utils.simulateInitalizeSetAnswer(id,inner_setAnswer);
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
		sf_OPE_Class_25 = SessionFactoryUtil.getSessionFactory();
		session25 = sf_OPE_Class_25.openSession();
	/*	create table seatr_message(
				  `id` int(11) unsigned NOT NULL AUTO_INCREMENT,
				  `student_id` int(11) unsigned NOT NULL,
				  `question_id` int(11) unsigned DEFAULT NULL,
				  `format_id` int(11) unsigned DEFAULT NULL,
				  `correct` tinyint(4) NOT NULL,
				  `timestamp` VARCHAR(100) NOT NULL,
				   PRIMARY KEY (`id`)
				   );*/
		session25.createQuery("delete from seatr_message").executeUpdate();
		for(int A=0;A<GlobalConstants.total_attempts_per_student;A++){
			for (int St = 0; St < GlobalConstants.total_Students; St++) {
				int S = Utils.getStudent(St);
				int Q = 0 + r.nextInt(GlobalConstants.total_Questions); 
				int F = 0 + r.nextInt(GlobalConstants.total_Formats); 
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

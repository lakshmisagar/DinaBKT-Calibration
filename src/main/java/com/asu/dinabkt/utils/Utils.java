package com.asu.dinabkt.utils;

import java.util.ArrayList;
import java.util.HashMap;

import com.asu.dinabkt.utils.GlobalConstants;

public class Utils {


	private static int[] mKC = new int[GlobalConstants.total_KCs];
	private static int[] questionsList = new int[GlobalConstants.total_Questions];
	private static int[] studentsList = new int[GlobalConstants.total_Students];

	// Datastructure to implement Question Qmatrix Slip and Guess
	static HashMap<Integer, HashMap<Integer, String>> Q_QM_Slip_Guess_map = new HashMap<Integer, HashMap<Integer, String>>();

	// Datastructure to implement  Slip Count
	static HashMap<Integer, Double> Q_QM_Slip_Count_map = new HashMap<>();
	
	// Datastructure to implement Guess Count
	static HashMap<Integer, Double> Q_QM_Guess_Count_map = new HashMap<>();

	// Datastructure to implement Slip Opportunities
	static HashMap<Integer, Double> Q_QM_Slip_Opportunities_map = new HashMap<>();

	// Datastructure to implement Guess Opportunities
	static HashMap<Integer, Double> Q_QM_Guess_Opportunities_map = new HashMap<>();
		
	// Datastructure to implement Slip Estimate
	static HashMap<Integer, Double> Q_QM_Slip_Estimate_map = new HashMap<>();
	
	// Datastructure to implement Guess Estimate
	static HashMap<Integer, Double> Q_QM_Guess_Estimate_map = new HashMap<>();

	// Datastructure to implement question id question
	static HashMap<Integer, Integer> id_question_map = new HashMap<Integer, Integer>();

	// Datastructure to implement QuestionMatrix(Q)
	static HashMap<Integer, ArrayList<Integer>> qMatrix_map = new HashMap<Integer, ArrayList<Integer>>();

	// Datastructure to implement Kc InitialMater and Learn
	static HashMap<Integer, HashMap<Integer, Double>> kc_initialMastery_Learn_map = new HashMap<Integer, HashMap<Integer, Double>>();

	// Datastructure to implement Kc InitialMater Count
	static HashMap<Integer, Double> kc_initialMastery_Count_map = new HashMap<Integer, Double>();

	// Datastructure to implement Kc InitialMater Opportunities
	static HashMap<Integer, Double> kc_initialMastery_Opportunities_map = new HashMap<Integer, Double>();

	// Datastructure to implement Kc InitialMater Estimate
	static HashMap<Integer, Double> kc_initialMastery_Estimate_map = new HashMap<Integer, Double>();

	// Datastructure to implement Kc Learn Count
	static HashMap<Integer, Double> kc_LearnCount_map = new HashMap<Integer, Double>();

	// Datastructure to implement Kc  Learn Opportunities
	static HashMap<Integer, Double> kc_LearnOpportunities_map = new HashMap<Integer, Double>();

	// Datastructure to implement Kc Learn Estimate
	static HashMap<Integer, Double> kc_LearnEstimate_map = new HashMap<Integer, Double>();

	// Datastructure to implement Competence
	static HashMap<Integer, HashMap<Integer, Double>> competence_Map = new HashMap<Integer, HashMap<Integer, Double>>();

	// Datastructure to implement Answer(S,A,Q)
	static HashMap<Integer, HashMap<Integer, Integer>> answer_SA_Map = new HashMap<Integer, HashMap<Integer, Integer>>();

	// Datastructure to implement Prior(S,K)
	static HashMap<Integer, HashMap<Integer, Double>> prior_Map = new HashMap<Integer, HashMap<Integer, Double>>();

	// Datastructure to implement Posterior(S,K)
	static HashMap<Integer, HashMap<Integer, Double>> posterior_Map = new HashMap<Integer, HashMap<Integer, Double>>();

	// Datastructure to implement Last[Student]
	static HashMap<Integer, Integer> last_map = new HashMap<Integer, Integer>();

	// LS
	// Datastructure to implement setBestLearnMap
	static HashMap<Integer, Double> best_Learn_Map = new HashMap<>();

	// Datastructure to implement setBestIMMap
	static HashMap<Integer, Double> best_IM_Map = new HashMap<>();

	// Datastructure to implement setBestLearnMap
	static HashMap<Integer, Double> best_Slip_Map = new HashMap<>();

	// Datastructure to implement setBestLearnMap
	static HashMap<Integer, Double> best_Guess_Map = new HashMap<>();
	
	// Datastructure to implement Question(S,A,Q)
	static HashMap<Integer, HashMap<Integer, Integer>> question_SA_Map = new HashMap<Integer, HashMap<Integer, Integer>>();

	public static void setQuestion(int index, int questionid) {
		Utils.questionsList[index] = questionid;
	}
	
	public static int getQuestion(int index) {
		// System.out.println("getQuestion :"+index+" ");
		return questionsList[index];
	}

	// ************* Q S G QM **/*******************************
	public static void setFormatMap(int format) {
		HashMap<Integer, String> map = new HashMap<Integer, String>();
		Q_QM_Slip_Guess_map.put(format, map);
	}

	// **************class question and its id*****************
	public static void setClassIdQuestion(int index, int questionid) {
		id_question_map.put(index, questionid);
	}
	
	public static int getClassIdQuestion(int index) {
		return id_question_map.get(index);
	}

	/*
	 * QMatrix
	 */
	public static void setQuestionMatrix(int mQuestion, int kc) {
		ArrayList<Integer> list = qMatrix_map.get(mQuestion);
		// System.out.println();
		// System.out.println("setQuestionMatrix :"+mQuestion+" : "+kc+" : "
		// +list);
		if (list == null) {
			list = new ArrayList<Integer>();
			list.add(kc);
		} else {
			list.add(kc);
		}
		qMatrix_map.put(mQuestion, list);
		// System.out.println("setQuestionMatrix :"+mQuestion+" : "+kc+" : "
		// +list);
		// System.out.println("count :"+qMatrix_map.size());
	}

	public static int getKc(int index) {
		return mKC[index];
	}
	
	public static void setInitialMasteryMap(int Kc, Double value) {
		kc_initialMastery_Learn_map.get(Kc).put(GlobalConstants.IM, value);
	}

	public static void setInitialMasteryCountMap(int Kc, Double value) {
		kc_initialMastery_Count_map.put(Kc, value);
	}

	public static void setInitialMasteryOpportunitiesMap(int Kc, Double value) {
		kc_initialMastery_Opportunities_map.put(Kc, value);
	}

	public static void setInitialMasteryEstimateMap(int Kc, Double value) {
		kc_initialMastery_Estimate_map.put(Kc, value);
	}

	public static void setLearnMap(int Kc, Double value) {
		// System.out.println(" setLearnMap : "+Kc+" "+value);
		kc_initialMastery_Learn_map.get(Kc).put(GlobalConstants.Learn, value);
	}

	public static void setLearnCountMap(int Kc, Double value) {
		kc_LearnCount_map.put(Kc, value);
	}

	public static void setLearnOpportunitiesMap(int Kc, Double value) {
		kc_LearnOpportunities_map.put(Kc, value);
	}

	public static void setLearnEstimateMap(int Kc, Double value) {
		kc_LearnEstimate_map.put(Kc, value);
	}

	public static Double getInitialMasteryMap(int Kc) {
		return kc_initialMastery_Learn_map.get(Kc).get(GlobalConstants.IM);
	}

	public static Double getInitialMasteryCountMap(int Kc) {
		return kc_initialMastery_Count_map.get(Kc);
	}

	public static Double getInitialMasteryOpportunitiesMap(int Kc) {
		return kc_initialMastery_Opportunities_map.get(Kc);
	}

	public static Double getInitialMasteryEstimateMap(int Kc) {
		return kc_initialMastery_Estimate_map.get(Kc);
	}

	public static Double getLearnMap(int Kc) {
		return kc_initialMastery_Learn_map.get(Kc).get(GlobalConstants.Learn);
	}

	public static Double getLearnCountMap(int Kc) {
		return kc_LearnCount_map.get(Kc);
	}

	public static Double getLearnOpportunitiesMap(int Kc) {
		return kc_LearnOpportunities_map.get(Kc);
	}

	public static Double getLearnEstimateMap(int Kc) {
		return kc_LearnEstimate_map.get(Kc);
	}

	public static void setSlipMap(int format, Double value) {
		Q_QM_Slip_Guess_map.get(format).put(GlobalConstants.Slip, value.toString());
	}

	public static void setGuessMap(int format, Double value) {
		Q_QM_Slip_Guess_map.get(format).put(GlobalConstants.Guess, value.toString());
	}

	public static Double getSlipMap(int format) {
		return new Double(Q_QM_Slip_Guess_map.get(format).get(GlobalConstants.Slip));
	}

	public static Double getGuessMap(int format) {
		return new Double(Q_QM_Slip_Guess_map.get(format).get(GlobalConstants.Guess));
	}

	public static void setSlipCountMap(int format, Double value) {
		Q_QM_Slip_Count_map.put(format, value);
	}

	public static void setGuessCountMap(int format, Double value) {
		Q_QM_Guess_Count_map.put(format, value);
	}

	public static Double getSlipCountMap(int format) {
		return new Double(Q_QM_Slip_Count_map.get(format));
	}

	public static Double getGuessCountMap(int format) {
		return new Double(Q_QM_Guess_Count_map.get(format));
	}

	public static void setSlipOpportunitiesMap(int format, Double value) {
		Q_QM_Slip_Opportunities_map.put(format, value);
	}

	public static void setGuessOpportunitiesMap(int format, Double value) {
		Q_QM_Guess_Opportunities_map.put(format, value);
	}

	public static Double getSlipOpportunitiesMap(int format) {
		return new Double(Q_QM_Slip_Opportunities_map.get(format));
	}

	public static Double getGuessOpportunitiesMap(int format) {
		return new Double(Q_QM_Guess_Opportunities_map.get(format));
	}

	public static void setSlipEstimateMap(int format, Double value) {
		Q_QM_Slip_Estimate_map.put(format, value);
	}

	public static void setGuessEstimateMap(int format, Double value) {
		Q_QM_Guess_Estimate_map.put(format, value);
	}

	public static Double getSlipEstimateMap(int format) {
		return new Double(Q_QM_Slip_Estimate_map.get(format));
	}

	public static Double getGuessEstimateMap(int format) {
		return new Double(Q_QM_Guess_Estimate_map.get(format));
	}

	public static void setStudent(int index, int studentid) {
		Utils.studentsList[index] = studentid;
	}
	
	public static int getStudent(int index) {
		return studentsList[index];
	}

	// Competence
	public static void initalizeCompetence(int S, HashMap<Integer, Double> inner_KcV_Map) {
		competence_Map.put(S, inner_KcV_Map);
	}

	public static void setCompetence(int s, int kc, Double value) {
		competence_Map.get(s).put(kc, value);
	}

	public static Double getCompetence(int s, int kC) {
		return competence_Map.get(s).get(kC);
	}

	public static ArrayList<Integer> getQuestionMatrix(int mQuestion) {
		ArrayList<Integer> list = qMatrix_map.get(mQuestion);
		if (list == null) {
			list = new ArrayList<Integer>();
			list.add(getKc(0));
		}
		return list;
	}

	public static void setAnswer(int s, HashMap<Integer, Integer> answer_AC_Map) {
		answer_SA_Map.put(s, answer_AC_Map);
	}

	public static int getAnswer(int S, int A) {
		HashMap<Integer, Integer> innerAC_map = answer_SA_Map.get(S);
		return innerAC_map.get(A);
	}

	/*
	 * Prior
	 */
	public static void setPriorMap(int S, HashMap<Integer, Double> prior_KV_Map) {
		//System.out.println("setprior from" +v+ "  S : " + S+" val "+ prior_KV_Map);
		prior_Map.put(S, prior_KV_Map);
	}

	public static double getPriorMap(int S, int Kc) {
		HashMap<Integer, Double> inner_Prior_map = prior_Map.get(S);
		//System.out.println("getPrior from"+v+"  S" + S + "Kc" + Kc + "value :" +inner_Prior_map.get(Kc));
		return inner_Prior_map.get(Kc);
	}
	/*
	 * Prior setters and getters
	 */
	public static void setPrior(Integer S, Integer kc, double value) {
		prior_Map.get(S).put(kc, value); 
	}

	public static double getPrior(int S, int Kc) {
		HashMap<Integer, Double> inner_Prior_map = prior_Map.get(S);
		return inner_Prior_map.get(Kc);
	}
	/*
	 * Posterior
	 */
	public static void setPosteriorMap(int S, HashMap<Integer, Double> posterior_KV_Map) {
		posterior_Map.put(S, posterior_KV_Map);
	}

	public static double getPosterior(int S, int Kc) {
		HashMap<Integer, Double> inner_Posterior_map = posterior_Map.get(S);
		return inner_Posterior_map.get(Kc);
	}

	public static void setPosterior(int S, int kc, double value) {
		posterior_Map.get(S).put(kc, value);
	}
	/*
	 * Last
	 */
	public static int getLast(int mStudentId) {
		if (!last_map.containsKey(mStudentId)) {
			return 0;
		}
		return last_map.get(mStudentId);
	}

	public static void setLast(int mStudentId, int questionsCount) {
		last_map.put(mStudentId, questionsCount);
	}

	// LS
	public static void setBestLearnMap(int Kc, Double value) {
		best_Learn_Map.put(Kc, value);
	}
	public static double getBestLearnMap(int Kc) {
		return best_Learn_Map.get(Kc);
	}

	public static void setBestIMMap(int Kc, Double value) {
		best_IM_Map.put(Kc, value);
	}
	public static double getBestIMMap(int Kc) {
		return best_IM_Map.get(Kc);
	}

	public static void setBestSlipMap(int F, Double value) {
		best_Slip_Map.put(F, value);
	}
	
	public static double getBestSlipMap(int F) {
		return best_Slip_Map.get(F);
	}
	public static void setBestGuessMap(int F, Double value) {
		best_Guess_Map.put(F, value);
	}
	public static double getBestGuessMap(int F) {
		return best_Guess_Map.get(F);
	}
	public static void setKc(int index, int questionid) {
		mKC[index] = questionid;
	}
	
	public static void setKcMap(int Kc) {
		HashMap<Integer, Double> map = new HashMap<Integer, Double>();
		kc_initialMastery_Learn_map.put(Kc, map);
	}
	public static void setStudentsList(int[] studentsList) {
		Utils.studentsList = studentsList;
	}
	
	public static int[] getStudentsList() {
		return studentsList;
	}
	/*
	 * Question
	 */
	public static void setQuestionSAMap(int s, HashMap<Integer, Integer> question_AQ_Map) {
		//System.out.println("Set SQA :"+s+" "+question_AQ_Map);
		question_SA_Map.put(s, question_AQ_Map);
	}

	public static int getQuestionSAMap(int S, int A) {
		HashMap<Integer, Integer> innerAQ_map = question_SA_Map.get(S);
		//System.out.println("get SAQ :"+S+" "+A+" "+innerAQ_map.get(A));
		return innerAQ_map.get(A);
	}
}

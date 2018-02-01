package com.asu.calibration.DianBKT;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

import com.asu.calibration.DianBKT.models.seatr_message;
import com.asu.dinabkt.database.SessionFactoryUtil;
import com.asu.dinabkt.database.SimulateDataBase;
import com.asu.dinabkt.utils.GlobalConstants;
import com.asu.dinabkt.utils.Operations;
import com.asu.dinabkt.utils.Utils;;

public class Calibration {
	static int climb = 0;
	static int CorrectAnswers;
	static int InCorrectAnswers;
	static int message_A = 0;
	static int message_Q = 0;
	static int message_answer = 0;
	static int message_F = 0;
	static ArrayList<Integer> message_KCs;
	static int[] TP;
	static int[] FP;
	static int Changers;
	static double BestAUC = 0.0;
	static double AUC = 0.0;

	private static Double climbOneStep() {
		intialization();
		
    	SessionFactory sf_OPE_Class_25 = SessionFactoryUtil.getSessionFactory();
		Session session25 = sf_OPE_Class_25.openSession();
		
		String seatrMsg_hql = "FROM seatr_message";
		Query seatrMSGquery = session25.createQuery(seatrMsg_hql);
		List<seatr_message> qResult = seatrMSGquery.list();
		for(seatr_message m:qResult){
			System.out.println("Student_id:"+m.getStudent_id()+"  Quetion_id:"+m.getQuestion_id()+"  format_id:"+m.getFormat_id()+" correct:"+m.getCorrect()+" timestamp:"+m.getTimestamp());
			processResponseFromOPE(m.getStudent_id(),m.getQuestion_id(),m.getFormat_id(),m.getCorrect(),m.getTimestamp());
		}
    	
		session25.disconnect();
		session25.close();
		
		GaugeProcess();
		return null;
	}

	private static void intialization() {
		CorrectAnswers = 0;
		InCorrectAnswers = 0;
		TP = new int[GlobalConstants.total_Threshold];
		FP = new int[GlobalConstants.total_Threshold];
		for (int T = 0; T < GlobalConstants.total_Threshold; T++) {
			TP[T] = 0;
			FP[T] = 0;
		}
		for (int St = 0; St < GlobalConstants.total_Students; St++) {
			int Student = Utils.getStudent(St);
			for (int K = 0; K < GlobalConstants.total_KCs; K++) {
				int Kc = Utils.getKc(K);
				HashMap<Integer, Double> prior_KV_Map = new HashMap<>();
				prior_KV_Map.put(Kc, Utils.getInitialMasteryMap(Kc));
				Utils.setPrior(Student, prior_KV_Map);
			}
		}		
	}

	private static void processResponseFromOPE(int S, int Q, int F, int Answer, String timeStamp) {



		Utils.setLast(S, Utils.getLast(S) + 1);
		message_A = Utils.getLast(S);
		message_Q = Q;
		message_answer = Answer;
		message_F = F;
		message_KCs = Utils.getQuestionMatrix(Q);
		if (Answer == 1) {
			CorrectAnswers++;
		} else {
			InCorrectAnswers++;
		}

		// update AUC Calculation
		double Applied = 1.0; // Applied is the probability that all the
								// relevant KCs were mastered and thus applied
		for (int list_K = 0; list_K < message_KCs.size(); list_K++) {
			Applied = Operations.multiplyDouble(Utils.getPrior(S, message_KCs.get(list_K)), Applied);
		}
		// Prediction is the probability of a correct answer. Follows from the
		// definition of slip and guess

		double one_minus_applied = Operations.substractDouble((double) 1, Applied);
		double guess_mul_one_minus_applied = Operations.multiplyDouble(Utils.getGuessMap(F), one_minus_applied);
		double one_minus_slip = Operations.substractDouble((double) 1, Utils.getSlipMap(F));
		double one_minus_slip_mul_applied = Operations.multiplyDouble(one_minus_slip, Applied);
		double Prediction = Operations.addDouble(one_minus_slip_mul_applied, guess_mul_one_minus_applied);

		// if the threshold T/NThresholds is below the predicted probability of
		// correctness, then the prediction made by a model using
		// T/Nthresholds as its threshold for correctness is that the answer is
		// correct.
		// we increment just those cells of the TruePos or FalsePos arrays up to
		// and probability of correctness

		for (int T = 0; T <= (GlobalConstants.total_Threshold * Prediction); T++) {
			if (Answer == 1) {
				TP[T]++;
			} else {
				FP[T]++;
			}
		}
		double PosteriorOfPreceding = 0.0;
		for (int list_K = 0; list_K < message_KCs.size(); list_K++) {
			if (message_A > 1) {
				PosteriorOfPreceding = Utils.getPosterior(S, message_KCs.get(list_K)); 
			}
			double Temp = Operations.substractDouble(Utils.getPrior(S, message_KCs.get(list_K)), Applied);
			if (Answer == 1) {
				double Temp_mul_Guess = Operations.multiplyDouble(Temp, Utils.getGuessMap(F));
				double numPart1 = Operations.addDouble(one_minus_slip_mul_applied, Temp_mul_Guess);
				double value1 = Operations.divideDouble(numPart1, Prediction);
				HashMap<Integer, Double> posterior_KV_Map = new HashMap<>();
				posterior_KV_Map.put(message_KCs.get(list_K), value1);
				Utils.setPosterior(S, posterior_KV_Map);
			} else {
				double one_minus_guess = Operations.substractDouble((double) 1, Utils.getGuessMap(F));
				double temp_mul_one_minus_guess = Operations.multiplyDouble(Temp, one_minus_guess);
				double applied_mul_slip = Operations.multiplyDouble(Applied, Utils.getSlipMap(F));
				double numPart2 = Operations.addDouble(applied_mul_slip, temp_mul_one_minus_guess);
				double one_minus_prediction = Operations.substractDouble((double) 1, Prediction);
				double value2 = Operations.divideDouble(numPart2, one_minus_prediction);
				HashMap<Integer, Double> posterior_KV_Map = new HashMap<>();
				posterior_KV_Map.put(message_KCs.get(list_K), value2);
				Utils.setPosterior(S, posterior_KV_Map);
			}
			double one_minus_posterior = Operations.substractDouble((double) 1,
					Utils.getPosterior(S, message_KCs.get(list_K)));
			double learn_mul_one_minus_posterior = Operations.multiplyDouble(Utils.getLearnMap(message_KCs.get(list_K)),
					one_minus_posterior);
			double valuePrior = Operations.addDouble(Utils.getPosterior(S, message_KCs.get(list_K)),
					learn_mul_one_minus_posterior);
			HashMap<Integer, Double> prior_KV_Map = new HashMap<>();
			prior_KV_Map.put(message_KCs.get(list_K), valuePrior);
			Utils.setPrior(S, prior_KV_Map); 
			if (message_A > 1) {
				double one_minus_PosteriorOfPreceding = Operations.substractDouble((double) 1, PosteriorOfPreceding);
				double one_minus_PosteriorOfPreceding_mul_posterior = Operations
						.multiplyDouble(one_minus_PosteriorOfPreceding, Utils.getPosterior(S, message_KCs.get(list_K)));
				double learnCount_plus_one_minus_PosteriorOfPreceding_mul_posterior = Operations.addDouble(
						Utils.getLearnCountMap(message_KCs.get(list_K)), one_minus_PosteriorOfPreceding_mul_posterior);
				Utils.setLearnCountMap(message_KCs.get(list_K),
						learnCount_plus_one_minus_PosteriorOfPreceding_mul_posterior);

				double learnOpportunities_plus_one_minus_PosteriorOfPreceding = Operations.addDouble(
						Utils.getLearnOpportunitiesMap(message_KCs.get(list_K)), one_minus_PosteriorOfPreceding);
				Utils.setLearnOpportunitiesMap(message_KCs.get(list_K),
						learnOpportunities_plus_one_minus_PosteriorOfPreceding);
			}
		}

		if (Answer == 1) {
			double guessCount_plus_one_minus_applied = Operations.addDouble(Utils.getGuessCountMap(F),
					Operations.substractDouble((double) 1, Applied));
			Utils.setGuessCountMap(F, guessCount_plus_one_minus_applied);
		} else {
			double slipCount_plus_applied = Operations.addDouble(Utils.getSlipCountMap(F), Applied);
			Utils.setSlipCountMap(F, slipCount_plus_applied);
		}
		double guessOpportunities_plus_one_minus_applied = Operations.addDouble(Utils.getGuessOpportunitiesMap(F),
				Operations.substractDouble((double) 1, Applied));
		Utils.setGuessCountMap(F, guessOpportunities_plus_one_minus_applied);

		double slipOpportnities_plus_applied = Operations.addDouble(Utils.getSlipOpportunitiesMap(F), Applied);
		Utils.setSlipCountMap(F, slipOpportnities_plus_applied);

		if (message_A == 1) {
			for (int list_K = 0; list_K < message_KCs.size(); list_K++) {
				Utils.setInitialMasteryCountMap(message_KCs.get(list_K),
						Utils.getPosterior(S, message_KCs.get(list_K)));
				Utils.setInitialMasteryOpportunitiesMap(message_KCs.get(list_K), Operations
						.addDouble(Utils.getInitialMasteryOpportunitiesMap(message_KCs.get(list_K)), (double) 1));
			}
		}

	}

	private static void GaugeProcess() {
		double RightTruePosRate = 1.0;
		double RightFalsePosRate = 1.0;
		AUC = 0.0;
		for (int T = 0; T < GlobalConstants.total_Threshold; T++) {
			double LeftTruePosRate = (double) (TP[T] / CorrectAnswers);
			double LeftFalsePosRate = (double) (FP[T] / InCorrectAnswers);
			double Height = Operations.divideDouble(Operations.addDouble(LeftTruePosRate, RightTruePosRate),
					(double) 2);
			double Width = Operations.substractDouble(RightTruePosRate, LeftTruePosRate);
			AUC = Operations.addDouble(AUC, Operations.multiplyDouble(Height, Width));
			RightTruePosRate = LeftTruePosRate;
			RightFalsePosRate = LeftFalsePosRate;
		}
		double LearnDistance = 0.0;
		double InitialMasteryDistance = 0.0;
		double SlipDistance = 0.0;
		double GuessDistance = 0.0;
		Changers = 0;
		double SmallChange = 0.1;
		for (int kc = 0; kc < GlobalConstants.total_KCs; kc++) {
			int K = Utils.getKc(kc);
			double LearnEst_Val = Operations.divideDouble(Utils.getLearnCountMap(K), Utils.getLearnOpportunitiesMap(K));
			Utils.setLearnEstimateMap(K, LearnEst_Val);
			double temp = Math.abs(Operations.substractDouble(Utils.getLearnMap(K), Utils.getLearnEstimateMap(K)));
			LearnDistance = Operations.addDouble(LearnDistance, temp);
			if (Operations.divideDouble(temp, Utils.getLearnMap(K)) > SmallChange) {
				Changers++;
			}
			double IntialMasteryEstimateValue = Operations.divideDouble(Utils.getInitialMasteryCountMap(K),
					Utils.getInitialMasteryOpportunitiesMap(K));
			Utils.setInitialMasteryEstimateMap(K, IntialMasteryEstimateValue);
			temp = Math.abs(
					Operations.substractDouble(Utils.getInitialMasteryMap(K), Utils.getInitialMasteryEstimateMap(K)));
			InitialMasteryDistance = Operations.addDouble(InitialMasteryDistance, temp);
			if (Operations.divideDouble(temp, Utils.getInitialMasteryMap(K)) > SmallChange) {
				Changers++;
			}
		}
		for (int F = 0; F < GlobalConstants.total_Formats; F++) {
			Utils.setSlipEstimateMap(F,
					Operations.divideDouble(Utils.getSlipCountMap(F), Utils.getSlipOpportunitiesMap(F)));
			double temp = Math.abs(Operations.substractDouble(Utils.getSlipMap(F), Utils.getSlipEstimateMap(F)));
			SlipDistance = Operations.addDouble(SlipDistance, temp);
			if (Operations.divideDouble(temp, Utils.getSlipMap(F)) > SmallChange) {
				Changers++;
			}
			Utils.setGuessEstimateMap(F,
					Operations.divideDouble(Utils.getGuessCountMap(F), Utils.getGuessOpportunitiesMap(F)));
			temp = Math.abs(com.asu.dinabkt.utils.Operations.substractDouble(Utils.getGuessMap(F),
					Utils.getGuessEstimateMap(F)));
			GuessDistance = Operations.addDouble(GuessDistance, temp);
			if (Operations.divideDouble(temp, Utils.getGuessMap(F)) > SmallChange) {
				Changers++;
			}

		}

	}

	private static void keepClimbing() {
		while (Changers > 10) {
			for (int kc = 0; kc < GlobalConstants.total_KCs; kc++) {
				int K = Utils.getKc(kc);
				Utils.setLearnMap(K, Utils.getLearnEstimateMap(K));
				Utils.setInitialMasteryMap(K, Utils.getInitialMasteryEstimateMap(K));
			}

			for (int F = 0; F < GlobalConstants.total_Formats; F++) {
				Utils.setSlipMap(F, Utils.getSlipEstimateMap(F));
				Utils.setGuessMap(F, Utils.getGuessEstimateMap(F));
			}
			climbOneStep();
		}
	}

	private static void saveGlobalMaximum() {
		for (int kc = 0; kc < GlobalConstants.total_KCs; kc++) {
			int K = Utils.getKc(kc);
			Utils.setBestLearnMap(K, Utils.getLearnMap(K));
			Utils.setBestIMMap(K, Utils.getInitialMasteryMap(K));
		}
		for (int F = 0; F < GlobalConstants.total_Formats; F++) {
			Utils.setBestSlipMap(F, Utils.getSlipMap(F));
			Utils.setBestGuessMap(F, Utils.getGuessMap(F));
		}
	}



	private static void findLocalMaximum() {
		SimulateDataBase.fillRandomParameters();
		climbOneStep();
		keepClimbing();
	}

	public static void START(){
		SimulateDataBase.setAllStudentsData();
		while (climb < 100) {
			findLocalMaximum();
			if (AUC > BestAUC) {
				saveGlobalMaximum();
				BestAUC = AUC;
			}
		}
	}

}

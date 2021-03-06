package com.asu.calibration.DianBKT;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
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

	private static String AUXExcelFilePath = "C:/Users/lkusnoor/Downloads/LOGS/MAINAUXExcelFile.xls";
	private static FileOutputStream fileOut;
	private static HSSFWorkbook workbook;
	private static HSSFSheet worksheet;
	private static int rowIndex = 0;

	private static Double climbOneStep() {
		intialization();

		SessionFactory sf_OPE_Class_25 = SessionFactoryUtil.getSessionFactory();
		Session session25 = sf_OPE_Class_25.openSession();

		String seatrMsg_hql = "FROM seatr_message";
		Query seatrMSGquery = session25.createQuery(seatrMsg_hql);
		List<seatr_message> qResult = seatrMSGquery.list();
		for (seatr_message m : qResult) {
			// System.out.println("Student_id:"+m.getStudent_id()+"
			// Quetion_id:"+m.getQuestion_id()+" format_id:"+m.getFormat_id()+"
			// correct:"+m.getCorrect()+" timestamp:"+m.getTimestamp());
			processResponseFromOPE(m.getStudent_id(), m.getQuestion_id(), m.getFormat_id(), m.getCorrect(),
					m.getTimestamp());
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
			HashMap<Integer, Double> prior_KV_Map = new HashMap<>();
			for (int K = 0; K < GlobalConstants.total_KCs; K++) {
				int Kc = Utils.getKc(K);
				prior_KV_Map.put(Kc, Utils.getInitialMasteryMap(Kc));
			}
			Utils.setPriorMap(Student, prior_KV_Map);
		}

		for (int St = 0; St < GlobalConstants.total_Students; St++) {
			int Student = Utils.getStudent(St);
			HashMap<Integer, Double> posterior_Map = new HashMap<>();
			for (int K = 0; K < GlobalConstants.total_KCs; K++) {
				int Kc = Utils.getKc(K);
				posterior_Map.put(Kc, 0.0);
			}
			Utils.setPosteriorMap(Student, posterior_Map);
		}
	}

	private static void processResponseFromOPE(int S, int Q, int F, int Answer, String timeStamp) {
		// System.out.println();
		// System.out.println();
		// System.out.println("processResponseFromOPE");

		Utils.setLast(S, Utils.getLast(S) + 1);
		message_A = Utils.getLast(S);
		message_Q = Q;
		message_answer = Answer;
		message_F = F;
		message_KCs = Utils.getQuestionMatrix(Q);
		// System.out.println("QMatrix:"+Utils.getQuestionMatrix(Q)+"
		// message_KCs:"+message_KCs);
		if (Answer == 1) {
			CorrectAnswers++;
		} else {
			InCorrectAnswers++;
		}

		// update AUC Calculation
		double Applied = 1.0; // Applied is the probability that all the
								// relevant KCs were mastered and thus applied
		for (int list_K = 0; list_K < message_KCs.size(); list_K++) {
			// System.out.println("list_K :"+message_KCs.get(list_K)+"
			// getPrior:"+Utils.getPrior(S, message_KCs.get(list_K))+"Applied
			// :"+Applied);
			Applied = Operations.multiplyDouble(Utils.getPrior(S, message_KCs.get(list_K)), Applied);
			// System.out.println("New Applied :"+Applied);

		}
		// Prediction is the probability of a correct answer. Follows from the
		// definition of slip and guess
		// System.out.println("Final Applied :"+Applied);
		double one_minus_applied = Operations.substractDouble((double) 1, Applied);
		double guess_mul_one_minus_applied = Operations.multiplyDouble(Utils.getGuessMap(F), one_minus_applied);
		// System.out.println("slip :"+Utils.getSlipMap(F));
		double one_minus_slip = Operations.substractDouble((double) 1, Utils.getSlipMap(F));
		double one_minus_slip_mul_applied = Operations.multiplyDouble(one_minus_slip, Applied);
		double Prediction = Operations.addDouble(one_minus_slip_mul_applied, guess_mul_one_minus_applied);
		// System.out.println("Prediction :"+Prediction);
		// System.out.println("one_minus_slip" + one_minus_slip + "applied: " +
		// Applied + " one_minus_slip_mul_applied : " +
		// one_minus_slip_mul_applied + "one_minus_applied: " +
		// one_minus_applied + " guessMap " + Utils.getGuessMap(F) + " :
		// guess_mul_one_minus_applied "+ guess_mul_one_minus_applied );

		// if the threshold T/NThresholds is below the predicted probability of
		// correctness, then the prediction made by a model using
		// T/Nthresholds as its threshold for correctness is that the answer is
		// correct.
		// we increment just those cells of the TruePos or FalsePos arrays up to
		// and probability of correctness

		for (int T = 0; T <= (GlobalConstants.total_Threshold * Prediction); T++) {
			// System.out.println("Prediction : " + Prediction + " T : " + T);
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
			// System.out.println("list_K :"+message_KCs.get(list_K)+"
			// getPrior:"+Utils.getPrior(S, message_KCs.get(list_K))+"Applied
			// :"+Applied);
			double Temp = Operations.substractDouble(Utils.getPrior(S, message_KCs.get(list_K)), Applied);
			// System.out.println("Temp :"+Temp);
			double value;
			if (Answer == 1) {
				double Temp_mul_Guess = Operations.multiplyDouble(Temp, Utils.getGuessMap(F));
				double numPart1 = Operations.addDouble(one_minus_slip_mul_applied, Temp_mul_Guess);
				value = Operations.divideDouble(numPart1, Prediction);
				// System.out.println("Prediction ;"+Prediction);
				// HashMap<Integer, Double> posterior_KV_Map = new HashMap<>();
				// posterior_KV_Map.put(message_KCs.get(list_K), value1);
			} else {
				double one_minus_guess = Operations.substractDouble((double) 1, Utils.getGuessMap(F));
				double temp_mul_one_minus_guess = Operations.multiplyDouble(Temp, one_minus_guess);
				double applied_mul_slip = Operations.multiplyDouble(Applied, Utils.getSlipMap(F));
				double numPart2 = Operations.addDouble(applied_mul_slip, temp_mul_one_minus_guess);
				double one_minus_prediction = Operations.substractDouble((double) 1, Prediction);
				value = Operations.divideDouble(numPart2, one_minus_prediction);
				// System.out.println("one_minus_prediction
				// :"+one_minus_prediction);
				// HashMap<Integer, Double> posterior_KV_Map = new HashMap<>();
				// posterior_KV_Map.put(message_KCs.get(list_K), value2);
			}
			// System.out.println("POSTERIOR VALUE :"+value);
			Utils.setPosterior(S, message_KCs.get(list_K), value);
			double one_minus_posterior = Operations.substractDouble((double) 1,
					Utils.getPosterior(S, message_KCs.get(list_K)));
			double learn_mul_one_minus_posterior = Operations.multiplyDouble(Utils.getLearnMap(message_KCs.get(list_K)),
					one_minus_posterior);
			// System.out.println("getPosterior :"+Utils.getPosterior(S,
			// message_KCs.get(list_K)) +" + (learn*(1-p)
			// "+learn_mul_one_minus_posterior);
			double valuePrior = Operations.addDouble(Utils.getPosterior(S, message_KCs.get(list_K)),
					learn_mul_one_minus_posterior);
			// HashMap<Integer, Double> prior_KV_Map = new HashMap<>();
			// prior_KV_Map.put(message_KCs.get(list_K), valuePrior);
			Utils.setPrior(S, message_KCs.get(list_K), valuePrior);
			// System.out.println("PRIOR VALUE :"+valuePrior);
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
		Utils.setGuessOpportunitiesMap(F, guessOpportunities_plus_one_minus_applied);

		double slipOpportnities_plus_applied = Operations.addDouble(Utils.getSlipOpportunitiesMap(F), Applied);
		Utils.setSlipOpportunitiesMap(F, slipOpportnities_plus_applied);

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
			// System.out.println(" TP["+T+"] "+TP[T]+" FP["+T+"] "+FP[T]);
			// System.out.println("CorrectAnswers :"+CorrectAnswers+ "
			// InCorrectAnswers:"+InCorrectAnswers);
			double LeftTruePosRate = ((double) TP[T] / (double) CorrectAnswers);
			double LeftFalsePosRate = ((double) FP[T] / (double) InCorrectAnswers);
			// System.out.println("LeftTruePosRate :"+LeftTruePosRate+ "
			// LeftFalsePosRate:"+LeftFalsePosRate);
			double Height = Operations.divideDouble(Operations.addDouble(LeftTruePosRate, RightTruePosRate),
					(double) 2);
			double Width = Operations.substractDouble(RightFalsePosRate, LeftFalsePosRate);
			// System.out.println("Height :"+Height+ " Width:"+Width);
			AUC = Operations.addDouble(AUC, Operations.multiplyDouble(Height, Width));
			// System.out.println("AUC "+AUC);
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
			temp = Math.abs(Operations.substractDouble(Utils.getGuessMap(F), Utils.getGuessEstimateMap(F)));
			GuessDistance = Operations.addDouble(GuessDistance, temp);
			if (Operations.divideDouble(temp, Utils.getGuessMap(F)) > SmallChange) {
				Changers++;
			}

		}

	}

	private static void keepClimbing() {
		// System.out.println("keepClimbing() ");
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
		climb++;
	}

	private static void saveGlobalMaximum() {
		// System.out.println("saveGlobalMaximum");
		for (int kc = 0; kc < GlobalConstants.total_KCs; kc++) {
			int K = Utils.getKc(kc);
			Utils.setBestLearnMap(K, Utils.getLearnMap(K));
			Utils.setBestIMMap(K, Utils.getInitialMasteryMap(K));
			System.out.println("Learn(" + K + ") :" + Utils.getBestLearnMap(K));
			System.out.println("IM(" + K + ") :" + Utils.getBestIMMap(K));
		}
		for (int F = 0; F < GlobalConstants.total_Formats; F++) {
			Utils.setBestSlipMap(F, Utils.getSlipMap(F));
			Utils.setBestGuessMap(F, Utils.getGuessMap(F));
			System.out.println("Slip(" + F + ") :" + Utils.getBestSlipMap(F));
			System.out.println("Guess(" + F + ") :" + Utils.getBestGuessMap(F));
		}
	}


	private static void secretVsBest() {
		HSSFRow row1 = worksheet.createRow(rowIndex++);

		// IM
		HSSFCell cellA1 = row1.createCell((short) 0);
		cellA1.setCellValue("Sim_IM");
		HSSFCellStyle cellStyle = workbook.createCellStyle();
		cellStyle.setFillForegroundColor(HSSFColor.GOLD.index);
		cellStyle.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
		cellA1.setCellStyle(cellStyle);

		HSSFCell cellB1 = row1.createCell((short) 1);
		cellB1.setCellValue("Best_IM");
		cellStyle = workbook.createCellStyle();
		cellStyle.setFillForegroundColor(HSSFColor.LIGHT_CORNFLOWER_BLUE.index);
		cellStyle.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
		cellB1.setCellStyle(cellStyle);

		for (int i = 0; i < GlobalConstants.total_KCs; i++) {
			int K = Utils.getKc(i);
			HSSFRow row = worksheet.createRow(rowIndex++);
			HSSFCell simCell = row.createCell(0);
			simCell.setCellValue(SimulateDataBase.fillrandom_initalMastery[K]);
			HSSFCell bestCell = row.createCell(1);
			bestCell.setCellValue(Utils.getBestIMMap(K));
		}
		rowIndex = rowIndex + 2;

		// Learn
		HSSFRow rowL = worksheet.createRow(rowIndex++);
		HSSFCell simCellL = rowL.createCell(0);
		simCellL.setCellValue("sim_Learn");
		HSSFCell bestCellL = rowL.createCell(1);
		bestCellL.setCellValue("best_learn");

		for (int i = 0; i < GlobalConstants.total_KCs; i++) {
			int K = Utils.getKc(i);
			HSSFRow row = worksheet.createRow(rowIndex++);
			HSSFCell simCell = row.createCell(0);
			simCell.setCellValue(SimulateDataBase.fillrandom_Learn[K]);
			HSSFCell bestCell = row.createCell(1);
			bestCell.setCellValue(Utils.getBestLearnMap(K));
		}
		rowIndex = rowIndex + 2;

		// Slip
		HSSFRow rowS = worksheet.createRow(rowIndex++);
		HSSFCell simCellS = rowS.createCell(0);
		simCellS.setCellValue("sim_Slip");
		HSSFCell bestCellS = rowL.createCell(1);
		bestCellS.setCellValue("best_Slip");

		for (int i = 0; i < GlobalConstants.total_Formats; i++) {
			int F = i;
			HSSFRow row = worksheet.createRow(rowIndex++);
			HSSFCell simCell = row.createCell(0);
			simCell.setCellValue(SimulateDataBase.fillrandom_slip[F]);
			HSSFCell bestCell = row.createCell(1);
			bestCell.setCellValue(Utils.getBestSlipMap(F));
		}
		rowIndex = rowIndex + 2;

		// Guess
		HSSFRow rowG = worksheet.createRow(rowIndex++);
		HSSFCell simCellG = rowG.createCell(0);
		simCellG.setCellValue("sim_Guess");
		HSSFCell bestCellG = rowL.createCell(1);
		bestCellG.setCellValue("best_Guess");

		for (int i = 0; i < GlobalConstants.total_Formats; i++) {
			int F = i;
			HSSFRow row = worksheet.createRow(rowIndex++);
			HSSFCell simCell = row.createCell(0);
			simCell.setCellValue(SimulateDataBase.fillrandom_guess[F]);
			HSSFCell bestCell = row.createCell(1);
			bestCell.setCellValue(Utils.getBestGuessMap(F));
		}
		rowIndex = rowIndex + 2;

	}

	private static void findLocalMaximum() {
		SimulateDataBase.fillRandomParameters();
		climbOneStep();
		keepClimbing();
	}

	public static void START() throws FileNotFoundException {
		SimulateDataBase.setAllStudentsData();
		PrintStream o = new PrintStream(new File("C:/Users/lkusnoor/Downloads/LOGS/CALIB3.txt"));
		System.setOut(o);
		fileOut = new FileOutputStream(AUXExcelFilePath);
		workbook = new HSSFWorkbook();
		worksheet = workbook.createSheet("AUC");
		while (climb < 100) {
			System.out.println("CLIMB:" + climb);
			findLocalMaximum();
			System.out.println("AUC:" + AUC + " BestAUC" + BestAUC);
			if (AUC > BestAUC) {
				saveGlobalMaximum();
				BestAUC = AUC;
			}
		}

		secretVsBest();
	}

}

package application;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.Videoio;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import utilities.Utilities;

public class Controller {
	
	@FXML
	private ImageView imageView; // the image display window in the GUI
	@FXML
	private Button SelectVideoBtn;
	@FXML
	private Button STICopyColBtn;
	@FXML
	private Button STICopyRowBtn;
	@FXML
	private Button STIHistDiffColBtn;
	@FXML
	private Button STIHistDiffRowBtn;
	
	private String fileDir;
	private VideoCapture capture;
	private ScheduledExecutorService timer;
	
	private ArrayList<Mat> framesList = new ArrayList<>();
	private int frameCount;
	
	private void showImage(Mat frame) {
		assert !frame.empty();
		Image img = Utilities.mat2Image(frame);
		imageView.setImage(img);
	}
	
	@FXML
	protected void openFile(ActionEvent event) throws InterruptedException {
		FileChooser fileDialog = new FileChooser();
		File fileName = fileDialog.showOpenDialog(null);
		if (fileName != null) {
			fileDir = fileName.getAbsolutePath();
			framesList = new ArrayList<>();
			getFrames();
			
			Alert alert = new Alert(Alert.AlertType.INFORMATION);
			alert.setHeaderText("Please wait for " + fileName + " to load");
			alert.getDialogPane().setExpandableContent(new ScrollPane(new TextArea("Success!")));
			alert.showAndWait();
		}
		else {
			Exception e = new Exception("No Video Exception");
			StringWriter sw = new StringWriter();
			e.printStackTrace(new PrintWriter(sw));

			Alert alert = new Alert(Alert.AlertType.ERROR);
			alert.setHeaderText("No video was selected");
			alert.getDialogPane().setExpandableContent(new ScrollPane(new TextArea(sw.toString())));
			alert.showAndWait();
		}
	}
	
	/**
	 STI by copying pixel columns
	 */
	@FXML
	public void STICopyCol(ActionEvent event) throws InterruptedException{
		if (fileDir == null) {
			Exception e = new Exception("No Video Exception");
			StringWriter sw = new StringWriter();
			e.printStackTrace(new PrintWriter(sw));

			Alert alert = new Alert(Alert.AlertType.ERROR);
			alert.setHeaderText("Please select a video");
			alert.getDialogPane().setExpandableContent(new ScrollPane(new TextArea(sw.toString())));
			alert.showAndWait();
			return;
		}
		
		capture = new VideoCapture(fileDir);
		if (capture.isOpened()) {
			frameCount = 0;
			double fps = capture.get(Videoio.CAP_PROP_FPS);
			ArrayList<Mat> columns = new ArrayList<Mat>();
			Runnable frameGrabber = new Runnable() {
		    	@Override
			    public void run() {
		    		Mat frame = new Mat();
			        if (capture.read(frame)) {
			        	SelectVideoBtn.setDisable(true);
			    		STICopyColBtn.setDisable(true);
			    		STICopyRowBtn.setDisable(true);
			    		STIHistDiffColBtn.setDisable(true);
			    		STIHistDiffRowBtn.setDisable(true);
			    		
			        	// Get middle column of each frame
			        	double currentFrameNumber = capture.get(Videoio.CAP_PROP_POS_FRAMES);
			        	double totalFrameCount = capture.get(Videoio.CAP_PROP_FRAME_COUNT);
			        	if(currentFrameNumber == totalFrameCount) {
			        		capture.release();
			        	}
			        	Mat resizedImage = new Mat();
			        	Imgproc.resize(frame, resizedImage, new Size(32, 32));
			        	columns.add(resizedImage.col(16));
			        }
			        else {
			        	// terminate the timer if it is running
						if (timer != null && !timer.isShutdown()) {
							timer.shutdown();
							try {
								timer.awaitTermination(Math.round(1000/fps), TimeUnit.MILLISECONDS);
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
						}
						Mat viewFrame = new Mat();
				    	viewFrame.create(32, frameCount, CvType.CV_8UC3);
				    	//Place each column into a frame sequentially
				    	for (int i = 0; i < frameCount; i++) {
				    		columns.get(i).copyTo(viewFrame.col(i));
						}
				    	showImage(viewFrame);
				    	SelectVideoBtn.setDisable(false);
			    		STICopyColBtn.setDisable(false);
			    		STICopyRowBtn.setDisable(false);
			    		STIHistDiffColBtn.setDisable(false);
			    		STIHistDiffRowBtn.setDisable(false);
			        }
			        frameCount++;
		    	}
			};
		    timer = Executors.newSingleThreadScheduledExecutor();
		    timer.scheduleAtFixedRate(frameGrabber, 0, Math.round(1000/fps), TimeUnit.MILLISECONDS);
		}
	}
	
	/**
	 STI by copying pixel rows
	 */
	@FXML
	public void STICopyRow(ActionEvent event) throws InterruptedException{
		if (fileDir == null) {
			Exception e = new Exception("No Video Exception");
			StringWriter sw = new StringWriter();
			e.printStackTrace(new PrintWriter(sw));

			Alert alert = new Alert(Alert.AlertType.ERROR);
			alert.setHeaderText("Please select a video");
			alert.getDialogPane().setExpandableContent(new ScrollPane(new TextArea(sw.toString())));
			alert.showAndWait();
			return;
		}
		
		capture = new VideoCapture(fileDir);
		if (capture.isOpened()) {
			frameCount = 0;
			double fps = capture.get(Videoio.CAP_PROP_FPS);
			ArrayList<Mat> rows = new ArrayList<Mat>();
			Runnable frameGrabber = new Runnable() {
		    	@Override
			    public void run() {
		    		Mat frame = new Mat();
			        if (capture.read(frame)) {
			        	SelectVideoBtn.setDisable(true);
			    		STICopyColBtn.setDisable(true);
			    		STICopyRowBtn.setDisable(true);
			    		STIHistDiffColBtn.setDisable(true);
			    		STIHistDiffRowBtn.setDisable(true);
			    		
			        	// Get middle row of each frame
			        	double currentFrameNumber = capture.get(Videoio.CAP_PROP_POS_FRAMES);
			        	double totalFrameCount = capture.get(Videoio.CAP_PROP_FRAME_COUNT);
			        	if(currentFrameNumber == totalFrameCount) {
			        		capture.release();
			        	}
			        	Mat resizedImage = new Mat();
			        	Imgproc.resize(frame, resizedImage, new Size(32, 32));
			        	rows.add(resizedImage.row(16));
			        }
			        else {
			        	if (timer != null && !timer.isShutdown()) {
		        			timer.shutdown();
							try {
								timer.awaitTermination(Math.round(1000/fps), TimeUnit.MILLISECONDS);
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
						}
			        	Mat viewFrame = new Mat();
				    	viewFrame.create(frameCount, 32, CvType.CV_8UC3);
				    	//Place each row into a frame sequentially
				    	for (int i = 0; i < frameCount; i++) {
				    		rows.get(i).copyTo(viewFrame.row(i));
						}
				    	showImage(viewFrame);
				    	
				    	SelectVideoBtn.setDisable(false);
			    		STICopyColBtn.setDisable(false);
			    		STICopyRowBtn.setDisable(false);
			    		STIHistDiffColBtn.setDisable(false);
			    		STIHistDiffRowBtn.setDisable(false);
			        }
			        frameCount++;
		    	}
			};
		    timer = Executors.newSingleThreadScheduledExecutor();
		    timer.scheduleAtFixedRate(frameGrabber, 0, Math.round(1000/fps), TimeUnit.MILLISECONDS);
		}
	}
	
	/**
	 STI by histogram differences for columns
	 */
	@FXML
	public void STIHistDiffCol(ActionEvent event) throws InterruptedException{
		if (fileDir == null) {
			Exception e = new Exception("No Video Exception");
			StringWriter sw = new StringWriter();
			e.printStackTrace(new PrintWriter(sw));

			Alert alert = new Alert(Alert.AlertType.ERROR);
			alert.setHeaderText("Please select a video");
			alert.getDialogPane().setExpandableContent(new ScrollPane(new TextArea(sw.toString())));
			alert.showAndWait();
			return;
		}
		ArrayList<ArrayList<Double>> HistDiffsTotal = new ArrayList<ArrayList<Double>>();
		if (framesList.size() == 0) {
			getFrames();
		}
		for(int colIndex = 0; colIndex < 32; colIndex++) {
			ArrayList<Mat> colEachFrame = new ArrayList<>();
			
			// Convert selected column of each frame to chromaticity
			for(int i = 0; i < framesList.size(); i++) {
				Mat resizedImage = new Mat();
	        	Imgproc.resize(framesList.get(i), resizedImage, new Size(32, 32));
				colEachFrame.add(resizedImage.col(colIndex));
			}
			ArrayList<Mat> chroma = convertChromaCols(colEachFrame);
			
			// Get histograms for selected column of each frame 
			ArrayList<Double[][]> histograms = new ArrayList<>();
			for(int i = 0; i < chroma.size(); i++) {
				// Note we use 1 + logbase2(32) = 6 bins
				Double[][] currentBins = new Double[6][6];
				for(int a = 0; a < 6; a++) {
					for(int b = 0; b < 6; b++) {
						currentBins[a][b] = 0.0;
					}
				}
				// Get 6x6 histogram for current column
				for(int row = 0; row < chroma.get(i).rows(); row++) {
					double[] intensity = chroma.get(i).get(row, 0);
					int R = (int)Math.floor((double)intensity[0] / 255 * 6);
					int G = (int)Math.floor((double)intensity[1] / 255 * 6);
					if(R >= 6) {
						R = 5;
					}
					if(G >= 6) {
						G = 5;
					}
					currentBins[R][G] += 1;
				}
				histograms.add(currentBins);
			}
			
			// Get histogram difference of selected column
			ArrayList<Double> HistDiffs = new ArrayList<>();
			for(int i = 0; i < histograms.size() - 1; i++) {
				Double[][] H_previous = normalize(histograms.get(i));
				Double[][] H_this = normalize(histograms.get(i+1));
				double total = 0;
				for(int row = 0; row < 6; row++) {
					for(int col = 0; col < 6; col++) {
						total += Math.min(H_this[row][col], H_previous[row][col]);
					}
				}
				HistDiffs.add(total);
			}
			HistDiffsTotal.add(HistDiffs);
		}
		
		// Create STI from the list of histogram differences for each frame
		Mat viewFrame = new Mat();
		viewFrame.create(HistDiffsTotal.size(), HistDiffsTotal.get(0).size(), CvType.CV_8UC3);
		for(int i = 0; i < HistDiffsTotal.size(); i++) {
			for(int j = 0; j < HistDiffsTotal.get(i).size(); j++) {
				double[] data = {HistDiffsTotal.get(i).get(j) *255 , HistDiffsTotal.get(i).get(j) *255 , HistDiffsTotal.get(i).get(j)*255};
				viewFrame.put(i, j, data);
			}
		}
		showImage(viewFrame);
	}
	
	/**
	 STI by histogram differences for rows
	 */
	@FXML
	public void STIHistDiffRow(ActionEvent event) throws InterruptedException{ // when click on "STI by histogram difference(row)"
		if (fileDir == null) {
			Exception e = new Exception("No Video Exception");
			StringWriter sw = new StringWriter();
			e.printStackTrace(new PrintWriter(sw));

			Alert alert = new Alert(Alert.AlertType.ERROR);
			alert.setHeaderText("Please select a video");
			alert.getDialogPane().setExpandableContent(new ScrollPane(new TextArea(sw.toString())));
			alert.showAndWait();
			return;
		}
		
		ArrayList<ArrayList<Double>> HistDiffsTotal = new ArrayList<ArrayList<Double>>();
		if (framesList.size() == 0) {
			getFrames();
		}
		for(int rowIndex = 0; rowIndex < 32; rowIndex++) {
			ArrayList<Mat> rowEachFrame = new ArrayList<>();
			
			// Convert selected row of each frame to chromaticity
			for(int i = 0; i < framesList.size(); i++) {
				Mat resizedImage = new Mat();
	        	Imgproc.resize(framesList.get(i), resizedImage, new Size(32, 32));
				rowEachFrame.add(resizedImage.row(rowIndex));
			}
			ArrayList<Mat> chroma = convertChromaRows(rowEachFrame);
			
			// Get histograms for selected row of each frame 
			ArrayList<Double[][]> histograms = new ArrayList<>();
			for(int i = 0; i < chroma.size(); i++) {
				// Note we use 1 + logbase2(32) = 6 bins
				Double[][] currentBins = new Double[6][6];
				for(int a = 0; a < 6; a++) {
					for(int b = 0; b < 6; b++) {
						currentBins[a][b] = 0.0;
					}
				}
				// Get 6x6 histogram for current column
				for(int col = 0; col < chroma.get(i).cols(); col++) {
					double[] intensity = chroma.get(i).get(0, col);
					int R = (int)Math.floor((double)intensity[0] / 255 * 6);
					int G = (int)Math.floor((double)intensity[1] / 255 * 6);
					if(R >= 6) {
						R = 5;
					}
					if(G >= 6) {
						G = 5;
					}
					currentBins[R][G] += 1;
				}
				histograms.add(currentBins);
			}
			
			// Get histogram difference of selected row
			ArrayList<Double> HistDiffs = new ArrayList<>();
			for(int i = 0; i < histograms.size() - 1; i++) {
				Double[][] H_previous = normalize(histograms.get(i));
				Double[][] H_this = normalize(histograms.get(i+1));
				double total = 0;
				for(int row = 0; row < 6; row++) {
					for(int col = 0; col < 6; col++) {
						total += Math.min(H_this[row][col], H_previous[row][col]); // apply histogram intersection
					}
				}
				HistDiffs.add(total);
			}
			HistDiffsTotal.add(HistDiffs);
		}
		
		// Create STI from the list of histogram differences for each frame
		Mat viewFrame = new Mat();
		viewFrame.create(HistDiffsTotal.size(), HistDiffsTotal.get(0).size(), CvType.CV_8UC3);
		for(int i = 0; i < HistDiffsTotal.size(); i++) {
			for(int j = 0; j < HistDiffsTotal.get(i).size(); j++) {
				double[] data = {HistDiffsTotal.get(i).get(j) *255 , HistDiffsTotal.get(i).get(j) *255 , HistDiffsTotal.get(i).get(j)*255};
				viewFrame.put(i, j, data);
			}
		}
		showImage(viewFrame);
	}
	
	/**
	 Function to get all frames and store them in a array list
	 */
	private void getFrames() {
		capture = new VideoCapture(fileDir);
		frameCount = 0;
		if (capture.isOpened()) {
			  if (capture != null && capture.isOpened()) {
				    double fps = capture.get(Videoio.CAP_PROP_FPS);
				    Runnable frameGrabber = new Runnable() {
				    	@Override
					    public void run() {
				    		SelectVideoBtn.setDisable(true);
				    		STICopyColBtn.setDisable(true);
				    		STICopyRowBtn.setDisable(true);
				    		STIHistDiffColBtn.setDisable(true);
				    		STIHistDiffRowBtn.setDisable(true);
				    		
				    		Mat frame = new Mat();
					        if (capture.read(frame)) {
					        	double currentFrameNumber = capture.get(Videoio.CAP_PROP_POS_FRAMES);
					        	double totalFrameCount = capture.get(Videoio.CAP_PROP_FRAME_COUNT);
					        	if(currentFrameNumber == totalFrameCount) {
					        		capture.release();
					        	}
					        	framesList.add(frame);
					        }
					        else {
					        	if (timer != null && !timer.isShutdown()) {
							    	timer.shutdown();
							    	try {
										timer.awaitTermination(Math.round(1000/fps), TimeUnit.MILLISECONDS);
									} catch (InterruptedException e) {
										e.printStackTrace();
									}
							    	
							    	SelectVideoBtn.setDisable(false);
						    		STICopyColBtn.setDisable(false);
						    		STICopyRowBtn.setDisable(false);
						    		STIHistDiffColBtn.setDisable(false);
						    		STIHistDiffRowBtn.setDisable(false);
							    	System.out.println("Done Frame Grab");
							    }
					        }
					        frameCount++;
					    }
				    };
				    timer = Executors.newSingleThreadScheduledExecutor();
				    timer.scheduleAtFixedRate(frameGrabber, 0, Math.round(1000/fps), TimeUnit.MILLISECONDS);
			  }
		}
	}
	
	/**
	 Function to convert RGB of a given list of columns to the corresponding chromaticity
	 */
	private ArrayList<Mat> convertChromaCols(ArrayList<Mat> columns) {
		ArrayList<Mat> chromaConverted = new ArrayList<>();
		for(int i = 0; i < columns.size(); i++) {
			Mat temp = new Mat();
			temp.create(columns.get(i).rows(), 1, CvType.CV_8UC3);
			for(int row = 0; row < columns.get(i).rows(); row++) {
				double convertedR, convertedG;
				double[] intensity = columns.get(i).get(row, 0);
				double R = intensity[0];
				double G = intensity[1];
				double B = intensity[2];
				double total = R + G + B;
				if(total == 0) {
					convertedR = 0;
					convertedG = 0;
				}
				else {
					convertedR = R / total;
					convertedG = G / total;
				}
				double[] new_intensity = {convertedR * 255, convertedG * 255, B};
				temp.put(row, 0, new_intensity);
			}
			chromaConverted.add(temp);
		}
		return chromaConverted;
	}
	
	/**
	 Function to convert RGB of a given list of rows to the corresponding chromaticity
	 */
	private ArrayList<Mat> convertChromaRows(ArrayList<Mat> rows) {
		ArrayList<Mat> chromaConverted = new ArrayList<>();
		for(int i = 0; i < rows.size(); i++) {
			Mat temp = new Mat();
			temp.create(1, rows.get(i).cols(), CvType.CV_8UC3);
			for(int col = 0; col < rows.get(i).cols(); col++) {
				double[] intensity = rows.get(i).get(0, col);
				double R = intensity[0];
				double G = intensity[1];
				double B = intensity[2];
				double total = R + G + B;
				double chroma_R;
				double chroma_G;
				if(total == 0) {
					chroma_R = 0;
					chroma_G = 0;
				}
				else {
					chroma_R = R / total;
					chroma_G = G / total;
				}
				double[] new_intensity = {chroma_R * 255, chroma_G * 255, B};
				temp.put(0, col, new_intensity);
			}
			chromaConverted.add(temp);
		}
		return chromaConverted;
	}
	
	/**
	 Function to normalize the values in the histogram
	 */
	private Double[][] normalize(Double[][] histVal){
		double sum = 0;
		Double[][] normalized = histVal;
		for(int i = 0; i < 6; i++) {
			for(int j = 0; j < 6; j++) {
				sum += histVal[i][j];
			}
		}
		for(int i = 0; i < 6; i++) {
			for(int j = 0; j < 6; j++) {
				normalized[i][j] = histVal[i][j] / sum;
			}
		}
		return normalized;
	}
}



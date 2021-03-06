/*
 * This file is part of AllSummarizer project
 * 
 * Copyright 2015 Abdelkrime Aries <kariminfo0@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package kariminf.as.confs.multiling;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import kariminf.as.preProcess.DynamicPreProcessor;
import kariminf.as.preProcess.StaticPreProcessor;
import kariminf.as.process.Scorer;
import kariminf.as.process.tcc.BayesScoreHandler;
import kariminf.as.process.tcc.Clusterer;
import kariminf.as.process.tcc.Feature;
import kariminf.as.process.tcc.NaiveCluster;
import kariminf.as.tools.Data;
import kariminf.ktoolja.file.FileManager;
import kariminf.as.tools.Tools;



public class MSS {
	
	private List<Feature> features = new ArrayList<Feature>();
	private String lang = "en";
	
	private Data data;
	private Clusterer clusterer;
	private StaticPreProcessor preprocessor;
	
	private boolean nobrackets = false;
	
	public MSS (String lang){
		this.lang = (lang.length()==2)?lang:"en";
		clusterer = null;
		data = new Data();
		preprocessor = new StaticPreProcessor(this.lang);
		preprocessor.setData(data);
	}
	
	/**
	 * This function is used when we use the test dataset 2015
	 * In training dataset there are brackets to tell that a sentence is a title and 
	 * not part of the text body.
	 * In test dataset, there is no brackets, so the only think to detect them is 
	 * to verify the end of sentence and the length
	 */
	public void noBrackets(){
		nobrackets = true;
	}

	public void preprocess(File file){
		String text = readFile(file);
		preprocessor.preProcess(text);
		clusterer = null;
	}
	
	public List<Double> getSimilarity(){
		return data.getSimList();
	}
	
	public int sentNum (){
		return data.getSentNumber();
	}
	
	public double getTermDistribution(){
		List<List<String>> sentWords = data.getSentWords();
		return Tools.termsDistribution(sentWords);
	}
	
	public List<Integer> getTermsStats(){
		List<List<String>> sentWords = data.getSentWords();
		return Tools.termsStats(sentWords);
	}
	
	public int getMaxFrequency(){
		List<List<String>> sentWords = data.getSentWords();
		HashMap<String, Integer> termsFreqs = Tools.getDocWordsFreq(sentWords);
		
		int maxFreq = 0;

		for (int freq : termsFreqs.values()){
			maxFreq = Math.max(maxFreq, freq);
		}
		return maxFreq;
	}
	
	public void cluster(double threshold){
		clusterer = new NaiveCluster(threshold);
	}
	
	
	/**
	 * Use a feature for scoring
	 * @param feature
	 */
	public void addFeature(Feature feature){
		if (!features.contains(feature))
			features.add(feature);
	}
	
	public String summarize(int summarySize, double simTH) throws Exception{
		
		if(features.size() <1 ) throw new Exception("add at least one feature");
		if (clusterer == null ) throw new Exception("Use cluster before summarize");
		
		BayesScoreHandler bc = new BayesScoreHandler(clusterer);
		Scorer scorer = Scorer.create(bc);
		
		for (Feature feature: features)
			bc.addFeature(feature);
		
		scorer.setData(data);
		scorer.scoreUnits();
		
		return getSummary(data, scorer.getOrdered(), summarySize, simTH);
	}
	
	/**
	 * Clear the used features if there is any
	 */
	public void clearFeatures(){
		features.clear();
	}


	public String readFile(File file){
		StringBuffer content = new StringBuffer();

		try {
			BufferedReader in = new BufferedReader(new FileReader(file));
			String line;
			while ( (line = in.readLine()) != null) {
				if (line.startsWith("[") || line.length() < 1)
					continue;
				if(nobrackets && line.length() < 50)
					if (! line.matches(".*[\\.\\!\\?؟。]$"))
						continue;
				content.append(line + " ");
			}
			in.close();
		}
		catch (IOException ex) {
			ex.printStackTrace();
			return "";
		}

		return content.toString();
	}
	
	
	private static String getSummary(Data data, List<Integer> order, int summarySize, double simTH){

		List<String> sentences = data.getSentences();
		List<List<String>> sentWords = data.getSentWords();

		/*
		String summary = sentences.get(order.get(0)) + "\n";
		int index = 1;

		while (index < order.size() && summary.length() < summarySize) {
			
			int sentPos = order.get(index);
			
			List<String> prevWords = sentWords.get(order.get(index-1));
			List<String> actWords = sentWords.get(index);

			if (Tools.similar(prevWords, actWords, simTH))
				summary += sentences.get(sentPos) + "\n";
			index++;
		};*/
		
		String summary = "";
		int numChars = 0;
		int numOrder = 0;

		while(true){

			if (numOrder >=  order.size())
				break;

			int index = order.get(numOrder);

			if (numOrder > 0){
				List<String> prevWords = sentWords.get(order.get(numOrder-1));
				List<String> actWords = sentWords.get(index);
				if (Tools.similar(prevWords, actWords, simTH)){
					numOrder ++;
					if (numOrder < order.size())
						index = order.get(numOrder);
				}
			}

			numChars += sentences.get(index).length();


			if (numChars > summarySize)
				break;
			
			summary += sentences.get(index) + "\n";

			numOrder ++;
		}
		
		if (summary.length() < 1)
			summary = sentences.get(order.get(0));
			

		return summary;
	}
	
	
	public static void main(String[] args) {
		
		MSS mss = new MSS("en");
		mss.noBrackets();
		File file = 
				new File("/home/kariminf/Data/ATS/multilingMss2015Testing/body/text/en/0f8047e125d506e389b7f2d2f2d7f289_body.txt");
		String fstr = mss.readFile(file);
		
		try {

			FileManager.saveFile("/home/kariminf/kkkkkk", fstr);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}


}

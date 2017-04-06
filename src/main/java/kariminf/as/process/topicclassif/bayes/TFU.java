/*
 * This file is part of AllSummarizer project
 * 
 * Copyright 2013-2015 Abdelkrime Aries <kariminfo0@gmail.com>
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

package kariminf.as.process.topicclassif.bayes;

import java.util.HashMap;
import java.util.List;

/**
 * TFU: term frequency (Unigrams).
 * 
 * This feature is used to score a sentence using the frequencies of 
 * terms in a text.
 * 
 * @author Abdelkrime Aries
 * 
 */
public class TFU implements Feature {

	private HashMap<Integer, HashMap<String, Integer>> classWordsFreq = 
			new HashMap<Integer, HashMap<String, Integer>>();

	
	@Override
	public String getTrainParam() {
		return "classes,sentWords";
	}

	
	@Override
	public void train(List<Object> trainParam) {

		@SuppressWarnings("unchecked")
		HashMap<Integer, List<Integer>> classes = 
				(HashMap<Integer, List<Integer>>) trainParam.get(0);
		
		@SuppressWarnings("unchecked")
		List<List<String>> sentWords = (List<List<String>>) trainParam.get(1);

		// Reset the classWordsFreq, when training this feature another time
		classWordsFreq = new HashMap<Integer, HashMap<String, Integer>>();

		//Begin: calculate unigrams' frequencies in each class of topics
		for (int classID = 0; classID < classes.size(); classID++) {
			
			HashMap<String, Integer> classIWordsFreq = new HashMap<String, Integer>();
			
			for (int sentID : classes.get(classID))
				for (String word : sentWords.get(sentID)) {
					int value = (classIWordsFreq.containsKey(word)) ? classIWordsFreq
							.get(word) + 1 : 1;
					classIWordsFreq.put(word, value);
				}
			
			classWordsFreq.put(classID, classIWordsFreq);
		}
		//End: calculate unigrams' frequencies in each class of topics
	}

	
	@Override
	public String getScoreParam() {
		return "sentWords";
	}
	

	@Override
	public Double score(int classID, List<Object> scoreParam) {
		
		Double score = 0.0;

		@SuppressWarnings("unchecked")
		List<String> sentence = (List<String>) scoreParam.get(0);

		for (String word : sentence)
			if (classWordsFreq.get(classID).containsKey(word))
				score += classWordsFreq.get(classID).get(word);

		return score;
	}

}

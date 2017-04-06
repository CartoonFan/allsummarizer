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

package kariminf.as.process;

import java.util.ArrayList;
import java.util.List;

import kariminf.as.process.topicclassif.BayesClassifier;
import kariminf.as.process.topicclassif.bayes.Feature;
import kariminf.as.tools.Data;


/**
 * This class is used to summarize a text or a set of texts.
 * 
 * @author Abdelkrime Aries
 *
 */
public class Scorer {

	private List<Integer> orderNumSent = new ArrayList<Integer>();
	//private List<Feature> features = new ArrayList<Feature>();
	private ScoreHandler scoreHandler;
	
	
	public Scorer(ScoreHandler scoreHandler){
		this.scoreHandler = scoreHandler;
	}


	/**
	 * Summarizes a text or more using their preprocessed data
	 * 
	 * @param data data container
	 */
	public void summarize(Data data) 
	{
		orderNumSent = scoreHandler.reorderUnits(data);
	}


	/**
	 * Gets the first important sentences in the text.
	 * 
	 * @param nbrSent number of sentences to be extracted
	 * @return a list of first important sentences
	 */
	public List<Integer> getSentNumber(int nbrSent) {

		if (nbrSent < 1)
			return null;

		return orderNumSent.subList(0, nbrSent);
	}


	/**
	 * Gets a percentage of the first important sentences
	 * 
	 * @param percent percentage to be extracted
	 * @return a list of first important sentences
	 */
	public List<Integer> getSentPercent(int percent) {

		if (percent < 1)
			return null;
		int nbrSent = (int) Math.ceil(percent * orderNumSent.size() / 100);

		if (nbrSent < 1)
			nbrSent = 1;

		return orderNumSent.subList(0, nbrSent - 1);
	}


	/**
	 * Gets the list of indexes of sentences ordered by their importance in the text.
	 * 
	 * @return a list of indexes of sentences
	 */
	public List<Integer> getOrdered(){

		return orderNumSent;
	}
	
	
	public Double getScore(int sentID){
		return scoreHandler.getScore(sentID);
	}
	
	/*public int getNbrFeatures(){
		return features.size();
	}*/

}

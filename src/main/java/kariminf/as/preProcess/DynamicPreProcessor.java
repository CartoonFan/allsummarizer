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

package kariminf.as.preProcess;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import kariminf.langpi.basic.def.DefNormalizer;
import kariminf.langpi.basic.def.DefSWEliminator;
import kariminf.langpi.basic.def.DefSegmenter;
import kariminf.langpi.basic.def.DefStemmer;
import kariminf.langpi.basic.Normalizer;
import kariminf.langpi.basic.BasicInfo;
import kariminf.langpi.basic.SWEliminator;
import kariminf.langpi.basic.Segmenter;
import kariminf.langpi.basic.Stemmer;
import kariminf.as.tools.Data;
import kariminf.ktoolja.plugins.JarLoader;



/**
 * Preprocess task
 * 
 * <p>Given a text in a certain language, this class is used to look for adequate 
 * methods to preprocess it.</p>
 * <p>PreProcessing consists of normalization (See {@link Normalizer}), sentences 
 * and word segmentation (See {@link Segmenter}), stemming (See {@link Stemmer})
 *  and stop words removing (See {@link SWEliminator}).
 * 
 * @author Abdelkrime Aries
 *
 */
public class DynamicPreProcessor extends PreProcessor{

	//The location of the preprocessing plugins
	private String location = "preProcess/";


	//Different preprocessing tasks
	private BasicInfo info = null;
	private Normalizer normalizer = null;
	private Segmenter segmenter = null;
	private SWEliminator sweliminator = null;
	private Stemmer stemmer = null;


	/**
	 * Creates a PreProcessor for a specific language
	 * 
	 * @param lang the language in ISO 639-1 code, eg. "en", "fr", "ar", etc.
	 * @param data The data container
	 */
	public DynamicPreProcessor(String lang){
		super(lang);
		setLanguage(lang);
		
	}
	
	public DynamicPreProcessor(String lang, String location){
		super(lang);
		this.location = location;
		setLanguage(lang);
	}


	/**
	 * Changes or sets the language of the preprocessor
	 * 
	 * @param lang the language in ISO 639-1 code, eg. "en", "fr", "ar", etc.
	 */
	public void setLanguage(String lang){
		
		//Search for all preprocessing plugins 
		JarLoader jarLoader = 
				new JarLoader(location, "kariminf/langpi/basic", BasicInfo.version);
		
		//get the info class for the preprocessed language
		info = jarLoader.getInfoService(lang, BasicInfo.class);

		//Try to get the preprocessing tasks' classes, otherwise use the default
		
		normalizer = jarLoader.getClassService(info, Normalizer.class);
		if (normalizer == null){
			System.out.println(lang + ": No Normalizer, using default");
			normalizer = new DefNormalizer();
		}
		
		segmenter = jarLoader.getClassService(info,Segmenter.class);
		if (segmenter == null){
			System.out.println(lang + ": No Segmenter, using default");
			segmenter = new DefSegmenter();
		}
		
		sweliminator = jarLoader.getClassService(info, SWEliminator.class);
		if (sweliminator == null){
			System.out.println(lang + ": No SWEliminator, using default");
			sweliminator = new DefSWEliminator();
		}
		
		stemmer = jarLoader.getClassService(info, Stemmer.class);
		
		if (stemmer == null){
			System.out.println(lang + ": No Stemmer, using default");
			stemmer = new DefStemmer();
		}

	}


	/**
	 * Preprocess all the entered texts using the method 
	 * {@link DynamicPreProcessor#addText(String)}
	 */
	public void preProcess(){

		for (String text: texts){
			text = normalizer.normalize(text);
			List<String> sentInText = segmenter.splitToSentences(text);
			data.addSentences(sentInText);
		}


		List<List<String>> sentWords = preProcess(data.getSentences());
		
		data.setSentWords(sentWords);

	}


	/**
	 * Preprocess a list of sentences; here the sentence segmentation task is not used
	 * 
	 * @param Sents list of sentences
	 * @return list of sentences where each sentence is a list of preprocessed words
	 */
	public List<List<String>> preProcess (List<String> Sents){

		List<List<String>> sentWords = new ArrayList<List<String>>();
		//number of words in the sentences including stop-words
		List<Integer> nbrWords = new ArrayList<Integer>(); 

		for (String sent: Sents){
			List<String> segSent = segmenter.segmentWords(sent);

			nbrWords.add(segSent.size());

			sweliminator.deleteSW(segSent);

			segSent = stemmer.stemListWords(segSent);

			sentWords.add(segSent);
		}

		data.setNbrWords(nbrWords);

		return sentWords;
	}


	/**
	 * Preprocess a given text
	 * 
	 * @param inTxt text to be preprocessed; the result can be found in the 
	 * {@link Data} object given in the constructor 
	 * (See {@link DynamicPreProcessor#PreProcessor(String, Data)}
	 */
	public void preProcess(String inTxt){

		inTxt = normalizer.normalize(inTxt);

		List<String> sentences = segmenter.splitToSentences(inTxt);
		List<List<String>> sentWords = preProcess(sentences);

		data.addSentences(sentences);
		data.setSentWords(sentWords);


	}
	
}

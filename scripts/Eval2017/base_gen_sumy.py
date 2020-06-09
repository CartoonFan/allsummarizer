# -*- coding: utf-8 -*-
from __future__ import absolute_import
from __future__ import division
from __future__ import print_function
from __future__ import unicode_literals

import sys

import nltk
from sumy.nlp.stemmers import Stemmer
from sumy.nlp.tokenizers import Tokenizer
from sumy.parsers.html import HtmlParser
from sumy.parsers.plaintext import PlaintextParser
from sumy.summarizers.edmundson import EdmundsonSummarizer as Summarizer
from sumy.utils import get_stop_words

LANGUAGE = "english"
SENTENCES_COUNT = 30

MODEL = "model/en"
SIZE_FILE = "src/target-length/en.txt"

sizes = {}


def extract(sentences, maxSize):
    summary = ""
    size = 0
    for sentence in sentences:
        size += len(unicode(sentence))
        if size > maxSize:
            break
        summary = summary + unicode(sentence) + "\n"
    return summary


def readTextFile(path):
    txt_file = open(path, "r")
    text = ""
    while 1:
        line = txt_file.readline()
        if line == "":
            break
        text = text + line + "\n"
    return text


if __name__ == "__main__":
    reload(sys)
    sys.setdefaultencoding("utf8")
    """
    nltk.data.path.append('/home/kariminf/Data/NLTK/')



    for sentence in summarizer(parser.document, SENTENCES_COUNT):
        print(sentence)
    """

    file = open(SIZE_FILE, "r")
    while 1:
        line = file.readline()
        if line == "":
            break
        parts = line.split(",")
        sizes[parts[0]] = int(parts[1])
    file.close()

    nltk.data.path.append("/home/kariminf/Data/NLTK/")
    for eval in sizes:
        txt_path = "src/body/text/en/" + eval
        parser = PlaintextParser.from_file(txt_path, Tokenizer(LANGUAGE))
        stemmer = Stemmer(LANGUAGE)
        summarizer = Summarizer(stemmer)
        summarizer.stop_words = get_stop_words(LANGUAGE)
        summary = extract(summarizer, sizes[eval])
        fout = open("baselines/EdmundsonSummarizer/en/" + eval[:-9] + ".txt",
                    "w")
        fout.write(summary)
        fout.close()

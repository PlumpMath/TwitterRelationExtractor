# Script to extract relations from Tweets using Twitter nlp and Reverb
# Input, Output : Same as Reverb

import sys
import os

#Find normal tag of Twitter NLP tag
def find(t):
	h = open('mapping','r')
	line = h.readline()
	while line:
		l = line.split(' ')
		if(l[0]==t):
			if(l[1][len(l[1])-1]=='\n'):
				return l[1][0:len(l[1])-1]
			else:
				return l[1]
		line = h.readline()
	print >> sys.stderr, 'tag not found !!!'

#converts : iono -> i_dont_know etc.
def find_slang(w):
	h = open('dictionary_slang','r')
	line = h.readline()
	found = False
	while line:
		if(line[0:len(line)-1] == w.upper()):
			found = True
			break
		line = h.readline()
	if(found):
		line = h.readline()
		s = line[0:len(line)-1]
		return s.replace(' ','_')
	else:
		return w
	h.close()
	
#converts : iono -> i dont know etc.
def find_slang_space(w):
	h = open('dictionary_slang','r')
	line = h.readline()
	found = False
	while line:
		if(line[0:len(line)-1] == w.upper()):
			found = True
			break
		line = h.readline()
	if(found):
		line = h.readline()
		s = line[0:len(line)-1]
		return s
	else:
		return w
	h.close()
	
	

#Function to replace slang by normal string
def Replace_Slang(T,W,j):
	word_list = []
	for i in range(0,len(T)):
		if(T[i] == 'L'):
			word_list.append(find_slang_space(W[i]))
		else:
			word_list.append(W[i])
				
	# Write to file 
	word = list_str(word_list)
	j.write(word+'\n')

# Function to compare old and new Twitter_NLP tags and generate a new tag
def join(N,O):
	if(N=='N' and (O=='NN' or O=='NNS')):
		return O
	elif(N=='V' and (O=='VB' or O=='VBD' or O=='VBG' or O=='VBN' or O=='VBP' or O=='VBZ' or O=='MD')):
		return O
	elif(N=='O' and (O=='PRP' or O=='PRP$' or O=='WP' or O=='WP$')):
		return O
	elif(N=='^' and (O=='NNP' or O=='NNPS')):
		return O
	elif(N=='A' and (O=='JJ' or O=='JJR' or O=='JJS')):  
		return O
	elif(N=='R' and (O=='RB' or O=='RBR' or O=='RBS' or O=='WRB')):  
		return O
	elif(N=='D' and (O=='WDT' or O=='DT' or O=='PRP$' or O=='WP$')):  
		return O
	elif(N=='G' and (O=='FW' or O=='POS' or O=='SYM' or O=='LS')):  
		return O
	else:
		return N

#Function to map new tag list with old tag list
def Map_New_Old(new_tag_list,old_tag_list):
	Final = []
	for i in range(0,len(new_tag_list)):
		Final.append(join(new_tag_list[i],old_tag_list[i]))
	return Final
	
#Function to map Twitter NLP to normal tags
def Map(T,W,m,j):
	tag_list = []
	word_list = []
	twitter_specific_tags = ['#','@','~','U','E']
	for i in range(0,len(T)):
		if(T[i] in twitter_specific_tags):
			continue
		elif(T[i] == ','):
			word_list.append(W[i])
			if(W[i]=='.'):
				tag_list.append('.')
			else:
				tag_list.append(',')
		elif(T[i] == 'L'):
			word_list.append(find_slang(W[i]))
			word_list.append('MergedWords')
			tag_list.append('NNS')
			tag_list.append('VB')
		elif(T[i] == 'M'):
			word_list.append(W[i])
			word_list.append('MergedWords')
			tag_list.append('NNPS')
			tag_list.append('VB')			
		elif(T[i] == 'S'):
			word_list.append(W[i])
			word_list.append('MergedWords')
			tag_list.append('NNS')
			tag_list.append('POS')
		elif(T[i] == 'Z'):
			word_list.append(W[i])
			word_list.append('MergedWords')
			tag_list.append('NNPS')
			tag_list.append('POS')	
		elif(T[i] == 'P'):
			word_list.append(W[i])
			if(W[i].lower() == 'to' or W[i].lower() == '2'):
				tag_list.append('TO')
			else:
				tag_list.append('IN')								
		elif(T[i] == 'X'):
			word_list.append(W[i])
			if(W[i].lower() == 'there' or W[i].lower() == 'thr'):
				tag_list.append('EX')
			else:
				tag_list.append('PDT')												
		elif(T[i] == 'Y'):
			word_list.append(W[i])
			word_list.append('MergedWords')
			if(W[i].lower() == 'there\'s'):
				tag_list.append('EX')
			else:
				tag_list.append('PDT')												
			tag_list.append('VB')			
		elif(T[i] == 'N' or T[i] == 'O' or T[i] == '^' or T[i] == 'V' or T[i] == 'A' or T[i] == 'R' or T[i] == '!' or T[i] == 'D' or T[i] == '&' or T[i] == 'T' or T[i] == '$' or T[i] == 'G'):
			word_list.append(W[i])
			tag_list.append(find(T[i]))
		else:
			word_list.append(W[i])
			tag_list.append(T[i])
			
	# Write to file 
	word = list_str(word_list)
	tag = list_str(tag_list)
	j.write(word+'\n')
	m.write(word+'\n')
	m.write(tag+'\n')
	

# function to convert list to string
def list_str(L):
	s = ''
	for e in L:
		s = s+e+' '
	return s[0:len(s)-1]

	
file_name = sys.argv[1]

# Step 1 : Clean the input
print >> sys.stderr,'Cleaning the input ...'
os.system('java InputPreProcess '+file_name+' emoticonsList '+file_name+'_cleaned')
file_name = file_name+'_cleaned'
print >> sys.stderr,'Input Cleaned!'

# Step 2 : Run Twitter NLP on cleaned tweets
print >> sys.stderr,'Running Twitter NLP ...'
os.system('./ark-tweet-nlp-0.3.2/runTagger.sh '+file_name+' > '+file_name+'_twitternlp_tags')
print >> sys.stderr,'Twitter NLP tags found, creating Merged and Input files ...'

#Step 3 : Get the sentences replacing slangs and run Twitter_nlp again
g = open(file_name+'_twitternlp_tags','r')
j = open('Input_Sentences_replace_slang','w')
line = g.readline()
while line:
	s = line.split('	')
	T = s[1].split(' ')
	W = s[0].split(' ')
	Replace_Slang(T,W,j)
	line = g.readline()
g.close()
j.close()
os.system('./ark-tweet-nlp-0.3.2/runTagger.sh Input_Sentences_replace_slang > Twitter_NLP_Tags')

#Tagging using old twitter nlp and replacing similar tags.

# Step 4 : Run Old Twitter NLP on cleaned tweets..
print >> sys.stderr,'Running Old twitter NLP ...'
os.environ["TWITTER_NLP"] = "./twitter_nlp-master/"
os.system('cat Input_Sentences_replace_slang | python ./twitter_nlp-master/python/ner/extractEntities2.py --classify --pos > Old_Twitter_NLP_Tags')

# Step 5 : Create Merge_sentences and Input file
f = open('Merged_Sentence_Tags','w')
g = open('Twitter_NLP_Tags','r')
j = open('Input_Sentences','w')
o = open('Old_Twitter_NLP_Tags','r')
line = g.readline()
line_o = o.readline()
while line:
	s = line.split('	')
	T = s[1].split(' ')
	W = s[0].split(' ')
	old_tag_list = []
	old_tnlp_list = line_o.split(' ')
	for element in old_tnlp_list:
		parts = element.split('/')
		if(len(parts)==3 and parts[2]!=''):
			if(parts[2]=='\n'):
				old_tag_list.append('.')
			else:
				old_tag_list.append(parts[2])
		else:
			old_tag_list.append('.')
	K=Map_New_Old(T,old_tag_list)
	Map(K,W,f,j)
	line = g.readline()
	line_o = o.readline()
f.close()
g.close()
j.close()
print >> sys.stderr, 'Files Merged and Input Sentences Created!'

# Step 6 : Call modified reverb, Uses Twitter nlp instead of Opennlp for tagging
print >> sys.stderr,'Running Reverb ...'
os.system('java -Xmx512m -jar ./reverb-core-master/core/target/reverb-core-1.4.1-SNAPSHOT-jar-with-dependencies.jar Input_Sentences')
print >> sys.stderr,'Done!'

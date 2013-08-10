# Takes a sentence as input and returns the tags which have been pre-computed and stored in Merged_Sentence_Tags
# Input :string ( sentence to be parsed )
# Output : All corresponding tags to the above sentence

import os
import sys

f = open('sentence not found','a')
f1 = open('sentence found','a')
flag = False

# Find the corresponding tags from Merged_Sentence_Tags
sentence = sys.argv[1]+'\n'
result = open('Merged_Sentence_Tags','r')
line = result.readline()
linet = result.readline()
while line:
	if(line == sentence):
		flag = True
		break
	line = result.readline()
	linet = result.readline()
result.close()

if(flag==False):
	f.write(sentence)
else:
	f1.write(sentence)
f.close()

# Final lists to be returned
print line[0:len(line)-1]
print linet[0:len(linet)-1]

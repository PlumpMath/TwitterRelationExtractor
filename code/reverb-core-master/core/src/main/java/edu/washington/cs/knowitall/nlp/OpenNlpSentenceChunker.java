package edu.washington.cs.knowitall.nlp;

import java.io.IOException;
import java.io.*;
import java.util.ArrayList;
import java.util.regex.Pattern;

import opennlp.tools.chunker.Chunker;
import opennlp.tools.postag.POSTagger;
import opennlp.tools.tokenize.Tokenizer;
import opennlp.tools.util.Span;
import edu.washington.cs.knowitall.commonlib.Range;
import edu.washington.cs.knowitall.util.DefaultObjects;

/**
 * A class that combines OpenNLP tokenizer, POS tagger, and chunker objects into
 * a single object that converts String sentences to {@link ChunkedSentence}
 * objects. By default, uses the models from
 * {@link DefaultObjects#getDefaultTokenizer()},
 * {@link DefaultObjects#getDefaultPosTagger()}, and
 * {@link DefaultObjects#getDefaultChunker()}.
 * 
 * @author afader
 * 
 */
public class OpenNlpSentenceChunker implements SentenceChunker {
    private Chunker chunker;
    private POSTagger posTagger;
    private Tokenizer tokenizer;

    private boolean attachOfs = true;
    private boolean attachPossessives = true;

    Pattern convertToSpace = Pattern.compile("\\xa0");

    /**
     * Constructs a new object using the default models from
     * {@link DefaultObjects}.
     * 
     * @throws IOException
     *             if unable to load the models.
     */
    public OpenNlpSentenceChunker() throws IOException {
        this.tokenizer = DefaultObjects.getDefaultTokenizer();
        this.posTagger = DefaultObjects.getDefaultPosTagger();
        this.chunker = DefaultObjects.getDefaultChunker();
    }

    /**
     * Constructs a new {@link OpenNlpSentenceChunker} object using the provided
     * OpenNLP objects.
     * 
     * @param tokenizer
     * @param posTagger
     * @param chunker
     */
    public OpenNlpSentenceChunker(Tokenizer tokenizer, POSTagger posTagger,
            Chunker chunker) {
        this.tokenizer = tokenizer;
        this.posTagger = posTagger;
        this.chunker = chunker;
    }

    /**
     * @return true if this object will attach NPs beginning with "of" with the
     *         previous NP.
     */
    public boolean attachOfs() {
        return attachOfs;
    }

    /**
     * @return true if this object will attach NPs beginning with the tag POS
     *         with the previous NP.
     */
    public boolean attachPossessives() {
        return attachPossessives;
    }

    /**
     * @param attachOfs
     */
    public void attachOfs(boolean attachOfs) {
        this.attachOfs = attachOfs;
    }

    /**
     * @param attachPossessives
     */
    public void attachPossessives(boolean attachPossessives) {
        this.attachPossessives = attachPossessives;
    }

    @Override
    /**
     * Chunks the given sentence and returns it as an {@link ChunkedSentence}
     * object.
     */
    public ChunkedSentence chunkSentence(String sent) throws ChunkerException {

        // OpenNLP cannot handle non-breaking whitespace
        sent = convertToSpace.matcher(sent).replaceAll(" ");
        ArrayList<Range> ranges = new ArrayList<Range>(1);
        String[] tokens ={}, posTags = {}, npChunkTags = {};
		    // OpenNLP can throw a NullPointerException. Catch it, and raise it
        // as a checked exception.
        // TODO: try to figure out what caused the NPE and actually fix the
        // problem
        try {
        
            
            String s = null;

          try {
				    String[] callAndArgs= {"python","getPOSTags.py",sent};
            Process p = Runtime.getRuntime().exec(callAndArgs);
            BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
            BufferedReader stdError = new BufferedReader(new InputStreamReader(p.getErrorStream()));
						boolean exit=false;
            // read any errors
            while ((s = stdError.readLine()) != null) {
            		System.out.println("Some error has occured.");
                System.out.println(s);
                exit=true;
            }
		        if(exit)
		        	System.exit(-1);

						s=stdInput.readLine();
						tokens=s.split(" ");
						/* Update sent as we are adding tokens for new tags */
						sent = "";
						for(int i=0;i<tokens.length-1;i++)
							sent = sent + tokens[i]+" ";
						sent = sent + tokens[tokens.length-1];
						ranges = new ArrayList<Range>(tokens.length);
						int start,end;
						start=end=0;
						String temp = sent;
						for(String token : tokens)
						{
							start = temp.indexOf(token) + end;
							end = start + token.length();
							ranges.add(Range.fromInterval( start,end ));
							if(end < sent.length())
								temp = sent.substring(end,sent.length());
						}
						
						s=stdInput.readLine();
						posTags=s.split(" ");
            }
          
          catch (IOException e) {
            System.out.println("Exception occured while finding pos tag for sentence: "+sent);
            e.printStackTrace();
            System.exit(-1);
        }  
           
            //old assignment for posTags in Opennnlp:
            //posTags = posTagger.tag(tokens);
            npChunkTags = chunker.chunk(tokens, posTags);
        } catch (NullPointerException e) {
            throw new ChunkerException("TwitterNLP threw NPE on '" + sent + "'", e);
        }

        if (attachOfs)
            OpenNlpUtils.attachOfs(tokens, npChunkTags);
        if (attachPossessives)
            OpenNlpUtils.attachPossessives(posTags, npChunkTags);
        
        ChunkedSentence result = new ChunkedSentence(
                ranges.toArray(new Range[] {}), tokens, posTags, npChunkTags);
        return result;
    }
}

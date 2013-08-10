import java.util.*;
import java.io.*;

class InputPreProcess
{
	public static void main(String args[]) throws Exception
	{
		if(args==null || args.length<3)
			{	
				System.err.println("Usage: java InputPreProcess InputFileName EmoticonsList OutputFileName");
				return;
			}
		
		
		FileReader fr1=new FileReader(args[1]);
		BufferedReader br1=new BufferedReader(fr1);
		String emoticonsList="";
		String temp=br1.readLine();
		while(temp!=null)
		{
			emoticonsList=emoticonsList+" "+temp;
			temp=br1.readLine();
		}
		String emoticons[]=emoticonsList.split("[ ]+");
		HashSet<String> hs=new HashSet<String>(Arrays.asList(emoticons));
		
		fr1.close();
		
		FileReader fr=new FileReader(args[0]);
		BufferedReader br=new BufferedReader(fr);
		String inLine=br.readLine();
		
		FileWriter fw=new FileWriter(args[2]);
		String outLine;
		
		
		boolean start=true;
		while(inLine!=null)
			{
				outLine="";
				
				// Clean outLine here.
				String tokens[]=inLine.split("[ ]+");
				for(int i=0;i<tokens.length;i++)
					{
						if(hs.contains(tokens[i]) || tokens[i].startsWith("http://") || tokens[i].startsWith("@") || tokens[i].startsWith("|") || tokens[i].equals("RT"))
							continue;
						String tempT=tokens[i];
						String old_tempT=tempT;
						//Remove start brackets
						while(tempT!=null && !tempT.equals("") && ( tempT.startsWith("(") || tempT.startsWith("{") || tempT.startsWith("[")))
						{
							if(tempT.length()==1)
								tempT="";
							else
								tempT=tempT.substring(1,tempT.length());
						}
						//Remove end brackets
						while(tempT!=null && !tempT.equals("") && (tempT.endsWith(")") || tempT.endsWith("}") || tempT.endsWith("]")))
						{
							if(tempT.length()==1)
								tempT="";
							else
								tempT=tempT.substring(0,tempT.length()-1);
						}
						boolean sentEndFound=false;
						//Remove extra ? etc.
						while(tempT!=null && !tempT.equals("") && tempT.indexOf("?")!=-1)
						{
							old_tempT=tempT;
							int pos=tempT.indexOf("?");
							if(tempT.length()==1)
								tempT="";
							else
								if(pos==tempT.length()-1)
									tempT=tempT.substring(0,tempT.length()-1);
								else
									if(pos==0)
										tempT=tempT.substring(1,tempT.length());
									else
										tempT=tempT.substring(0,pos)+tempT.substring(pos+1,tempT.length());
							sentEndFound=true;
						}
						tempT=old_tempT; // Keep last ? intact.
						
						
						//Remove ! etc.
						while(tempT!=null && !tempT.equals("") && tempT.indexOf("!")!=-1)
						{
							old_tempT=tempT;
							int pos=tempT.indexOf("!");
							if(tempT.length()==1)
								tempT="";
							else
								if(pos==tempT.length()-1)
									tempT=tempT.substring(0,tempT.length()-1);
								else
									if(pos==0)
										tempT=tempT.substring(1,tempT.length());
									else
										tempT=tempT.substring(0,pos)+tempT.substring(pos+1,tempT.length());
							sentEndFound=true;
						}
						tempT=old_tempT; // Keep last ! intact.
						
						
						//Remove . etc.
						while(tempT!=null && !tempT.equals("") && tempT.indexOf(".")!=-1)
						{
							old_tempT=tempT;
							int pos=tempT.indexOf(".");
							if(tempT.length()==1)
								tempT="";
							else
								if(pos==tempT.length()-1)
									tempT=tempT.substring(0,tempT.length()-1);
								else
									if(pos==0)
										tempT=tempT.substring(1,tempT.length());
									else
										tempT=tempT.substring(0,pos)+tempT.substring(pos+1,tempT.length());
							sentEndFound=true;
						}
						tempT=old_tempT; // Keep last . intact.
						if(sentEndFound)
						{
							int delimiter=-1;
							if((delimiter=tempT.indexOf("."))==-1)
								if((delimiter=tempT.indexOf("!"))==-1)
									if((delimiter=tempT.indexOf("?"))==-1)
									{	
										System.err.println("Some unexpected error has occured in InputPreProcess.java!!");
										System.exit(1);
									}
							if(outLine==null || outLine.equals(""))
								outLine=tempT.substring(0,delimiter+1);
							else if(tempT!=null && tempT!="")
								outLine=outLine+" "+tempT.substring(0,delimiter+1);
							if((!outLine.equals(".")) && (!outLine.equals("?")) && (!outLine.equals("!"))) //Make sure sentence is just not delimiter.
							{
								if(!start)
									fw.write("\n");
								start=false;
								fw.write(outLine);
							}
							outLine=tempT.substring(delimiter+1);
						}
						else
						{
							if(outLine==null || outLine.equals(""))
								outLine=tempT;
							else if(tempT!=null && tempT!="")
								outLine=outLine+" "+tempT;
						}
					}
				if(outLine!=null && !outLine.equals("")) // if delimiter not found in the end add one.
				{
					if(outLine.charAt(outLine.length()-1)!='.' && outLine.charAt(outLine.length()-1)!='?' && outLine.charAt(outLine.length()-1)!='!')
						outLine=outLine+" .";
					if(!start)
					{
						fw.write("\n");
					}
					start=false;
					fw.write(outLine);
				}
				inLine=br.readLine();
			}
		fw.close();
		fr.close();
	}
}

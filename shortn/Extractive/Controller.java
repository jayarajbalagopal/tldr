import java.util.Hashtable;
import java.util.List;
import java.util.Collections;
import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.Locale;
import java.io.*;
import java.util.StringTokenizer;




public class Controller {
	
	ExtractSentence extractSentence;
	Lexrank lexrank;
	List<Hashtable<String,Double>> score;
	int [] degree;
	public static String filename;
	public static float ratio;
		
	public String getSummary() throws Exception{
		
		String summary = " ";
		String preprocessedText=null;
		List<String> sentences=null;
		
		BufferedReader reader = new BufferedReader(new FileReader(filename));
		StringBuilder stringBuilder = new StringBuilder();
		String line = null;
		String ls = System.getProperty("line.separator");
		while ((line = reader.readLine()) != null) {
			stringBuilder.append(line);
			stringBuilder.append(ls);
		}

		// delete the last new line separator
		stringBuilder.deleteCharAt(stringBuilder.length() - 1);
		reader.close();

		String content = stringBuilder.toString();

		String summarizableText = content;

		if(!summarizableText.equals("")){
			
			preprocessedText = summarizableText;
			
		}
		else{
			throw new Exception(Constants.TEXT_EXTRACTION_FAILED);
		}
		
		extractSentence = new ExtractSentence();
		sentences = extractSentence.getSentences(preprocessedText);
        int count;
       
        count=sentences.size();
       

        
       double[][] similarityMatrix = getSimilarityMatrixFromSentenceList(sentences);
     
        
		lexrank = new Lexrank(sentences, similarityMatrix	,0.85, degree);
		double[] resultEigenValues = lexrank.powerMethod(.001);
		
        int sample[]=new int[sentences.size()];
        
        for(int i=0;i<sentences.size();i++)
        	{
        		sample[i]=i;
        	}
        for (int i = 0; i < sentences.size()-1; i++)
            for (int j = 0; j < sentences.size()-i-1; j++)
                if (resultEigenValues[j] < resultEigenValues[j+1])
                {
                    double temp = resultEigenValues[j];
                    resultEigenValues[j] = resultEigenValues[j+1];
                    resultEigenValues[j+1] = temp;
                    Collections.swap(sentences,j,j+1);
                }
        
       
        int k=1;
        double x=(ratio*count);
        int max=(int)x;
        int m=1;
        for(String s:sentences)
        	{
                if(k<=max)
                {
                    summary=summary+s;
                    m++;
                    k++;
                }
                else
                    ;
            }

		return summary;
		
	}
	
	
	private void printArray(double[] A){
		
		for(double i: A){
			System.out.print(" "+i);
		}
		
	}
	private void printArray(int[] A){
		
		for(int i: A){
			System.out.print(" "+i);
		}
		
	}
	
	private double[][] getSimilarityMatrixFromSentenceList(List<String> sentenceList){
		
		
		DocumentVectorCalculator dvc=new DocumentVectorCalculator(sentenceList);
        
		score = dvc.generateTF_IDFScores();
        
		SimilarityMatrixGenerator simg=new SimilarityMatrixGenerator(score);
		
		double[][] result=simg.similarityMatrixGenerator();
        //result contains the similarity matrix ..in this case a [30][30] matrix
		degree = new int[result.length];
		
		for(int i=0;i<degree.length;i++)
        {
			degree[i] = 0;
		}
        
        
		
			for(int i=0;i<result.length;i++)
			{
				for(int j=0;j<result[i].length;j++)
				{
					
					if(i!=j)
					{
						if(result[i][j] >= 0.1)
						{result[i][j]=1.0;degree[i]++;}
						else
						result[i][j]=0.0;
					}
				}
			}
		

		
		//End
			for(int i=0;i<result.length;i++)
			{
				for(int j=0;j<result[i].length;j++)
				{
                    if(degree[i]!=0)
                    {
                        result[i][j]=result[i][j]/degree[i];

                    }
                    else
                    {
                        result[i][j]=0;

                    }
				}
			}
			
		return result;
		
	}
	public static void main(String...a) throws Exception{

		filename = a[0];
		Controller c = new Controller();

		ratio = Float.parseFloat(a[1]);
		//System.out.println(c.getSummary());
		PrintWriter writer = new PrintWriter("/home/kannan/MainProject/tldr/shortn/Outputs/out.txt", "UTF-8");
		writer.println(c.getSummary());
		writer.close();
	}


}



class ExtractSentence {

    public List<String> count(BreakIterator bi, String source) {
        
    	int counter = 0;
        List<String> sentenceList=new ArrayList<String>();
        /*bi.setText(source);

        int lastIndex = bi.first();
        while (lastIndex != BreakIterator.DONE) {
            int firstIndex = lastIndex;
            lastIndex = bi.next();

            if (lastIndex != BreakIterator.DONE) {
                String sentence = source.substring(firstIndex, lastIndex);
                sentenceList.add(sentence);                
                counter++;
            }
        }
        System.out.println(counter);*/
        

        BreakIterator iterator = BreakIterator.getSentenceInstance(Locale.ENGLISH);
        iterator.setText(source);
        int start = iterator.first();
        int i=1;
        for (int end = iterator.next();end != BreakIterator.DONE; start = end, end = iterator.next()) {
            //System.out.println("Sentence "+i+" : "+source.substring(start,end));
            String sentence = source.substring(start,end);
            sentenceList.add(sentence);
            i++;
        }
        return sentenceList;
    }
    
    public List<String> getSentences(String paragraph){
    	 List<String> sentences;
    	 BreakIterator iterator =BreakIterator.getSentenceInstance(Locale.US);
         
    	 sentences = count(iterator, paragraph);
         
    	
    	
    	 return sentences;
    }
}



class Lexrank {
	private Matrix _matrix;
	private List<String> slist;
	
	private double _damping;
	private int[] _degree;
	
	public Lexrank(List<String> slist, double[][] sMatrix, double damping,int[] degree)
	{
		this.slist = slist;
		//_threshold = threshold;
		_damping = damping;
		_matrix =new Matrix(sMatrix);
		_degree = degree;
		
       // _matrix.show();
		
		
		
		for(int i=0; i<_matrix.getRows(); i++)
        {
			for(int j=0; j<_matrix.getCols(); j++)
            {
            	if(degree[i]!=0)
					{
						double val = _damping/slist.size() + _damping * _matrix.get(i,j) / _degree[i];
					   _matrix.insert(val, i, j);
				    }
				else
				{
					_matrix.insert(0,i,j);
				}

			}
		}
		//_matrix.show();
        System.out.println();
    }
	
	
	public double[] powerMethod(double error)
	{
		Matrix p0 = new Matrix(slist.size(),1);
		Matrix p1 = new Matrix(slist.size(),1);
		
		for(int i=0; i<slist.size(); i++){
			p0.insert(1/(double)slist.size(),i,0);
		}
		//System.out.println("P0 Matrix ");
		
		//p0.show();
		
		//System.out.println("_matrix Matrix ");
		
		//_matrix.show();
		
		Matrix mT = _matrix.transpose();
		
		//System.out.println("mT Matrix ");
		
		//mT.show();
		
		Matrix pMinus; 
		p1 = mT.times(p0);
		
		//System.out.println("p1 Matrix ");
		
		//p1.show();
		pMinus = p1.minus(p0);
		
		//pMinus.show();
		
		int iteration=0;
		while(pMinus.getMax() <= error&&iteration<=100){
			p0 = p1;
			p1 = mT.times(p0);
			pMinus = p1.minus(p0);
			iteration++;
			//System.out.print(" "+iteration);
			
			//pMinus.show();
			
		}
				
		return p1.getCol(0);
	}
	
	
	
}



class SimilarityMatrixGenerator {

List<Hashtable<String,Double>> scoreVector;
double[][] similarityMatrix;
double[] documentWeight;



public SimilarityMatrixGenerator(List<Hashtable<String,Double>> scoreVector)
{
	this.scoreVector=scoreVector;
	similarityMatrix=new double[scoreVector.size()][scoreVector.size()];
    documentWeightCalc();
}


private void documentWeightCalc()
{
	documentWeight=new double[scoreVector.size()];
    for(int j=0;j<documentWeight.length;j++)
		documentWeight[j]=0.0;
    
    for(int i=0;i<documentWeight.length;i++)
	{
				for(double tfidf:scoreVector.get(i).values())
				{
				documentWeight[i]+=Math.pow(tfidf, 2);
				//System.out.print(" "+documentWeight[i]);
				
				}
				//System.out.println();
				
				double temp=Math.sqrt(documentWeight[i]);
				documentWeight[i]=temp;
	}
    for(int i=0;i<scoreVector.size();i++)
    {
            //System.out.print(" "+documentWeight[i]);
    }


	//for(double d:documentWeight)
		//System.out.println(" "+d);
    
   // System.out.println();
	
}

private double cosineXY(int i,int j)
{
	double result=0.0;
	double X,Y,XY;
	X=Y=XY=0.0;
	
	
	for(String word:scoreVector.get(i).keySet())
	{
			if(scoreVector.get(j).containsKey(word))
			XY+=scoreVector.get(i).get(word)*scoreVector.get(j).get(word);
	}
	
	
	
	X=documentWeight[i];
	Y=documentWeight[j];
	
	result=XY/(X*Y);
	
	
	return result;
}


public double[][] similarityMatrixGenerator()
{
	int N=scoreVector.size();
	similarityMatrix=new double[N][N];
	int i=0;
	for(i=0;i<N;i++)
	{
		for(int j=0;j<N;j++)
			similarityMatrix[i][j]=0.0;
	}
	
	for(i=0;i<N;i++)
		similarityMatrix[i][i]=1;
	
	
	for(i=0;i<N;i++)
	{
		for(int j=0;j<N;j++)
		{
			
				if(i!=j)
				similarityMatrix[i][j]=cosineXY(i, j);
		
		}
	}
   /* for(i=0;i<scoreVector.size();i++)
    {
        for(int j=0;j<scoreVector.size();j++)
            //System.out.print(" "+similarityMatrix[i][j]);
        System.out.println();
    }*/
    //System.out.println();
    //System.out.println();

	return similarityMatrix;
}


}


final class Matrix {
    private final int M;             // number of rows
    private final int N;             // number of columns
    private final double[][] data;   // M-by-N array

    // create M-by-N matrix of 0's
    public Matrix(int M, int N) {
        this.M = M;
        this.N = N;
        data = new double[M][N];
    }

    // create matrix based on 2d array
    public Matrix(double[][] data) {
        M = data.length;
        N = data[0].length;
        this.data = new double[M][N];
        for (int i = 0; i < M; i++)
            for (int j = 0; j < N; j++)
                    this.data[i][j] = data[i][j];
    }

    // copy constructor
    private Matrix(Matrix A) { this(A.data); }
    
    public int getRows(){
    	return this.M;
    }
    
    public int getCols(){
    	return this.N;
    }
    
    public void insert(double val,int m, int n){
    	this.data[m][n] = val;
    }
    
    public double get(int m, int n){
    	return this.data[m][n];
    }

    public double getMax(){
    	double max = 0;
    	double val;
    	for (int i = 0; i < M; i++){
            for (int j = 0; j < N; j++){
            	val = Math.abs(this.data[i][j]);
            	if ( val > max)
            		max = val;
            }
    	}
    	return max;
    }
    
    public double[] getCol(int index){
    	double[] ans = new double[M];
    	for (int i = 0; i < M; i++){
    		ans[i] = this.data[i][index];
    	}
    	return ans;
    }
    // create and return a random M-by-N matrix with values between 0 and 1
    public static Matrix random(int M, int N) {
        Matrix A = new Matrix(M, N);
        for (int i = 0; i < M; i++)
            for (int j = 0; j < N; j++)
                A.data[i][j] = Math.random();
        return A;
    }

    // create and return the N-by-N identity matrix
    public static Matrix identity(int N) {
        Matrix I = new Matrix(N, N);
        for (int i = 0; i < N; i++)
            I.data[i][i] = 1;
        return I;
    }

    // swap rows i and j
    private void swap(int i, int j) {
        double[] temp = data[i];
        data[i] = data[j];
        data[j] = temp;
    }

    // create and return the transpose of the invoking matrix
    public Matrix transpose() {
        Matrix A = new Matrix(N, M);
        for (int i = 0; i < M; i++)
            for (int j = 0; j < N; j++)
                A.data[j][i] = this.data[i][j];
        return A;
    }

    // return C = A + B
    public Matrix plus(Matrix B) {
        Matrix A = this;
        if (B.M != A.M || B.N != A.N) throw new RuntimeException("Illegal matrix dimensions.");
        Matrix C = new Matrix(M, N);
        for (int i = 0; i < M; i++)
            for (int j = 0; j < N; j++)
                C.data[i][j] = A.data[i][j] + B.data[i][j];
        return C;
    }


    // return C = A - B
    public Matrix minus(Matrix B) {
        Matrix A = this;
        if (B.M != A.M || B.N != A.N) throw new RuntimeException("Illegal matrix dimensions.");
        Matrix C = new Matrix(M, N);
        for (int i = 0; i < M; i++)
            for (int j = 0; j < N; j++)
                C.data[i][j] = A.data[i][j] - B.data[i][j];
        return C;
    }

    // does A = B exactly?
    public boolean eq(Matrix B) {
        Matrix A = this;
        if (B.M != A.M || B.N != A.N) throw new RuntimeException("Illegal matrix dimensions.");
        for (int i = 0; i < M; i++)
            for (int j = 0; j < N; j++)
                if (A.data[i][j] != B.data[i][j]) return false;
        return true;
    }

    // return C = A * B
    public Matrix times(Matrix B) {
        Matrix A = this;
        if (A.N != B.M) throw new RuntimeException("Illegal matrix dimensions.");
        Matrix C = new Matrix(A.M, B.N);
        for (int i = 0; i < C.M; i++)
            for (int j = 0; j < C.N; j++)
                for (int k = 0; k < A.N; k++)
                    C.data[i][j] += (A.data[i][k] * B.data[k][j]);
        return C;
    }


    // return x = A^-1 b, assuming A is square and has full rank
    public Matrix solve(Matrix rhs) {
        if (M != N || rhs.M != N || rhs.N != 1)
            throw new RuntimeException("Illegal matrix dimensions.");

        // create copies of the data
        Matrix A = new Matrix(this);
        Matrix b = new Matrix(rhs);

        // Gaussian elimination with partial pivoting
        for (int i = 0; i < N; i++) {

            // find pivot row and swap
            int max = i;
            for (int j = i + 1; j < N; j++)
                if (Math.abs(A.data[j][i]) > Math.abs(A.data[max][i]))
                    max = j;
            A.swap(i, max);
            b.swap(i, max);

            // singular
            if (A.data[i][i] == 0.0) throw new RuntimeException("Matrix is singular.");

            // pivot within b
            for (int j = i + 1; j < N; j++)
                b.data[j][0] -= b.data[i][0] * A.data[j][i] / A.data[i][i];

            // pivot within A
            for (int j = i + 1; j < N; j++) {
                double m = A.data[j][i] / A.data[i][i];
                for (int k = i+1; k < N; k++) {
                    A.data[j][k] -= A.data[i][k] * m;
                }
                A.data[j][i] = 0.0;
            }
        }

        // back substitution
        Matrix x = new Matrix(N, 1);
        for (int j = N - 1; j >= 0; j--) {
            double t = 0.0;
            for (int k = j + 1; k < N; k++)
                t += A.data[j][k] * x.data[k][0];
            x.data[j][0] = (b.data[j][0] - t) / A.data[j][j];
        }
        return x;
   
    }

    // print matrix to standard output
    public void show() {
        for (int i = 0; i < M; i++) {
           for (int j = 0; j < N; j++)
               System.out.printf("%9.4f ", data[i][j]);
            System.out.println();
        }
    }

 
}



class DocumentVectorCalculator {

List<String> documentList;
public List<Hashtable<String,Double>> scoreVector;


public DocumentVectorCalculator(List<String> documentList)
{
	this.documentList=documentList;
	scoreVector=new ArrayList<Hashtable<String,Double>>(this.documentList.size());
}


public void generateTFScores()
{
	for(String t:documentList)
	{
	
		StringTokenizer stNew=new StringTokenizer(t," ");
		Hashtable<String, Double> tempHash=new Hashtable<String,Double>();
		double max=1.0;
		
		while(stNew.hasMoreTokens())
		{
				String token=stNew.nextToken();
            
		
				
				if(tempHash.containsKey(token))
                    tempHash.put(token, Double.valueOf((tempHash.get(token)+1)));
				else
                    tempHash.put(token, 1.0);
			
                double freq=tempHash.get(token);
                if(max<freq)
                    max=freq;
		
		
		}
		//System.out.println(tempHash.toString());
		//System.out.println("Max="+max);
		
		
		for(String key:tempHash.keySet())
		{
			tempHash.put(key, tempHash.get(key)/max);
		}
			
		
		
		scoreVector.add(tempHash);
        //System.out.println(scoreVector);
    }
	
}

private double calcIDF(String word)
{
	int rho=documentList.size();
	int rhow=0;
	
	
	for(Hashtable<String,Double> temp:scoreVector)
	{
		if(temp.containsKey(word))
			rhow++;
	}
	
	//System.out.println(word+" rho="+rho+" rhow="+rhow);
	
	return Math.log((double)rho/(double)rhow);
}

/*
 * Generates TF-IDF scores
 * 
 */
public List<Hashtable<String,Double>> generateTF_IDFScores()
{
	generateTFScores();

	//System.out.println("After only TF Scores");
	//System.out.println(scoreVector.toString());
	
	for(Hashtable<String,Double> tempHash:scoreVector)
	{
		
		for(String key:tempHash.keySet())
		{
			tempHash.put(key, tempHash.get(key)*calcIDF(key));
			//System.out.println(key+" =>"+calcIDF(key));
			
		}
		
			
	}
	

	return scoreVector;
}

}

class Constants {

	public static final String EMPTY_URL_FIELD="Empty URL field";
	public static final String TEXT_EXTRACTION_FAILED="Problem in Extracting Text From Web";
}

/**
   Copyright (c) 2007 and ongoing, University of Pittsburgh
   and the SemanticVectors AUTHORS.

   All rights reserved.

   Redistribution and use in source and binary forms, with or without
   modification, are permitted provided that the following conditions are
   met:

 * Redistributions of source code must retain the above copyright
   notice, this list of conditions and the following disclaimer.

 * Redistributions in binary form must reproduce the above
   copyright notice, this list of conditions and the following
   disclaimer in the documentation and/or other materials provided
   with the distribution.

 * Neither the name of the University of Pittsburgh nor the names
   of its contributors may be used to endorse or promote products
   derived from this software without specific prior written
   permission.

   THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
   "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
   LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
   A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
   CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
   EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
   PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
   PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
   LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
   NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
   SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 **/

package pitt.search.semanticvectors.viz;

import pitt.search.semanticvectors.FlagConfig;
import pitt.search.semanticvectors.ObjectVector;
import pitt.search.semanticvectors.Search;
import pitt.search.semanticvectors.vectors.IncompatibleVectorsException;
import pitt.search.semanticvectors.vectors.RealVector;
import pitt.search.semanticvectors.vectors.ZeroVectorException;
import ch.akuhn.edu.mit.tedlab.DMat;
import ch.akuhn.edu.mit.tedlab.SVDRec;
import ch.akuhn.edu.mit.tedlab.Svdlib;

/**
 * Class for creating 2d plots of search results.
 * 
 * Basic usage is something like: In the main semantic vectors source directory:
 * ant compile-ext In the directory with your vector indexes: java
 * pitt.util.vectors.PrincipalComponents $ARGS
 * 
 * $ARGS includes first regular semantic vectors flags, e.g., -queryvectorfile
 * and -numsearchresults, followed by query terms.
 */
public class PrincipalComponents
{
    ObjectVector[] vectorInput;
    DMat matrix;
    Svdlib svd;
    SVDRec svdR;
    int dimension;

    public PrincipalComponents(ObjectVector[] vectorInput)
    {
        this.vectorInput = vectorInput;
        this.dimension = vectorInput[0].getVector().getDimension();
        double[][] vectorArray = new double[vectorInput.length][dimension];

        for (int i = 0; i < vectorInput.length; ++i)
        {
            if (vectorInput[i].getVector().getClass() != RealVector.class)
            {
                throw new IncompatibleVectorsException(
                        "Principal components class only works with Real Vectors so far!");
            }
            if (vectorInput[i].getVector().getDimension() != dimension)
            {
                throw new IncompatibleVectorsException(
                        "Dimensions must all be equal!");
            }
            RealVector realVector = (RealVector) vectorInput[i].getVector();
            //System.out.print("realVector:"+realVector);
            float[] tempVec = realVector.getCoordinates().clone();
           /* System.out.print("tempVec"+vectorInput[i].getObject()+","+tempVec.length);
             
            for(int ii=0;ii<10;ii++){
            	System.out.print(tempVec[ii]+",");
            }System.out.println("###");*/
            for (int j = 0; j < dimension; ++j)
            {
                vectorArray[i][j] = (double) tempVec[j];
            }
        }
        //System.out.println();
        //System.err.println(vectorInput.length);
        this.matrix = new DMat(vectorArray.length, vectorArray[0].length);
        matrix.value = vectorArray;
        System.err.println("Created matrix ... performing svd ...");
        Svdlib svd = new Svdlib();
        System.err.println("Starting SVD using algorithm LAS2");
        System.err.println("principal components:"+matrix.rows+","+matrix.cols);
        int count=vectorInput.length;
          System.err.println("principal components dimension:"+dimension);
          System.err.println("principal components count:"+count);
        if(matrix.cols>=count)
        	   dimension=count/2;
        if(matrix.cols<count && matrix.cols>count/2){
        	   dimension=count/2;
           }
        svdR = svd
                .svdLAS2A(Svdlib.svdConvertDtoS(matrix), dimension);//100
    }

    // Now we have an object with the reduced matrices, plot some reduced
    // vectors.
    public void plotVectors()
    {
        DMat reducedVectors = this.svdR.Ut;
        ObjectVector[] vectorsToPlot = new ObjectVector[vectorInput.length];
        int truncate = 4;//4
        for (int i = 0; i < vectorInput.length; i++)
        {
            float[] tempVec = new float[truncate];
            /*
             * for (int j = 0; j < truncate; ++j) { tempVec[j] = (float)
             * (reducedVectors.value[j][i]); }
             */
            for (int j = 0; j < truncate; j++)
            {
                tempVec[j] = (float) (reducedVectors.value[j][i]);
            }
            vectorsToPlot[i] = new ObjectVector(vectorInput[i].getObject()
                    .toString(), new RealVector(tempVec));
        }
        Plot2dVectors myPlot = new Plot2dVectors(vectorsToPlot);
        myPlot.createAndShowGUI();
    }

    // added by yl   
    public void plotVectorsFromLda()
    {
        Plot2dVectors myPlot = new Plot2dVectors(this.vectorInput);
        myPlot.createAndShowGUI();
    }
    public ObjectVector[] get2DVectors()
    {
        DMat reducedVectors = this.svdR.Ut;
        ObjectVector[] vectorsToPlot = new ObjectVector[vectorInput.length];
        int truncate = 2;
        // System.out.println("reducedVectors:"+reducedVectors.toString());
        for (int i = 0; i < vectorInput.length; i++)
        {
            float[] tempVec = new float[truncate];
            for (int j = 0; j < truncate; j++)
            {
                tempVec[j] = (float) (reducedVectors.value[j][i]);
            }
            vectorsToPlot[i] = new ObjectVector(vectorInput[i].getObject()
                    .toString(), new RealVector(tempVec));
        }
        this.paintComponent(vectorsToPlot);
        return vectorsToPlot;
    }
    public ObjectVector[] getReductionVectors(int dimension)
    {
        DMat reducedVectors = this.svdR.Ut;
        //System.out.println(reducedVectors.toString());
        ObjectVector[] vectorsToPlot = new ObjectVector[vectorInput.length];
        int truncate = dimension;
        System.err.println("getReductionVector dimension:"+dimension);
        // System.out.println("reducedVectors:"+reducedVectors.toString());
        for (int i = 0; i < vectorInput.length; i++)
        {
            float[] tempVec = new float[truncate];
            for (int j = 0; j < truncate; j++)
            {
                tempVec[j] = (float) (reducedVectors.value[j][i]);
                //System.err.println(i+","+j+","+reducedVectors.value[j][i]);
            }
            vectorsToPlot[i] = new ObjectVector(vectorInput[i].getObject()
                    .toString(), new RealVector(tempVec));
        }
        //this.paintComponent(vectorsToPlot);
        return vectorsToPlot;
    }
    public ObjectVector[] get2DVectorsPrimitive()
    {
        DMat reducedVectors = this.svdR.Ut;
        ObjectVector[] vectorsToPlot = new ObjectVector[vectorInput.length];
        int truncate = 100;
        System.out.println("reducedVectors:"+reducedVectors.toString());
        for (int i = 0; i < vectorInput.length; i++)
        {
            float[] tempVec = new float[truncate];
            for (int j = 0; j < truncate; j++)
            {
                tempVec[j] = (float) (reducedVectors.value[j][i]);
            }
            vectorsToPlot[i] = new ObjectVector(vectorInput[i].getObject()
                    .toString(), new RealVector(tempVec));
        }
        return vectorsToPlot;
    }

    public void paintComponent(ObjectVector[] vectors)
    {
        int scale = 500;
        int pad = 50;
        int comp1 = 0;
        int comp2 = 1;
        // int maxplot = 60;
        int c1, c2;
        float min1, max1, min2, max2;
        min1 = min2 = 100;
        max1 = max2 = -100;
        // Compute max and min.
        for (int i = 0; i < vectors.length; ++i)
        {
            RealVector realVector = (RealVector) vectors[i].getVector();
            float[] tmpVec = realVector.getCoordinates();
            if (tmpVec[comp1] < min1)
                min1 = tmpVec[comp1];
            if (tmpVec[comp2] < min2)
                min2 = tmpVec[comp2];
            if (tmpVec[comp1] > max1)
                max1 = tmpVec[comp1];
            if (tmpVec[comp2] > max2)
                max2 = tmpVec[comp2];
            /*
             * if (i > maxplot) { break; }
             */
        }

        for (int i = 0; i < vectors.length; ++i)
        {
            RealVector realVector = (RealVector) vectors[i].getVector();
            //System.out.println("rv:" + realVector);
            float[] tmpVec = realVector.getCoordinates();
            c1 = (pad / 2)
                    + Math.round(scale * (tmpVec[comp1] - min1) / (max1 - min1));
            c2 = (pad / 2)
                    + Math.round(scale * (tmpVec[comp2] - min2) / (max2 - min2));
            tmpVec[comp1] = c1;
            tmpVec[comp2] = c2;
            /*
             * if( i > maxplot ){ break; }
             */
            //System.out.println("c1:" + c1 + ",c2:" + c2);
        }
    }

    /**
     * Main function gathers search results for a particular query, performs
     * svd, and plots results.
     */
    public static void main(String[] args) throws ZeroVectorException
    {
        args = new String[]
        { "-luceneindexpath", "luceneindexpath", "-elementalmethod",
                "CONTENTHASH", "-queryvectorfile", "termvectors.bin",
                "-searchvectorfile", "docvectors.bin", "移动通信",// The same was in
                                                              // the beginning
                                                              // with God.
        };
        ;
        // Stage i. Assemble command line options.
        FlagConfig flagConfig = FlagConfig.getFlagConfig(args);
        args = flagConfig.remainingArgs;

        // Get search results, perform clustering, and print out results.
        ObjectVector[] resultsVectors = Search
                .getSearchResultVectors(flagConfig);
        PrincipalComponents pcs = new PrincipalComponents(resultsVectors);
        pcs.plotVectors();
    }
}

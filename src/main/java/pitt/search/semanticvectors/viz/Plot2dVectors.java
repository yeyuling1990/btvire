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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.swing.JFrame;
import javax.swing.JPanel;

import pitt.search.semanticvectors.ObjectVector;
import pitt.search.semanticvectors.vectors.RealVector;

/**
   Class for plotting 2d vectors as a Swing component on your screen.

   Also contains a very grubby implementation of a printout in TeX
   readable format, which can be accessed by setting PRINT_TEX_OUTPUT=true
   internally.
 */
public class Plot2dVectors extends JPanel {
  ObjectVector[] vectors;
  static final int scale = 500;
  static final int pad = 50;
  static final int comp1 = 0;//1
  static final int comp2 = 1;//2
  static final int maxplot = 50;
  // Setting this to true will make the plotter output TeX source to
  // your console.
  public final boolean PRINT_TEX_OUTPUT = false;

  public Plot2dVectors (ObjectVector[] vectors) {
    System.err.println("Constructing plotter ...");
    this.vectors = vectors;
    this.setSize(new Dimension(scale + 2*pad, scale + 2*pad));
  }

  //
  private Random randomNumber = new Random();

  private Color getRandomColor() {
      return new Color(randomNumber.nextFloat(),
              randomNumber.nextFloat(), randomNumber.nextFloat());
  }
  //
  @Override
  public void paintComponent(Graphics g) {
    int c1, c2;
    float min1, max1, min2, max2;
    min1 = min2 = 100;
    max1 = max2 = -100;
    List<Integer> centroids=new ArrayList<Integer>();
    //Color[] colors=new Color[]{Color.black,Color.red,Color.green,Color.orange,Color.blue};
    List<Color> colors=new ArrayList<Color>();
    // Compute max and min.
    for (int i = 0; i < vectors.length; ++i) {
    	String centroid=vectors[i].getObject().toString();
    	//System.err.println(centroid);
    	if(centroid.startsWith("centroid")){
    		centroids.add(i);
    		colors.add(this.getRandomColor());
    	}
      RealVector realVector = (RealVector) vectors[i].getVector();
      float[] tmpVec = realVector.getCoordinates();
      if (tmpVec[comp1] < min1) min1 = tmpVec[comp1];
      if (tmpVec[comp2] < min2) min2 = tmpVec[comp2];
      if (tmpVec[comp1] > max1) max1 = tmpVec[comp1];
      if (tmpVec[comp2] > max2) max2 = tmpVec[comp2];
      if (i > maxplot) {
        //break;
      }
    }

    System.err.println("Painting component ...");
    if (PRINT_TEX_OUTPUT) {
      int len = scale + pad;
      System.out.println("\\begin{figure}[t]");
      System.out.println("\\begin{center}");
      System.out.println("\\footnotesize");
      System.out.println("\\setlength{\\unitlength}{.55pt}");
      System.out.println();
      System.out.println("\\begin{picture}(" + len + "," + len + ")");
      System.out.println("\\put(0,0){\\framebox(" + len + "," + len + "){}}");
    }
    List<Integer> xs=new ArrayList<Integer>();
    List<Integer> ys=new ArrayList<Integer>();
    for (int i = 0; i < vectors.length; ++i) {
      RealVector realVector = (RealVector) vectors[i].getVector();
      float[] tmpVec = realVector.getCoordinates();
      c1 = (pad/2) + Math.round(scale*(tmpVec[comp1]-min1) / (max1-min1));
      c2 = (pad/2) + Math.round(scale*(tmpVec[comp2]-min2) / (max2-min2));

      //System.err.println("centroids size:"+centroids.size());
      int index=0;
      for(int k=0;k<centroids.size()-1;k++){
        	if(i>centroids.get(k) && i<=centroids.get(k+1))
        		index=k+1;
        }
      g.setColor(colors.get(index));
      g.drawString(vectors[i].getObject().toString(), c1, c2);
       xs.add(c1);
       ys.add(c2);
      if( i > maxplot ){
       // break;
        }

      if (PRINT_TEX_OUTPUT) {
        System.out.println("\\put(" + c1 + "," + c2 + ")" + "{\\makebox(0,0){"
                           + vectors[i].getObject().toString() + "}}");

      }
    }
    /*int index=-1;;
    for(int i=0;i<centroids.size();i++){
    	 g.setColor(colors.get(i));
    	 int j=centroids.get(i);
    	 int x1=xs.get(j);
    	 int y1=ys.get(j);
    	 for(int k=index+1;k<=j;k++){
    		 index++; 
    		 int x2=xs.get(k);
    		 int y2=ys.get(k);
    		 g.drawLine(x1, y1, x2, y2);
    	 }
     }*/
    if (PRINT_TEX_OUTPUT) {
      System.out.println("\\end{picture}");
      System.out.println("\\caption{ADD CAPTION}");
      System.out.println("\\label{ADD LABEL}");
      System.out.println("\\end{center}");
      System.out.println("\\end{figure}");
    }

    System.err.println("Finished painting component ...");
    for(Integer i:centroids){
    	System.out.println(i);
    }
  }

  public void createAndShowGUI() {
    // Create and set up the window.
    JFrame frame = new JFrame("Term Vector Plotter");
    frame.setSize(new Dimension(scale+2*pad, scale+2*pad));
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    System.err.println("Trying to set content pane ...");
    frame.setContentPane(this);
    // Display the window.
    frame.setVisible(true);
  }
}

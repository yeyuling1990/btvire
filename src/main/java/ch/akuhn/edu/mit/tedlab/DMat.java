/**
 *   @author Adrian Kuhn
 *   @author David Erni   
 *             
 *      Copyright (c) 2010 University of Bern
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package ch.akuhn.edu.mit.tedlab;

public class DMat {
    public int rows;
    public int cols;
    public double[][] value; /*
     * Accessed by [row][col]. Free value[0] and value to
     * free.
     */

    public DMat(int rows, int cols) {
        this.rows = rows;
        this.cols = cols;
        this.value = new double[rows][cols];
    }
    //added by yl
    public String toString(){
    	StringBuilder sb=new StringBuilder();
    	sb.append("rows:"+rows);
    	sb.append(",");
    	sb.append("cols:"+cols);
    	sb.append("\n");
    	for(int i=0;i<rows;i++){
    		for(int j=0;j<10;j++){//cols
    			sb.append(value[i][j]);
    			sb.append(" ");
    		}
    		sb.append("\n");
    	}
    	return sb.toString();
    }
}
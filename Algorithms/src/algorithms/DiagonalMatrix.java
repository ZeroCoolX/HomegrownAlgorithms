/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package algorithms;

/**
 *
 * @author dewit
 */
public class DiagonalMatrix {
    
    public static void main(String[] args) throws java.lang.Exception {
        int[][] A = new int[4][4];

        /*fill starting diagnol from (i,j)->(i+1, j+1)->(i+2,j+2)->...->(i+n,j+n)
         literally just give it any values. everthing higher than this diagonal will be calculated*/
        A[0][0] = 3;
        A[1][1] = 2;
        A[2][2] = 4;
        A[3][3] = 3;
        prettyPrint(calculateDiagonals(A));
    }

    private static int[][] calculateDiagonals(int[][] arr) {
        int count = 0;
        while (count < arr.length - 1) {
            int i = 0;
            int j = count + 1;
            while (j <= arr.length - 1) {
                int i2 = i;
                int j2 = j;
                int temp = 0;
                while (i2 != j && j2 != i) {
                    //	System.out.println("Multiplying: A[i][i2] * A[j2][j2] = " + A[i][i2] + " * " +A[j2][j]);
                    temp += (arr[i][i2] * arr[j2][j]);
                    ++i2;
                    --j2;
                }
                arr[i][j] = temp;
                ++i;
                ++j;
            }
            ++count;
        }
        return arr;
    }
    
    private static void prettyPrint(int[][] arr) {
        for (int i = 0; i < arr.length; ++i) {
            for (int j = 0; j < arr.length; ++j) {
                System.out.print(arr[i][j] + " ");
            }
            System.out.print("\n");
        }
    }

}

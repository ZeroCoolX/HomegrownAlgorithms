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
public class KthSmallestElementSearch {

        public static void main(String[] args) {
            int[] A = {4, 5, 7, 45, 67, 77, 86, 94, 101};
            int[] B = {1, 3, 6, 8, 23, 24, 34, 44, 51};
            int k = 6;
            System.out.print("\nDetermine the kth element under the union of the two arrays\nA: {");
            for(int i=0; i < A.length; ++i){
                System.out.print(A[i]);
                if(i==A.length-1){
                    System.out.println("}");
                }else{
                    System.out.print(",");
                }
            }
            System.out.print("\nB: {");
            for(int i=0; i < B.length; ++i){
                System.out.print(B[i]);
                if(i==B.length-1){
                    System.out.println("}");
                }else{
                    System.out.print(",");
                }
            }
            System.out.println("\n\n\tThe "+k+"th element: "+getKthSmallest(A, B, k));
        }
        
        
        
        /**
         * 
         * @param h high threshold 
         * @param l low threshold
         * @return Return the middle value between the low and the high. 
         * EX: 6-0/2 = 3 + 0 = 3 (index 3 which middle of 0->6)
         * EX: 4-2/2 = 1 + 2 = 3 (index 3 exact middle of 2->4)
         */
        private static int getK(int h, int l){
            return l+((h-l)/2);
        }

        private static int getKthSmallest(int[] a, int[] b, int k) {
            //lower thresholds begin at 0 for both arrays
            int lowA = 0, lowB = 0;
            //higher thresholds begin at either array's length or k-1 (whichever is smaller)
            int highA = Math.min(a.length-1, k-1);
            int highB = Math.min(b.length-1, k-1);
            if(highA <= 0){//there are no elements in A so just return the kth index of B
                return b[highB];
            }else if(highB <= 0){//no elements in B so same as above
                return a[highA];
            }else if((highA + highB) < k-1){//both array lengths summed up can't be less than K...
                return -1;
            }
            int k1, k2 = 0;
            int kth = 0;
            //we want to half k each time we loop through (in order to keep its runtime O(logn)
            while(k>0){
                //set k1 and k2 to respective mids
                k1 = getK(highA, lowA);
                k2 = getK(highB, lowB);
                /*System.out.println("k1 = " + k1 + "\n k2 = " + k2);
                System.out.println("lowA = " + lowA + "\n lowB = " + lowB);
                System.out.println("highA = " + highA + "\n highB = " + highB);*/
                
                /*this indicates that the element at k1 is larger than k2
                since we know k2+1 is >= k2 (because the arrays are sorted) and k2<=k1, then it holds that its possible k2+1<=k1 which means theres a possibilty that the 
                kth element lies to the right of k2.
                Simmilarly since we know k1-1<k1 (sorted) but k1>=k2, its possible that k1-1>=k2 so that means there is a possibility that the kth elements lies to the 
                left of k1.*/
                if(a[k1] >= b[k2]){
                    //decrement k by 1 + the difference between the range [smaller elements low,higher elements mid]
                    k -= 1+(k2 - lowB);
                    //store the smaller of the two elements in case we're looping our last time and we are actually ON the kth smallest element
                    kth = b[k2];
                    //highA now becomes k1-1 (one less than k1 which we already compared)
                    highA = k1-1;
                    //lowB now becomes k2+1 (one MORE than k2)
                    lowB = k2+1;                    
                    
                }else{//same explaination as above holds for if a[k1] < b[k2] just reverse directions obviously
                    //decrement k by 1 + the difference between the range [smaller elements low, higher elements mid]
                    k -= 1+(k1 - lowA);
                    //store the smaller of the two elements in case we're looping our last time and we are actually ON the kth smallest element
                    kth = a[k1];
                    //highA now becomes k1-1 (one less than k1 which we already compared)
                    highB = k2-1;
                    //lowB now becomes k2+1 (one MORE than k2)
                    lowA = k1+1;    
                }
                System.out.println("k = " + k);
            }
            return kth;
        }

    }


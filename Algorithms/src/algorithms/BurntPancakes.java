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
public class BurntPancakes {
    private static int [] P = 
            //new int[8];
            //{-3,2,4,-1,-5}
            {-12,-4,-2,13,3,9,-10,-1,-5,8,-11,-7,-6};
    
    public static void main (String [] args){
        System.out.println("ready to flip some perncerks?!?!");
        print(P, "Before Flipping");
        presentPancakes();
        print(P, "After Flipping");
    }
    
    /*
        RUNTIME analysis
        The outer while loop(){
            Proportional to the number of elements so -> 
          O(n)
            the inner for loop(){
                Proportional to the index at which is search for which is n-1, n-2, n-3,...n-n
                But each time we correctly place an entry, the number of times this loop has to run is 1 less, which is proportional to the growth rate of the numComplete which tracks how many entries we've processed
                So if say 
                n=P.length-1, and
                k=numComplete
                then this would run n-k, n-(k+1), n-(k+2),...,n-(n-k) so ->
              O(n-(n-k))
                then a maximum of 3 flips are called each iteration each proportional to the number of items that need to be flipped
                In the worst case flip(n) would be called running in n/2 time. Then the next iteration of the loop flip is called it can only have a max of flip(n-1) running in (n-1)/2 because we already correctly processed 
                one item, pushed it as far right as possible, and moved left so that entry will never be processed again. Then the next time the max is flip(n-2),flip(n-3),flip(n-(n-1)) -> running in -> (n-2)/2, (n-3)/2, (n-(n-1))/2 time
                thus, it runs proportionaly to the growth rate (once again) of how many items we've processed already
                (n-k)/2 + (n-(k+1))/2 +...+ (n-(n-k+1))/2 so ->
              O(n-(n-k+1))/2
            }
        }
    
        The larger of the two:   O(n-(n-k+1))/2   and     O(n-(n-k))  is   O(n-(n-k))
        So since the runtime is O(n) on the outside, and O(n-(n-k)) on the inside they're multiplied to get
        O(n) * O(n-(n-k)) =>  O(n) * O(n-n+k) =>  O(n) * O(k) [but k is proportional to n so] =>  O(n) * O(n) = O(n^2)
    */
    
    private static void presentPancakes(){
        //finally finished when each value is exactly equal to value-1 index in the array. thus [1,2,3,4,5] -> [0,1,2,3,4] is complete
        int numComplete = 0;
        //scan for the element = P.length-1 and place it at the farthest right possible at index L-1, then repeat placing it at index (L-2, L-3,...,L-L)
        while(numComplete < P.length){//O(P.length) -> O(n)
            int targetIndex = (P.length-1)-numComplete; //given P.length = 5, then targetIndex will be 5-0=5, then 5-1=4, then 5-2=3....etc
            //System.out.println("targetIndex = " + targetIndex + "\nnumComplete = " + numComplete + "\nP["+targetIndex+"] = " + P[targetIndex]);
            if(P[targetIndex]!=targetIndex+1){//it's not in the right spot so begin looking backwards from here
               for(int i = targetIndex; i >= 0; --i){//O(n)
                   //System.out.println("Math.abs("+P[i]+"])=="+(targetIndex+1));
                   if(Math.abs(P[i])==targetIndex+1){
                       //flip to the first, (then flip just the first once more if needed) then flip to the targetIndex
                       if(P[i]<0){
                           flip(i+1);
                           flip(1);
                           flip(targetIndex+1);
                       }else{
                           flip(i+1);
                           flip(targetIndex+1);
                       }
                       ++numComplete;
                       break;
                   }
               }
            }else{
                ++numComplete;
            }
        }
    }
    
    //the number value-1 corresponds to the index it should be located at in the array. for any j it should be located in P[j-1]
    private static void flip(int flipHere){
        for(int i = flipHere-1; i>((flipHere-1)/2);--i){//O(n/2)
            int temp = P[i]*-1;
            P[i]=P[(flipHere-1)%i]*-1;
            P[((flipHere-1)%i)] = temp;
            System.out.println("switching " +i+" with" + ((flipHere-1)%i) + " and " + ((flipHere-1)%i) + " with "+i);
        }
        if(flipHere%2!=0){//must flip the sign bit of the exact middle since it didn't move
            P[(flipHere-1)/2] *= -1;//O(1)
        }
    }
    
        private static void print(int[] A, String str){
            System.out.print(str+"\n[");
        for(int i = 0; i < A.length; ++i){
            System.out.print(A[i]);
            if(i+1 != A.length){
                System.out.print(",");
            }else{
                System.out.println("]");
            }
        }
    }
    
}


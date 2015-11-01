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
public class InPlaceSwapSort {
    public static void main(String[] args){
        int [] A = new int[] 
        {0,1,-1,-1,1,0,1,0,-1,0,-1,-1,1,1,0,-1,-1,1,0,0,-1}
        /*{1,1,1,1,1,1,1,1,1,1}*/
        /*{0,0,0,0,0,0,0,0,0}*/
        /*{-1,-1,-1,-1,-1,-1,-1}*/
        /*{0,1,-1,-1,1,2,0,1,0,-1,0}*/;
        print(A,"BEFORE In Place Swap Sort");
        print(inPlaceSwapSort(A),"AFTER In Place Swap Sort");
    }
    
    private static int[] inPlaceSwapSort(int[] A){
        try{
            int zb=-1, ze=-1, one=-1, neg = -1;
            for(int i = 0; i < A.length; ++i){
                if(A[i]==-1){
                    neg = i;
                    if(zb >= 0){
                        while(zb-neg != 1){
                            if(A[neg-1]==0&&neg-1==zb){
                                ++zb;
                            }else if (A[neg-1]==1&&neg-1==one){
                                ++one;
                            }
                            swap(A, neg, -1);
                            --neg;
                        }
                    }
                }else if (A[i]==0){
                    if(zb<0){
                        zb = i;
                    }
                    ze=i;
                    if(one>=0){
                        while(one-ze != 1){
                            if(A[ze-1] == -1&&ze-1==neg){
                                ++neg;
                            }else if(A[ze-1]==1&&ze-1==one){
                                ++one;
                            }
                            swap(A, ze, -1);
                            --ze;
                        }
                    }
                }else if(A[i]==1){
                    if(one<0){
                        one = i;
                    }
                    while(one- (ze>=zb?ze:zb) != 1 && one<A.length-1 && A[one+1]!=1){
                        if(ze>=zb){
                            --ze;
                        }else{
                             --zb;
                        }
                        swap(A, one, 1);
                        ++one;
                    }
                }else{
                    throw new Exception(A[i]+" is not a valid character from the alphabet \nReturning unfinished collection");
                }
            }
        }catch(Exception e){
            System.out.println(e.getMessage());
        }
        return A;
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
    

    
    private static void swap(int [] arr, int swap, int pad){
        //if -1 is passed as the inc, dont inc
        int temp = arr[swap];
        arr[swap] = arr[swap+pad];
        arr[swap+pad] = temp;
    }
    
}

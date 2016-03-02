# HomegrownAlgorithms

This repository holds a mix of original and replicated (for learning purposes) algorithms.
The main algorithms to note are SwiftPathing.java, Solver.java and SolverV2.java found in Algorithms/src/algorithms.

SwiftPathing.java was the result of being commissioned Lucky 8's Entertainment LLC to come up with an algorithm which could randomly generate a level for a game they created called "Shift" which is a puzzle based app game on the Google Play store. The task involved creating a maze-like level given certain parameters (such as wall density, number of moves, starting position...etc) that was solvable in >= x moves. 
The level had to first be solvable, then solvable only in a specific number of moves (it could however be solved in more moves just not less), the path taken to solve it needed to be recorded, and finally the entire level needed to be recreated into its visual counterpart written in XML so the game could simply take the XML file in and use the level in the game.

I wrote SwiftPathing from start to finish in 5 days from Nov 5-10. On the 10th of November I showed the company a working presentation of the level generation which could be run any number of times to generate any number of levels which were each re-contrsucted as XML files to easily be posted into their game.
After showing a colleague, Nick, what I had done he asked if he could use my code as a reference and attempt to recreate the algorithm himself. Nick rewrote the level generation algorithm, and together in using Nicks level gen algorithm and my original XML file creation algorithm we were able to construct a more efficient (in processing power) and space conservative (by using parallel computations with threads and abstracting many of the original ideas and data structures) product. The outcome of this was Solver.java.

Because of this, myself and Nick were brought into Lucky 8's Entertainment LLC as contractors to continue upkeep on the level and xml creation algorithms which turned into myself being brought into the actual app development in addition to the algorithm upkeep.

Later, SolverV2.java was created due to Lucky 8's changing the way they needed their levels physically contructed, which went from XML to a MUCH, much more efficient string representation.


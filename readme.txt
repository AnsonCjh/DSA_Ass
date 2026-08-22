================================================================================
                    TARUMT RESORT MANAGEMENT SYSTEM
               Data Structures & Algorithms (DSA) Assignment
================================================================================

PROJECT DETAILS
--------------------------------------------------------------------------------
- Project Name   : TARUMT Resort Management System
- Main Class     : dsa_ass.DSA_Ass
- Architecture   : Entity-Control-Boundary (ECB) Pattern
- Custom ADTs    : Queue<T>, Stack<T>, BinarySearchTree<T> (Located in dsa_ass.adt)
- Data Storage   : CSV files (Located in data/ folder)


SYSTEM REQUIREMENTS
--------------------------------------------------------------------------------
- IDE            : Apache NetBeans 21 / 23
- Java Version   : JDK 21


================================================================================
HOW TO RUN THE PROJECT (FOR TUTOR)
================================================================================

--- METHOD 1: VIA APACHE NETBEANS (RECOMMENDED) ---

1. Open Apache NetBeans 21 or 23.
2. Click: File -> Open Project...
3. Browse and select the "DSA_Ass" project folder, then click "Open Project".
4. Ensure Java Platform is set to JDK 21 (Right-click project -> Properties -> Libraries/Sources).
5. Right-click the "DSA_Ass" project -> Click "Clean and Build".
6. Right-click project -> "Run".


--- METHOD 2: VIA COMMAND PROMPT / TERMINAL (OPTIONAL) ---

Open Command Prompt / PowerShell in the "DSA_Ass" folder:

1. Compile:
   javac -encoding UTF-8 -d bin src\dsa_ass\*.java src\dsa_ass\adt\*.java src\dsa_ass\entity\*.java src\dsa_ass\control\*.java src\dsa_ass\boundary\*.java src\dsa_ass\module\*.java src\dsa_ass\util\*.java

2. Run:
   java -cp bin dsa_ass.DSA_Ass


================================================================================
SYSTEM MODULES & MAIN MENU
================================================================================
When the application starts, select from the main menu:

  [1] Walk-In Registration Module
      - Register walk-in guests (FIFO Queue ADT)
      - View & process waiting queue
      - Instant room booking & confirmation generation
      - Management Reports

  [2] Front Desk Service Module
      - Search reservation by Confirmation Number (BST ADT)
      - Guest profile management & search
      - Check-In & Check-Out (Auto-updates room status & triggers cleaning task)
      - Management Reports

  [3] Housekeeping Module
      - Cleaning task management (LIFO Stack ADT)
      - Room inspection & status lifecycle
      - Undo last room status change (LIFO Status History Stack)
      - Management Reports

  [0] Exit (Auto-saves all data to data/*.csv)


================================================================================
NOTES FOR TUTOR / MARKING
================================================================================
1. Custom ADT Implementation (No Java Collections Framework):
   - All lists, queues, stacks, and trees are built from scratch under:
     src/dsa_ass/adt/
     * Queue.java              -> Custom FIFO linked queue
     * Stack.java              -> Custom LIFO linked stack
     * BinarySearchTree.java   -> Custom BST with in-order traversal

2. Sample Data:
   - Sample guests, rooms, and housekeeping tasks are pre-seeded in the data/ folder.
   - All transactions auto-save to CSV files in the data/ directory upon exit.
================================================================================

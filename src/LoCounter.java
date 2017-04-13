import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Scanner;

/*Important Limitation: Cannot count lines that have a multiline comment and then CODE as a code line.
*
* e.g: /*COMMENT*\/ CODE
* Would be counted as a comment line, even though it has code after the comment. Considered inconsequential in most cases.
*/
public class LoCounter {

   private static String usage = "Please run the program as follows:\n" +
           "java LoCouter top_level_directory_name\n\n";


   //todo - implement reading only specific file extensions
   private static String extenderUsage = "Please run the program as follows:\n" +
           "java LoCouter top_level_directory_name -extensions a b c\n\n" +
           "Where 'a,b,c' are the file extensions of the files you want counted. e.g \".java\"";

   private static DecimalFormat f = new DecimalFormat("#0.00");

   private static int numCommentLines = 0;
   private static int numCodeLines = 0;
   private static int numBlankLines = 0;

   private static int TotalNumCommentLines = 0;
   private static int TotalNumCodeLines = 0;
   private static int TotalNumBlankLines = 0;

   private static int numFiles = 0;

   private static String[] extensions = {"java", "cpp", "hpp", "c", "h"};

   public static void main(String[] args){

      if(args.length == 1) {
         try {
            //String fileName = "F:\\Programming_Projects\\2016Sem2\\OOSE\\Assignment";
            String fileName = args[0];

            File f = new File(fileName);

            //listFilesForFolder(f);

            System.out.println("Reading: " + f.getAbsolutePath());

            recurseRead(f);

            printTotalNumLines();
         }
         catch(IOException e){
            System.out.println("An internal error occurred, oops...?\n" + e.getMessage());
         }
      }
      else{
         System.out.println(usage);
      }
   }


   private static void recurseRead(File file) throws IOException{
      //If the given file is actually a directory.
      if( file.isDirectory() ) {
         for ( final File fileEntry : file.listFiles() ) {
            if ( fileEntry.isDirectory() && (!fileEntry.getName().endsWith(".git")) ) {
               recurseRead(fileEntry);
            } else {
               if(hasValidExtension(fileEntry.getName())) {
                  countLines(fileEntry);

                  printNumLines();
                  numFiles++;
                  TotalNumBlankLines += numBlankLines;
                  TotalNumCodeLines += numCodeLines;
                  TotalNumCommentLines += numCommentLines;

                  numBlankLines = 0;
                  numCodeLines = 0;
                  numCommentLines = 0;
               }
            }
         }
      }else{
         countLines(file);
      }
   }

   private static boolean hasValidExtension(String name){
      for(String valid : extensions){
         if(name.endsWith(valid) && (!name.endsWith(".git")) ){
            return true;
         }
      }
      //System.out.println("invalid extension: " + name);
      return false;
   }

   //Guaranteed to be a file at this point and not a directory, so start counting lines.
   private static void countLines(File f) throws IOException {
      System.out.println("File: " + f.getName());

      Scanner sc = new Scanner(f);

      String currLine;

      boolean inComment = false;

      while(sc.hasNext()){
         boolean codeLine = true;
         currLine = sc.nextLine();

         //If inside a comment section, count and check for end of comments
         if(inComment){
            codeLine = false;
            int ii = 0;
            char currChar;
            char prevChar = '=';
            while( (ii < currLine.length()) && inComment) {
               currChar = currLine.charAt(ii);
               //Check for exit comment
               if ((currChar == '/') && (prevChar == '*')) {
                  inComment = false;
               }

               prevChar = currChar;
               ii++;
            }

            numCommentLines++;
         }
         //If its blank, count and ignore.
         else if(isBlankLine(currLine)){
            numBlankLines++;
            codeLine = false;
         }
         else{
            boolean done = false;
            //while I haven't determined what the line is, keep trying
            char currChar;
            char prevChar = '+';
            int ii = 0;
            while( (ii < currLine.length()) && !done){
               currChar = currLine.charAt(ii);

               //Check single line comment
               if( (currChar == '/') && (prevChar == '/') ){
                  numCommentLines++;
                  codeLine = false;
                  done = true;
               }
               //Check multiline comment
               else if((currChar == '*') && (prevChar == '/')){
                  //Internal loop to check for end of comment
                  while( (ii < currLine.length())){
                     inComment = true;
                     currChar = currLine.charAt(ii);
                     //Check for exit comment
                     if( (currChar == '/') && (prevChar == '*') ){
                        inComment = false;
                     }

                     prevChar = currChar;
                     ii++;
                  }

                  numCommentLines++;
                  codeLine = false;
               }

               prevChar = currChar;
               ii++;
            }
         }if(codeLine){
            numCodeLines++;
         }
      }
   }

   private static boolean isBlankLine(String line){
      boolean isBlank = true;

      for(int ii = 0; ii < line.length(); ii++){
         if( (line.charAt(ii) != ' ') || (line.charAt(ii) != '\n') ){
            isBlank = false;
         }
      }

      return isBlank;
   }


   private static void printNumLines(){

      int total = (numBlankLines + numCodeLines + numCommentLines);

      double percentCode = ((double)numCodeLines / (double)total);

      System.out.println("Blank: " + numBlankLines);
      System.out.println("Comments: " + numCommentLines);
      System.out.println("Code: " + numCodeLines);
      System.out.println("------------\nTotal: " + total + " ( " + f.format(percentCode) + "% code)\n------------\n" );

   }

   private static void printTotalNumLines(){

      int total = (TotalNumBlankLines + TotalNumCommentLines + TotalNumCodeLines);

      double percentCode = ((double)TotalNumCodeLines / (double)total);

      System.out.println("Blank: " + TotalNumBlankLines);
      System.out.println("Comments: " + TotalNumCommentLines);
      System.out.println("Code: " + TotalNumCodeLines);
      System.out.println("------------\nTotal (" + numFiles + " files read): " + total + " ( " + f.format(percentCode) + "% code)\n------------" );

   }
}

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

   private static final String usage = "Please run the program as follows:\n" +
           "java LoCouter top_level_directory_name -options\n\tFor help: java LoCounter -help\n";


   //todo - implement reading only specific file extensions
   private static final String extenderUsage = "Please run the program as follows:\n" +
           "java LoCouter top_level_directory_name -extensions a b c\n\n" +
           "Where 'a,b,c' are the file extensions of the files you want counted. e.g \".java\"";

   private static final int MIN_ARGS = 1;
   private static final int MAX_ARGS = 3;

   private static DecimalFormat f = new DecimalFormat("#0.00");

   private static int numCommentLines = 0;
   private static int numCodeLines = 0;
   private static int numBlankLines = 0;

   private static int TotalNumCommentLines = 0;
   private static int TotalNumCodeLines = 0;
   private static int TotalNumBlankLines = 0;

   private static int numFiles = 0;

   private static String[] extensions = {"java", "cpp", "hpp", "c", "h", "glsl"};

   private static String exclude_dir = "";

   public static void main(String[] args){

      if(args.length >= MIN_ARGS && args.length <= MAX_ARGS) {
         try {
            //String fileName = "F:\\Programming_Projects\\2016Sem2\\OOSE\\Assignment";
            String fileName = args[0];

            System.out.println(args.length);
            for(int ii = 0; ii < args.length; ii++){
                System.out.println(ii + ") " + args[ii]);
            }

            if(args.length == 3){
                if(args[1].equals("-exclude")){
                    exclude_dir = args[2];
                    System.out.println("Excluding: " + exclude_dir);
                }else{
                    System.out.println("Ignoring unknown arg: " + args[1] + "\n(Only supported arg is ''-exclude' to exclude a directory.) \n");
                }
            }

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
            if (!file.getName().equals(exclude_dir)){
                System.out.println("========================\nReading directory: " + file.getName() + "\n========================");
                // Read all files in this dir.
                for ( final File fileEntry : file.listFiles() ) {
                    if ( fileEntry.isDirectory() && (!fileEntry.getName().endsWith(".git")) ) {
                        recurseRead(fileEntry);
                    } else {
                        if(hasValidExtension(fileEntry.getName())) {
                            countLines(fileEntry);
                            System.out.println("File: " + fileEntry.getName());
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

            //Eat leading spaces.
            while(currLine.charAt(ii) == ' '){
                ii++;
            }

            char currChar;
            char prevChar = '='; //Assign to random char ???
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

            char currChar;
            char prevChar = '+'; //Assign to random char ???
            int ii = 0;

            //Eat leading spaces.
            while(currLine.charAt(ii) == ' '){
                ii++;
            }

            //while I haven't determined what the line is, keep trying
            while( (ii < currLine.length()) && !done){
               currChar = currLine.charAt(ii);

               //If the first non-space character is not a slash, it is definately
               //not a comment starting.
               if(currChar != '/'){
                   codeLine = true;
                   done = true;
               }else{
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
               }

               prevChar = currChar;
               ii++;
            }
         }

         if(codeLine){
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

      double percentCode = ((double)numCodeLines / (double)total) * 100;

      System.out.println("Blank: " + numBlankLines);
      System.out.println("Comments: " + numCommentLines);
      System.out.println("Code: " + numCodeLines);
      System.out.println("------------\nTotal: " + total + " ( " + f.format(percentCode) + "% code)\n------------\n" );

   }

   private static void printTotalNumLines(){

      int total = (TotalNumBlankLines + TotalNumCommentLines + TotalNumCodeLines);

      double percentCode = ((double)TotalNumCodeLines / (double)total);
      double percentComment = ((double)TotalNumCommentLines / (double)total);
      double percentBlank = ((double)TotalNumBlankLines / (double)total);

      System.out.println("Blank: " + TotalNumBlankLines + " (" + f.format(percentBlank*100) + "%)");
      System.out.println("Comments: " + TotalNumCommentLines + " (" + f.format(percentComment*100) + "%)");
      System.out.println("Code: " + TotalNumCodeLines + " (" + f.format(percentCode*100) + "%)");
      System.out.println("------------\nTotal lines (" + numFiles + " files read): " + total + "\n------------" );

   }
}

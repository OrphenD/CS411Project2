import java.io.*;

public class Project2Main
{

    /**
     * Simple test driver for the java parser. Just runs it on some
     * input files, gives no useful output.
     */
    public static void main(String args[])
    {
        try
        {
            System.out.println("Parsing " + args[0]);
            ToyLexScanner lexer = new ToyLexScanner(new FileReader(args[0]));
            parser p = new parser(lexer);
            p.parse();
            System.out.println("No errors.");
        }
        catch (Exception e) {
            e.printStackTrace(System.out);
            System.exit(1);
        }
    }
}



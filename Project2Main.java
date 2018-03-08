import java.io.*;

public class Project2Main
{

    /**
     * Simple test driver for the java parser. Just runs it on some
     * input files, gives no useful output.
     */
    public static void main(String argv[])
    {
        try
        {
            System.out.println("Parsing input.txt");
            ToyLexScanner lexer = new ToyLexScanner(new FileReader("input.txt"));
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



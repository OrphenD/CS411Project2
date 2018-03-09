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
            System.out.println("Parsing ");
            ToyLexScanner lexer = new ToyLexScanner(new FileReader("input.txt"));
            parser p = new parser(lexer);
            p.parse();
            System.out.println("No errors.");
        }
        catch (Exception e) {


            try (BufferedWriter bw = new BufferedWriter(new FileWriter("OUTPUT.txt",true))) {
                bw.write("ERROR");
                bw.close();
            } catch (IOException ex) {
                e.printStackTrace();
            }

            e.printStackTrace(System.out);
            System.exit(1);
        }
    }
}



import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;

public class Trie
{

    //Use a character array to determine the indices for the switch array.
    String alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    char[] alpha = alphabet.toCharArray();

    int[] Switch = new int[alphabet.length()];
    char[] symbol;
    int[] next;

    int ptr = 0;
    int nextOpen = 0;

    public Trie(int size)
    {
        //instantiate the symbol and next arrays to be of input size.
        symbol = new char[size];
        next = new int[symbol.length];
        //Symbol and next arrays with -1 to instantiate as "empty".
        Arrays.fill(Switch, -1);
        Arrays.fill(next, -1);
    }

    public Trie()
    {
        //instantiate the symbol and next arrays to be of standard size 1000.
        symbol = new char[1000];
        next = new int[symbol.length];
        //Symbol and next arrays with -1 to instantiate as "empty".
        Arrays.fill(Switch, -1);
        Arrays.fill(next, -1);
    }


    /* Method insert
       input: String, Output: boolean
       Inserts input string into the Trie data structure. Returns true if insert was able to insert string into the structure.
       Returns false if the string was unable to be inserted, ie the string already exists within the Trie.
     */
    public boolean insert(String str)
    {
        //Add the end of string character to the end of the string str.
        String toAdd = str + "@";
        //nextSymbol denotes the index of the character to be compared with characters in the symbol table.
        int nextSymbol = 0;
        int index = alphabet.indexOf(toAdd.charAt(nextSymbol)); //nextSymbol is used to keep track of the nextSymbol to be compared.

        ptr = Switch[index];

        //if the first symbol of the string does not exist in the Trie, the string does not exist in the Trie.
        if (ptr == -1)
        {
            //shifts to the next symbol to be added to Trie
            nextSymbol++;
            //reference the index of the symbol table where the first letter is kept.
            Switch[index] = nextOpen;
            //adds the rest of the symbols to the symbol table.
            create(toAdd.substring(nextSymbol));
        }
        else
        {
            //shifts to the next symbol in the string.
            nextSymbol++;
            boolean exit = false;

            while(!exit)
            {
                //if the symbol exists in the symbol
                if(symbol[ptr] == toAdd.charAt(nextSymbol))
                {
                    //check if each other symbol exists in the symbol table
                    if(toAdd.charAt(nextSymbol) != '@')
                    {
                        ptr++;
                        nextSymbol++;
                    }
                    else{
                        //if all symbols exist in the symbol table, the string already exists in the Trie.
                        exit = true;
                        return false;
                    }
                //if the symbol in the symbol table does not match with the symbol in the string
                }else if(next[ptr] != -1) //if there is a reference to another string of symbols in the next
                {
                    //shift ptr to next reference.
                    ptr = next[ptr];
                }
                else// if the next does not have a reference, create a reference and add the rest of the symbols
                {   // to the next open position in the symbol table.
                    next[ptr] = nextOpen;
                    create(toAdd.substring(nextSymbol));
                    exit = true;
                }
            }

        }

        return true;

    }

    /*Method create
        method to add the symbols of a string to the next open spot in the symbol table.
        */
    public void create(String str)
    {
        ptr = nextOpen;

        for(int i = 0; i < str.length(); i++) {
            symbol[nextOpen] = str.charAt(i);
            nextOpen++;
        }
    }

    /*Method print
        method to print the switch, symbol, and next tables in a nice format to a specified output file.
     */
    public void print(String output) {
        try (PrintWriter pw = new PrintWriter(
                new BufferedWriter(new FileWriter(output, true)))) {
            // PRINTING FORMATTED
            pw.println();
            int a1 = 0;
            int s1 = 0;
            while (a1 < 40) {
                pw.print("        ");
                for (int a = 0; a < 20; a++)
                    pw.printf("%4c ", alpha[a1++]);
                pw.println();
                pw.print("switch: ");
                for (int a = 0; a < 20; a++)
                    pw.printf("%4d ", Switch[s1++]);
                pw.println("\n");
            }
            pw.print("        ");
            for (int a = 40; a < alpha.length; a++)
                pw.printf("%4c ", alpha[a1++]);
            pw.println();
            pw.print("switch: ");
            for (int a = 40; a < alpha.length; a++)
                pw.printf("%4d ", Switch[s1++]);
            pw.println("\n");
            int count = 0;
            int s2 = 0;
            int n2 = 0;
            while (count < symbol.length) {
                pw.print("        ");
                for (int n = 0; n < 20; n++)
                    pw.printf("%4d ", count++);
                pw.println();
                pw.print("symbol: ");
                for (int n = 0; n < 20; n++)
                    pw.printf("%4c ", symbol[s2++]);
                pw.println();
                pw.print("next:   ");
                for (int n = 0; n < 20; n++) {
                    if (next[n2] != -1) {
                        pw.printf("%4d ", next[n2++]);
                    } else {
                        pw.printf("%4c ", ' ');
                        n2++;
                    }
                }
                pw.println("\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


}

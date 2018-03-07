/*
 * Decompiled with CFR 0_123.
 * 
 * Could not load the following classes:
 *  java_cup.runtime.ComplexSymbolFactory
 *  java_cup.runtime.ComplexSymbolFactory$ComplexSymbol
 *  java_cup.runtime.ComplexSymbolFactory$Location
 *  java_cup.runtime.DefaultSymbolFactory
 *  java_cup.runtime.Scanner
 *  java_cup.runtime.Symbol
 *  java_cup.runtime.SymbolFactory
 *  java_cup.runtime.virtual_parse_stack
 */
package java_cup.runtime;

import java.io.PrintStream;
import java.lang.reflect.Field;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;
import java_cup.runtime.ComplexSymbolFactory;
import java_cup.runtime.DefaultSymbolFactory;
import java_cup.runtime.Scanner;
import java_cup.runtime.Symbol;
import java_cup.runtime.SymbolFactory;
import java_cup.runtime.virtual_parse_stack;

public abstract class lr_parser {
    public SymbolFactory symbolFactory;
    protected static final int _error_sync_size = 3;
    protected boolean _done_parsing = false;
    protected int tos;
    protected Symbol cur_token;
    protected Stack stack = new Stack();
    protected short[][] production_tab;
    protected short[][] action_tab;
    protected short[][] reduce_tab;
    private Scanner _scanner;
    protected Symbol[] lookahead;
    protected int lookahead_pos;

    @Deprecated
    public lr_parser() {
        this((SymbolFactory)new DefaultSymbolFactory());
    }

    public lr_parser(SymbolFactory fac) {
        this.symbolFactory = fac;
    }

    @Deprecated
    public lr_parser(Scanner s) {
        this(s, (SymbolFactory)new DefaultSymbolFactory());
    }

    public lr_parser(Scanner s, SymbolFactory symfac) {
        this();
        this.symbolFactory = symfac;
        this.setScanner(s);
    }

    public SymbolFactory getSymbolFactory() {
        return this.symbolFactory;
    }

    protected int error_sync_size() {
        return 3;
    }

    public abstract short[][] production_table();

    public abstract short[][] action_table();

    public abstract short[][] reduce_table();

    public abstract int start_state();

    public abstract int start_production();

    public abstract int EOF_sym();

    public abstract int error_sym();

    public void done_parsing() {
        this._done_parsing = true;
    }

    public void setScanner(Scanner s) {
        this._scanner = s;
    }

    public Scanner getScanner() {
        return this._scanner;
    }

    public abstract Symbol do_action(int var1, lr_parser var2, Stack var3, int var4) throws Exception;

    public void user_init() throws Exception {
    }

    protected abstract void init_actions() throws Exception;

    public Symbol scan() throws Exception {
        Symbol sym = this.getScanner().next_token();
        return sym != null ? sym : this.getSymbolFactory().newSymbol("END_OF_FILE", this.EOF_sym());
    }

    public void report_fatal_error(String message, Object info) throws Exception {
        this.done_parsing();
        this.report_error(message, info);
        throw new Exception("Can't recover from previous error(s)");
    }

    public void report_error(String message, Object info) {
        if (info instanceof ComplexSymbolFactory.ComplexSymbol) {
            ComplexSymbolFactory.ComplexSymbol cs = (ComplexSymbolFactory.ComplexSymbol)info;
            System.err.println(message + " for input symbol \"" + cs.getName() + "\" spanning from " + (Object)cs.getLeft() + " to " + (Object)cs.getRight());
            return;
        }
        System.err.print(message);
        System.err.flush();
        if (info instanceof Symbol) {
            if (((Symbol)info).left != -1) {
                System.err.println(" at character " + ((Symbol)info).left + " of input");
            } else {
                System.err.println("");
            }
        }
    }

    public void syntax_error(Symbol cur_token) {
        this.report_error("Syntax error", (Object)cur_token);
        this.report_expected_token_ids();
    }

    public Class getSymbolContainer() {
        return null;
    }

    protected void report_expected_token_ids() {
        List<Integer> ids = this.expected_token_ids();
        LinkedList<String> list = new LinkedList<String>();
        for (Integer expected : ids) {
            list.add(this.symbl_name_from_id(expected));
        }
        System.out.println("instead expected token classes are " + list);
    }

    public String symbl_name_from_id(int id) {
        Field[] fields;
        for (Field f : fields = this.getSymbolContainer().getFields()) {
            try {
                if (f.getInt(null) != id) continue;
                return f.getName();
            }
            catch (IllegalArgumentException illegalArgumentException) {
            }
            catch (IllegalAccessException illegalAccessException) {
                // empty catch block
            }
        }
        return "invalid symbol id";
    }

    public List<Integer> expected_token_ids() {
        LinkedList<Integer> ret = new LinkedList<Integer>();
        int parse_state = ((Symbol)this.stack.peek()).parse_state;
        short[] row = this.action_tab[parse_state];
        for (int i = 0; i < row.length; i += 2) {
            if (row[i] == -1 || !this.validate_expected_symbol(row[i])) continue;
            ret.add(new Integer(row[i]));
        }
        return ret;
    }

    private boolean validate_expected_symbol(int id) {
        try {
            virtual_parse_stack vstack = new virtual_parse_stack(this.stack);
            do {
                short act;
                if ((act = this.get_action(vstack.top(), id)) == 0) {
                    return false;
                }
                if (act > 0) {
                    vstack.push(act - 1);
                    if (this.advance_lookahead()) continue;
                    return true;
                }
                if (- act - 1 == this.start_production()) {
                    return true;
                }
                short lhs = this.production_tab[- act - 1][0];
                int rhs_size = this.production_tab[- act - 1][1];
                for (int i = 0; i < rhs_size; ++i) {
                    vstack.pop();
                }
                vstack.push((int)this.get_reduce(vstack.top(), lhs));
            } while (true);
        }
        catch (Exception e) {
            e.printStackTrace();
            return true;
        }
    }

    public void unrecovered_syntax_error(Symbol cur_token) throws Exception {
        this.report_fatal_error("Couldn't repair and continue parse", (Object)cur_token);
    }

    protected final short get_action(int state, int sym) {
        short[] row = this.action_tab[state];
        if (row.length < 20) {
            for (int probe = 0; probe < row.length; ++probe) {
                short tag;
                if ((tag = row[probe++]) != sym && tag != -1) continue;
                return row[probe];
            }
        } else {
            int first = 0;
            int last = (row.length - 1) / 2 - 1;
            while (first <= last) {
                int probe = (first + last) / 2;
                if (sym == row[probe * 2]) {
                    return row[probe * 2 + 1];
                }
                if (sym > row[probe * 2]) {
                    first = probe + 1;
                    continue;
                }
                last = probe - 1;
            }
            return row[row.length - 1];
        }
        return 0;
    }

    protected final short get_reduce(int state, int sym) {
        short[] row = this.reduce_tab[state];
        if (row == null) {
            return -1;
        }
        for (int probe = 0; probe < row.length; ++probe) {
            short tag;
            if ((tag = row[probe++]) != sym && tag != -1) continue;
            return row[probe];
        }
        return -1;
    }

    public Symbol parse() throws Exception {
        Symbol lhs_sym = null;
        this.production_tab = this.production_table();
        this.action_tab = this.action_table();
        this.reduce_tab = this.reduce_table();
        this.init_actions();
        this.user_init();
        this.cur_token = this.scan();
        this.stack.removeAllElements();
        this.stack.push(this.getSymbolFactory().startSymbol("START", 0, this.start_state()));
        this.tos = 0;
        this._done_parsing = false;
        while (!this._done_parsing) {
            if (this.cur_token.used_by_parser) {
                throw new Error("Symbol recycling detected (fix your scanner).");
            }
            short act = this.get_action(((Symbol)this.stack.peek()).parse_state, this.cur_token.sym);
            if (act > 0) {
                this.cur_token.parse_state = act - 1;
                this.cur_token.used_by_parser = true;
                this.stack.push(this.cur_token);
                ++this.tos;
                this.cur_token = this.scan();
                continue;
            }
            if (act < 0) {
                lhs_sym = this.do_action(- act - 1, this, this.stack, this.tos);
                short lhs_sym_num = this.production_tab[- act - 1][0];
                int handle_size = this.production_tab[- act - 1][1];
                for (int i = 0; i < handle_size; ++i) {
                    this.stack.pop();
                    --this.tos;
                }
                act = this.get_reduce(((Symbol)this.stack.peek()).parse_state, lhs_sym_num);
                lhs_sym.parse_state = act;
                lhs_sym.used_by_parser = true;
                this.stack.push(lhs_sym);
                ++this.tos;
                continue;
            }
            if (act != 0) continue;
            this.syntax_error(this.cur_token);
            if (!this.error_recovery(false)) {
                this.unrecovered_syntax_error(this.cur_token);
                this.done_parsing();
                continue;
            }
            lhs_sym = (Symbol)this.stack.peek();
        }
        return lhs_sym;
    }

    public void debug_message(String mess) {
        System.err.println(mess);
    }

    public void dump_stack() {
        if (this.stack == null) {
            this.debug_message("# Stack dump requested, but stack is null");
            return;
        }
        this.debug_message("============ Parse Stack Dump ============");
        for (int i = 0; i < this.stack.size(); ++i) {
            this.debug_message("Symbol: " + ((Symbol)this.stack.elementAt((int)i)).sym + " State: " + ((Symbol)this.stack.elementAt((int)i)).parse_state);
        }
        this.debug_message("==========================================");
    }

    public void debug_reduce(int prod_num, int nt_num, int rhs_size) {
        this.debug_message("# Reduce with prod #" + prod_num + " [NT=" + nt_num + ", " + "SZ=" + rhs_size + "]");
    }

    public void debug_shift(Symbol shift_tkn) {
        this.debug_message("# Shift under term #" + shift_tkn.sym + " to state #" + shift_tkn.parse_state);
    }

    public void debug_stack() {
        StringBuffer sb = new StringBuffer("## STACK:");
        for (int i = 0; i < this.stack.size(); ++i) {
            Symbol s = (Symbol)this.stack.elementAt(i);
            sb.append(" <state " + s.parse_state + ", sym " + s.sym + ">");
            if (i % 3 != 2 && i != this.stack.size() - 1) continue;
            this.debug_message(sb.toString());
            sb = new StringBuffer("         ");
        }
    }

    public Symbol debug_parse() throws Exception {
        Symbol lhs_sym = null;
        this.production_tab = this.production_table();
        this.action_tab = this.action_table();
        this.reduce_tab = this.reduce_table();
        this.debug_message("# Initializing parser");
        this.init_actions();
        this.user_init();
        this.cur_token = this.scan();
        this.debug_message("# Current Symbol is #" + this.cur_token.sym);
        this.stack.removeAllElements();
        this.stack.push(this.getSymbolFactory().startSymbol("START", 0, this.start_state()));
        this.tos = 0;
        this._done_parsing = false;
        while (!this._done_parsing) {
            if (this.cur_token.used_by_parser) {
                throw new Error("Symbol recycling detected (fix your scanner).");
            }
            short act = this.get_action(((Symbol)this.stack.peek()).parse_state, this.cur_token.sym);
            if (act > 0) {
                this.cur_token.parse_state = act - 1;
                this.cur_token.used_by_parser = true;
                this.debug_shift(this.cur_token);
                this.stack.push(this.cur_token);
                ++this.tos;
                this.cur_token = this.scan();
                this.debug_message("# Current token is " + (Object)this.cur_token);
                continue;
            }
            if (act < 0) {
                lhs_sym = this.do_action(- act - 1, this, this.stack, this.tos);
                short lhs_sym_num = this.production_tab[- act - 1][0];
                int handle_size = this.production_tab[- act - 1][1];
                this.debug_reduce(- act - 1, lhs_sym_num, handle_size);
                for (int i = 0; i < handle_size; ++i) {
                    this.stack.pop();
                    --this.tos;
                }
                act = this.get_reduce(((Symbol)this.stack.peek()).parse_state, lhs_sym_num);
                this.debug_message("# Reduce rule: top state " + ((Symbol)this.stack.peek()).parse_state + ", lhs sym " + lhs_sym_num + " -> state " + act);
                lhs_sym.parse_state = act;
                lhs_sym.used_by_parser = true;
                this.stack.push(lhs_sym);
                ++this.tos;
                this.debug_message("# Goto state #" + act);
                continue;
            }
            if (act != 0) continue;
            this.syntax_error(this.cur_token);
            if (!this.error_recovery(true)) {
                this.unrecovered_syntax_error(this.cur_token);
                this.done_parsing();
                continue;
            }
            lhs_sym = (Symbol)this.stack.peek();
        }
        return lhs_sym;
    }

    protected boolean error_recovery(boolean debug) throws Exception {
        if (debug) {
            this.debug_message("# Attempting error recovery");
        }
        if (!this.find_recovery_config(debug)) {
            if (debug) {
                this.debug_message("# Error recovery fails");
            }
            return false;
        }
        this.read_lookahead();
        do {
            if (debug) {
                this.debug_message("# Trying to parse ahead");
            }
            if (this.try_parse_ahead(debug)) break;
            if (this.lookahead[0].sym == this.EOF_sym()) {
                if (debug) {
                    this.debug_message("# Error recovery fails at EOF");
                }
                return false;
            }
            if (debug) {
                this.debug_message("# Consuming Symbol #" + this.lookahead[0].sym);
            }
            this.restart_lookahead();
        } while (true);
        if (debug) {
            this.debug_message("# Parse-ahead ok, going back to normal parse");
        }
        this.parse_lookahead(debug);
        return true;
    }

    protected boolean shift_under_error() {
        return this.get_action(((Symbol)this.stack.peek()).parse_state, this.error_sym()) > 0;
    }

    protected boolean find_recovery_config(boolean debug) {
        Symbol right;
        if (debug) {
            this.debug_message("# Finding recovery state on stack");
        }
        Symbol left = right = (Symbol)this.stack.peek();
        while (!this.shift_under_error()) {
            if (debug) {
                this.debug_message("# Pop stack by one, state was # " + ((Symbol)this.stack.peek()).parse_state);
            }
            left = (Symbol)this.stack.pop();
            --this.tos;
            if (!this.stack.empty()) continue;
            if (debug) {
                this.debug_message("# No recovery state found on stack");
            }
            return false;
        }
        short act = this.get_action(((Symbol)this.stack.peek()).parse_state, this.error_sym());
        if (debug) {
            this.debug_message("# Recover state found (#" + ((Symbol)this.stack.peek()).parse_state + ")");
            this.debug_message("# Shifting on error to state #" + (act - 1));
        }
        Symbol error_token = this.getSymbolFactory().newSymbol("ERROR", this.error_sym(), left, right);
        error_token.parse_state = act - 1;
        error_token.used_by_parser = true;
        this.stack.push(error_token);
        ++this.tos;
        return true;
    }

    protected void read_lookahead() throws Exception {
        this.lookahead = new Symbol[this.error_sync_size()];
        for (int i = 0; i < this.error_sync_size(); ++i) {
            this.lookahead[i] = this.cur_token;
            this.cur_token = this.scan();
        }
        this.lookahead_pos = 0;
    }

    protected Symbol cur_err_token() {
        return this.lookahead[this.lookahead_pos];
    }

    protected boolean advance_lookahead() {
        ++this.lookahead_pos;
        return this.lookahead_pos < this.error_sync_size();
    }

    protected void restart_lookahead() throws Exception {
        for (int i = 1; i < this.error_sync_size(); ++i) {
            this.lookahead[i - 1] = this.lookahead[i];
        }
        this.lookahead[this.error_sync_size() - 1] = this.cur_token;
        this.cur_token = this.scan();
        this.lookahead_pos = 0;
    }

    protected boolean try_parse_ahead(boolean debug) throws Exception {
        virtual_parse_stack vstack = new virtual_parse_stack(this.stack);
        short act;
        while ((act = this.get_action(vstack.top(), this.cur_err_token().sym)) != 0) {
            if (act > 0) {
                vstack.push(act - 1);
                if (debug) {
                    this.debug_message("# Parse-ahead shifts Symbol #" + this.cur_err_token().sym + " into state #" + (act - 1));
                }
                if (this.advance_lookahead()) continue;
                return true;
            }
            if (- act - 1 == this.start_production()) {
                if (debug) {
                    this.debug_message("# Parse-ahead accepts");
                }
                return true;
            }
            short lhs = this.production_tab[- act - 1][0];
            int rhs_size = this.production_tab[- act - 1][1];
            for (int i = 0; i < rhs_size; ++i) {
                vstack.pop();
            }
            if (debug) {
                this.debug_message("# Parse-ahead reduces: handle size = " + rhs_size + " lhs = #" + lhs + " from state #" + vstack.top());
            }
            vstack.push((int)this.get_reduce(vstack.top(), lhs));
            if (!debug) continue;
            this.debug_message("# Goto state #" + vstack.top());
        }
        return false;
    }

    protected void parse_lookahead(boolean debug) throws Exception {
        Symbol lhs_sym = null;
        this.lookahead_pos = 0;
        if (debug) {
            this.debug_message("# Reparsing saved input with actions");
            this.debug_message("# Current Symbol is #" + this.cur_err_token().sym);
            this.debug_message("# Current state is #" + ((Symbol)this.stack.peek()).parse_state);
        }
        while (!this._done_parsing) {
            short act = this.get_action(((Symbol)this.stack.peek()).parse_state, this.cur_err_token().sym);
            if (act > 0) {
                this.cur_err_token().parse_state = act - 1;
                this.cur_err_token().used_by_parser = true;
                if (debug) {
                    this.debug_shift(this.cur_err_token());
                }
                this.stack.push(this.cur_err_token());
                ++this.tos;
                if (!this.advance_lookahead()) {
                    if (debug) {
                        this.debug_message("# Completed reparse");
                    }
                    return;
                }
                if (!debug) continue;
                this.debug_message("# Current Symbol is #" + this.cur_err_token().sym);
                continue;
            }
            if (act < 0) {
                lhs_sym = this.do_action(- act - 1, this, this.stack, this.tos);
                short lhs_sym_num = this.production_tab[- act - 1][0];
                int handle_size = this.production_tab[- act - 1][1];
                if (debug) {
                    this.debug_reduce(- act - 1, lhs_sym_num, handle_size);
                }
                for (int i = 0; i < handle_size; ++i) {
                    this.stack.pop();
                    --this.tos;
                }
                act = this.get_reduce(((Symbol)this.stack.peek()).parse_state, lhs_sym_num);
                lhs_sym.parse_state = act;
                lhs_sym.used_by_parser = true;
                this.stack.push(lhs_sym);
                ++this.tos;
                if (!debug) continue;
                this.debug_message("# Goto state #" + act);
                continue;
            }
            if (act != 0) continue;
            this.report_fatal_error("Syntax error", (Object)lhs_sym);
            return;
        }
    }

    protected static short[][] unpackFromStrings(String[] sa) {
        StringBuffer sb = new StringBuffer(sa[0]);
        for (int i = 1; i < sa.length; ++i) {
            sb.append(sa[i]);
        }
        int n = 0;
        int size1 = sb.charAt(n) << 16 | sb.charAt(n + 1);
        n += 2;
        short[][] result = new short[size1][];
        for (int i = 0; i < size1; ++i) {
            int size2 = sb.charAt(n) << 16 | sb.charAt(n + 1);
            n += 2;
            result[i] = new short[size2];
            for (int j = 0; j < size2; ++j) {
                result[i][j] = (short)(sb.charAt(n++) - 2);
            }
        }
        return result;
    }
}

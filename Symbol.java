/*
 * Decompiled with CFR 0_123.
 */
package java_cup.runtime;

public class Symbol {
    public int sym;
    public int parse_state;
    public boolean used_by_parser = false;
    public int left;
    public int right;
    public Object value;

    public Symbol(int id, Symbol left, Symbol right, Object o) {
        this(id, left.left, right.right, o);
    }

    public Symbol(int id, Symbol left, Symbol right) {
        this(id, left.left, right.right);
    }

    public Symbol(int id, Symbol left, Object o) {
        this(id, left.right, left.right, o);
    }

    public Symbol(int id, int l, int r, Object o) {
        this(id);
        this.left = l;
        this.right = r;
        this.value = o;
    }

    public Symbol(int id, Object o) {
        this(id, -1, -1, o);
    }

    public Symbol(int id, int l, int r) {
        this(id, l, r, (Object)null);
    }

    public Symbol(int sym_num) {
        this(sym_num, -1);
        this.left = -1;
        this.right = -1;
    }

    Symbol(int sym_num, int state) {
        this.sym = sym_num;
        this.parse_state = state;
    }

    public String toString() {
        return "#" + this.sym;
    }
}

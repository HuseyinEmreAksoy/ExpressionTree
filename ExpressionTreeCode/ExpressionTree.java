package ExpressionTreeCode;


import java.util.ArrayList;
import java.util.List;

class TreeNodes {// help for the create linked tree.
    String value;
    TreeNodes left, right;

    TreeNodes(String item) {
        value = item;
        left = null;
        right = null;
    }
}

public class ExpressionTree extends LinkedBinaryTree<String> {
    String tree;
    String post;
    TreeNodes root;

    public ExpressionTree(String str) {
        tree = str + " ";
        post = infixToPostfix(str); // we need postordered and inordered expression for exact true tree

        String[] strArray = new String[post.length()];

        for (int i = 0; i < strArray.length; i++) { // String arraye atiyoruz treeye cevirmek icin
            strArray[i] = "" + post.substring(i, post.substring(i).indexOf(" ") + i);
            i = post.substring(i).indexOf(" ") + i;
        }

        root = constructTree(strArray);
        addRoot(root.value);
        coverters(root, root()); // convert the LinkedBinaryTree
    }

    private void coverters(TreeNodes t, Position<String> p) {//convert the LinkedBinaryTree
        if (t.left != null) { // if t has left child add our tree and keep going
            addLeft(p, t.left.value);
            coverters(t.left, left(p));
        }
        if (t.right != null) {// if t has right child add our tree and keep going
            addRight(p, t.right.value);
            coverters(t.right, right(p));
        }

    }

    public double evaluate(double xvalue) {
        return evaluateAux(xvalue, root());
    }

    private double evaluateAux(double xvalue, Position<String> p) {
        double x, y;

        if (isExternal(p)) {
            if (p.getElement().equals("X"))
                return xvalue;// assign xvalue to X
            return Double.parseDouble(p.getElement()); //return the leaf element
        } else {
            if (p.getElement().equals("cos")) {
                return Math.cos(Math.toRadians(evaluateAux(xvalue, right(p))));// convert degree to radian and return cos value
            } else if (p.getElement().equals("sin"))
                return Math.sin(Math.toRadians(evaluateAux(xvalue, right(p))));
            else {
                x = evaluateAux(xvalue, left(p));
                y = evaluateAux(xvalue, right(p));

                return evalOp(p.getElement(), x, y); // return the value of operation
            }
        }
    }

    private double evalOp(String op, double x, double y) {// return the value of operation
        if (op.equals("+"))
            return x + y;
        else if (op.equals("-"))
            return x - y;
        else
            return x * y;
    }

    public ExpressionTree derivative() {
        ExpressionTree tree = new ExpressionTree(this.tree);
        derivativeAux(tree, tree.root());
        return tree;
    }

    private int derivativeAux(ExpressionTree t, Position<String> p) {

        if (isOperator(p.getElement())) { // if our element is op we should do differant things
            if (p.getElement().equals("+")) {
                String a = "" + (derivativeAux(t, left(p)) + derivativeAux(t, right(p)));
                if (!isOperator(t.left(p).getElement()) && !isOperator(t.right(p).getElement())) {
                    set(p, a);// change the value because we are in leaves or both sides are number
                    remove(right(p));// we do not need to child
                    remove(left(p));
                }

            } else if (p.getElement().equals("-")) {
                String a = "" + (derivativeAux(t, left(p)) - derivativeAux(t, right(p)));

                if (!isOperator(t.left(p).getElement()) && !isOperator(t.right(p).getElement())) {
                    t.set(p, a);// change the value because we are in leaves or both sides are number
                    remove(right(p));// we do not need to child
                    remove(left(p));
                }

            } else if (p.getElement().equals("*")) {
                String a = "";

                if (right(p).getElement().equals("X") && !left(p).getElement().equals("X"))
                    a += derivativeAux(t, right(p)); //left side return 0 therefore we do not need to reducant call
                else if (left(p).getElement().equals("X") && !right(p).getElement().equals("X"))
                    a += derivativeAux(t, left(p)); //right side return 0 therefore we do not need to reducant call
                else
                    a += (derivativeAux(t, right(p)) + derivativeAux(t, left(p)));

                if (!isOperator(t.left(p).getElement()) && !isOperator(t.right(p).getElement())) {
                    t.set(p, a);// change the value because we are in leaves or both sides are number
                    remove(right(p));// we do not need to child
                    remove(left(p));
                }

            } else if (p.getElement().equals("cos")) {
                if (t.right(p).getElement().equals("*"))
                    t.set(p, "" + (derivativeAux(t, t.right(t.right(p))) + derivativeAux(t, t.right(t.left(p)))) + " * -sin");
                else
                    t.set(p, "-sin"); // if op is differant from "*" always return -1 * sin we dont need reducant calls
            } else {
                if (t.right(p).getElement().equals("*"))
                    t.set(p, "" + (derivativeAux(t, t.right(t.right(p))) + derivativeAux(t, t.right(t.left(p)))) + " * cos");
                else
                    t.set(p, "cos");// if op is differant from "*" always return 1 * cos we dont need reducant calls
            }
        }
        if (p.getElement().equals("X"))
            return 1;
        else
            return 0;
    }

    public ExpressionTree simplify() {
        ExpressionTree newTree = new ExpressionTree(tree);
        simplifyAux(newTree, newTree.root());
        return newTree;
    }

    private void simplifyAux(ExpressionTree t, Position<String> p) {
        boolean flag = false;

        if (isOperator(p.getElement()) && !p.getElement().equals("cos") && !p.getElement().equals("sin")) {
            simplifyAux(t, t.right(p));// if we want do evaluate we need 2 number or like x and 0 elements
            simplifyAux(t, t.left(p));

            if (t.root() == p)
                flag = true;

            if (!flag && !isOperator(p.getElement())) {
                t.remove(t.right(p)); // we dont need child anymore
                t.remove(t.left(p));
            }
        } else if (p.getElement().equals("cos")) {
            if (isOperator(t.right(p).getElement()))// first get number and then evaluate cos
                simplifyAux(t, t.right(p));
            if (!isOperator(right(p).getElement())) {
                t.set(p, "" + Math.cos(Math.toRadians(Double.parseDouble(right(p).getElement()))));
                t.remove(right(p));
            }
        } else if (p.getElement().equals("sin")) {// first get number and then evaluate sin
            if (isOperator(t.right(p).getElement()))
                simplifyAux(t, t.right(p));
            if (!isOperator(right(p).getElement())) {
                t.set(p, "" + Math.sin(Math.toRadians(Double.parseDouble(right(p).getElement()))));
                t.remove(right(p));
            }
        } else {// firstly we are in leaves and evaluate our element and sibling if it is X we check out in other method
            if (!t.right(parent(p)).getElement().equals("X") && !t.left(t.parent(p)).getElement().equals("X") && isOperator(parent(p).getElement()) && !isOperator(t.left(t.parent(p)).getElement()) && !isOperator(t.right(t.parent(p)).getElement()))
                t.set(parent(p), "" + t.evalOp(parent(p).getElement(), Double.parseDouble(t.right(parent(p)).getElement()), Double.parseDouble(t.left(t.parent(p)).getElement())));
            else if (isOperator(parent(p).getElement()) && (t.sibling(p).getElement().equals("0") || (t.parent(p).getElement().equals("*") && t.sibling(p).getElement().equals("1"))))
                t.set(parent(p), xSimpler(parent(p).getElement(), t.sibling(p).getElement()));

        }
        if (flag && !t.right(p).getElement().equals("X") && !t.left(p).getElement().equals("X") && t.size() < 4) {//if we are in node and both child is number we evalute the op
            t.set(p, "" + t.evalOp(p.getElement(), Double.parseDouble(t.right(p).getElement()), Double.parseDouble(t.left(p).getElement())));
            t.remove(t.right(p));
            t.remove(t.left(p));
        }

    }

    private String xSimpler(String op, String num) { // check and return value of X evaluation
        if (op.equals("*") && num.equals("1"))
            return "X";
        else if (op.equals("*") && num.equals("0"))
            return "0";
        else if ((op.equals("+") || op.equals("-")) && num.equals("0"))
            return "X";
        return "";
    }

    public void displayTree() {
        displayTreeAux(root());
    }

    private void displayTreeAux(Position<String> p) {// inorder print tree with "."
        if (left(p) != null)
            displayTreeAux(left(p));
        for (int i = 0; i < depth(p); i++)
            System.out.print("." + " ");
        System.out.println(p.getElement());
        if (right(p) != null)
            displayTreeAux(right(p));
    }

    private void inorderSubtree(Position<String> p, List<String> snapshot) {//help for to String

        if (left(p) != null) {
            snapshot.add("( ");
            inorderSubtree(left(p), snapshot);
        }
        snapshot.add(p.getElement() + " ");
        if (right(p) != null) {
            inorderSubtree(right(p), snapshot);
            snapshot.add(") ");
        }
    }

    public String toString() { // String with fully params
        List<String> snapshot = new ArrayList<>();
        String result = "";
        snapshot.add("( ");
        inorderSubtree(root(), snapshot);
        for (int i = 0; i < snapshot.size(); i++)
            result += snapshot.get(i);
        return result;
    }

    private boolean isOperator(String c) { // check is operation
        if (c.equals("+") || c.equals("-")
                || c.equals("*") || c.equals("cos") || c.equals("sin") || c.equals("-sin")) {
            return true;
        }
        return false;
    }

    private TreeNodes constructTree(String postfix[]) {//postorder to tree
        for (int i = 0; i < postfix.length; i++) {

            if (postfix[i] == null)
                postfix[i] = "temp";
        }
        ArrayStack<TreeNodes> st = new ArrayStack<TreeNodes>();
        TreeNodes t, t1, t2;

        // Traverse every String of input expression
        for (int i = 0; i < postfix.length; i++) {

            // If operand, simply push into stack
            if (!isOperator(postfix[i]) && !postfix[i].equals("temp")) {
                t = new TreeNodes(postfix[i]);
                st.push(t);
            } else if (postfix[i].equals("temp"))
                continue;//dummy
            else // operator
            {
                t = new TreeNodes(postfix[i]);
                if (postfix[i].equals("cos") || postfix[i].equals("sin")) {
                    t1 = st.pop();
                    t.right = t1;
                    st.push(t);
                } else {// Pop two top nodes on Store top
                    t1 = st.pop();
                    t2 = st.pop();

                    t.right = t1; //  make them children
                    t.left = t2;

                    st.push(t); // Add this subexpression to stack
                }
            }
        }
        t = st.top();    // tree
        st.pop();
        return t;
    }

    private int Prec(String ch) {// return prec of op
        switch (ch) {
            case "+":
            case "-":
                return 1;
            case "*":
            case "/":
                return 2;
            case "cos":
            case "sin":
                return 3;
        }
        return -1;
    }

    private String infixToPostfix(String exp) { // method that converts given infix expression to postfix expression.
        String result ="";
        ArrayStack<String> stack = new ArrayStack<>();

        String c = "";

        for (int i = 0; i < exp.length() - 1; ++i) {
            if (exp.substring(i, i + 1).equals(" "))
                continue;

            if (exp.substring(i).indexOf(" ") == -1)
                c = exp.substring(i);

            c = exp.substring(i, exp.substring(i).indexOf(" ") + i); // assign the every token until space

            if (!isOperator(c) && !c.equals("(") && !c.equals(")"))
                result += c + " "; // if is element add our postorder string

            else if (c.equals("(")) // If the scanned character is an '(', push it to the stack
                stack.push(c);

                // If the scanned string is an ")" pop and output from the stack until an "(" is encountered
            else if (c.equals(")")) {
                while (!stack.isEmpty() && !stack.top().equals("("))
                    result += stack.pop() + " ";// space for more beautiful output
                stack.pop();
            } else // an operator is encountered
            {
                while (!stack.isEmpty() && Prec(c) < Prec(stack.top()))
                    result += stack.pop() + " ";
                stack.push(c);
            }
            i = exp.substring(i).indexOf(" ") + i - 1;// change the value of i
        }
        while (!stack.isEmpty()) { // pop all the operators from the stack
            if (stack.top().equals("("))
                return "Invalid Expression";
            result += stack.pop();
        }
        return result;
    }
}

package ExpressionTreeCode;

import java.util.Iterator;

public class Driver {
    public static void main(String[] args) {
        ExpressionTree tree = new ExpressionTree("( ( cos ( X + 1 ) + 3 ) + ( 5 * 4 ) ) ");

        Iterator<Position<String>> iter = tree.simplify().inorder().iterator();

        System.out.println("inorder non param simplify: ");
        System.out.println("----------------------------------");

        while (iter.hasNext())
            System.out.print(iter.next().getElement() + " ");

        System.out.println();
        System.out.println("----------------------------------");

        System.out.println(tree.toString());

        System.out.println("evaluate: " + tree.evaluate(179));

        System.out.println("simply evaluate: " + tree.simplify().evaluate(179));

        tree.displayTree();
    }
}

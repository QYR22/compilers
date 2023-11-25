import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Stack;

/******* 词法分析 使用RE表达式，更清晰简洁 *******/
class LexicalAnalyzer {
    static List<String> tokenize(String input) {
        return Arrays.asList(input.split("\\s+"));
    }
}
// 语法分析树节点 一棵多叉树
class Node {
    private int id;
    private int parentId;
    private String data;
    private ArrayList<Node> children  = new ArrayList<>();

    public Node() {}
    public Node(Node node) {
        this.id = node.id;
        this.parentId = node.parentId;
        this.data = node.data;
    }

    public Node(int id, int parentId, String data) {
        this.id = id;
        this.parentId = parentId;
        this.data = data;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public int getParentId() { return parentId; }
    public void setParentId(int parentId) { this.parentId = parentId; }
    
    public String getData() { return data; }
    public void setData(String data) { this.data = data; }
    
    public void addChild(Node child) {
        this.children.add(child);
    }
    public ArrayList<Node> getChildren() {
        return children;
    }
    public void setChildren(ArrayList<Node> children) {
        this.children = children;
    }
    @Override
    public String toString(){
        return data;
    }
}
// LL(1) 语法分析树
class ParseTree {
    private Node root;
    private int size;
    private int printCnt;
    private boolean isNodeOfPaserFound;

    public ParseTree() {
        root = new Node(-1, -1, "root");
        size = 0;
    }

    public int size() {
        return size;
    }
    
    // 找到对应 id 的 node
    public Node getNode(int id) {
        if (id < this.size) {
            return getNode(this.root, id);
        }
        return null;
    }
    // DFS寻找对应node
    private Node getNode(Node subRoot, int id) {
        if (subRoot != null) {
            if (id == subRoot.getId()) {
                return subRoot;
            }
            // 否则 子树中DFS递归寻找
            for (Node curNode : subRoot.getChildren()) {
                Node result = getNode(curNode, id);
                if (result != null) {
                    return result;
                }
            }
        }
        return null;
    }

    public void add(Node node) {
        // 先找到父结点
        Node parentNode = getNode(node.getParentId());
        if (parentNode != null) {
            parentNode.getChildren().add(new Node(node));
            size++;
        }
    }

    public String getNextTerminal(int id) {
        this.isNodeOfPaserFound = false;
        return getNextTerminal(root, id);
    }

    private String getNextTerminal(Node node, int id) {
        if (node != null) {
            if (this.isNodeOfPaserFound) {
                if (Java_LLParserAnalysis.terminals.contains(node.getData())) {
                    return node.getData();
                }
            } else {
                if (node.getId() == id) {
                    this.isNodeOfPaserFound = true;
                }
            }
            for (Node cur : node.getChildren()) {
                String result = getNextTerminal(cur, id);
                if (!"".equals(result)) {
                    return result;
                }
            }
            return "";
        }
        return ""; // 默认值
    }
    
    public void print() {
        this.printCnt = 0;
        for (Node cur : root.getChildren()) {
            print(cur, 0);
        }
    }
    private void print(Node node, int indentCount) {
        if (node != null) {
            for (int i = 0; i < indentCount; i++) {
                System.out.print("\t");
            }
            System.out.print(node);
            if (printCnt < size - 1) {
                System.out.println();
            }
            printCnt++;
            indentCount++;
            for (Node cur : node.getChildren()) {
                print(cur, indentCount);
            }
        }
    }
}

/******* 产生式 *******/
class Production {
    String left;
    private List<List<String>> right = new ArrayList<>();

    public Production() { }

    public Production(String left, List<String> right) {
        this.left = left;
        for (String cur : right) {
            List<String> anotherRight = LexicalAnalyzer.tokenize(cur);
            this.right.add(anotherRight);
        }
        // System.out.println("Production构造方法 : left = " + this.left + "  right = " + this.right);
    }

    public String getLeft() {
        return left;
    }

    public List<List<String>> getRight() {
        return right;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }
        Production otheProduction = (Production) other;
        return left.equals(otheProduction.getLeft()) &&
                right.equals(otheProduction.getRight());
    }
}
// 被拆分过的单产生式
class SingleProduction {
    String left;
    List<String> right;
    public SingleProduction() { }
    public SingleProduction(String left, List<String> right) {
        this.left = left;
        this.right = right;
    }

    public String getLeft() {
        return left;
    }

    public List<String> getRight() {
        return right;
    }
}

public class Java_LLParserAnalysis {
    // input
    private static StringBuffer prog = new StringBuffer();
    public static final String START = "program";
    // 输入处理后的 token
    private static ArrayList<String> tokens = new ArrayList<>();
    // FIRST 集
    private static HashMap<String, HashSet<String>> firstSet = new HashMap<>();
    // FOLLOW 集
    private static HashMap<String, HashSet<String>> followSet = new HashMap<>();
    // LL(1)分析预测表
    private static HashMap<String, HashMap<String, SingleProduction>> parsingTable = new HashMap<>();
    // 运行的栈
    private static Stack<Node> parsingStack = new Stack<>();
    // LL(1)语法树
    private static ParseTree parseTree = new ParseTree();

    // 非终结符
    public static List<String> nonterminals = new ArrayList<>(
            Arrays.asList("program", "stmt", "compoundstmt", "stmts", "ifstmt", "whilestmt",
                    "assgstmt", "boolexpr", "boolop", "arithexpr", "arithexprprime", "multexpr", "multexprprime",
                    "simpleexpr"));
    // 终结符
    public static List<String> terminals = new ArrayList<>(
            Arrays.asList("{", "}", "if", "(", ")", "then", "else", "while", "ID", ";",
                    "=", ">", "<", ">=", "<=", "==", "+", "-", "*", "/", "NUM", "E", "$"));
 
    /* 所有产生式
     * 1. program -> compoundstmt
     * 2 3 4 5. stmt -> ifstmt | whilestmt | assgstmt | compoundstmt
     * 6. compoundstmt -> { stmts }
     * 7 8. stmts -> stmt stmts | E
     * 9. ifstmt -> if ( boolexpr ) then stmt else stmt
     * 10. whilestmt -> while ( boolexpr ) stmt
     * 11. assgstmt -> ID = arithexpr ;
     * 12. boolexpr -> arithexpr boolop arithexpr
     * 13 14 15 16 17. boolop -> < | > | <= | >= | ==
     * 18. arithexpr -> multexpr arithexprprime
     * 19 20 21. arithexprprime -> + multexpr arithexprprime | - multexpr arithexprprime | E
     * 22. multexpr -> simpleexpr multexprprime
     * 23 24 25. multexprprime -> * simpleexpr multexprprime | / simpleexpr multexprprime | E
     * 26 27 28. simpleexpr -> ID | NUM | ( arithexpr )
     */
     private static List<Production> productions = new ArrayList<>(Arrays.asList(
            new Production("program", Arrays.asList("compoundstmt")),
            new Production("stmt", Arrays.asList("ifstmt", "whilestmt", "assgstmt", "compoundstmt")),
            new Production("compoundstmt", Arrays.asList("{ stmts }")),
            new Production("stmts", Arrays.asList("stmt stmts", "E")),
            new Production("ifstmt", Arrays.asList("if ( boolexpr ) then stmt else stmt")),
            new Production("whilestmt", Arrays.asList("while ( boolexpr ) stmt")),
            new Production("assgstmt", Arrays.asList("ID = arithexpr ;")),
            new Production("boolexpr", Arrays.asList("arithexpr boolop arithexpr")),
            new Production("boolop", Arrays.asList("<", ">", "<=", ">=", "==")),
            new Production("arithexpr", Arrays.asList("multexpr arithexprprime")),
            new Production("arithexprprime", Arrays.asList("+ multexpr arithexprprime", "- multexpr arithexprprime", "E")),
            new Production("multexpr", Arrays.asList("simpleexpr multexprprime")),
            new Production("multexprprime", Arrays.asList("* simpleexpr multexprprime", "/ simpleexpr multexprprime", "E")),
            new Production("simpleexpr", Arrays.asList("ID", "NUM", "( arithexpr )"))));
    
    
    
    // 计算单个非终结符的first集 递归方法
    public static void getFirst(String expr) {
        // 如果已经计算过
        if (firstSet.containsKey(expr)) {
            return;
        }
        HashSet<String> set = new HashSet<>();
        // 单个终结符
        if (terminals.contains(expr)) {
            set.add(expr);
            firstSet.put(expr, set);
            return;
        }
        // E 空串
        if (expr.equals("E")) {
            set.add(expr);
            firstSet.put(expr, set);
            return;
        }
        // 非终结符 
        /* List<List<String>> rights = new ArrayList<>();
        for(Production production : productions){
            if (production.getLeft().equals(expr)) {
                rights = production.getRight();
                break;
            }
        } */
        // 获取它所有的产生式 可能一个，可能是`|`分隔开的多个
        List<List<String>> rights = productions.stream()
                .filter(p -> p.getLeft().equals(expr))
                .findFirst()
                .map(Production::getRight)
                .orElse(new ArrayList<>());
        // 对每条产生式
        for (List<String> curProduction : rights) {
            // 对该条产生式从左至右
            for (int i = 0; i < curProduction.size(); ++i) {
                String curProd = curProduction.get(i);
                // 空串
                if (curProd.equals("E")) {
                    set.add("E");
                    break;
                }
                // 终结符 不用看下一个符号了
                if (terminals.contains(curProd)) {
                    set.add(curProd);
                    break;
                }
                // 非终结符 没有的话继续找
                if (!firstSet.containsKey(curProd)) {
                    getFirst(curProd);
                }
                HashSet<String> curProdFirst = firstSet.get(curProd);
                // 没有E 加完就结束
                // 有E再看下一个符号 若最后一个的first还有E，加E
                if (!curProdFirst.contains("E") || i == curProduction.size() - 1) {
                    set.addAll(curProdFirst);
                    break;
                }
                curProdFirst.remove("E");
                set.addAll(curProdFirst);
            }
            firstSet.put(expr, set);
        }
    }

    // 计算单个非终结符的follow集
    public static void getFollow(String expr, int times) {
        
        HashSet<String> exprFollowSet = followSet.containsKey(expr) ? followSet.get(expr) : new HashSet<>();

        if (expr == START) {
            exprFollowSet.add("$");
        }
        // 对所有产生式 A --> B|C|D  找到右边包含expr的式子
        for (Production curProd : productions) {
            // B,C,D每条遍历
            for (List<String> curProdRight : curProd.getRight()) {
                // 可能包含不止一个expr...
                if (curProdRight.contains(expr)) {
                    // 遍历该产生式中每个字符
                    for (int i = 0; i < curProdRight.size() - 1; i++) {

                        if (curProdRight.get(i).equals(expr)) {
                            String x = curProdRight.get(i + 1);
                            // A -> aBx x为终结符 加到follow集中
                            if (terminals.contains(x)) {
                                exprFollowSet.add(x);
                            }
                            // A -> aBx x为非终结符
                            else {
                                // 加入first(x)非空部分 到FOLLOW(B)中 
                                // 若first(x)中有 空串E 加FOLLOW(A)全部
                                HashSet<String> xFirst = firstSet.get(x);
                                if (xFirst.contains("E")) {
                                    xFirst.remove("E");
                                    if (!followSet.containsKey(curProd.getLeft())) {
                                        // if (times <= 1)
                                        //     break;
                                        getFollow(curProd.getLeft(), times - 1);
                                    }
                                    // 加FOLLOW(A)
                                    exprFollowSet.addAll(followSet.get(curProd.getLeft()));
                                }
                                exprFollowSet.addAll(xFirst);
                            }
                        }
                    }
                    // A -> aB B在最右 FOLLOW(A)全加
                    // 注意 A -> aA的情况，忽略
                    if (expr.equals(curProdRight.get(curProdRight.size() - 1)) && !curProd.getLeft().equals(expr)) {
                        if (!followSet.containsKey(curProd.getLeft())) {
                            if (times <= 1) {
                                break;
                            }
                            getFollow(curProd.getLeft(), times - 1);
                        }
                        exprFollowSet.addAll(followSet.get(curProd.getLeft()));
                    }
                }
            }
        }
        followSet.put(expr, exprFollowSet);
    }
    
    /* xxx 计算first集 --> 计算follow集 --> 利用follow集填充LL(1)预测分析表 */
    public static void init_parsingTable() {
        // 对所有非终结符： 计算first集
        for (String nonTerminal : nonterminals) {
            getFirst(nonTerminal);
        }
        /* 查看FIRST计算结果
        for (Map.Entry<String, HashSet<String>> e : firstSet.entrySet()) {
            System.out.println("--:" + e.getKey());
            
            System.out.println("value = " + e.getValue());
            System.out.println();
        } */
        getFollow(START, 3);
        for (String nonTerminal : nonterminals) {
            getFollow(nonTerminal, 3);
        }
        /* 查看FOLLOW计算结果 */
        // for (Map.Entry<String, HashSet<String>> e : followSet.entrySet()) {
        //     System.out.println(e.getKey() + " " + e.getValue());
        // } 
        // 初始化第一列 (非终结符列) 对应位置全部置空
        for (String nonTerminal : nonterminals) {
            parsingTable.put(nonTerminal, new HashMap<>());
        }
        // 利用follow集填充LL(1)预测分析表
        // FIRST 集
        // HashMap<String, HashSet<String>> firstSet
        // FOLLOW 集
        // HashMap<String, HashSet<String>> followSet
        // HashMap<String, HashMap<String, Production>>
        // 根据first集follow集填充
        // A --> a FIRST(a)所有终结符x 产生式写到M[A,x]下
        for (Production curProd : productions) {

            for (int i = 0; i < curProd.getRight().size(); i++) {
                // 单条产生式 的右边
                List<String> curProdRight = curProd.getRight().get(i);
                boolean isEmpty = true;
                // 单条产生式 curProd.getLeft() --> curProdRight
                for (String curExpr : curProdRight) {
                    // 单单 一个E
                    if (curExpr.equals("E")) {
                        isEmpty = true;
                        break;
                    }
                    // 终结符 curExpr 
                    if (terminals.contains(curExpr)) {
                        parsingTable.get(curProd.getLeft()).put(curExpr,
                                new SingleProduction(curProd.getLeft(), curProdRight));
                        isEmpty = false;
                        break;
                    }
                    // 非终结符 查first表 curFirst 为当前非终结符的FIRST集
                    HashSet<String> curFirst = firstSet.get(curExpr);
                    // 若有E继续 
                    if (curFirst.contains("E")) {
                        isEmpty = true;
                        curFirst.remove("E");
                    } else
                        isEmpty = false;
                    if (!curFirst.contains("E")) {
                        for (String NT : curFirst) {
                            parsingTable.get(curProd.getLeft()).put(NT,
                                    new SingleProduction(curProd.getLeft(), curProdRight));
                        }
                        if (!isEmpty)
                            break;
                    }
                }
                // 单条产生式所有结束后，还有E 加入FOLLOW(A)集中所有符号 有$ 包括$
                if (isEmpty) {
                    HashSet<String> curFollow = followSet.get(curProd.getLeft());
                    for (String followNTi : curFollow) {
                        // System.out.print("  " + followNTi);
                        parsingTable.get(curProd.getLeft()).put(followNTi,
                                new SingleProduction(curProd.getLeft(), curProdRight));
                    }
                }
            }
        }
        /* System.out.println("使用 keySet() 方法遍历 parsingTable");
        for (String key : parsingTable.keySet()) { // 遍历外部的 HashMap 的键
            System.out.print(key + ": "); // 输出键
            System.out.println();
            HashMap<String, SingleProduction> value = parsingTable.get(key); // 获取对应的值，也就是内部的 HashMap
            for (String k : value.keySet()) { // 遍历内部的 HashMap 的键
                System.out.print(k + " :: " + value.get(k).getRight() + ", "); // 输出键和值
                System.out.println();
            }
            System.out.println(); // 换行
        } */
    }
    
    /*xxx 初始化栈 */
    private static void initStack() {
        parsingStack.push(new Node(-1, -1, "$"));
        // 开始符号
        parsingStack.push(new Node(0, -1, START));
    }

    public static void parse() {
        int index = 0;
        int nodeCnt = 0;
        Node cur = parsingStack.peek();
        parseTree.add(cur);
        nodeCnt++;
        // 对栈顶peek
        while (!"$".equals(cur.getData())) {
            String current = tokens.get(index);
            // 空串
            if ("E".equals(cur.getData())) {
                parsingStack.pop();
            }
            // 匹配的终结符
            else if (cur.getData().equals(current)) {
                parsingStack.pop();
                index++;
            }
            // 不匹配的终结符 --> error
            else if (terminals.contains(cur.getData())) {
                // 错误输出
                String missingToken = cur.getData();
                error(index, missingToken);
                // 错误处理
                tokens.add(index, missingToken);
            }
            // 处理非终结符
            else if (parsingTable.containsKey(cur.getData()) && parsingTable.get(cur.getData()).containsKey(current)) {
                SingleProduction production = parsingTable.get(cur.getData()).get(current);
                parsingStack.pop();
                // 入栈
                Stack<Node> tempStack = new Stack<>();;
                for (String token : production.getRight()) {
                    Node node = new Node(nodeCnt, cur.getId(), token);
                    tempStack.push(node);
                    parseTree.add(node);
                    nodeCnt++;
                }
                while (!tempStack.empty()) {
                    parsingStack.push(tempStack.peek());
                    tempStack.pop();
                }
            }
            else { // 错误输出
                String missingToken = parseTree.getNextTerminal(cur.getId());
                error(index, missingToken);
                // 错误处理
                tokens.add(index, missingToken);
            }
            cur = parsingStack.peek();
        }
    }
    
    private static void error() {
        System.out.println("语法错误");
    }

    private static void error(int index, String token) {
        int line = 1; // 获取行号
        int count = 0;
        for (int i = 0; i < prog.toString().length() && count < index; i++) {
            if (prog.charAt(i) == tokens.get(count).charAt(0)) {
                int j = 1;
                while (j < tokens.get(count).length() && prog.charAt(i) == tokens.get(count).charAt(j)) {
                    i++; j++;
                }
                count++;
            } 
            else if (prog.charAt(i) == '\n') {
                line++;
            }
        }
        System.out.println("语法错误,第" + line + "行,缺少\"" + token + "\"");
    }
    /******* this method is to read the standard input *******/
    private static void read_prog() {
        try (Scanner sc = new Scanner(System.in)) {
            while (sc.hasNextLine()) {
                prog.append(sc.nextLine()).append("\n");
            }
            // 添加特殊符号
            prog.append("$");
            System.out.println("prog");
            System.out.println(prog);
        }
    }
    /******* you should add some code in this method to achieve this lab ******/
    private static void analysis() {
        // init_productions();
        init_parsingTable();
        initStack();
        read_prog();
        tokens.addAll(LexicalAnalyzer.tokenize(prog.toString()));
        parse();
        parseTree.print();
        // System.out.print(prog);
    }
    
    public static void main(String[] args) {
        analysis();
    }
}

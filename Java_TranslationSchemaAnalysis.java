package lab4;

import java.util.*;

enum Action {
    ADDITION,
    SUBTRACTION,
    MULTIPLICATION,
    DIVISION,
    ASSIGNMENT,
    DECLARE,
    COMPARISION,
    CONDITIONING,
    NO_ACTION
}

enum DigitsType {
    INT,
    REAL,
    NONDIGITS;
    public static String getType(DigitsType digitsType) {
        switch (digitsType) {
            case INT:
                return "int";
            case REAL:
                return "realnum";
            default:
                return "";
        }
    }
}

class Production {
    private String left;
    private List<String> right = new ArrayList<>();
    private Action action = Action.NO_ACTION;
    public Production() {
    }

    Production(String left, String right) {
        this.left = left;
        this.right.addAll(Arrays.asList(right.split("\\s+")));
    }

    public Production(String left, String right, Action action) {
        this.left = left;
        this.right.addAll(Arrays.asList(right.split("\\s+")));
        this.action = action;
    }

    public String getLeft() {
        return left;
    }

    public List<String> getRight() {
        return right;
    }
    public Action getAction() {
        return action;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Production that = (Production) o;
        return Objects.equals(getLeft(), that.getLeft()) &&
                Objects.equals(getRight(), that.getRight()) &&
                getAction() == that.getAction();
    }
}

class Node {
    private String token;
    private DigitsType digitsType = DigitsType.NONDIGITS;
    private Double value;
    private String identifier;
    public Node() {
        token = "";
    }

    public Node(String token) {
        this.token = token;
    }
    public String getToken() {
        return token;
    }

    public DigitsType getDigitsType() {
        return digitsType;
    }

    public void setDigitsType(DigitsType digitsType) {
        this.digitsType = digitsType;
    }

    public Double getValue() {
        return value;
    }

    public void setValue(Double value) {
        this.value = value;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    @Override
    public String toString() {
        return token;
    }
}

class Calculator {
    private final static char[] operatorsDef = new char[]{'+', '-', '*', '/', '(', ')'};
    public static boolean isOperator(char op) {
        for (char c : operatorsDef) {
            if (op == c) {
                return true;
            }
        }
        return false;
    }
    // 运算符优先等级
    // ()最高=2  */ 其次=1  +- 最低=0
    public static int getOperatorPriorityValue(char op) {
        return (String.copyValueOf(operatorsDef).indexOf(op)) / 2;
    }
    // 将中缀表达式转换成后缀表达式 逆波兰表达式 RPN
    // input:expression 一个中缀表达式
    // output 逆波兰表达式
    /*
    辅助: 运算符栈stack
    1. 遍历expression 中每个char curChar
    2.
    curChar为数字时: 直接加到output中
    curChar为运算符时:
        curChar=='(' 入栈
        curChar==')' 栈pop加入output中 直到左括号'('
        curChar=='+'- * /  栈pop加入output中 直到左括号'('
            运算符栈stack = 空 or '(' ----> curChar push入栈
            运算符栈stack顶top优先级 < curChar优先级 -->
                curChar push入栈
            运算符栈stack顶top优先级 >= curChar优先级 -->
                运算符栈stack弹出 直到遇到'(' or 比curChar优先级更低的((让curChar优先级 >
                    curChar再push入栈
            遍历完 弹出运算符栈stack所有运算符 按照弹出顺序append到output末尾
    */
    public static List<String> parse(String expression) {
        // output 逆波兰表达式
        List<String> output = new LinkedList<>();
        // 运算符栈
        Deque<Character> operators = new LinkedList<>();

        // 记录单次截取expression的start + end(excluded)
        int fromPos = 0;
        int toPos = 0;

        // 从左到右遍历expression中的每一个字符curChar
        for (char curChar : expression.toCharArray()) {
            ++toPos;
            if (isOperator(curChar)) {
                if (fromPos < toPos - 1) {
                    output.add(expression.substring(fromPos, toPos - 1));
                }
                fromPos = toPos;
                if (curChar == '(') {
                    operators.push(curChar);
                }
                else if (curChar == ')') {
                    char op;
                    while (!operators.isEmpty() && (op = operators.pop()) != '(') {
                        output.add(String.valueOf(op));
                    }
                }
                else {
                    // 直接push入栈
                    if (operators.isEmpty()) {
                        operators.push(curChar);
                    }
                    else if (operators.peek() == '(') {
                        operators.push(curChar);
                    }
                    else if (getOperatorPriorityValue(curChar) > getOperatorPriorityValue(operators.peek())) {
                        operators.push(curChar);
                    }
                    else {
                        while (!operators.isEmpty() && getOperatorPriorityValue(curChar) <= getOperatorPriorityValue(operators.peek()) && operators.peek() != '(') {
                            output.add(String.valueOf(operators.pop()));
                        }
                        operators.push(curChar);
                    }
                }
            }
        }
        if (fromPos < expression.length()) {
            output.add(expression.substring(fromPos));
        }
        while (!operators.isEmpty()) {
            output.add(String.valueOf(operators.pop()));
        }
        return output;
    }

    private static String expressionCompletion(String expression) {
        StringBuilder sb = new StringBuilder(expression);
        int len = sb.length();
        boolean flag = false;
        for (int i = 0; i < len; ++i) {
            char c = sb.charAt(i);
            flag = false;
            if (c == '-' || c == '+') {
                if (i == 0) {
                    sb.insert(0, "(0");
                    flag = true;
                }
                else if (isOperator(sb.charAt(i - 1)) && sb.charAt(i - 1) != ')')  {
                    sb.insert(i, "(0");
                    flag = true;
                }
                if (flag) {
                    len += 2;
                    for (int j = i + 3; i < len; j++) {
                        if (isOperator(sb.charAt(j))) {
                            sb.insert(j, ')');
                            ++len;
                            break;
                        }
                    }
                }
            }
        }
        return sb.toString();
    }

    public static double calculate(String expr) {
        // 0补充中序表达式 免得异常
        expr = expressionCompletion(expr);
        List<String> rpnList = parse(expr);
        Deque<Double> operands = new LinkedList<>();
        if (isOperator(rpnList.get(0).charAt(0))) {
            return 0.0;
        }
        for (String elem : rpnList) {
            if (isOperator(elem.charAt(0))) {
                if (operands.size() < 2) {
                    return 0.0;
                }
                double value2 = operands.pop();
                double value1 = operands.pop();
                double result = binaryOperation(elem.charAt(0), value1, value2);
                operands.push(result);
            }
            else {
                operands.push(Double.parseDouble(elem));
            }
        }
        if (operands.size() != 1) {
            return 0.0;
        }
        return operands.pop();
    }

    // 二元运算
    private static double binaryOperation(char operator, double value1, double value2) {
        switch (operator) {
            case '+':
                return value1 + value2;
            case '-':
                return value1 - value2;
            case '*':
                return value1 * value2;
            case '/': // 处理分母=0的问题
                if (value2 == 0)  return 0.0;
                return value1 / value2;
            default:
                return 0.0;
        }
    }
}

public class Java_TranslationSchemaAnalysis {
    // 程序输入
    private static StringBuffer prog = new StringBuffer();

    private static List<String> nonterminals = new ArrayList<>(Arrays.asList("program", "decls", "decl", "stmt",
            "compoundstmt",
            "stmts", "ifstmt", "assgstmt", "boolexpr", "boolop", "arithexpr", "arithexprprime", "multexpr",
            "multexprprime", "simpleexpr"
    ));

    private static List<String> terminals = new ArrayList<>(Arrays.asList(";", "int", "ID", "=", "INTNUM",
            "real", "REALNUM", "{", "}", "if", "(", ")", "then", "else", "<", ">", "<=", ">=", "==", "+", "-", "*", "/"));

    private static List<Production> productions = new ArrayList<>(Arrays.asList(
            new Production("program", "decls compoundstmt"),
            new Production("decls", "decl ; decls"),
            new Production("decls", "E"),
            new Production("decl", "int ID = INTNUM", Action.DECLARE),
            new Production("decl", "real ID = REALNUM", Action.DECLARE),
            new Production("stmt", "ifstmt"),
            new Production("stmt", "assgstmt"),
            new Production("stmt", "compoundstmt"),
            new Production("compoundstmt", "{ stmts }"),
            new Production("stmts", "stmt stmts"),
            new Production("stmts", "E"),
            new Production("ifstmt", "if ( boolexpr ) then stmt else stmt", Action.CONDITIONING),
            new Production("assgstmt", "ID = arithexpr ;", Action.ASSIGNMENT),
            new Production("boolexpr", "arithexpr boolop arithexpr", Action.COMPARISION),
            new Production("boolop", "<"),
            new Production("boolop", ">"),
            new Production("boolop", "<="),
            new Production("boolop", ">="),
            new Production("boolop", "=="),
            new Production("arithexpr", "multexpr arithexprprime"),
            new Production("arithexprprime", "+ multexpr arithexprprime", Action.ADDITION),
            new Production("arithexprprime", "- multexpr arithexprprime", Action.SUBTRACTION),
            new Production("arithexprprime", "E"),
            new Production("multexpr", "simpleexpr  multexprprime"),
            new Production("multexprprime", "* simpleexpr multexprprime", Action.MULTIPLICATION),
            new Production("multexprprime", "/ simpleexpr multexprprime", Action.DIVISION),
            new Production("multexprprime", "E"),
            new Production("simpleexpr", "ID"),
            new Production("simpleexpr", "INTNUM"),
            new Production("simpleexpr", "REALNUM"),
            new Production("simpleexpr", "( arithexpr )")
    ));
    private static final String startSymbol = "program";

    private static List<String> tokens = new ArrayList<>();

    private static HashMap<String, HashMap<String, Production>> parseTable = new HashMap<>();
    
    private static Deque<Node> pStack = new LinkedList<>();

    private static List<Node> identifiers = new ArrayList<>();

    private static boolean isError = false;

    private static void initParseTable() {

        for (String nonTerminal : nonterminals) {
            parseTable.put(nonTerminal, new HashMap<>());
        }
        parseTable.get("program").put("int", productions.get(0));
        parseTable.get("program").put("real", productions.get(0));
        parseTable.get("program").put("{", productions.get(0));
        parseTable.get("decls").put("int", productions.get(1));
        parseTable.get("decls").put("real", productions.get(1));
        parseTable.get("decls").put("{", productions.get(2));
        parseTable.get("decl").put("int", productions.get(3));
        parseTable.get("decl").put("real", productions.get(4));
        parseTable.get("stmt").put("ID", productions.get(6));
        parseTable.get("stmt").put("{", productions.get(7));
        parseTable.get("stmt").put("if", productions.get(5));
        parseTable.get("compoundstmt").put("{", productions.get(8));
        parseTable.get("stmts").put("ID", productions.get(9));
        parseTable.get("stmts").put("{", productions.get(9));
        parseTable.get("stmts").put("}", productions.get(10));
        parseTable.get("stmts").put("if", productions.get(9));
        parseTable.get("ifstmt").put("if", productions.get(11));
        parseTable.get("assgstmt").put("ID", productions.get(12));
        parseTable.get("boolexpr").put("ID", productions.get(13));
        parseTable.get("boolexpr").put("INTNUM", productions.get(13));
        parseTable.get("boolexpr").put("REALNUM", productions.get(13));
        parseTable.get("boolexpr").put("(", productions.get(13));
        parseTable.get("boolop").put("<", productions.get(14));
        parseTable.get("boolop").put(">", productions.get(15));
        parseTable.get("boolop").put("<=", productions.get(16));
        parseTable.get("boolop").put(">=", productions.get(17));
        parseTable.get("boolop").put("==", productions.get(18));
        parseTable.get("arithexpr").put("ID", productions.get(19));
        parseTable.get("arithexpr").put("INTNUM", productions.get(19));
        parseTable.get("arithexpr").put("REALNUM", productions.get(19));
        parseTable.get("arithexpr").put("(", productions.get(19));
        parseTable.get("arithexprprime").put(";", productions.get(22));
        parseTable.get("arithexprprime").put(")", productions.get(22));
        parseTable.get("arithexprprime").put("<", productions.get(22));
        parseTable.get("arithexprprime").put(">", productions.get(22));
        parseTable.get("arithexprprime").put("<=", productions.get(22));
        parseTable.get("arithexprprime").put(">=", productions.get(22));
        parseTable.get("arithexprprime").put("==", productions.get(22));
        parseTable.get("arithexprprime").put("+", productions.get(20));
        parseTable.get("arithexprprime").put("-", productions.get(21));
        parseTable.get("multexpr").put("ID", productions.get(23));
        parseTable.get("multexpr").put("INTNUM", productions.get(23));
        parseTable.get("multexpr").put("REALNUM", productions.get(23));
        parseTable.get("multexpr").put("(", productions.get(23));
        parseTable.get("multexprprime").put(";", productions.get(26));
        parseTable.get("multexprprime").put(")", productions.get(26));
        parseTable.get("multexprprime").put("<", productions.get(26));
        parseTable.get("multexprprime").put(">", productions.get(26));
        parseTable.get("multexprprime").put("<=", productions.get(26));
        parseTable.get("multexprprime").put(">=", productions.get(26));
        parseTable.get("multexprprime").put("==", productions.get(26));
        parseTable.get("multexprprime").put("+", productions.get(26));
        parseTable.get("multexprprime").put("-", productions.get(26));
        parseTable.get("multexprprime").put("*", productions.get(24));
        parseTable.get("multexprprime").put("/", productions.get(25));
        parseTable.get("simpleexpr").put("ID", productions.get(27));
        parseTable.get("simpleexpr").put("INTNUM", productions.get(28));
        parseTable.get("simpleexpr").put("REALNUM", productions.get(29));
        parseTable.get("simpleexpr").put("(", productions.get(30));
        // 栈初始化
        pStack.push(new Node("$"));
        pStack.push(new Node(startSymbol));
        tokens.addAll(Arrays.asList(prog.toString().split("\\s+")));
    }

    private static boolean isTerminal(String str) {
        if (str.isEmpty() || typeOfDigit(str) != DigitsType.NONDIGITS) {
            return false;
        }
        return !terminals.contains(str) && !nonterminals.contains(str);
    }

    private static DigitsType typeOfDigit(String str) {
        if (str.isEmpty()) {
            return DigitsType.NONDIGITS;
        }
        boolean decimalPoint = false;
        for(char curChar : str.toCharArray()){
            if(curChar == '.'){
                decimalPoint = true;
            }
            else if(curChar < '0' || curChar > '9'){
                return DigitsType.NONDIGITS;
            }
        }
        return decimalPoint ? DigitsType.REAL : DigitsType.INT;
    }

    private static int indexOfIdentifier(String token) {
        for (int i = 0; i < identifiers.size(); i++) {
            if (identifiers.get(i).getIdentifier().equals(token)) {
                return i;
            }
        }
        return -1;
    }

    // this method is to read the standard input
    private static void read_prog() {
        try(Scanner sc = new Scanner(System.in)){
            while (sc.hasNextLine()) {
                prog.append(sc.nextLine()).append("\n");
            }
            prog.append("$");
//            System.out.println(prog);
        }
    }

    private static void parse() {
        int index = 0, branchCount = 0;
        boolean calculating = false;
        boolean compareResult = true;
        boolean isConditioning = false;
        boolean executable = true;
        List<String> condition = new ArrayList<>();
        StringBuilder polandExpr = new StringBuilder();
        String preParsed = null;
        Node leftIdentifier = null;
        Action curAction = Action.NO_ACTION;
        Node stackTop = pStack.peek();
        while (!"$".equals(stackTop.getToken())) {
            String curToken = stackTop.getToken();
            String current = tokens.get(index);
            if ("E".equals(curToken)) {
                pStack.pop();
            } else if (curToken.equals(current)) {
                if (";".equals(current)) {
                    switch (curAction) {
                        case DECLARE:
                            curAction = Action.NO_ACTION;
                            leftIdentifier = null;
                            break;
                        case ASSIGNMENT:
                            double result = Calculator.calculate(polandExpr.toString());
                            if (executable) {
                                leftIdentifier.setValue(result);
                            }
                            leftIdentifier = null;
                            calculating = false;
                            curAction = Action.NO_ACTION;
                            polandExpr.setLength(0);
                            break;
                        default:
                            break;
                    }
                    if (isConditioning) {
                        branchCount--;
                    }
                    if (branchCount == 0) {
                        isConditioning = false;
                        executable = true;
                        condition.clear();
                    }
                }
                else if ("real".equals(current) && curAction == Action.DECLARE) {
                    leftIdentifier = new Node();
                    leftIdentifier.setDigitsType(DigitsType.REAL);
                } else if ("int".equals(current) && curAction == Action.DECLARE) {
                    leftIdentifier = new Node();
                    leftIdentifier.setDigitsType(DigitsType.INT);
                } else if (("+".equals(current) ||
                        "-".equals(current) ||
                        "*".equals(current) ||
                        "/".equals(current)) &&
                        calculating &&
                        curAction == Action.ASSIGNMENT) {
                    polandExpr.append(current).append(" ");
                }
                else if (isConditioning && ")".equals(current)) {
                    String curOp = condition.get(1);
                    if (">".equals(curOp)) {
                        compareResult = Double.parseDouble(condition.get(0)) > Double.parseDouble(condition.get(2));
                    } else if ("<".equals(curOp)) {
                        compareResult = Double.parseDouble(condition.get(0)) < Double.parseDouble(condition.get(2));
                    } else if (">=".equals(curOp)) {
                        compareResult = Double.parseDouble(condition.get(0)) >= Double.parseDouble(condition.get(2));
                    } else if ("<=".equals(curOp)) {
                        compareResult = Double.parseDouble(condition.get(0)) <= Double.parseDouble(condition.get(2));
                    } else if ("==".equals(curOp)) {
                        compareResult = Double.parseDouble(condition.get(0)) == Double.parseDouble(condition.get(2));
                    } else {
                        compareResult = false;
                    }
                    curAction = Action.NO_ACTION;
                }
                else if("then".equals(current) && isConditioning){
                    executable = compareResult;
                }
                else if("else".equals(current) && isConditioning){
                    executable = !compareResult;
                }
                else if (curAction == Action.COMPARISION && isConditioning &&
                        (">".equals(current) || "<".equals(current) || ">=".equals(current) || "<=".equals(current) || "==".equals(current))) {
                    condition.add(current);
                }
                preParsed = pStack.pop().getToken();
                index++;
            }
            else if ("ID".equals(curToken) || "REALNUM".equals(curToken) ||
                    "INTNUM".equals(curToken)) {
                if (typeOfDigit(current) == DigitsType.INT) {
                    int value = Integer.parseInt(current);
                    // divide by 0
                    if (value == 0 && "/".equals(preParsed)) {
                        error(index);
                    }
                    if (curAction == Action.DECLARE) {
                        leftIdentifier = identifiers.get(indexOfIdentifier(leftIdentifier.getIdentifier()));
                        if (leftIdentifier.getDigitsType() != DigitsType.INT) {
                            error(index, DigitsType.INT, DigitsType.REAL);
                        }
                        leftIdentifier.setValue(Double.parseDouble(String.valueOf(value)));
                    }
                    if (curAction == Action.ASSIGNMENT && calculating) {
                        polandExpr.append(current).append(" ");
                    }
                    if (curAction == Action.COMPARISION && isConditioning) {
                        condition.add(current);
                    }
                    preParsed = pStack.pop().getToken();
                    index++;
                } else if (typeOfDigit(current) == DigitsType.REAL) {
                    double value = Double.parseDouble(current);
                    // 除法分母是 0
                    if (value == 0.0 && "/".equals(preParsed)) {
                        error(index);
                    }
                    if (curAction == Action.DECLARE) {
                        leftIdentifier = identifiers.get(indexOfIdentifier(leftIdentifier.getIdentifier()));
                        if (leftIdentifier.getDigitsType() != DigitsType.REAL) {
                            error(index, DigitsType.REAL, DigitsType.INT);
                        }
                        leftIdentifier.setValue(value);
                    }
                    if (curAction == Action.ASSIGNMENT && calculating) {
                        polandExpr.append(current).append(" ");
                    }
                    if (curAction == Action.COMPARISION && isConditioning) {
                        condition.add(current);
                    }
                    preParsed = pStack.pop().getToken();
                    index++;
                } else {
                    if (!isTerminal(current)) {
                        error();
                        break;
                    }
                    if (curAction == Action.DECLARE) {
                        leftIdentifier.setIdentifier(current);
                        identifiers.add(leftIdentifier);
                    }
                    if (curAction == Action.ASSIGNMENT) {
                        if (!calculating) {
                            leftIdentifier = identifiers.get(indexOfIdentifier(current));
                            calculating = true;
                        } else {
                            polandExpr.append(identifiers.get(indexOfIdentifier(current)).getValue()).append(" ");
                        }
                    }
                    if (curAction == Action.COMPARISION && isConditioning) {
                        Node identifier = identifiers.get(indexOfIdentifier(current));
                        condition.add(identifier.getValue().toString());
                    }
                    preParsed = pStack.pop().getToken();
                    index++;
                }
            }
            else if (terminals.contains(curToken)) {
                error();
            }
            else if ((parseTable.containsKey(curToken) && parseTable.get(curToken).containsKey(current)) ||
                    (typeOfDigit(current) == DigitsType.NONDIGITS && parseTable.containsKey(curToken) && parseTable.get(curToken).containsKey("ID")) ||
                    (typeOfDigit(current) == DigitsType.INT && parseTable.containsKey(curToken) && parseTable.get(curToken).containsKey("INTNUM")) ||
                    (typeOfDigit(current) == DigitsType.REAL && parseTable.containsKey(curToken) && parseTable.get(curToken).containsKey("REALNUM"))) {
                Production production = null;

                if ((parseTable.containsKey(curToken) && parseTable.get(curToken).containsKey(current))) {
                    production = parseTable.get(curToken).get(current);
                }
                else if (typeOfDigit(current) == DigitsType.NONDIGITS && parseTable.containsKey(curToken) && parseTable.get(curToken).containsKey("ID")) {
                    production = parseTable.get(curToken).get("ID");
                }
                else if (typeOfDigit(current) == DigitsType.INT && parseTable.containsKey(curToken) && parseTable.get(curToken).containsKey("INTNUM")) {
                    production = parseTable.get(curToken).get("INTNUM");
                }
                else if (typeOfDigit(current) == DigitsType.REAL && parseTable.containsKey(curToken) && parseTable.get(curToken).containsKey("REALNUM")) {
                    production = parseTable.get(curToken).get("REALNUM");
                }
                pStack.pop();
                if (curAction == Action.NO_ACTION) {
                    switch (production.getAction()) {
                        case DECLARE:
                            curAction = Action.DECLARE;
                            break;
                        case ASSIGNMENT:
                            curAction = Action.ASSIGNMENT;
                            break;
                        case COMPARISION:
                            curAction = Action.COMPARISION;
                            break;
                        case CONDITIONING:
                            isConditioning = true;
                            branchCount = 2;
                        default:
                            curAction = Action.NO_ACTION;
                            break;
                    }
                }
                Deque<Node> tempStack = new LinkedList<>();
                for (String token : production.getRight()) {
                    tempStack.push(new Node(token));
                }
                while (!tempStack.isEmpty()) {
                    pStack.push(tempStack.peek());
                    tempStack.pop();
                }
            }
            else {
                error();
            }
            stackTop = pStack.peek();
        }
    }

    // error出现行
    private static int getErrorLine(int index) {
        int lineNo = 1, cnt = 0;
        char[] prog2Chars = prog.toString().toCharArray();
        for (int i = 0; i < prog2Chars.length && cnt < index; i++) {
            if (prog2Chars[i] == tokens.get(cnt).charAt(0)) {
                int j = 1;
                while (j < tokens.get(cnt).length() && prog2Chars[i] == tokens.get(cnt).charAt(j)) {
                    i++; j++;
                }
                cnt++;
            }
            else if (prog2Chars[i] == '\n') {
                lineNo++;
            }
        }
        return lineNo;
    }
    private static void error() {
        isError = true;
        System.out.println("syntax error");
    }
    private static void error(int index) {
        isError = true;
        System.out.print("error message:line " + getErrorLine(index) + ",division by zero");
    }
    private static void error(int index, DigitsType from, DigitsType to) {
        isError = true;
        System.out.println("error message:line " + getErrorLine(index) + "," + DigitsType.getType(from) + " can not be translated into " + DigitsType.getType(to) + " type");
    }

    public static void main(String[] args) {
        read_prog();
        initParseTable();
        parse();
        if (!isError) {
            for (int i = 0; i < identifiers.size(); i++) {
                Node id = identifiers.get(i);
                System.out.print(id.getIdentifier() + ": " + ((id.getDigitsType() == DigitsType.INT) ? String.format("%.0f", id.getValue()) : id.getValue()));
                if (i < identifiers.size() - 1) {
                    System.out.println();
                }
            }
        }
    }

}
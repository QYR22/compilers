import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;


enum Type {
    IDENTIFIER, // 标识符
    NUMBER, // 常数
    RESERVED_WORD, // 保留字
    DELIMITER, // 分隔符
    OPERATOR, // 运算符
    STRING,
    COMMENT,
    ENDOFFILE,
    ILLEGAL,
    UNKNOWN; // 用于错误处理
}

enum CommentType {
    MULTILINECOMMENT, // "/**/"
    SINGLELINECOMMENT, // "//"
    NOCOMMENT
}

// 用类将符号表抽象 类型+符号+编号
class Token {
    protected Integer id;
    protected Type type;
    protected String symbol;

    public Token() {
        this.type = Type.UNKNOWN;
    }

    public Token(Type type) {
        this.type = type;
    }

    public Token(Integer id, Type type, String symbol) {
        this.id = id;
        this.type = type;
        this.symbol = symbol;
    }

    public Token(Integer id, String symbol) {
        this.id = id;
        this.type = Type.UNKNOWN; // 默认设置为UNKNOWN
        this.symbol = symbol;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    // 定义格式化输出 方便打印
    @Override
    public String toString() {
        return "<" + symbol + "," + id + ">";
    }
}

// todo 看看可不可以顺利用
class TokenException extends RuntimeException {
    public TokenException() {
        super();
    }

    public TokenException(String message) {
        super(message);
    }
}

public class Java_LexAnalysis {
    /* xxx 一些预先定义 */
    private static Set<Character> delimiters = new HashSet<>(
            Arrays.asList('(', ')', '{', '}', '[', ']', ',', ':', ';', '?', '"', '\'', '.'));
    private static Set<Character> operators = new HashSet<>(
            Arrays.asList('+', '-', '*', '/', '%', '^', '&', '|', '~', '!', '<', '=', '>'));
    // 定义不合法的运算符组合
    private static final Set<String> illegalOperators = new HashSet<>(
            Arrays.asList("<=<","=<","=>","++--","&&&","+++","---","===","!=="));
    
    
            // prog读入待分析的
    private static StringBuffer prog = new StringBuffer();

    // 利用 HashMap存储符号表
    // 相对于用动态数组，1. HashMap的<key,value>存储+查找更高效；2. HashMap可以存储1个null-key和多个null-value
    // 如果使用数组or动态数组等线性结构查找过程耗时很长
    public static HashMap<String, Integer> sybmolMap = new HashMap<>();

    // 存储输入与符号表配对结果
    private static List<Token> tokens = new ArrayList<>();

    // 输出序列
    private static StringBuffer tokenOutput = new StringBuffer();

    /* xxx this method is to read the standard input  */
    private static void read_prog() {
        Scanner sc = new Scanner(System.in);
        while (sc.hasNextLine()) {
            // 不添加\n prog会变成一行 since Wins行末\r\n \n不会读入到prog中
            prog.append(sc.nextLine() + "\n");
        } 
        // xxx 删除最后一个自行添加的换行符\n
        prog.deleteCharAt(prog.length() - 1);
        sc.close(); // sc should be closed
    }

    /* ----------------- add your method here!! ----------------- */

    /* xxx 初始化符号表sybmolMap 读入c_key.txt自动生成 相对手动完成优势：拓展性、可重复性、自动化、批量 */
    private static void initSymbolMap() {
        String filePath = System.getProperty("user.dir") + File.separator + "c_key.txt";
        // 使用try-with-resource 确保资源在语句结束时关闭
        try (BufferedReader bufferedReader = Files.newBufferedReader(Paths.get(filePath))) {
            String newLine;
            while ((newLine = bufferedReader.readLine()) != null) {
                String[] symbolList = newLine.split("\\t| "); // delimiter = \t or blankspace
                if (symbolList.length > 0) {
                    // System.out.println(symbolList[0] + Integer.valueOf(symbolList[symbolList.length - 1]));
                    sybmolMap.put(symbolList[0], Integer.valueOf(symbolList[symbolList.length - 1]));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    /* xxx 判读是否是常数 用RE书写 */
    private static boolean isDigit(String str) {
        // ?表示小数点可选
        return str.matches("\\d*\\.?\\d+");
    }

    /* xxx 判断是否是delimiter分隔符 */
    private static boolean isDelimiter(char symb) {
        return delimiters.contains(Character.valueOf(symb));
    }

    /* xxx 判断是否是operator运算符 */
    private static boolean isOperator(char symb) {
        return operators.contains(Character.valueOf(symb));
    }

    private static void tokenize() throws TokenException {
        CommentType commentType = CommentType.NOCOMMENT;
        StringBuffer tokenStr = new StringBuffer();

        for (int i = 0; i < prog.length(); i++) {
            // 当前字符
            char curSymb = prog.charAt(i);
            // todo 这里注释看看能不能改成while直到的
            // 1.多行注释
            if (commentType == CommentType.MULTILINECOMMENT) {
                // 注释结束
                if (i > 0 && curSymb == '/' && prog.charAt(i - 1) == '*') {
                    tokenStr.append(curSymb);
                    tokens.add(new Token(Integer.valueOf(79), Type.COMMENT, tokenStr.toString()));
                    commentType = CommentType.NOCOMMENT;
                    tokenStr.setLength(0);
                }
                // 注释ing
                else {
                    tokenStr.append(curSymb);
                }
            }
            // 2. 单行注释
            // done 注意，C_key.txt中缺少“//注释”的情况，请也映射到编号79
            else if (commentType == CommentType.SINGLELINECOMMENT) {
                // 注释结束
                if (curSymb == '\n') {
                    tokens.add(new Token(Integer.valueOf(79), Type.COMMENT, tokenStr.toString()));
                    commentType = CommentType.NOCOMMENT;
                    tokenStr.setLength(0);
                }
                // 注释ing
                else {
                    tokenStr.append(curSymb);
                }
            }
            // 3.注释外
            else { // 一个token收集完
                // 方便跳过' ' '\n' '\t' '\r'的情况
                if (curSymb == ' ' || curSymb == '\n' || curSymb == '\r' || curSymb == '\t') {
                    if (tokenStr.length() != 0) {
                        matchToken(tokenStr);
                    }
                    else {
                        continue;      
                    }
                }
                // 注释开始
                else if (i < prog.length() - 1 && curSymb == '/') {
                    char nextSymb = prog.charAt(i + 1);
                    if (nextSymb == '/' || nextSymb == '*') {
                        commentType = (nextSymb == '/') ? CommentType.SINGLELINECOMMENT : CommentType.MULTILINECOMMENT;
                        tokenStr.append(curSymb);
                        tokenStr.append(nextSymb);
                        i++;
                    }
                }
                // 运算符 (>=1个)运算符会一次性添加
                // 所以如果进if时tokenStr不为空，说明上一个token没有匹配，先匹配
                else if (isOperator(curSymb)) {
                    if (tokenStr.length() != 0) {
                        matchToken(tokenStr);
                    }
                    tokenStr.append(curSymb);
                    // 检查是否为多运算符
                    if (i < prog.length() - 1) {
                        char nextSymb = prog.charAt(i + 1);
                        char nnextSymb = prog.charAt(i + 2);
                        // >1 operator
                        // done why || nextSymb != '-' 
                        if (isOperator(nextSymb) /* && (nextSymb != '!' || nextSymb != '-') */) {
                            tokenStr.append(nextSymb);
                            i++;
                        }
                        // 三目 >>=
                        if (nnextSymb == '=') {
                            tokenStr.append(nnextSymb);
                            i++;
                        }
                        // done 错误处理 检测是否是C语言中的非法运算符
                        if (illegalOperators.contains(tokenStr.toString())) {
                            throw new TokenException("illegal operators combination");
                        }
                    }
                    // 向结果集添加
                    matchToken(tokenStr);
                }
                // delimiter 分隔符
                else if (isDelimiter(curSymb)) {
                    if (tokenStr.length() != 0) {
                        matchToken(tokenStr);
                    }
                    // 引号
                    if (curSymb == '"') {
                        tokenStr.append(curSymb);
                        matchToken(tokenStr);
                        i++;
                        // 直接找到下一个引号 排除引号内部被转义的部分，同时`\`也要转义，所以写成`\\`
                        while (i < prog.length() &&
                                (prog.charAt(i) != '"' || (prog.charAt(i) == '"' && prog.charAt(i - 1) == '\\'))) {
                            tokenStr.append(prog.charAt(i));
                            i++;
                        }
                        // 引号内部内容作为identifier
                        matchToken(tokenStr);
                        // 结尾处的引号加入
                        tokenStr.append(prog.charAt(i));
                        matchToken(tokenStr);
                    }
                    // 单引号 测试用例没有>< 整体放到identifier里面处理了
                    else if (curSymb == '\'') {
                        tokenStr.append(curSymb);
                        matchToken(tokenStr);
                        i++;

                        while (i < prog.length() &&
                                (prog.charAt(i) != '\'' || (prog.charAt(i) == '\'' && prog.charAt(i - 1) == '\\'))) {
                            tokenStr.append(prog.charAt(i));
                            i++;
                        }
                        matchToken(tokenStr);
                        tokenStr.append(prog.charAt(i));
                        matchToken(tokenStr);
                    } else if (curSymb == '.') {
                        if (isDigit(tokenStr.toString())) {
                            tokenStr.append(prog.charAt(i));
                        } else {
                            tokenStr.append(curSymb);
                            matchToken(tokenStr);
                        }
                    } else {
                        tokenStr.append(curSymb);
                        matchToken(tokenStr);
                    }
                }
                else {
                    tokenStr.append(curSymb);
                }
            }
        }
    }

    //done keyword digit identifer-81
    private static void matchToken(StringBuffer tokenStr) {
        String tempString = tokenStr.toString();
        // keyword保留字
        if (sybmolMap.containsKey(tempString)) {
            tokens.add(new Token(sybmolMap.get(tempString), tempString));
        }
        // digit
        else if (isDigit(tempString)) {
            tokens.add(new Token(Integer.valueOf(80), tempString));
        }
        // identifier
        else {
            tokens.add(new Token(Integer.valueOf(81), tempString));
        }
        tokenStr.setLength(0);
    }

    private static void print() {
        for (int i = 1; i <= tokens.size(); i++) {
            tokenOutput.append(i + ": " + tokens.get(i - 1).toString() + '\n');
        }
        tokenOutput.deleteCharAt(tokenOutput.length() - 1);
        // 用println会多\n
        System.out.print(tokenOutput);
    }

    /* you should add some code in this method to achieve this lab */

    private static void analysis() {
        initSymbolMap();
        read_prog();
        tokenize();
        print();
    }

    /**
     * this is the main method
     * 
     * @param args
     */
    public static void main(String[] args) {
        analysis();
        // System.out.println(System.getProperty("user.dir"));
    }
}

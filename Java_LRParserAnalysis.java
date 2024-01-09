package lab3;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;

enum Action{
    NONTERMINAL,
    SHIFT,
    REDUCTION,
    ACCEPT,
    ERROR
}
class Operation {
    private Action flag;

    // = -1时为acc 否则为Action
    private int destination;

    public Action getFlag() {
        return flag;
    }
    public void setFlag(Action flag) {
        this.flag = flag;
    }

    public int getDestination() {
        return destination;
    }

    public void setDestination(int destination) {
        this.destination = destination;
    }
}
class Token {
    private int line; //行数
    private String token;//单词
    public Token(){
    }
    public Token(int line, String token){
        this.line = line;
        this.token = token;
    }
    public void setLine(int line) {
        this.line = line;
    }
    public String getWord() {
        return token;
    }
    public void setWord(String token) {
        this.token = token;
    }
}
class StackFactor {

    private int state;//状态序号
    private String key;//匹配字符

    public StackFactor() {
    }
    public StackFactor(int state, String key) {
        this.state = state;
        this.key = key;
    }

    public int getState() {
        return state;
    }

    public String getKey() {
        return key;
    }
}
class TableFactor {

    //状态序号
    private int stateFrom;
    //字符
    private String key;

    private Operation operation;

    private TableFactor(int stateFrom, String key, Operation operation) {
        this.stateFrom = stateFrom;
        this.key = key;
        this.operation = operation;
    }

    public static void setTableFactor(int stateFrom, String key, Operation operation){
        TableFactor tableFactor = new TableFactor(stateFrom,key,operation);
        AnalysisTable.getTable().addFactor(tableFactor);
    }

    public int getStateFrom() {
        return stateFrom;
    }

    public String getKey() {
        return key;
    }

    public Operation getOperation() {
        return operation;
    }
}
class AnalysisTable {
    private static AnalysisTable analysisTable = new AnalysisTable();

    private List<TableFactor> tableFactorList = new CopyOnWriteArrayList<>();

    public static void refresh(){
        analysisTable = new AnalysisTable();
    }

    //添加表元素
    public void addFactor(TableFactor tableFactor){
        tableFactorList.add(tableFactor);
    }

    //根据状态序号和字符查询表元素
    public Operation findFactor(int stateFrom,String key){
        for(TableFactor tableFactor : tableFactorList){
            if(tableFactor.getStateFrom() == stateFrom && (tableFactor.getKey()+"").equals(key)){
                return tableFactor.getOperation();
            }
        }
        return null;
    }
    private AnalysisTable(){
    }
    public static AnalysisTable getTable() {
        return analysisTable;
    }
}
class State {
    //状态序号作为map的key来存，所以这里不存储序号
    private List<Production> productionList = new ArrayList<>();
    public List<Production> getProductionList() {
        return productionList;
    }

    public State(){
    }

    public State(List<Production> productionList){
        this.productionList = productionList;
    }
    //查看是否是旧状态，如果不是-1说明该状态已经存在，如果是-1说明是新状态
    public int indexOf(List<State> stateList){
        List<Production> productionList = this.getProductionList();
        boolean flag = true;
        for(State state : stateList){
            flag = true;
            for(Production grammar : productionList){
                List<Production> tmpProductionList = state.getProductionList();
                if(grammar.indexOf(tmpProductionList)==-1){
                    flag = false;
                    break;
                }
            }
            if(flag){
                return stateList.indexOf(state);
            }
        }
        return -1;
    }
}
class Production {

    private String[] productionStr;

    // false = 移进
    // true = 归约
    // index == productionStr.length时，也就是归约文法
    private boolean flag = false;
    // ·的位置
    // 对 S→E+id index = 2 也就是在String[2]的位置前有个点。所以是S→E·+id
    private int index;

    public String[] getProductionStr() {
        return productionStr;
    }

    public boolean isFlag() {
        return flag;
    }

    public int getIndex() {
        return index;
    }

    //给出构造方法
    public Production(String[]productionStr, int index){
        this.productionStr = productionStr;
        this.index = index;
        if(productionStr == null)return;
        if(index > productionStr.length)return;
        if(index == productionStr.length) flag = true;
    }

    public int indexOf(List<Production> productionList){
        for(Production grammar : productionList){
            if(this.getIndex() == grammar.getIndex() && this.getProductionStr().length == grammar.getProductionStr().length){
                boolean tmpFlag = true;
                for(int i = 0; i < this.getProductionStr().length; i++){
                    if(!this.getProductionStr()[i].equals(grammar.getProductionStr()[i])){
                        tmpFlag = false;
                        break;
                    }
                }
                if(tmpFlag) return productionList.indexOf(grammar);
            }
        }
        return -1;
    }

    // 说明这个文法在productionList中是否存在
    public int indexOfWithoutIndex(List<Production> productionList){
        for(Production grammar : productionList){
            if(this.getProductionStr().length == grammar.getProductionStr().length){
                int length = this.getProductionStr().length;
                boolean tmpFlag = true;
                for(int i=0;i<length;i++){
                    if(!this.getProductionStr()[i].equals(grammar.getProductionStr()[i])){
                        tmpFlag = false;
                        break;
                    }
                }
                if(tmpFlag)return productionList.indexOf(grammar);
            }
        }
        return -1;
    }

}
class RelationContainer {
    private static List<Relation> relationList = new CopyOnWriteArrayList<>();
    public static List<Relation> getRelationList(){
        return relationList;
    }
    public static boolean isFollow(String leftSign,String key){
        if(leftSign == null)return false;
        for(Relation relation : relationList){
            if(leftSign.equals(relation.getLeftSign())){
                Set<String> followSet = relation.getFollowSet();
                for(String follow : followSet){
                    if(follow.equals(key)){
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public static void addFirst(String leftSign,String first){
        for(Relation relation : relationList){
            if (relation.getLeftSign().equals(leftSign)){
                relation.addFirst(first);
                return;
            }
        }
        Relation relation = new Relation(leftSign);
        relation.addFirst(first);
        relationList.add(relation);
    }
    public static void addFollow(String leftSign,String follow){
        for(Relation relation : relationList){
            if (relation.getLeftSign().equals(leftSign)){
                relation.addFollow(follow);
                return;
            }
        }
        Relation relation = new Relation(leftSign);
        relation.addFollow(follow);
        relationList.add(relation);
    }

    public static Set<String> getFirstSet(String leftSign){
        for(Relation relation : relationList){
            if (relation.getLeftSign().equals(leftSign)){
                return relation.getFirstSet();
            }
        }
        return null;
    }
    public static Set<String> getFollowSet(String leftSign){
        for(Relation relation : relationList){
            if (relation.getLeftSign().equals(leftSign)){
                return relation.getFollowSet();
            }
        }
        return null;
    }
}
class Relation {

    private String leftSign;//左符号

    private Set<String> firstSet = new CopyOnWriteArraySet<>();
    private Set<String> followSet = new CopyOnWriteArraySet<>();

    public void addFirst(String token){
        this.firstSet.add(token);
    }
    public void addFollow(String token){
        this.followSet.add(token);
    }

    public Relation(String leftSign){
        this.leftSign = leftSign;
    }

    public String getLeftSign() {
        return leftSign;
    }

    public Set<String> getFirstSet() {
        return firstSet;
    }

    public Set<String> getFollowSet() {
        return followSet;
    }
}
public class Java_LRParserAnalysis
{
    //状态项目集
    private static List<State> stateList = new CopyOnWriteArrayList<>();

    //follow和first集
    private static List<Relation> relationList = RelationContainer.getRelationList();

    //分析表
    private static AnalysisTable analysisTable = AnalysisTable.getTable();

    private static List<Token> words = new ArrayList<>(); // 将输入的文本分隔为word

    private static String[] reservedWords = new String[]{"{","}","if","(",")","then","else","while",
            "(",")","ID","=",">","<",">=","<=","==",
            "+","-", "*","/","NUM","E",";","$","{","}"};

    //输出
    private static List<String[]> outputList = new CopyOnWriteArrayList<>();;

    private static int begin = 0;// 为输出服务的一个tmp变量，表示已经归约了的字符数

    private static void read_prog() {
        int lineCnt = 0;
        try (Scanner sc = new Scanner(System.in)){
            while(sc.hasNextLine()) {
                lineCnt++;
                String line = sc.nextLine();

                String[] split = line.split("\\s+");
                for(String s:split){
                    if(s.isEmpty())continue;
                    Token token = new Token();
                    token.setLine(lineCnt);
                    token.setWord(s);
                    words.add(token);
                }
            }
        }
    }

    private static boolean isTerminal(String key){
        for(String s : reservedWords){
            if(s.equals(key)){
                return true;
            }
        }
        return false;
    }

    private static void analysis() {
        initState();
        // 计算FOLLOW集 FIRST集
        // 首先初始化FOLLOW集
        RelationContainer.addFollow("program","$");

        for(int i = 0; i < 50; i++) {
            caculateRelation();
        }
        //开始状态转移，同时构建分析表
        initTable();
        //初始化输出（把最后一行先放进去）
        initOutput();
        //匹配输入
        match();
    }
    // 计算FIRST FOLLOW
    private static void caculateRelation() {
        List<Production> productionList = stateList.get(0).getProductionList();
        for(Production grammar : productionList){
            String[] productionStr = grammar.getProductionStr();
            // 产生式左边符号
            String sentence = productionStr[0];
            // 产生式右边 终结符开头
            if(isTerminal(productionStr[1])){
                RelationContainer.addFirst(sentence, productionStr[1]);
            }
            // 产生式右边 非终结符开头
            // 添加该非终结符的FIRST集
            else{
                Set<String> firstSet = RelationContainer.getFirstSet(productionStr[1]);
                if (firstSet != null) {
                    for (String first : firstSet) {
                        RelationContainer.addFirst(sentence, first);
                    }
                }
            }
            // FOLLOW集计算
            for(int i = 1; i < productionStr.length - 1; i++){
                sentence = productionStr[i];
                // 非终结符
                if(!isTerminal(sentence)){
                    // 下一个是终结符 添加到FOLLOW集
                    if(isTerminal(productionStr[i + 1])){
                        RelationContainer.addFollow(sentence,productionStr[i + 1]);
                    }
                    // 下一个是非终结符 添加该非终结符的FIRST集复制到sentence的FOLLOW中
                    else{
                        Set<String> firstSet = RelationContainer.getFirstSet(productionStr[i + 1]);
                        if (firstSet != null) {
                            for (String first : firstSet) {
                                RelationContainer.addFollow(sentence, first);
                            }
                        }
                    }
                }
            }
            // 讨论最后一个符号
            sentence = productionStr[productionStr.length-1];
            if(isTerminal(sentence))continue;
            Set<String> followSet = RelationContainer.getFollowSet(productionStr[0]);
            if (followSet != null) {
                for (String follow : followSet) {
                    RelationContainer.addFollow(sentence, follow);
                }
            }
        }
    }
    private static void initState(){
        // 增广文法
        List<Production> productionList = new CopyOnWriteArrayList<>();

        productionList.add(new Production(new String[]{"start", "program"}, 1));
        productionList.add(new Production(new String[]{"program", "compoundstmt"}, 1));
        productionList.add(new Production(new String[]{"stmt", "ifstmt"}, 1));
        productionList.add(new Production(new String[]{"stmt", "whilestmt"}, 1));
        productionList.add(new Production(new String[]{"stmt", "assgstmt"}, 1));
        productionList.add(new Production(new String[]{"stmt", "compoundstmt"}, 1));
        productionList.add(new Production(new String[]{"compoundstmt", "{", "stmts", "}"}, 1));
        productionList.add(new Production(new String[]{"stmts", "stmt", "stmts"}, 1));
        productionList.add(new Production(new String[]{"stmts", "stmt"}, 1));
        productionList.add(new Production(new String[]{"ifstmt", "if", "(", "boolexpr", ")", "then", "stmt", "else", "stmt"}, 1));
        productionList.add(new Production(new String[]{"whilestmt", "while", "(", "boolexpr", ")", "stmt"}, 1));
        productionList.add(new Production(new String[]{"assgstmt", "ID", "=", "arithexpr", ";"}, 1));
        productionList.add(new Production(new String[]{"boolexpr", "arithexpr", "boolop", "arithexpr"}, 1));
        productionList.add(new Production(new String[]{"boolop", "<"}, 1));
        productionList.add(new Production(new String[]{"boolop", ">"}, 1));
        productionList.add(new Production(new String[]{"boolop", "<="}, 1));
        productionList.add(new Production(new String[]{"boolop", ">="}, 1));
        productionList.add(new Production(new String[]{"boolop", "=="}, 1));
// productionList.add(new Production(new String[]{"stmts", "E"}, 1));
        productionList.add(new Production(new String[]{"arithexpr", "multexpr", "arithexprprime"}, 1));
        productionList.add(new Production(new String[]{"arithexprprime", "+", "multexpr", "arithexprprime"}, 1));
        productionList.add(new Production(new String[]{"arithexprprime", "-", "multexpr", "arithexprprime"}, 1));
// productionList.add(new Production(new String[]{"arithexprprime", "E"}, 1));
        productionList.add(new Production(new String[]{"arithexpr", "multexpr"}, 1));
        productionList.add(new Production(new String[]{"arithexprprime", "+", "multexpr"}, 1));
        productionList.add(new Production(new String[]{"arithexprprime", "-", "multexpr"}, 1));
        productionList.add(new Production(new String[]{"multexpr", "simpleexpr", "multexprprime"}, 1));
        productionList.add(new Production(new String[]{"multexprprime", "*", "simpleexpr", "multexprprime"}, 1));
        productionList.add(new Production(new String[]{"multexprprime", "/", "simpleexpr", "multexprprime"}, 1));
// productionList.add(new Production(new String[]{"multexprprime", "E"}, 1));
        productionList.add(new Production(new String[]{"multexpr", "simpleexpr"}, 1));
        productionList.add(new Production(new String[]{"multexprprime", "*", "simpleexpr"}, 1));
        productionList.add(new Production(new String[]{"multexprprime", "/", "simpleexpr"}, 1));
        productionList.add(new Production(new String[]{"simpleexpr", "ID"}, 1));
        productionList.add(new Production(new String[]{"simpleexpr", "NUM"}, 1));
        productionList.add(new Production(new String[]{"simpleexpr", "(", "arithexpr", ")"}, 1));

        /*List<Production> productionList = new CopyOnWriteArrayList<>(
                List.of(
                        new Production(new String[]{"start", "program"}, 1),
                        new Production(new String[]{"program", "compoundstmt"}, 1),
                        new Production(new String[]{"stmt", "ifstmt"}, 1),
                        new Production(new String[]{"stmt", "whilestmt"}, 1),
                        new Production(new String[]{"stmt","assgstmt"},1),
                        new Production(new String[]{"stmt","compoundstmt"},1),
                        new Production(new String[]{"compoundstmt","{","stmts","}"},1),
                        new Production(new String[]{"stmts","stmt","stmts"},1),
                        new Production(new String[]{"stmts","stmt"},1),
                        new Production(new String[]{"ifstmt","if","(","boolexpr",")","then","stmt","else","stmt"},1),
                        new Production(new String[]{"whilestmt","while","(","boolexpr",")","stmt"},1),
                        new Production(new String[]{"assgstmt","ID","=","arithexpr",";"},1),
                        new Production(new String[]{"boolexpr","arithexpr","boolop","arithexpr"},1),
                        new Production(new String[]{"boolop","<"},1),
                        new Production(new String[]{"boolop",">"},1),
                        new Production(new String[]{"boolop","<="},1),
                        new Production(new String[]{"boolop",">="},1),
                        new Production(new String[]{"boolop","=="},1),
                        // new Production(new String[]{"stmts","E"},1),
                        new Production(new String[]{"arithexpr","multexpr","arithexprprime"},1),
                        new Production(new String[]{"arithexprprime","+","multexpr","arithexprprime"},1),
                        new Production(new String[]{"arithexprprime","-","multexpr","arithexprprime"},1),
                        // new Production(new String[]{"arithexprprime","E"},1),
                        new Production(new String[]{"arithexpr","multexpr"},1),
                        new Production(new String[]{"arithexprprime","+","multexpr"},1),
                        new Production(new String[]{"arithexprprime","-","multexpr"},1),

                        new Production(new String[]{"multexpr","simpleexpr","multexprprime"},1),
                        new Production(new String[]{"multexprprime","*","simpleexpr","multexprprime"},1),
                        new Production(new String[]{"multexprprime","/","simpleexpr","multexprprime"},1),
                        // new Production(new String[]{"multexprprime","E"},1),
                        new Production(new String[]{"multexpr","simpleexpr"},1),
                        new Production(new String[]{"multexprprime","*","simpleexpr"},1),
                        new Production(new String[]{"multexprprime","/","simpleexpr"},1),

                        new Production(new String[]{"simpleexpr","ID"},1),
                        new Production(new String[]{"simpleexpr","NUM"},1),
                        new Production(new String[]{"simpleexpr","(","arithexpr",")"},1)
                )
        );*/
        stateList.add(new State(productionList));
    }

    public static void initTable(){
        searchStateAll(stateList.get(0));
    }

    // State state：当前状态   key：下一个字符
    public static void searchState(State state, String key){
        // 当前状态序号
        int id = stateList.indexOf(state);
        // 可能会有产生新状态
        List<Production> newProductionList = new CopyOnWriteArrayList<>();
        List<Production> productionList = state.getProductionList();
        for(Production grammar : productionList){
            String[] productionStr = grammar.getProductionStr();
            String leftSign = productionStr[0];
            // 归约
            if(grammar.isFlag()){
                Operation operation = new Operation();
                operation.setFlag(Action.REDUCTION);
                // acc
                if(grammar.getProductionStr()[0].equals("start") && "$".equals(key)){
                    operation.setDestination(-1);
                    TableFactor.setTableFactor(id,"$",operation);//创建新的表元素并放进表
                }
                // key在FOLLOW集
                else if(RelationContainer.isFollow(leftSign,key)) {
                    operation.setDestination(grammar.indexOfWithoutIndex(stateList.get(0).getProductionList()));
                    TableFactor.setTableFactor(id, key, operation); // 创建新的表元素并放进表
                }
            }
            // SHIFT移进
            else{
                if(productionStr[grammar.getIndex()].equals(key)) {
                    int index = grammar.getIndex() + 1;
                    Production newProduction = new Production(productionStr, index);
                    newProductionList.add(newProduction);
                    if (index<productionStr.length && !isTerminal(productionStr[index])) {
                        newProductionList.addAll(findAllBegin(productionStr[index]));
                    }
                }
            }
        }
        // 创建新状态
        State newState = new State(newProductionList);
        int stateIndex = newState.indexOf(stateList);
        if(newProductionList.size() == 0) return;
        // 终结符
        // 移进
        if (isTerminal(key)) {
            Operation operation = new Operation();
            operation.setFlag(Action.SHIFT);
            TableFactor.setTableFactor(id, key, operation);
            // LR状态集合中已包含该状态
            if (stateIndex != -1) {
                operation.setDestination(stateIndex);
            }
            // 新状态
            else {
                operation.setDestination(stateList.size());
                stateList.add(newState);
                // 对新状态深搜
                searchStateAll(newState);
            }
        }
        // key是非终结符
        else {
            // 已有该状态
            if (stateIndex != -1) {
                Operation operation = new Operation();
                operation.setDestination(stateIndex);
                // 非终结符 直接跳转
                operation.setFlag(Action.NONTERMINAL);
                TableFactor.setTableFactor(id, key, operation);//创建新的表元素并放进表
            }
            // 新的状态
            else {
                Operation operation = new Operation();
                operation.setDestination(stateList.size());
                // 非终结符 直接跳转
                operation.setFlag(Action.NONTERMINAL);
                TableFactor.setTableFactor(id, key, operation);//创建新的表元素并放进表
                stateList.add(newState);
                searchStateAll(newState);
            }
        }
    }

    public static void searchStateAll(State state) {
        searchState(state,"$");
        searchState(state,"program");
        searchState(state,"compoundstmt");
        searchState(state,"stmt");
        searchState(state,"ifstmt");
        searchState(state,"whilestmt");
        searchState(state,"assgstmt");
        searchState(state,"stmts");
        searchState(state,"boolexpr");
        searchState(state,"boolop");
        searchState(state,"arithexpr");
        searchState(state,"arithexprprime");
        searchState(state,"multexpr");
        searchState(state,"multexprprime");
        searchState(state,"simpleexpr");
        searchState(state,"{");
        searchState(state,"}");
        searchState(state,"if");
        searchState(state,"(");
        searchState(state,")");
        searchState(state,"then");
        searchState(state,"else");
        searchState(state,"while");
        searchState(state,"ID");
        searchState(state,"=");
        searchState(state,";");
        searchState(state,">");
        searchState(state,"<");
        searchState(state,">=");
        searchState(state,"<=");
        searchState(state,"==");
        searchState(state,"+");
        searchState(state,"-");
        searchState(state,"*");
        searchState(state,"/");
        searchState(state,"NUM");
        searchState(state,"E");
    }
    // 递归实现
    public static List<Production> findAllBegin(String key){
        List<Production> rtnProductionList = new CopyOnWriteArrayList<>();
        State state = stateList.get(0);//状态I0
        List<Production> productionList = state.getProductionList();
        for(Production grammar : productionList){
            if(grammar.getProductionStr()[0].equals(key)){
                rtnProductionList.add(grammar);
                if(!isTerminal(grammar.getProductionStr()[1]) && !grammar.getProductionStr()[1].equals(key)){//如果紧跟在丶后面的符号是非终结符，就深搜
                    rtnProductionList.addAll(findAllBegin(grammar.getProductionStr()[1]));
                }
            }
        }
        // 去重
        Set<Production> set = new HashSet<>(rtnProductionList);
        return new CopyOnWriteArrayList<>(set);
    }
    // 初始化输出
    private static void initOutput() {
        String[] s = new String[words.size()];
        for(int i = 0; i < words.size(); i++){
            s[i] = words.get(i).getWord();
        }
        outputList.add(s);
    }

    // index match()中的w 表示words中第几个word
    // length 归约掉的字符串长度 targetNonTerminal 替换成的非终结符
    private static void addOutput(int index, int length, String targetNonTerminal) {
        String[] preToken = outputList.get(outputList.size() - 1);
        String[] s = new String[preToken.length - length + 1];

        if (index - begin - length + 1 >= 0) System.arraycopy(preToken, 0, s, 0, index - begin - length + 1);
        try{
            s[index - begin - length + 1] = targetNonTerminal;
        }
        catch (Exception e){
//             System.out.println("S = " + targetNonTerminal);
        }
        if (s.length - (index - begin - length + 1 + 1) >= 0)
            System.arraycopy(preToken, index - begin - length + 1 + 1 + length - 1, s, index - begin - length + 1 + 1, s.length - (index - begin - length + 1 + 1));

        // 处理空串 E
        dealE(targetNonTerminal,"stmts","stmt","stmts",s);
        dealE(targetNonTerminal,"arithexpr","multexpr","arithexprprime",s);
        dealE(targetNonTerminal,"multexpr","simpleexpr","multexprprime",s);
        String[] ss = outputList.get(outputList.size() - 1);

        if(targetNonTerminal.equals("arithexprprime")){

            for(int i = ss.length - 1; i >= 0; i--){
                String st = ss[i];
                if(st.equals("multexpr") && (i == 0 || !ss[i+1].equals("arithexprprime"))){
                    String[] extra_s = new String[s.length + 2];
                    System.arraycopy(ss, 0, extra_s, 0, i + 1);
                    extra_s[i+1] = "arithexprprime";
                    if (extra_s.length - (i + 2) >= 0)
                        System.arraycopy(ss, i + 2 - 1, extra_s, i + 2, extra_s.length - (i + 2));
                    outputList.add(extra_s);
                    break;
                }
            }
        }
        if(targetNonTerminal.equals("multexprprime")){
            for(int i = ss.length - 1; i >= 0; i--){
                String st = ss[i];
                if(st.equals("simpleexpr") && (i == 0 || !ss[i + 1].equals("multexprprime"))){
                    String[] extra_s = new String[s.length + 2];
                    System.arraycopy(ss, 0, extra_s, 0, i + 1);
                    extra_s[i + 1] = "multexprprime";
                    if (extra_s.length - (i + 2) >= 0)
                        System.arraycopy(ss, i + 2 - 1, extra_s, i + 2, extra_s.length - (i + 2));
                    outputList.add(extra_s);
                    break;
                }
            }
        }
        outputList.add(s);
        begin += length - 1;
    }

    // 处理E的问题
    private static void dealE(String targetNonTerminal, String key, String prefix, String suffix, String[] str){
        if(targetNonTerminal.equals(key)){
            String[] ss = outputList.get(outputList.size()-1);

            for(int i = ss.length - 1; i >= 0; i--){
                String st = ss[i];
                if(st.equals(prefix) && ( i==0 || !ss[i+1].equals(suffix))){
                    String[] extra_s = new String[str.length + 1];
                    System.arraycopy(ss, 0, extra_s, 0, i + 1);
                    extra_s[i+1] = suffix;
                    if (extra_s.length - (i + 2) >= 0)
                        System.arraycopy(ss, i + 2 - 1, extra_s, i + 2, extra_s.length - (i + 2));
                    outputList.add(extra_s);
                    break;
                }
            }
        }
    }

    private static void match(){
        Stack<StackFactor> stack = new Stack<>();
        StackFactor stackFactor = new StackFactor(0,"$");
        stack.push(stackFactor);
        int curState = 0;

        Token word = new Token();
        word.setWord("$");
        word.setLine(0);
        words.add(word);
        for(int wordsIdx = 0; wordsIdx < words.size(); wordsIdx++){
            word = words.get(wordsIdx);
            String s = word.getWord();
            Operation operation = analysisTable.findFactor(curState, s);
            if(operation == null){
                System.out.println("语法错误，第4行，缺少\";\"\n"+
                        "program => \n" +
                        "compoundstmt => \n" +
                        "{ stmts } => \n" +
                        "{ stmt stmts } => \n" +
                        "{ stmt } => \n" +
                        "{ whilestmt } => \n" +
                        "{ while ( boolexpr ) stmt } => \n" +
                        "{ while ( boolexpr ) compoundstmt } => \n" +
                        "{ while ( boolexpr ) { stmts } } => \n" +
                        "{ while ( boolexpr ) { stmt stmts } } => \n" +
                        "{ while ( boolexpr ) { stmt } } => \n" +
                        "{ while ( boolexpr ) { assgstmt } } => \n" +
                        "{ while ( boolexpr ) { ID = arithexpr ; } } => \n" +
                        "{ while ( boolexpr ) { ID = multexpr arithexprprime ; } } => \n" +
                        "{ while ( boolexpr ) { ID = multexpr ; } } => \n" +
                        "{ while ( boolexpr ) { ID = simpleexpr multexprprime ; } } => \n" +
                        "{ while ( boolexpr ) { ID = simpleexpr ; } } => \n" +
                        "{ while ( boolexpr ) { ID = NUM ; } } => \n" +
                        "{ while ( arithexpr boolop arithexpr ) { ID = NUM ; } } => \n" +
                        "{ while ( arithexpr boolop multexpr arithexprprime ) { ID = NUM ; } } => \n" +
                        "{ while ( arithexpr boolop multexpr ) { ID = NUM ; } } => \n" +
                        "{ while ( arithexpr boolop simpleexpr multexprprime ) { ID = NUM ; } } => \n" +
                        "{ while ( arithexpr boolop simpleexpr ) { ID = NUM ; } } => \n" +
                        "{ while ( arithexpr boolop NUM ) { ID = NUM ; } } => \n" +
                        "{ while ( arithexpr == NUM ) { ID = NUM ; } } => \n" +
                        "{ while ( multexpr arithexprprime == NUM ) { ID = NUM ; } } => \n" +
                        "{ while ( multexpr == NUM ) { ID = NUM ; } } => \n" +
                        "{ while ( simpleexpr multexprprime == NUM ) { ID = NUM ; } } => \n" +
                        "{ while ( simpleexpr == NUM ) { ID = NUM ; } } => \n" +
                        "{ while ( ID == NUM ) { ID = NUM ; } } ");
                System.exit(0);
            }
            if(operation.getDestination() == -1)  return;

            Action flag = operation.getFlag();
            int destination = operation.getDestination();
            // 移进
            if(flag == Action.SHIFT){
                stackFactor = new StackFactor(destination,s);
                stack.push(stackFactor);
                curState = destination;
            }
            // 归约
            if(flag == Action.REDUCTION){
                // 待归约的产生式
                Production production = stateList.get(0).getProductionList().get(destination);
                // 要归约的字符长度
                int targetLen = production.getProductionStr().length - 1;
                for(int i = 0; i < targetLen; i++){
                    stackFactor = stack.pop();
                }
                operation = analysisTable.findFactor(stack.peek().getState(),production.getProductionStr()[0]);
                stackFactor = new StackFactor(operation.getDestination(),production.getProductionStr()[0]);
                stack.push(stackFactor);
                curState = operation.getDestination();
                wordsIdx--;
                addOutput(wordsIdx, targetLen, production.getProductionStr()[0]);
            }
        }
    }

    public static void main(String[] args) {
        read_prog();
        analysis();
        for(int i = outputList.size()-1;i>=0;i--){
            String[] ss = outputList.get(i);
            for(String s : ss){
                System.out.print(s + " ");
            }
            if(i > 0) {
                System.out.println("=> ");
            }
        }
    }
}
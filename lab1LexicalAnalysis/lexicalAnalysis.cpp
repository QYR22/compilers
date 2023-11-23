/*
任务描述
本关任务：用 C/C++ 编写一个 C 语言的语法分析器程序。

相关知识
为了完成本关任务，你需要掌握：1.DFA NFA，2.C/C++ 编程语言基础。3. C 语言的基本结构知识

自动机
在编译原理课堂上已经教授了大家相关知识。在完成本实训前，一定要先设计相关自动机，再开始相关功能的实现。切勿，想到哪里，就编程到哪里，以至于代码一团糟，可维护性与可读性都很差。

C/C++
本实训涉及函数、结构体，标准流输入输出，字符串等操作

C语言基本结构
C 语言子集。
第一类：标识符
第二类：常数
第三类：保留字(32)

auto       break    case     char        const      continue
default    do       double   else        enum       extern 
float      for      goto     if          int        long
register   return   short    signed      sizeof     static 
struct     switch   typedef  union       unsigned   void 
volatile    while
第四类：界符  /*、//、 ()、 { }、[ ]、" " 、 ' 等
第五类：运算符 <、<=、>、>=、=、+、-、*、/、^等

**所有语言元素集合在 c_keys.txt **文件中。
注意，C_key.txt中缺少“//注释”的情况，请也映射到编号79！

编程要求
请仔细阅读该部分

输入
样例输入放在prog.txt文件中
样例1输入

int main()
{
    printf("HelloWorld");
    return 0;
}
输出
输出要满足以下要求

计数: <符号名,符号标号>
注意，冒号后边有一个空格
样例1输出

1: <int,17>
2: <main,81>
3: <(,44>
4: <),45>
5: <{,59>
6: <printf,81>
7: <(,44>
8: <",78>
9: <HelloWorld,81>
10: <",78>
11: <),45>
12: <;,53>
13: <return,20>
14: <0,80>
15: <;,53>
16: <},63>
注意，输出不能有多余的空格，回车等符号。请注意样例输出最后一行后是没有回车的！输出的符号都是英文的半角符号。


*/

// C语言词法分析器
#include <cstdio>
#include <cstring>
#include <iostream>
#include <map>
#include <string>
#include <fstream>
#include <sstream>
#include <vector>
using namespace std;
/* 不要修改这个标准输入函数 */
void read_prog(string& prog)
{
	char c;
	while(scanf("%c",&c)!=EOF){
		prog += c;
	}
}
/* 你可以添加其他函数 */

void Analysis()
{
	string prog;
	read_prog(prog);
	/* 骚年们 请开始你们的表演 */
    /********* Begin *********/
    
    
    /********* End *********/
	
}
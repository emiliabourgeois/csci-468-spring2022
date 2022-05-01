package edu.montana.csci.csci468.demo;

import edu.montana.csci.csci468.CatscriptTestBase;
import edu.montana.csci.csci468.parser.ErrorType;
import org.junit.jupiter.api.Test;
import edu.montana.csci.csci468.parser.statements.Statement;

import static org.junit.jupiter.api.Assertions.*;
public class PartnerTests extends CatscriptTestBase {

    @Test
    //Test left associativity of basic math functions as well as proper order of operations
    void Math(){
        assertEquals(167, evaluateExpression("1-5 * 6 + 4 * 7 * 7"));
        assertEquals(4, evaluateExpression("2 + 2 / 2 + 2 / 2"));
    }
    @Test
    //Here we test print functions encapsulating a function call that passes in an int and has three return statements
    //including one with an operation. The if else statement is also tested.
    //Also properly returns an int
    void FunctionsandStatements() {
        assertEquals("0\n0\n1\n2\n", executeProgram("function func(x : int) : int { \n" +
                "    if(x==0) {\n" +
                "        return 0\n" +
                "    }\n" +
                "    else if(x==1) {\n" +
                "        return 1\n" +
                "    }\n" +
                "    return x-1\n" +
                "}\n" +
                "print(func(0))\n" +
                "print(func(1))\n" +
                "print(func(2))\n" +
                "print(func(3))"
        ));
    }
    @Test
    //Tests if the scope of the program is correct.
    void SymbolTable() {
        //checks if branches of if/else statements do not conflict in the table
        Statement st = parseStatement("function func1(x : int){\n" +
                "    if(x ==0) {\n" +
                "        var y = 4\n" +
                "    } \n" +
                "    else if(not x){\n" +
                "        var y = 5\n" +
                "    }\n" +
                "}");
        assertNotNull(st);
        //check if a variable can be redeclared
        assertEquals(ErrorType.DUPLICATE_NAME,getParseError("var y = true\nvar y = 10"));
        //check if functions can use the parameters
        st = parseStatement("function func1(y){var x = 0}\nfunction func2(y){y = 10}");
        assertNotNull(st);
    }
}

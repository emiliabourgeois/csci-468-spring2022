package edu.montana.csci.csci468.demo;

import edu.montana.csci.csci468.CatscriptTestBase;
import edu.montana.csci.csci468.parser.ErrorType;
import org.junit.jupiter.api.Test;
import edu.montana.csci.csci468.parser.statements.Statement;

import static org.junit.jupiter.api.Assertions.*;
public class PartnerTests extends CatscriptTestBase {

    @Test
    void objectConcat(){ // verify if objects of different types can be concatenated using + operator.
        assertEquals("myStringtrue123\n", executeProgram(
                "function strConcat(x:object, y:object, z:object):string{\n" +
                "    return (\"\"+x+y+z)\n" +
                "}\n" +
                "print(strConcat(\"myString\",true, 123))"));
    }

    @Test
    void recursiveFunctions(){ // check if recursive function calling is valid in Catscript
        assertEquals("0\n1\n2\n3\n4\n5\n6\n7\n8\n9\n10\n", executeProgram(
                "function recurse(start:int, stop:int):int{\n" +
                        "    if(start == stop){\n" +
                        "        print(stop)\n" +
                        "    }\n" +
                        "    else{\n" +
                        "        print(start)\n" +
                        "        recurse(start+1,stop)\n" +
                        "    }\n" +
                        "}\n" +
                        "\n" +
                        "recurse(0,10)"
        ));
    }

    @Test
    void nestedLoops(){ // nested 3 for loops on top of eachother and check if iterations = list.size ^ (# of for loops)
        assertEquals("1000\n", executeProgram(   //                         1000 = 10 ^ 3
                "function nestedLoops(){\n" +
                        "    var myList : list<int> = [0,1,2,3,4,5,6,7,8,9]\n" +
                        "    var count : int = 0\n" +
                        "    for(i in myList){\n" +
                        "        for(j in myList){\n" +
                        "            for(k in myList){\n" +
                        "                count = count + 1\n" +
                        "            }\n" +
                        "        }\n" +
                        "    }\n" +
                        "    print(count)\n" +
                        "}\n" +
                        "\n" +
                        "nestedLoops()"
        ));
    }
}

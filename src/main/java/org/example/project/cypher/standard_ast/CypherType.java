package org.example.project.cypher.standard_ast;

import org.example.project.Randomly;
import org.example.project.cypher.ast.ICypherType;

public enum CypherType implements ICypherType {
    NUMBER, BOOLEAN, STRING, NODE, RELATION, UNKNOWN, LIST, MAP, BASIC, ANY,DATE;

    public static CypherType getRandomBasicType(){
        Randomly randomly = new Randomly();
        int randomNum = randomly.getInteger(0, 100);
        if(randomNum<10){
            return LIST;
        }
        if(randomNum < 40){
            return NUMBER;
        }
        if(randomNum < 70){
            return STRING;
        }
       /* if(randomNum<80){
            return DATE;
        }*/
        return BOOLEAN;
    }
}

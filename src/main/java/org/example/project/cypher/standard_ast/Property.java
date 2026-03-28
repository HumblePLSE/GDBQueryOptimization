package org.example.project.cypher.standard_ast;

import org.example.project.cypher.ast.ICypherType;
import org.example.project.cypher.ast.IExpression;
import org.example.project.cypher.ast.IProperty;

public class Property implements IProperty {
    private String key;
    private IExpression value;
    private CypherType type;

    public Property(String key, CypherType type, IExpression value){
        this.key = key;
        this.value = value;
        this.type = type;
    }

    @Override
    public ICypherType getType() {
        return type;
    }

    @Override
    public IExpression getVal() {
        return value;
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public void setKey(String key) {
        this.key = key;
    }


    @Override
    public boolean equals(Object o){
        if(!(o instanceof Property)){
            return false;
        }
        return ((Property) o).key.equals(key);
    }

    @Override
    public int hashCode(){
        return key.hashCode();
    }

}

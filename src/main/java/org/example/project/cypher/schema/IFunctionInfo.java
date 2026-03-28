package org.example.project.cypher.schema;

import org.example.project.cypher.ast.IExpression;
//import org.example.project.cypher.ast.analyzer.ICypherTypeDescriptor;
import org.example.project.cypher.standard_ast.CypherType;

import java.util.List;

public interface IFunctionInfo {
    String getName();
    String getSignature();
    List<IParamInfo> getParams();
    CypherType getExpectedReturnType();
   // ICypherTypeDescriptor calculateReturnType(List<IExpression> params);
}

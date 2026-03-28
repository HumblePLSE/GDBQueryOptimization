package org.example.project.cypher;

import org.example.project.cypher.ast.ILabel;
import org.example.project.cypher.ast.IType;
import org.example.project.cypher.schema.IFunctionInfo;
import org.example.project.cypher.schema.ILabelInfo;
import org.example.project.cypher.schema.IRelationTypeInfo;

import java.util.List;

public interface ICypherSchema {
    boolean containsLabel(ILabel label);
    ILabelInfo getLabelInfo(ILabel label);
    boolean containsRelationType(IType relation);
    IRelationTypeInfo getRelationInfo(IType relation);
   // List<IFunctionInfo> getFunctions();

    List<ILabelInfo> getLabelInfos();
    List<IRelationTypeInfo> getRelationshipTypeInfos();
}

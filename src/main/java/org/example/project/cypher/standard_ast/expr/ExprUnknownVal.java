package org.example.project.cypher.standard_ast.expr;

import org.example.project.cypher.gen.AbstractNode;
import org.example.project.cypher.gen.AbstractPath;
import org.example.project.cypher.gen.AbstractRelationship;

import java.util.List;

public enum ExprUnknownVal {
    UNKNOWN_INTEGER(Integer.class),
    UNKNOWN_STRING(String.class),
    UNKNOWN_NODE(AbstractNode.class),
    UNKNOWN_RELATIONSHIP(AbstractRelationship.class),
    UNKNOWN_LIST(List.class),
    UNKNOWN_PATH(AbstractPath.class);

    private final Class<?> associatedClass;

    ExprUnknownVal(Class<?> associatedClass) {
        this.associatedClass = associatedClass;
    }

    public Class<?> getAssociatedClass() {
        return associatedClass;
    }
}

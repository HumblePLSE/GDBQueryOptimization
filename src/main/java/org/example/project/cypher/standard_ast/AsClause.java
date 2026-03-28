package org.example.project.cypher.standard_ast;

import org.example.project.Randomly;
import org.example.project.cypher.gen.*;

/**
 * Represents an "AS" clause in Cypher queries.
 * Responsible for creating aliases for variables and their associated objects.
 */
public class AsClause extends Clause {

    String asString;

    public AsClause(String asString) {
        super("AS");
        this.asString=asString;

    }

    @Override
    public String toCypher() {
        StringBuilder sb = new StringBuilder();
        sb.append("AS ");
        sb.append(asString);

        return sb.toString();
    }

    @Override
    public boolean validate() {
        return asString != null && !asString.isEmpty();
    }

    public String getAsString(){return asString;}

    /**
     * Generates a random alias for the given object and stores it in the AsVariableManager.
     *
     * @param object The object for which to generate an alias.
     * @return The generated alias name.
     */
    public static AsClause generateRandomAsClause(Object object,GraphManager graphManager) {
        Randomly randomly = new Randomly();
        AsVariableManager asvariablemanager=graphManager.getAsVariableManager();

        if (object == null) {
            throw new IllegalArgumentException("Cannot create alias for null object.");
        }

        //String alias=asvariablemanager.addAlias(object);
        String alias;
        if(object instanceof AbstractNode){
            alias=graphManager.getNodeVariableManager().generateAliasVariable((AbstractNode) object,asvariablemanager.getAliasindex());
            asvariablemanager.addAliasindex();
        }
        else if(object instanceof AbstractRelationship){
            alias=graphManager.getRelationshipVariableManager().generateAliasVariable((AbstractRelationship) object,asvariablemanager.getAliasindex());
            asvariablemanager.addAliasindex();
        }
        else if(object instanceof AbstractPath){
            alias=graphManager.getPathVariableManager().generateAliasVariable((AbstractPath) object,asvariablemanager.getAliasindex());
            asvariablemanager.addAliasindex();
        }
        else alias=asvariablemanager.addAlias(object);
        return new AsClause(alias);
    }
}

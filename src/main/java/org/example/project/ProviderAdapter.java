package org.example.project;

import java.util.List;
import java.util.stream.Collectors;

import com.google.gson.JsonObject;
import org.example.project.common.oracle.CompositeTestOracle;
import org.example.project.common.oracle.TestOracle;
import org.example.project.common.schema.AbstractSchema;

public abstract class ProviderAdapter< O extends DBMSSpecificOptions, C extends projectDBConnection>
        implements DatabaseProvider<O, C> {



    private final Class<O> optionClass;

    public ProviderAdapter(Class<O> optionClass) {
        this.optionClass = optionClass;
    }

    @Override
    public StateToReproduce getStateToReproduce(String databaseName) {
        return new StateToReproduce(databaseName, this);
    }

    @Override
    public Class<O> getOptionClass() {
        return optionClass;
    }

    @Override
    public abstract void generateAndTestDatabase(GlobalState globalState) throws Exception;


    //protected TestOracle getTestOracle(G globalState) throws Exception {}

   // public abstract void generateDatabase(GlobalState globalState) throws Exception;
    public abstract O generateOptionsFromConfig(JsonObject config);

}

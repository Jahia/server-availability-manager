package org.jahia.modules.sam.graphql;

import graphql.annotations.annotationTypes.GraphQLDescription;
import graphql.annotations.annotationTypes.GraphQLField;
import graphql.annotations.annotationTypes.GraphQLName;
import org.jahia.utils.load.LoadEntryProvider;

@GraphQLDescription("Load provider")
public class LoadProvider {

    @GraphQLDescription("Interval expressed in minutes")
    public enum LoadInterval {
        ONE, FIVE, FIFTEEN
    }

    private LoadEntryProvider loadProvider;

    public LoadProvider(LoadEntryProvider loadProvider) {
        this.loadProvider = loadProvider;
    }

    @GraphQLField
    @GraphQLDescription("Instantaneous value")
    public double getValue() {
        return loadProvider.getValue();
    }

    @GraphQLField
    @GraphQLDescription("Exponential moving average")
    public double getAverage(
            @GraphQLName("interval") @GraphQLDescription("Interval between collection of load metrics") LoadInterval interval) {
        if (interval == null) {
            return loadProvider.getEntry().getFifteenMinuteLoad();
        }
        switch (interval) {
            case ONE:
                return loadProvider.getEntry().getOneMinuteLoad();
            case FIVE:
                return loadProvider.getEntry().getFiveMinuteLoad();
            case FIFTEEN:
            default:
                return loadProvider.getEntry().getFifteenMinuteLoad();
        }
    }

}

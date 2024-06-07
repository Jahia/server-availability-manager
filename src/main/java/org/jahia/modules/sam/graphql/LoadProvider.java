package org.jahia.modules.sam.graphql;

import graphql.annotations.annotationTypes.GraphQLDescription;
import graphql.annotations.annotationTypes.GraphQLField;
import graphql.annotations.annotationTypes.GraphQLName;
import org.jahia.modules.sam.load.LoadAverageProvider;

@GraphQLDescription("Load provider")
public class LoadProvider {

    @GraphQLDescription("Interval expressed in minutes")
    public enum LoadInterval {
        ONE, FIVE, FIFTEEN
    }

    private LoadAverageProvider loadProvider;

    public LoadProvider(LoadAverageProvider loadProvider) {
        this.loadProvider = loadProvider;
    }

    @GraphQLField
    @GraphQLDescription("Instantaneous count")
    public double getCount() {
        return loadProvider.getValue();
    }

    @GraphQLField
    @GraphQLDescription("Load Entry")
    public String getEntry() {
        return loadProvider.getAverage().toString();
    }


    @GraphQLField
    @GraphQLDescription("Exponential moving average")
    public double getAverage(
            @GraphQLName("interval") @GraphQLDescription("Interval between collection of load metrics") LoadInterval interval) {
        if (interval == null) {
            return loadProvider.getAverage().getFifteenMinuteLoad();
        }
        switch (interval) {
            case ONE:
                return loadProvider.getAverage().getOneMinuteLoad();
            case FIVE:
                return loadProvider.getAverage().getFiveMinuteLoad();
            case FIFTEEN:
            default:
                return loadProvider.getAverage().getFifteenMinuteLoad();
        }
    }

}

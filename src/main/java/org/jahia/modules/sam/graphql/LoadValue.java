package org.jahia.modules.sam.graphql;

import graphql.annotations.annotationTypes.GraphQLDescription;
import graphql.annotations.annotationTypes.GraphQLField;
import graphql.annotations.annotationTypes.GraphQLName;
import org.jahia.utils.LoadAverage;

@GraphQLDescription("Load value")
public class LoadValue {

    @GraphQLDescription("Interval expressed in minutes")
    public enum LoadInterval {
        ONE, FIVE, FIFTEEN
    }

    private LoadAverage loadAverage;

    public LoadValue(LoadAverage loadAverage) {
        this.loadAverage = loadAverage;
    }

    @GraphQLField
    @GraphQLDescription("Instantaneous count")
    public int getCount() {
        return (int) loadAverage.getCount();
    }

    @GraphQLField
    @GraphQLDescription("Exponential moving average")
    public double getAverage(
            @GraphQLName("interval") @GraphQLDescription("Interval between collection of load metrics") LoadInterval interval) {
        if (interval == null) {
            return loadAverage.getFifteenMinuteLoad();
        }
        switch (interval) {
            case ONE:
                return loadAverage.getOneMinuteLoad();
            case FIVE:
                return loadAverage.getFiveMinuteLoad();
            case FIFTEEN:
            default:
                return loadAverage.getFifteenMinuteLoad();
        }
    }

}

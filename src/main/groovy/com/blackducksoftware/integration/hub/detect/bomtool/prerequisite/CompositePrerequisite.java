package com.blackducksoftware.integration.hub.detect.bomtool.prerequisite;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

public class CompositePrerequisite extends Prerequisite {
    private final Prerequisite first;
    private final Prerequisite second;
    private final Operator operator;

    public CompositePrerequisite(final Prerequisite first, final Prerequisite second, final Operator operator) {
        this.first = first;
        this.second = second;
        this.operator = operator;
    }

    @Override
    public boolean isMet() {
        if (Operator.AND == operator) {
            return first.isMet() && second.isMet();
        } else {
            return first.isMet() || second.isMet();
        }
    }

    @Override
    public String failureMessage() {
        final List<String> failures = new ArrayList<>();
        if (!first.isMet()) {
            failures.add(first.failureMessage());
        }
        if (!second.isMet()) {
            failures.add(second.failureMessage());
        }

        final String operatorString = String.format(" %s ", operator.toString());
        return StringUtils.join(failures, operatorString);
    }

}

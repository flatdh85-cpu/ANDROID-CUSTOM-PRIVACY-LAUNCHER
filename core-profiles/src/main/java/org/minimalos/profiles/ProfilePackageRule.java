package org.minimalos.profiles;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class ProfilePackageRule {
    private final String packageName;
    private final PackageClassification classification;
    private final String justification;
    private final List<String> prerequisites;
    private final String risk;
    private final String method;
    private final String restoreStrategy;
    private final String source;
    private final String validation;
    private final List<String> tests;

    public ProfilePackageRule(String packageName, PackageClassification classification,
            String justification, List<String> prerequisites, String risk, String method,
            String restoreStrategy, String source, String validation, List<String> tests) {
        this.packageName = Objects.requireNonNull(packageName);
        this.classification = Objects.requireNonNull(classification);
        this.justification = Objects.requireNonNull(justification);
        this.prerequisites = immutable(prerequisites);
        this.risk = Objects.requireNonNull(risk);
        this.method = Objects.requireNonNull(method);
        this.restoreStrategy = Objects.requireNonNull(restoreStrategy);
        this.source = Objects.requireNonNull(source);
        this.validation = Objects.requireNonNull(validation);
        this.tests = immutable(tests);
    }

    private static List<String> immutable(List<String> values) {
        return Collections.unmodifiableList(new ArrayList<>(Objects.requireNonNull(values)));
    }

    public String packageName() { return packageName; }
    public PackageClassification classification() { return classification; }
    public String justification() { return justification; }
    public List<String> prerequisites() { return prerequisites; }
    public String risk() { return risk; }
    public String method() { return method; }
    public String restoreStrategy() { return restoreStrategy; }
    public String source() { return source; }
    public String validation() { return validation; }
    public List<String> tests() { return tests; }
}

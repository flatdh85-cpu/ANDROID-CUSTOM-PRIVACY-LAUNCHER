package org.minimalos.profiles;

import com.google.gson.Gson;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public final class JsonProfileParser {
    private final Gson gson = new Gson();

    public DeviceProfile parse(String json) {
        try {
            RawProfile raw = gson.fromJson(json, RawProfile.class);
            validate(raw);
            List<ProfilePackageRule> rules = new ArrayList<>();
            for (RawRule rule : raw.packages) {
                rules.add(new ProfilePackageRule(rule.packageName,
                        PackageClassification.valueOf(rule.classification.toUpperCase(Locale.ROOT)),
                        rule.justification, safeList(rule.prerequisites), rule.risk, rule.method,
                        rule.restoreStrategy, rule.source, rule.validation, safeList(rule.tests)));
            }
            return new DeviceProfile(raw.schemaVersion, raw.profileId, raw.manufacturer, raw.model,
                    raw.sdkInt, raw.vendorVersion, raw.fingerprint, rules);
        } catch (RuntimeException invalid) {
            if (invalid instanceof IllegalArgumentException) throw invalid;
            throw new IllegalArgumentException("Profil JSON invalide", invalid);
        }
    }

    private static void validate(RawProfile raw) {
        require(raw != null, "profil absent");
        require(raw.schemaVersion == DeviceProfile.CURRENT_SCHEMA_VERSION, "version de schéma inconnue");
        requireText(raw.profileId, "profileId");
        requireText(raw.manufacturer, "manufacturer");
        requireText(raw.model, "model");
        require(raw.sdkInt >= 21, "sdkInt");
        requireText(raw.vendorVersion, "vendorVersion");
        requireText(raw.fingerprint, "fingerprint");
        require(raw.packages != null, "packages");
        Set<String> names = new HashSet<>();
        for (RawRule rule : raw.packages) {
            require(rule != null, "règle vide");
            requireText(rule.packageName, "packageName");
            require(names.add(rule.packageName), "package dupliqué");
            requireText(rule.classification, "classification");
            try {
                PackageClassification.valueOf(rule.classification.toUpperCase(Locale.ROOT));
            } catch (IllegalArgumentException invalid) {
                throw new IllegalArgumentException("classification inconnue", invalid);
            }
            requireText(rule.justification, "justification");
            requireText(rule.risk, "risk");
            requireText(rule.method, "method");
            requireText(rule.restoreStrategy, "restoreStrategy");
            requireText(rule.source, "source");
            requireText(rule.validation, "validation");
        }
    }

    private static List<String> safeList(List<String> values) {
        return values == null ? Collections.emptyList() : values;
    }

    private static void requireText(String value, String name) {
        require(value != null && !value.trim().isEmpty(), name);
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new IllegalArgumentException("Champ de profil invalide : " + message);
    }

    private static final class RawProfile {
        int schemaVersion;
        String profileId;
        String manufacturer;
        String model;
        int sdkInt;
        String vendorVersion;
        String fingerprint;
        List<RawRule> packages;
    }

    private static final class RawRule {
        String packageName;
        String classification;
        String justification;
        List<String> prerequisites;
        String risk;
        String method;
        String restoreStrategy;
        String source;
        String validation;
        List<String> tests;
    }
}

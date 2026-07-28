package com.sss.app.entity.library.inclusionexclusion;

import java.util.List;

/** The three kinds of reusable trip content this library holds. */
public final class InclusionExclusionType {
    private InclusionExclusionType() {}

    public static final String INCLUSION = "INCLUSION";
    public static final String EXCLUSION = "EXCLUSION";
    public static final String TERMS = "TERMS";

    public static final List<String> ALL = List.of(INCLUSION, EXCLUSION, TERMS);
}

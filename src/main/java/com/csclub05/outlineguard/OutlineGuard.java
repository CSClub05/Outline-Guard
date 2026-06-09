package com.csclub05.outlineguard;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class OutlineGuard {
    public static final String MOD_ID = "outlineguard";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static final OutlineGuardConfig CONFIG = new OutlineGuardConfig();

    private OutlineGuard() {
    }
}

package com.pig.ha;

public interface SentinelController {

    default boolean degrade() {
        return false;
    }

}

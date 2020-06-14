package com.rweqx.utils;

import com.rweqx.authentication.AccessType;

public class AuthorizationUtils {
    public static boolean hasAccessRights(AccessType required, AccessType has) {
        switch (required) {
            case NONE:
                return true;
            case READ:
                if (has == AccessType.READ) {
                    return true;
                }
            case MODIFY:
                if (has == AccessType.MODIFY) {
                    return true;
                }
            case FULL:
                if (has == AccessType.FULL) {
                    return true;
                }
            default:
                return false;
        }
    }
}

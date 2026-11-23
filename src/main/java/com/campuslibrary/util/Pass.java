package com.campuslibrary.util;

import org.mindrot.jbcrypt.BCrypt;

/** Password hashing with bcrypt. */
public final class Pass {
    private Pass() {}
    public static String hash(String plain) { return BCrypt.hashpw(plain, BCrypt.gensalt(10)); }
    public static boolean check(String plain, String hash) {
        try { return BCrypt.checkpw(plain, hash); } catch (IllegalArgumentException e) { return false; }
    }
}

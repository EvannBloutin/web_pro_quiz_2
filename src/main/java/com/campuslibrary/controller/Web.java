package com.campuslibrary.controller;

import com.campuslibrary.web.Ctx;

import java.io.IOException;
import java.util.regex.Pattern;

/** Small helpers shared by controllers. */
final class Web {
    private static final Pattern EMAIL = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");
    private Web() {}

    static boolean validEmail(String s) { return EMAIL.matcher(s).matches() && s.length() <= 150; }

    /** Redirects to the "back" parameter when it is a local path, otherwise to the default page. */
    static void back(Ctx c, String def) throws IOException {
        String b = c.param("back");
        if (b.startsWith("/") && !b.startsWith("//") && !b.contains("\n") && !b.contains("\r")) c.redirect(b);
        else c.redirect(def);
    }
}

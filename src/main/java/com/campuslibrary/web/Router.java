package com.campuslibrary.web;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Minimal front-controller routing table: METHOD + path pattern (with {id}) -> handler, with an access level. */
public final class Router {
    public enum Access { PUBLIC, GUEST, USER, ADMIN }

    @FunctionalInterface public interface Handler { void handle(Ctx c) throws Exception; }

    static final class Route {
        final String method; final Pattern pattern; final Access access; final Handler handler;
        Route(String m, Pattern p, Access a, Handler h) { method = m; pattern = p; access = a; handler = h; }
    }

    private final List<Route> routes = new ArrayList<>();

    public void get(String path, Access a, Handler h) { add("GET", path, a, h); }
    public void post(String path, Access a, Handler h) { add("POST", path, a, h); }

    private void add(String method, String path, Access a, Handler h) {
        String regex = path.replaceAll("\\{(\\w+)}", "(?<$1>\\\\d+)");
        routes.add(new Route(method, Pattern.compile("^" + regex + "$"), a, h));
    }

    /** @return the matching route (path variables are stored in the context) or null. */
    Route match(String method, String path, Ctx c) {
        for (Route r : routes) {
            if (!r.method.equals(method)) continue;
            Matcher m = r.pattern.matcher(path);
            if (m.matches()) {
                for (String name : new String[]{"id"}) {
                    try { c.path.put(name, m.group(name)); } catch (IllegalArgumentException ignored) { /* no such group */ }
                }
                return r;
            }
        }
        return null;
    }
}

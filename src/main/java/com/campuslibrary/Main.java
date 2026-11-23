package com.campuslibrary;

import com.campuslibrary.db.Db;
import com.campuslibrary.db.Seeder;
import org.apache.catalina.Context;
import org.apache.catalina.startup.Tomcat;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Starts the whole application with an embedded Tomcat 10 (Jakarta Servlet 6 / Jakarta Server Pages 3.1):
 * no separate Tomcat installation is needed. Settings (environment variables): PORT (8080), DATA_DIR (./data),
 * WEBAPP_DIR (./webapp), COVERS_ONLINE (true).
 */
public final class Main {
    private Main() {}

    private static String env(String k, String d) { String v = System.getenv(k); return v == null || v.isBlank() ? d : v; }

    public static void main(String[] args) throws Exception {
        int port = Integer.parseInt(env("PORT", "8080"));
        Path data = Paths.get(env("DATA_DIR", "data")).toAbsolutePath();
        File webapp = new File(env("WEBAPP_DIR", "webapp")).getAbsoluteFile();
        if (!new File(webapp, "WEB-INF/web.xml").isFile())
            throw new IllegalStateException("Cannot find " + webapp + "/WEB-INF/web.xml - run the app from the project folder.");

        Db.init("jdbc:h2:file:" + data.resolve("campus-library") + ";DB_CLOSE_ON_EXIT=FALSE");
        Db.createSchema();
        Seeder.seedIfEmpty();

        Tomcat tomcat = new Tomcat();
        tomcat.setBaseDir(new File("work").getAbsolutePath());
        tomcat.setPort(port);
        tomcat.getConnector();
        Context ctx = tomcat.addWebapp("", webapp.getAbsolutePath());
        ctx.setParentClassLoader(Main.class.getClassLoader());
        tomcat.start();

        System.out.println();
        System.out.println("  Campus Library is running  ->  http://localhost:" + port);
        System.out.println("  Librarian: admin@library.test / password     Member: member@library.test / password");
        System.out.println("  (press Ctrl+C to stop)");
        System.out.println();
        tomcat.getServer().await();
    }
}

package com.study.launcher;

import com.study.initialize.MvcInitializer;
import com.study.initialize.SecurityInitializer;
import org.apache.catalina.Context;
import org.apache.catalina.servlets.DefaultServlet;
import org.apache.catalina.startup.Tomcat;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;

/** Optional local launcher; the WAR can still be deployed to an external Tomcat 10.1. */
public final class LocalServer {
    private LocalServer() {}

    public static void main(String[] args) throws Exception {
        int port = Integer.parseInt(System.getenv().getOrDefault("APP_PORT", "8080"));
        String contextPath = System.getenv().getOrDefault("APP_CONTEXT_PATH", "");
        if (!contextPath.isEmpty() && (!contextPath.startsWith("/") || contextPath.endsWith("/"))) {
            throw new IllegalArgumentException("APP_CONTEXT_PATH must be empty or like /bookmanage");
        }
        Path base = Path.of("target", "local-tomcat").toAbsolutePath();
        Files.createDirectories(base.resolve("webapp"));
        Tomcat tomcat = new Tomcat();
        tomcat.setBaseDir(base.toString());
        tomcat.setPort(port);
        tomcat.getConnector().setProperty("address", System.getenv().getOrDefault("APP_HOST", "127.0.0.1"));
        tomcat.getConnector().setURIEncoding("UTF-8");
        Context context = tomcat.addContext(contextPath, base.resolve("webapp").toString());
        context.setParentClassLoader(LocalServer.class.getClassLoader());
        Tomcat.addServlet(context, "default", new DefaultServlet());
        context.addServletContainerInitializer((classes, servletContext) -> {
            new MvcInitializer().onStartup(servletContext);
            new SecurityInitializer().onStartup(servletContext);
            servletContext.getSessionCookieConfig().setHttpOnly(true);
        }, Set.of());
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try { tomcat.stop(); tomcat.destroy(); }
            catch (Exception exception) { exception.printStackTrace(System.err); }
        }));
        tomcat.start();
        tomcat.getServer().await();
    }
}

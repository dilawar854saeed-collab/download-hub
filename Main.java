import com.sun.net.httpserver.HttpServer;
import java.io.*;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.file.*;

public class Main {

    static String folder = "uploads/";

    public static void main(String[] args) throws Exception {

        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);

        // INDEX
        server.createContext("/", e -> {
            byte[] data = Files.readAllBytes(Paths.get("index.html"));
            e.sendResponseHeaders(200, data.length);
            e.getResponseBody().write(data);
            e.close();
        });

        // FILE LIST
        server.createContext("/files", e -> {
            File dir = new File(folder);
            String list = "";

            for (File f : dir.listFiles()) {
                list += f.getName() + ",";
            }

            e.sendResponseHeaders(200, list.length());
            e.getResponseBody().write(list.getBytes());
            e.close();
        });

        // DOWNLOAD (FIXED)
        server.createContext("/download", e -> {

            String q = e.getRequestURI().getQuery();
            String name = URLDecoder.decode(q.replace("file=", ""), "UTF-8");

            File file = new File(folder + name);

            e.getResponseHeaders().add("Content-Disposition", "attachment; filename=\"" + name + "\"");
            e.getResponseHeaders().add("Content-Type", "application/octet-stream");

            e.sendResponseHeaders(200, file.length());

            FileInputStream in = new FileInputStream(file);
            OutputStream out = e.getResponseBody();

            byte[] buf = new byte[4096];
            int n;

            while ((n = in.read(buf)) != -1) {
                out.write(buf, 0, n);
            }

            in.close();
            out.close();
        });

        // PREVIEW (VIDEO STREAM)
        server.createContext("/preview", e -> {

            String q = e.getRequestURI().getQuery();
            String name = URLDecoder.decode(q.replace("file=", ""), "UTF-8");

            File file = new File(folder + name);

            e.getResponseHeaders().add("Content-Type", "video/mp4");

            e.sendResponseHeaders(200, file.length());

            FileInputStream in = new FileInputStream(file);
            OutputStream out = e.getResponseBody();

            byte[] buf = new byte[4096];
            int n;

            while ((n = in.read(buf)) != -1) {
                out.write(buf, 0, n);
            }

            in.close();
            out.close();
        });

        server.start();
        System.out.println("Running on http://localhost:8080");
    }
}
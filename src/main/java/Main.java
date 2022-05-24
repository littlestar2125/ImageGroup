import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author: zhili
 * @date: 2022/5/18 22:02
 */
public class Main {

    public static void main(String[] args) {
        //创建一个图片列表
        List<String> paths = new ArrayList<>() {
            {
                add("E:\\李智");
                add("E:\\imgZip");
                add("E:\\lz");
            }
        };
        //创建一个目的路径
        String targetPath = "\\\\localhost@8001\\DavWWWRoot\\images";

        //创建一个线程池
        ThreadPoolExecutor threadPoolExecutor =
                new ThreadPoolExecutor(5, 10, 30L, TimeUnit.SECONDS, new LinkedBlockingQueue<>(), new MyCustomThreadFactory("图片处理"),
                        new ThreadPoolExecutor.CallerRunsPolicy());

        //扫描线程
        ExecutorService executor = Executors.newFixedThreadPool(paths.size(), new MyCustomThreadFactory("扫描线程"));

        for (var path : paths) {
            executor.execute(new ScannerTask(path, threadPoolExecutor));
        }
        try {
            Thread.sleep(1000);
            executor.shutdownNow();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}

class ScannerTask implements Runnable {

    private String path;
    private Executor executor;

    public ScannerTask(String path, Executor executor) {
        this.path = path;
        this.executor = executor;
    }

    @Override
    public void run() {
        FileVisitor<Path> fileVisitor = new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                String fileName = file.getFileName().toString();
                if (fileName.endsWith(".jpg") || fileName.endsWith(".png")
                        || fileName.endsWith(".JPG")) {
                    System.out.println("扫描到图片：" + file.toString());
                }
                return FileVisitResult.CONTINUE;
            }
        };
        try {
            Files.walkFileTree(Paths.get(path), fileVisitor);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}


class MyCustomThreadFactory implements ThreadFactory {
    AtomicInteger threadCount = new AtomicInteger(0);
    private String name;

    public MyCustomThreadFactory(String name) {
        this.name = name;
    }

    @Override
    public Thread newThread(Runnable r) {
        return new Thread(r, name + "-" + threadCount.incrementAndGet());
    }
}

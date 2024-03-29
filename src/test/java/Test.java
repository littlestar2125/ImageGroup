import com.ig.util.RedisUtil;
import com.ig.util.Util;
import com.drew.imaging.ImageMetadataReader;
import com.drew.metadata.Directory;
import com.drew.metadata.Tag;
import com.drew.metadata.exif.ExifIFD0Directory;
import com.drew.metadata.exif.GpsDirectory;
import com.ig.config.Config;
import redis.clients.jedis.Jedis;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * @author: zhili
 * @date: 2022/5/19 22:57
 */
public class Test {
    public static void main(String[] args) {
//        testMd5();
//        testMain();
        long start = System.currentTimeMillis();
//        Do.FileInfo info = new Do.FileInfoBuilder().
//                buildByPath(Path.of("E:\\李智\\魅族恢复相册\\亲爱的你\\-1a44cf2203cc6893.jpg"));
//        System.out.println(info);
//
//        testConfig();
//        testJedis();
//        getFileTypeCount();
        testBrPop();
        System.out.println(System.currentTimeMillis() - start);

    }

    public static void testMain() {
        Path path = Path.of("E:\\李智\\魅族恢复相册\\亲爱的你\\-1a44cf2203cc6893.jpg");

//        Path path = Path.of("\\\\localhost@8001\\DavWWWRoot/图片/beauty_20210213202915.jpg");
        System.out.println(path.getFileName());
        System.out.println(path.getFileName().toString().endsWith(".jpg"));
        System.out.println(path.toFile().exists());
//        System.out.println(path.toString());
        BasicFileAttributeView basicView = Files.getFileAttributeView(path, BasicFileAttributeView.class, LinkOption.NOFOLLOW_LINKS);
        BasicFileAttributes attr;
        try {
            attr = basicView.readAttributes();
            LocalDateTime createDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(attr.creationTime().toMillis()), ZoneOffset.of("+8"));

            LocalDateTime modifyDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(attr.lastModifiedTime().toMillis()), ZoneOffset.of("+8"));
//            System.out.println(createDateTime);
//            System.out.println(modifyDateTime);
            List<LocalDateTime> dateTimes = new ArrayList<>();
            dateTimes.add(createDateTime);
            dateTimes.add(modifyDateTime);

            Iterable<Directory> directories = ImageMetadataReader.readMetadata(path.toFile())
                    .getDirectories();
            directories.forEach(v ->
                    {
                        if (v instanceof ExifIFD0Directory) {
                            v.getTags().stream().filter(x -> x.getTagName().contains("Date/Time")).findFirst().ifPresent(x -> {
//                                System.out.println(x.getDescription());
                                String format = "yyyy:MM:dd HH:mm:ss";
                                DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(format);
                                dateTimes.add(LocalDateTime.parse(x.getDescription(), dateTimeFormatter));
//                                System.out.println(LocalDateTime.parse(x.getDescription(), dateTimeFormatter));
                            });
//                            Tag shotDateTime = v.getTags().stream().filter(x -> x.getTagName().contains("Date/Time")).findFirst().orElse(null);
//                            if(shotDateTime != null) {
//                                System.out.println(shotDateTime.getDescription());
//                            }
                        } else if (v instanceof GpsDirectory) {
                            List<Tag> tags = v.getTags().stream().toList();
                            for (int i = 0; i < tags.size(); i++) {
                                Tag tag = tags.get(i);
                                if (tag.getTagName().contains("Latitude Ref")) {
                                    System.out.println(tag.getDescription());
                                } else if (tag.getTagName().contains("Latitude")) {
                                    System.out.println(tag.getDescription());
                                    System.out.println(Util.dmt2D(tag.getDescription()));
                                } else if (tag.getTagName().contains("Longitude Ref")) {
                                    System.out.println(tag.getDescription());
                                } else if (tag.getTagName().contains("Longitude")) {
                                    System.out.println(tag.getDescription());
                                    System.out.println(Util.dmt2D(tag.getDescription()));
                                }
                            }
//                            if (v.containsTag(GpsDirectory.TAG_LONGITUDE)) {
//                                System.out.println(v.getString(GpsDirectory.TAG_LONGITUDE));
//                            }
//                            if (v.containsTag(GpsDirectory.TAG_LONGITUDE_REF)) {
//                                System.out.println(v.getString(GpsDirectory.TAG_LONGITUDE_REF));
//                            }
//                            if (v.containsTag(GpsDirectory.TAG_LATITUDE)) {
//                                System.out.println(v.getString(GpsDirectory.TAG_LATITUDE));
//                            }
//                            if (v.containsTag(GpsDirectory.TAG_LATITUDE_REF)) {
//                                System.out.println(v.getString(GpsDirectory.TAG_LATITUDE_REF));
//                            }
//                            if (v.containsTag(GpsDirectory.TAG_LATITUDE_REF)) {
//                                System.out.println(v.getString(GpsDirectory.TAG_LATITUDE_REF));
//                            }

                            v.getTags().forEach(System.out::println);
                        }
//                        else{
//                            System.out.println("#############"+v.getName()+"###############");
//                            v.getTags().forEach(t -> System.out.println(t.getTagName() + ":" + t.getDescription()));
//                        }
//
//                        System.out.println(v.getName());
//                        System.out.println("############################");
//                        v.getTags().forEach(t -> System.out.println(t.getTagName() + ":" + t.getDescription()));
                    }
//                            forEach(t -> {
//                                System.out.println(t.getTagName() + ":" + t.getDescription());
//                                switch (t.getTagName()) {
//                                    //                    经度
//                                    case "GPS Longitude":
//                                        System.out.println(t.getDescription());
//                                        break;
//                                    //                        纬度
//                                    case "GPS Latitude":
//                                        System.out.println(t.getDescription());
//                                        break;
//                                    //                        拍摄时间
//                                    case "Date/Time":
//                                        System.out.println("xxxxxxx" + t.getDescription());
//                                    default:
//                                        break;
//                                }
//                            })
            );
            LocalDateTime fileNameDateTime = Util.localDateTimeByName(path.getFileName().toString());
            if (fileNameDateTime != null) {
                dateTimes.add(fileNameDateTime);
            }
            dateTimes.forEach(System.out::println);
//            Files.copy(path, Path.of("\\\\localhost@8001\\DavWWWRoot/images/" + path.getFileName()));
            System.out.println(Util.getEarliestTime(dateTimes));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void testMain2() {
        var nameList = new ArrayList<String>() {
            {
                add("IMG_20160724_175600.jpg");
                add("1cc12df906e256a0.jpg");
                add("1499159831534.jpg");
                add("IMG_MIRROR_20170518_091036.jpg");
                add("IMG_20171119_165456_BURST002.jpg");
                add("IMG_20171119_165456_BURST001_COVER.jpg");
                add("IMG20170109133206.jpg");
                add("qq_pic_merged_1491056544972.jpg");
                add("Screenshot_2016-08-28-17-26-00.jpg");
                add("mmexport1499048494783.jpg");
                add("wx_camera_1483589884142.jpg ");
            }
        };
        var ruleList = new ArrayList<String>() {
            {
                add("(\\d{8})_(\\d{6})");
                add("(IMG\\d{14})");
                add("(\\d{13})");
                add("(\\d{4})-(\\d{2})-(\\d{2})-(\\d{2})-(\\d{2})-(\\d{2})");
            }
        };
        nameList.forEach(name -> {
            System.out.println(name);
            System.out.println(Util.localDateTimeByName(name));
            System.out.println("==========================");
        });
    }

    public static void testMd5() {
        Path path = Path.of("E:\\李智\\魅族恢复相册\\亲爱的你\\-1a44cf2203cc6893.jpg");
        System.out.println(UUID.nameUUIDFromBytes(path.getFileName().toString().getBytes()));
        try {
            String md5 = Util.Md5ByFile(path.toFile());
            System.out.println(md5);
        } catch (Exception e) {
            e.printStackTrace();
        }
//        System.out.println(Util.Util.md5(str));
    }

    public static void testConfig() {
        System.out.println(Config.getProperties("redis.host"));
        System.out.println(Config.getProperties("redis.test_on_borrow"));
        System.out.println(Config.getProperties("redis.test_on_borrow").getClass());
    }

    public static void testJedis() {
        Jedis jedis = RedisUtil.getJedis();
        jedis.set("测试", "21231");
        System.out.println(jedis.get("测试"));
        jedis.del("测试");
        System.out.println(jedis.get("测试"));
        RedisUtil.returnResource(jedis);
    }


    public static void getFileTypeCount() {
        File dir = new File("E:\\MY\\李智\\魅族恢复相册\\我们都要好好的");
        HashMap<String, Integer> map = new HashMap<>();
        map = sum(dir, map);
        bianli(map);
    }

    private static void bianli(HashMap<String, Integer> map) {
        Set<String> set = map.keySet();
        for (String s : set) {
            System.out.println(s + "类型有" + map.get(s));
        }
    }

    private static HashMap<String, Integer> sum(File dir, HashMap<String, Integer> map) {
        File[] files = dir.listFiles();
        Set<String> set = map.keySet();
        for (File f : files) {
            if (f.isDirectory()) {
                sum(f, map);
            } else {
                System.out.println(f);
                String key = f.getName().substring(f.getName().indexOf(".") + 1);
                if (set.contains(key)) {
                    map.put(key, map.get(key) + 1);
                } else {
                    map.put(key, 1);
                }
            }
        }
        return map;
    }

    private static void testBrPop() {
        Jedis jedis;
        long getClientTime = System.currentTimeMillis();
        int key = 0;
        do {
            jedis = RedisUtil.getJedis();
            ++key;
        } while (jedis == null);
        System.out.println("get client try:"+key+",used time:" + (System.currentTimeMillis() - getClientTime));

        long startTime = System.currentTimeMillis();
        List<String> res = jedis.brpop(10, Config.getProperties("stream.key"));
        long endTime = System.currentTimeMillis();
        System.out.println("first time get key:" + res + ",used time:" + (endTime - startTime));
//
//        long startTime1 = System.currentTimeMillis();
//        List<String> res1 = jedis.brpop(10, Config.getProperties("stream.key"));
//        long endTime1 = System.currentTimeMillis();
//        System.out.println("second time get key:" + res + ",used time:" + (endTime1 - startTime1));

        RedisUtil.returnResource(jedis);
    }
}

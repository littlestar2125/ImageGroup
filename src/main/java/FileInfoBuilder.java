import com.drew.imaging.ImageMetadataReader;
import com.drew.metadata.Directory;
import com.drew.metadata.Tag;
import com.drew.metadata.exif.ExifIFD0Directory;
import com.drew.metadata.exif.ExifSubIFDDirectory;
import com.drew.metadata.exif.GpsDirectory;

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
import java.util.List;

/**
 * @author: zhili
 * @date: 2022/5/21 19:36
 */
public class FileInfoBuilder {

    public FileInfo buildByPath(Path path) {
        FileInfo fileInfo = new FileInfo();
        File file = path.toFile();
        fileInfo.setFileName(file.getName());
        fileInfo.setTargetFileName(file.getName());
        fileInfo.setFilePath(file.getPath());
        fileInfo.setFileSuffix(file.getName().substring(file.getName().lastIndexOf(".") + 1));
        fileInfo.setFileNameTime(Util.localDateTimeByName(file.getName()));
        fileInfo.setModifyTime(LocalDateTime.ofInstant(Instant.ofEpochMilli(file.lastModified()), ZoneOffset.of("+8")));
        try {
            fileInfo.setMd5(Util.Md5ByFile(file));
            BasicFileAttributeView basicView = Files.getFileAttributeView(path, BasicFileAttributeView.class, LinkOption.NOFOLLOW_LINKS);
            BasicFileAttributes attr = basicView.readAttributes();
            fileInfo.setCreateTime(LocalDateTime.ofInstant(Instant.ofEpochMilli(attr.creationTime().toMillis()), ZoneOffset.of("+8")));
            fileInfo.setModifyTime(LocalDateTime.ofInstant(Instant.ofEpochMilli(attr.lastModifiedTime().toMillis()), ZoneOffset.of("+8")));
            String format = "yyyy:MM:dd HH:mm:ss";
            DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(format);
            Iterable<Directory> directories = ImageMetadataReader.readMetadata(file).getDirectories();
            directories.forEach(v ->
                    {
                        List<Tag> tags = v.getTags().stream().toList();
                        if (v instanceof ExifIFD0Directory) {
                            tags.stream().filter(x -> x.getTagName().contains("Date/Time"))
                                    .findFirst().ifPresent(x ->
                                            fileInfo.setShotTime(LocalDateTime.parse(x.getDescription(), dateTimeFormatter)));
                        } else if (v instanceof ExifSubIFDDirectory) {
                            for (Tag tag : tags) {
                                if (tag.getTagName().contains("Date/Time Original")) {
                                    fileInfo.setOriginalTime(LocalDateTime.parse(tag.getDescription(), dateTimeFormatter));
                                } else if (tag.getTagName().contains("Date/Time Digitized")) {
                                    fileInfo.setDigitizedTime(LocalDateTime.parse(tag.getDescription(), dateTimeFormatter));
                                }
                            }
                        } else if (v instanceof GpsDirectory) {
                            String dateStamp = null;
                            String timeStamp = null;
                            String latitude = null;
                            String longitude = null;
                            for (Tag tag : tags) {
                                if (tag.getTagName().contains("Date Stamp")) {
                                    dateStamp = tag.getDescription();
                                } else if (tag.getTagName().contains("Time-Stamp")) {
                                    timeStamp = tag.getDescription();
                                } else if (tag.getTagName().contains("GPS Latitude") && !tag.getTagName().contains("Ref")) {
                                    latitude = Util.dmt2D(tag.getDescription());
                                    fileInfo.setGpsLatitude(latitude);
                                } else if (tag.getTagName().contains("GPS Longitude") && !tag.getTagName().contains("Ref")) {
                                    longitude = Util.dmt2D(tag.getDescription());
                                    fileInfo.setGpsLongitude(longitude);
                                }
                            }
                            if (dateStamp != null && timeStamp != null) {
                                fileInfo.setGpsTime(LocalDateTime.parse(dateStamp + " " + timeStamp, DateTimeFormatter.ofPattern("yyyy:MM:dd H:mm:s z")));
                            }
                            if (latitude != null && longitude != null) {
                                String gps = longitude + "," + latitude;
                                fileInfo.setAddress(Util.gps2Address(gps));
                            }
                        }
                    }
            );
            fileInfo.setTargetPath(Util.getEarliestTime(Util.getDateTimeListByFile(fileInfo)));
        } catch (Exception e) {
            // TODO 记录错误信息等待后续处理
        }
        return fileInfo;
    }

}

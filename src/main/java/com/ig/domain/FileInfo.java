package com.ig.domain;

import com.drew.imaging.ImageMetadataReader;
import com.drew.metadata.Directory;
import com.drew.metadata.Tag;
import com.drew.metadata.exif.ExifIFD0Directory;
import com.drew.metadata.exif.ExifSubIFDDirectory;
import com.drew.metadata.exif.GpsDirectory;
import com.ig.util.Util;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

/**
 * @author: zhili
 * @date: 2022/5/21 15:58
 */
public class FileInfo {

    private String fileName;

    private String filePath;

    private String targetPath;

    private String targetFileName;

    private String fileSuffix;

    private String md5;

    private LocalDateTime createTime;

    private LocalDateTime modifyTime;

    private LocalDateTime fileNameTime;

    private LocalDateTime shotTime;

    private LocalDateTime originalTime;

    private LocalDateTime digitizedTime;

    private LocalDateTime gpsTime;//待转换

    //纬度
    private String gpsLatitude;

    //经度
    private String gpsLongitude;

    private String address;

    public FileInfo() {
    }

    public FileInfo(Path path) {
        File file = path.toFile();
        this.fileName = file.getName();
        this.filePath = file.getPath();
        this.fileSuffix = file.getName().substring(file.getName().lastIndexOf(".") + 1);
        this.fileNameTime = Util.localDateTimeByName(file.getName());
        this.modifyTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(file.lastModified()), ZoneOffset.of("+8"));
        try {
            this.md5 = Util.Md5ByFile(file);
            BasicFileAttributeView basicView = Files.getFileAttributeView(path, BasicFileAttributeView.class, LinkOption.NOFOLLOW_LINKS);
            BasicFileAttributes attr = basicView.readAttributes();
            this.createTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(attr.creationTime().toMillis()), ZoneOffset.of("+8"));
            this.modifyTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(attr.lastModifiedTime().toMillis()), ZoneOffset.of("+8"));
            Iterable<Directory> directories = ImageMetadataReader.readMetadata(file).getDirectories();
            directories.forEach(v ->
                    {
                        List<Tag> tags = v.getTags().stream().toList();
                        if (v instanceof ExifIFD0Directory) {
                            tags.stream().filter(x -> x.getTagName().contains("Date/Time"))
                                    .findFirst().ifPresent(x ->
                                            this.shotTime = Util.getDateTimeFromString(x.getDescription())
                                    );
                        } else if (v instanceof ExifSubIFDDirectory) {
                            for (Tag tag : tags) {
                                if (tag.getTagName().contains("Date/Time Original")) {
                                    this.originalTime = Util.getDateTimeFromString(tag.getDescription());
                                } else if (tag.getTagName().contains("Date/Time Digitized")) {
                                    this.digitizedTime = Util.getDateTimeFromString(tag.getDescription());
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
                                    this.gpsLatitude = latitude;
                                } else if (tag.getTagName().contains("GPS Longitude") && !tag.getTagName().contains("Ref")) {
                                    longitude = Util.dmt2D(tag.getDescription());
                                    this.gpsLongitude = longitude;
                                }
                            }
                            if (dateStamp != null && timeStamp != null) {
                                this.gpsTime = Util.getDateTimeFromString(dateStamp + " " + timeStamp);
                            }
                            if (latitude != null && longitude != null) {
                                String gps = longitude + "," + latitude;
                                this.address = Util.gps2Address(gps);
                            }
                        }
                    }
            );
            this.targetPath = Util.getEarliestTime(Util.getDateTimeListByFile(this));
            this.targetFileName = this.getAddress() + this.getFileName();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getTargetPath() {
        return targetPath;
    }

    public void setTargetPath(String targetPath) {
        this.targetPath = targetPath;
    }

    public String getTargetFileName() {
        return targetFileName;
    }

    public void setTargetFileName(String targetFileName) {
        this.targetFileName = targetFileName;
    }

    public String getFileSuffix() {
        return fileSuffix;
    }

    public void setFileSuffix(String fileSuffix) {
        this.fileSuffix = fileSuffix;
    }

    public String getMd5() {
        return md5;
    }

    public void setMd5(String md5) {
        this.md5 = md5;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public LocalDateTime getModifyTime() {
        return modifyTime;
    }

    public void setModifyTime(LocalDateTime modifyTime) {
        this.modifyTime = modifyTime;
    }

    public LocalDateTime getFileNameTime() {
        return fileNameTime;
    }

    public void setFileNameTime(LocalDateTime fileNameTime) {
        this.fileNameTime = fileNameTime;
    }

    public LocalDateTime getShotTime() {
        return shotTime;
    }

    public void setShotTime(LocalDateTime shotTime) {
        this.shotTime = shotTime;
    }

    public LocalDateTime getOriginalTime() {
        return originalTime;
    }

    public void setOriginalTime(LocalDateTime originalTime) {
        this.originalTime = originalTime;
    }

    public LocalDateTime getDigitizedTime() {
        return digitizedTime;
    }

    public void setDigitizedTime(LocalDateTime digitizedTime) {
        this.digitizedTime = digitizedTime;
    }

    public LocalDateTime getGpsTime() {
        return gpsTime;
    }

    public void setGpsTime(LocalDateTime gpsTime) {
        this.gpsTime = gpsTime;
    }

    public String getGpsLatitude() {
        return gpsLatitude;
    }

    public void setGpsLatitude(String gpsLatitude) {
        this.gpsLatitude = gpsLatitude;
    }

    public String getGpsLongitude() {
        return gpsLongitude;
    }

    public void setGpsLongitude(String gpsLongitude) {
        this.gpsLongitude = gpsLongitude;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    @Override
    public String toString() {
        return "Do.FileInfo{" +
                "fileName='" + getFileName() + '\'' +
                ", filePath='" + getFilePath() + '\'' +
                ", targetPath='" + getTargetPath() + '\'' +
                ", targetFileName='" + getTargetFileName() + '\'' +
                ", fileSuffix='" + getFileSuffix() + '\'' +
                ", md5='" + getMd5() + '\'' +
                ", createTime=" + getCreateTime() +
                ", modifyTime=" + getModifyTime() +
                ", fileNameTime=" + getFileNameTime() +
                ", shotTime=" + getShotTime() +
                ", originalTime=" + getOriginalTime() +
                ", digitizedTime=" + getDigitizedTime() +
                ", gpsTime=" + getGpsTime() +
                ", gpsLatitude='" + getGpsLatitude() + '\'' +
                ", gpsLongitude='" + getGpsLongitude() + '\'' +
                ", getAddress='" + getAddress() + '\'' +
                '}';
    }
}

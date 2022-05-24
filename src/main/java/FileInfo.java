import java.time.LocalDateTime;

/**
 * @author: zhili
 * @date: 2022/5/21 15:58
 */
public class FileInfo {

    private String fileName;

    private String filePath;

    private String targetPath;//未设置

    private String targetFileName;//未设置

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

    private String address;//未设置

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
        return "FileInfo{" +
                "fileName='" + fileName + '\'' +
                ", filePath='" + filePath + '\'' +
                ", targetPath='" + targetPath + '\'' +
                ", targetFileName='" + targetFileName + '\'' +
                ", fileSuffix='" + fileSuffix + '\'' +
                ", md5='" + md5 + '\'' +
                ", createTime=" + createTime +
                ", modifyTime=" + modifyTime +
                ", fileNameTime=" + fileNameTime +
                ", shotTime=" + shotTime +
                ", originalTime=" + originalTime +
                ", digitizedTime=" + digitizedTime +
                ", gpsTime=" + gpsTime +
                ", gpsLatitude='" + gpsLatitude + '\'' +
                ", gpsLongitude='" + gpsLongitude + '\'' +
                ", getAddress='" + address + '\'' +
                '}';
    }
}

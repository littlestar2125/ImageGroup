package com.ig.util;

import com.ig.domain.FileInfo;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.ig.config.Config;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;
import redis.clients.jedis.Jedis;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author: zhili
 * @date: 2022/5/21 0:36
 */
public class Util {
    //统一的上传地址
    public static final String targetPath = "\\\\localhost@8001\\DavWWWRoot\\images\\";

    //高德API调用地址
    private static final String BASE_URL = "https://restapi.amap.com/v3/geocode/regeo?key=" + Config.getProperties("map.key") + "&location=";

    //文件名解析时间规则列表
    private final static List<String> ruleList = new ArrayList<>() {
        {
            add("(\\d{8})_(\\d{6})");
            add("(IMG\\d{14})");
            add("(\\d{13})");
            add("(\\d{4})-(\\d{2})-(\\d{2})-(\\d{2})-(\\d{2})-(\\d{2})");
        }
    };

    private final static List<DateTimeFormatter> DATE_TIME_FORMATTER_LIST = new ArrayList<>() {
        {
            add(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            add(DateTimeFormatter.ofPattern("yyyy:MM:dd HH:mm:ss.SSS zzz"));
            add(DateTimeFormatter.ofPattern("yyyy:MM:dd HH:mm:ss"));
        }
    };

    //文件目的路径地址规则
    public static String fileTargetPatten = "yyyy" + File.separatorChar + "MM" + File.separatorChar
            + "dd" + File.separatorChar + "HHmmss";

    public static LocalDateTime getDateTimeFromString(String timeStr) {
        if (timeStr == null || timeStr.isBlank()) {
            return null;
        }
        LocalDateTime temp;
        if (timeStr.contains("UTC")) {
            try {
                ZonedDateTime zonedDateTime = ZonedDateTime.parse(timeStr, DATE_TIME_FORMATTER_LIST.get(1));
                return zonedDateTime.withZoneSameInstant(ZoneId.of("Asia/Shanghai")).toLocalDateTime();
            } catch (DateTimeParseException ex) {
                return null;
            }
        }
        for (DateTimeFormatter formatter : DATE_TIME_FORMATTER_LIST) {
            temp = strToTime(timeStr, formatter);
            if (temp != null) {
                return temp;
            }
        }
        return null;
    }


    private static LocalDateTime strToTime(String str, DateTimeFormatter dateFormatter) {
        if (str == null || str.isBlank()) {
            return null;
        }
        try {
            return LocalDateTime.parse(str, dateFormatter);
        } catch (DateTimeParseException ex) {
            return null;
        }
    }

    /**
     * 根据文件名解析文件时间
     *
     * @param fileName 文件名
     * @return 文件时间
     */
    public static LocalDateTime localDateTimeByName(String fileName) {
        DateTimeFormatter dateTimeFormatter;
        Pattern pattern;
        Matcher matcher;
        String format;
        for (int i = 0; i < ruleList.size(); i++) {
            pattern = Pattern.compile(ruleList.get(i));
            matcher = pattern.matcher(fileName);
            if (matcher.find()) {
                if (i == 0) {
                    format = "yyyyMMdd HHmmss";
                    dateTimeFormatter = DateTimeFormatter.ofPattern(format);
                    return LocalDateTime.parse(matcher.group(1) + " " + matcher.group(2), dateTimeFormatter);
                } else if (i == 1) {
                    format = "yyyyMMddHHmmss";
                    dateTimeFormatter = DateTimeFormatter.ofPattern(format);
                    return LocalDateTime.parse(matcher.group(1).substring(3), dateTimeFormatter);
                } else if (i == 2) {
                    return LocalDateTime.ofInstant(Instant.ofEpochMilli(Long.parseLong(matcher.group(1))), ZoneOffset.of("+8"));
                } else {
                    format = "yyyyMMddHHmmss";
                    dateTimeFormatter = DateTimeFormatter.ofPattern(format);
                    StringBuilder sb = new StringBuilder();
                    for (int j = 1; j <= matcher.groupCount(); j++) {
                        sb.append(matcher.group(j));
                    }
                    return LocalDateTime.parse(sb.toString(), dateTimeFormatter);
                }
            }
        }
        return null;
    }

    /**
     * 根据时间列表获取最早时间(gps时间可能不是24小时制  需要酌情抛弃)
     *
     * @param localDateTimes 时间列表
     * @return 最早时间
     */
    public static String getEarliestTime(List<LocalDateTime> localDateTimes) {
        LocalDateTime earliestTime = null;
        for (LocalDateTime localDateTime : localDateTimes) {
            if (localDateTime != null) {
                if (earliestTime == null) {
                    earliestTime = localDateTime;
                } else {
                    if (earliestTime.isAfter(localDateTime)) {
                        earliestTime = localDateTime;
                    }
                }
            }
        }
        if (earliestTime == null) {
            return null;
        }
        return targetPath + earliestTime.format(DateTimeFormatter.ofPattern(fileTargetPatten));
    }

    /**
     * 根据文件获取MD5
     *
     * @param file 文件对象
     * @return md5 md5
     * @throws IOException IO异常
     */
    public static String Md5ByFile(File file) throws IOException {
        return DigestUtils.md5Hex(new FileInputStream(file));
    }

    /**
     * 根据fileInfo获取时间列表
     *
     * @param file 文件信息对象
     * @return 时间列表
     */
    public static List<LocalDateTime> getDateTimeListByFile(FileInfo file) {
        List<LocalDateTime> localDateTimes = new ArrayList<>();
        if (file.getCreateTime() != null) {
            localDateTimes.add(file.getCreateTime());
        }
        if (file.getModifyTime() != null) {
            localDateTimes.add(file.getModifyTime());
        }
        if (file.getFileNameTime() != null) {
            localDateTimes.add(file.getFileNameTime());
        }
        if (file.getOriginalTime() != null) {
            localDateTimes.add(file.getOriginalTime());
        }
        if (file.getDigitizedTime() != null) {
            localDateTimes.add(file.getDigitizedTime());
        }
        if (file.getShotTime() != null) {
            localDateTimes.add(file.getShotTime());
        }
        if (file.getFileNameTime() == null &&
                file.getShotTime() == null &&
                file.getGpsTime() != null) {
            localDateTimes.add(file.getGpsTime());
        }
        return localDateTimes;
    }

    /**
     * 经纬度  度分秒转换成度
     *
     * @param jwd 度分秒
     * @return 度
     */
    public static String dmt2D(String jwd) {
        if (jwd != null && !jwd.isBlank() && (jwd.contains("°"))) {//如果不为空并且存在度单位
            //计算前进行数据处理
            jwd = jwd.replace("E", "").replace("N", "").replace(":", "").replace("：", "");
            double m = 0, s = 0;
            double d = Double.parseDouble(jwd.split("°")[0].trim());
            jwd = jwd.split("°")[1];
            //不同单位的分，可扩展
            if (jwd.contains("′")) {//正常的′
                m = Double.parseDouble(jwd.split("′")[0].trim());
                jwd = jwd.split("′")[1];
            } else if (jwd.contains("'")) {//特殊的'
                m = Double.parseDouble(jwd.split("'")[0].trim());
                jwd = jwd.split("'")[1];
            }
            //不同单位的秒，可扩展
            if (jwd.contains("″")) {//正常的″
                //有时候没有分 如：112°10.25″
                s = Double.parseDouble(jwd.split("″")[0].trim());
            } else if (jwd.contains("''")) {//特殊的''
                //有时候没有分 如：112°10.25''
                s = Double.parseDouble(jwd.split("''")[0].trim());
            } else if (jwd.contains("\"")) {
                s = Double.parseDouble(jwd.split("\"")[0].trim());
            }
            BigDecimal b = new BigDecimal(d + m / 60 + s / 60 / 60);
            double f1 = b.setScale(6, RoundingMode.HALF_UP).doubleValue();
            return String.valueOf(f1);//计算并转换为string
        }
        return null;
    }

    /**
     * 经纬度调取高德API获取地址  并使用缓存
     *
     * @param gps 经纬度
     * @return 地址
     */
    public static String gps2Address(String gps) {
        Jedis jedis = null;
        GetMethod getMethod = null;
        try {
            jedis = RedisUtil.getJedis();
            String address;
            if (jedis != null && (address = jedis.get(gps)) != null) {
                return address;
            }
            // 创建httpClient实例对象
            HttpClient httpClient = new HttpClient();
            // 设置httpClient连接主机服务器超时时间：15000毫秒
            httpClient.getHttpConnectionManager().getParams().setConnectionTimeout(15000);
            // 创建GET请求方法实例对象
            getMethod = new GetMethod(BASE_URL + gps);
            // 设置post请求超时时间
            getMethod.getParams().setParameter(HttpMethodParams.SO_TIMEOUT, 60000);
            getMethod.addRequestHeader("Content-Type", "application/json;charset=UTF-8");

            httpClient.executeMethod(getMethod);
            JSONObject jsonObject = JSON.parseObject(getMethod.getResponseBodyAsString());
            if ("1".equals(jsonObject.get("status"))) {
                JSONObject resultObject = jsonObject.getJSONObject("regeocode");
                address = resultObject.getString("formatted_address");
                if (jedis != null) {
                    jedis.set(gps, address);
                }
                return address;
            }
        } catch (Exception e) {
            // TODO 记录错误信息等待后续处理
            throw new RuntimeException(e);
        } finally {
            if (jedis != null) {
                RedisUtil.returnResource(jedis);
            }
            if (getMethod != null) {
                getMethod.releaseConnection();
            }
        }
        return null;
    }
}

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author: zhili
 * @date: 2022/5/21 0:36
 */
public class Util {
    private static final String BASE_URL = "https://restapi.amap.com/v3/geocode/regeo?key=24998c8857c1004e7e35fcedde31c1a2&location=";

    public static Map<String, String> mapKey = new HashMap<>();

    public static List<String> ruleList = new ArrayList<>() {
        {
            add("(\\d{8})_(\\d{6})");
            add("(IMG\\d{14})");
            add("(\\d{13})");
            add("(\\d{4})-(\\d{2})-(\\d{2})-(\\d{2})-(\\d{2})-(\\d{2})");
        }
    };

    public static String fileTargetPatten = "yyyy" + File.separatorChar + "MM" + File.separatorChar
            + "dd" + File.separatorChar + "HH" + File.separatorChar + "mm" + File.separatorChar + "ss" + File.separatorChar;

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
        return earliestTime.format(DateTimeFormatter.ofPattern(fileTargetPatten));
    }

    public static String Md5ByFile(File file) throws IOException {
        return DigestUtils.md5Hex(new FileInputStream(file));
    }

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

    public static String gps2Address(String gps) {
        if (mapKey.containsKey(gps)) {
            return mapKey.get(gps);
        }
        // 创建httpClient实例对象
        HttpClient httpClient = new HttpClient();
        // 设置httpClient连接主机服务器超时时间：15000毫秒
        httpClient.getHttpConnectionManager().getParams().setConnectionTimeout(15000);
        // 创建GET请求方法实例对象
        GetMethod getMethod = new GetMethod(BASE_URL + gps);
        // 设置post请求超时时间
        getMethod.getParams().setParameter(HttpMethodParams.SO_TIMEOUT, 60000);
        getMethod.addRequestHeader("Content-Type", "application/json;charset=UTF-8");
        try {
            httpClient.executeMethod(getMethod);
            JSONObject jsonObject = JSON.parseObject(getMethod.getResponseBodyAsString());
            if ("1".equals(jsonObject.get("status"))) {
                JSONObject resultObject = jsonObject.getJSONObject("regeocode");
                String address = resultObject.getString("formatted_address");
                mapKey.putIfAbsent(gps, address);
                return address;
            }
        } catch (Exception e) {
            // TODO 记录错误信息等待后续处理
            throw new RuntimeException(e);
        } finally {
            getMethod.releaseConnection();
        }
        return null;
    }
}

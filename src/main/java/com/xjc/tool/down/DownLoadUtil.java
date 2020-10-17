package com.xjc.tool.down;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;

/** @Version 1.0 @ClassName DownLoadUtil @Author jiachenXu @Date 2020/3/3 @Description 下载 */
@Component
public class DownLoadUtil {

  public String downLoadFile(HttpServletRequest request, HttpServletResponse response) {
    // 读取文件地址
    File scFileDir = new File("F:\\redis-2.8.9");
    File[] trxFiles = scFileDir.listFiles();

    // 获取当前目录下的第几个文件
    String fileName = trxFiles[1].getName();
    String path = scFileDir.getPath();

    if (!StringUtils.isEmpty(fileName)) {
      File file = new File(path, fileName);
      if (file.exists()) {
        response.setContentType("application/force-download");
        response.setHeader("Content-Disposition", "attachment;fileName=" + fileName);

        OutputStream os = null;
        try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file), 1024)) {
          os = response.getOutputStream();
          int read = bis.read();
          while (read != 0) {
            os.write(bis.read());
          }
        } catch (Exception e) {
          e.printStackTrace();
        } finally {
          if (os != null) {
            try {
              os.close();
            } catch (IOException e) {
              e.printStackTrace();
            }
          }
        }
      }
    }
    return fileName;
  }
}

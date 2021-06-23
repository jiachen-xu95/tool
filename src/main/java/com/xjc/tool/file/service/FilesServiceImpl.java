package com.xjc.tool.file.service;

import com.xjc.tool.file.api.FilesService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;
import org.springframework.util.StringUtils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileStore;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static java.nio.file.StandardOpenOption.APPEND;

/**
 * @Version 1.0
 * @ClassName FilesServiceImpl
 * @Author jiachenXu
 * @Date 2020/3/8
 * @Description
 */
@Slf4j
@Service
@SuppressWarnings("all")
public class FilesServiceImpl implements FilesService {

    private static final Long SPACE_MAX = 100L;

    @Autowired
    private static StopWatch stopWatch;

    @Override
    public Boolean createFile(String path) {
        StopWatch stopWatch = null;
        try {
            stopWatch = getWatch("FilesUtilImpl-createFile");

            checkSpace(path);
            Path dirPath = Paths.get(path);
            if (!Files.exists(dirPath)) {
                Files.createFile(dirPath);
                log.info("FileUtil-createFile success, runTime=", stopWatch.getTotalTimeMillis());
                return true;
            }
        } catch (Exception e) {
            log.error("FileUtil-createFile error, runTime=", stopWatch.getTotalTimeMillis(), e);
        } finally {
            if (stopWatch != null) {
                stopWatch.stop();
            }
        }
        return false;
    }

    @Override
    public Boolean createDirectories(String path) {
        StopWatch stopWatch = null;
        try {
            stopWatch = getWatch("FilesUtilImpl-createDirectories");
            checkSpace(path);
            Path dirPath = Paths.get(path);
            if (Files.notExists(dirPath)) {
                Files.createDirectories(dirPath);
                log.info("FileUtil-createDirectories success, runTime=", stopWatch.getTotalTimeMillis());
                return true;
            }
        } catch (Exception e) {
            log.error("FileUtil-createDirectories error, runTime=", stopWatch.getTotalTimeMillis(), e);
        } finally {
            if (stopWatch != null) {
                stopWatch.stop();
            }
        }
        return false;
    }

    @Override
    public Boolean write(String path, String count) {
        StopWatch stopWatch = null;
        BufferedWriter writer = null;
        try {
            stopWatch = getWatch("FilesUtilImpl-write");
            checkSpace(path);

            Path dirPath = Paths.get(path);

            if (!StringUtils.isEmpty(count.trim())) {
                writer = Files.newBufferedWriter(dirPath, APPEND);
                writer.write(count);
                writer.flush();
                log.info("FileUtil-write success, runTime=", stopWatch.getTotalTimeMillis());
                return true;
            }
        } catch (Exception e) {
            log.error("FileUtil-write error, runTime=", stopWatch.getTotalTimeMillis(), e);
        } finally {
            try {
                if (writer != null) {
                    writer.close();
                }
                if (stopWatch != null) {
                    stopWatch.stop();
                }
            } catch (IOException e) {
                log.error("FileUtil-write close error", e);
            }
        }
        return false;
    }


    @Override
    public String read(String path) {
        StopWatch stopWatch = null;
        BufferedReader reader = null;
        String line;
        try {
            stopWatch = getWatch("FilesUtilImpl-read");
            Path dirPath = Paths.get(path);
            reader = Files.newBufferedReader(dirPath, StandardCharsets.UTF_8);
            while ((line = reader.readLine()) != null) {
                log.info("FileUtil-read success, runTime=", stopWatch.getTotalTimeMillis());
                return line;
            }
        } catch (Exception e) {
            log.error("FileUtil-read error, runTime=", stopWatch.getTotalTimeMillis(), e);
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
                if (stopWatch != null) {
                    stopWatch.stop();
                }
            } catch (IOException e) {
                log.error("FileUtil-write read error, runTime=", stopWatch.getTotalTimeMillis(), e);
            }
        }
        return null;
    }

    @Override
    public Boolean delete(String path) {
        StopWatch stopWatch = null;
        try {
            stopWatch = getWatch("FilesUtilImpl-delete");
            Path dirPath = Paths.get(path);
            if (Files.exists(dirPath)) {
                return Files.deleteIfExists(dirPath);
            }
        } catch (Exception e) {
            log.error("FileUtil-delete error, runTime=", stopWatch.getTotalTimeMillis(), e);
        } finally {
            if (stopWatch != null) {
                stopWatch.stop();
            }
        }
        return false;
    }

    @Override
    public Boolean copy(String oldPath, String newPath) {
        StopWatch stopWatch = null;
        try {
            stopWatch = getWatch("FilesUtilImpl-copy");
            checkSpace(newPath);
            Path oldDir = Paths.get(oldPath);
            if (Files.exists(oldDir)) {
                Files.copy(oldDir, Paths.get(newPath));
                return true;
            }
        } catch (Exception e) {
            log.error("FileUtil-copy error", e);
        } finally {
            if (stopWatch != null) {
                stopWatch.stop();
            }
        }
        return false;
    }

    /**
     * 文件夹里面内容正序排序
     *
     * @param files
     * @return
     */
    @Override
    public File[] sortFolders(File[] files) {
        File temp;
        StringBuilder stringBuilder = new StringBuilder(32);
        for (int j = 0; j < files.length - 1; j++) {
            String min = files[j].getName();
            int minIndex = j;
            for (int k = j + 1; k < files.length; k++) {
                if (Long.parseLong(substringStr(min, stringBuilder)) > Long.parseLong(substringStr(files[k].getName(), stringBuilder))) {
                    min = files[k].getName();
                    minIndex = k;
                }
            }
            temp = files[j];
            files[j] = files[minIndex];
            files[minIndex] = temp;
        }
        return files;
    }

    @Override
    public boolean checkFileSize(Long len, int size, String unit) {
        double fileSize = 0;
        if ("B".equals(unit.toUpperCase())) {
            fileSize = (double) len;
        } else if ("K".equals(unit.toUpperCase())) {
            fileSize = (double) len / 1024;
        } else if ("M".equals(unit.toUpperCase())) {
            fileSize = (double) len / 1048576;
        } else if ("G".equals(unit.toUpperCase())) {
            fileSize = (double) len / 1073741824;
        }
        return fileSize > size;
    }

    private String substringStr(String str, StringBuilder stringBuilder) {
        String result = null;
        stringBuilder.setLength(0);
        String[] s = str.substring(0, str.lastIndexOf(".")).split("_");
        if (str.contains("Screenshot")) {
            String strings = s[1];
            String[] split = strings.split("-");
            for (String value : split) {
                stringBuilder.append(value);
            }
            result = stringBuilder.toString();
        } else if (str.contains("IMG")) {
            result = s[1] + s[2];
        }
        return result;
    }

    /**
     * 获取空间，预留参数后期可以改造
     */
    private static void checkSpace(String dir) throws IOException {
        // 分区的总空间
        long totalSpace = 0;
        // 分区的已用空间
        long usableSpace = 0;
        // 分区的剩余空间
        long unallocatedspace = 0;

        Path path = Paths.get(dir.substring(0, 2));
        FileStore fileStore = Files.getFileStore(path);

        if (fileStore.isReadOnly()) {
            unallocatedspace = fileStore.getUnallocatedSpace();
            if (unallocatedspace <= SPACE_MAX) {
                throw new IllegalArgumentException("可使用空间不足");
            }
        }
    }

    /**
     * 计数器初始化
     *
     * @param functionName 函数
     * @return StopWatch
     */
    private static StopWatch getWatch(String functionName) {
        stopWatch = new StopWatch(functionName);
        stopWatch.start();
        return stopWatch;
    }

}

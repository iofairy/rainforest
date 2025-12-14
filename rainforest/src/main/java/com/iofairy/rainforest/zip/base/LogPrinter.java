/*
 * Copyright (C) 2021 iofairy, <https://github.com/iofairy/rainforest>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.iofairy.rainforest.zip.base;

import com.iofairy.time.Stopwatch;
import com.iofairy.falcon.unit.Bytes;
import com.iofairy.rainforest.zip.ac.SuperACs;
import com.iofairy.top.S;
import lombok.extern.slf4j.Slf4j;

/**
 * SuperAC日志打印控制
 *
 * @since 0.2.0
 */
@Slf4j
public class LogPrinter {
    private static final int REPEAT_FACTOR = 5;
    private static final int MIN_REPEAT_TIMES = 1;
    private static final int MAX_REPEAT_TIMES = 6;

    public static void logs(String format, Object... arguments) {
        log.info(format, arguments);
    }

    public static void warnLogs(String format, Object... arguments) {
        log.warn(format, arguments);
    }

    public static void printBeforeUnzip(String unzipId, String zipFileName, ZipLogLevel zipLogLevel, String logSource) {
        if (zipLogLevel.level >= ZipLogLevel.BRIEF.level) {
            logs(">*>*>*>*>*>*>*>*>*>*>*>*>*>*>*>*>*>*>*> 解压ID：[{}]，压缩包【{}】正在处理…… <<{}>>", unzipId, zipFileName, logSource);
        }
    }

    public static void printAfterUnzip(String unzipId, String zipFileName, ZipLogLevel zipLogLevel, String logSource, Stopwatch stopwatch) {
        if (zipLogLevel.level >= ZipLogLevel.BRIEF.level) {
            logs("<*<*<*<*<*<*<*<*<*<*<*<*<*<*<*<*<*<*<*< 解压ID：[{}]，压缩包【{}】完成处理！耗时：【{}】 <<{}>>", unzipId, zipFileName, stopwatch, logSource);
        }
    }

    public static void printAfterReZip(String unzipId, String zipFileName, ZipLogLevel zipLogLevel, String logSource, Stopwatch stopwatch, long byteLength) {
        if (zipLogLevel.level >= ZipLogLevel.BRIEF.level) {
            String byteFormat = Bytes.ofBs((double) byteLength, true).format();
            logs("<*<*<*<*<*<*<*<*<*<*<*<*<*<*<*<*<*<*<*< 解压ID：[{}]，压缩包【{}】完成处理，重压缩后大小：【{}】！耗时：【{}】 <<{}>>", unzipId, zipFileName, byteFormat, stopwatch, logSource);
        }
    }

    public static void printBeforeUnzip(String unzipId, int unzipTimes, String zipFileName, String entryFileName, ZipLogLevel zipLogLevel, String logSource) {
        if (zipLogLevel.level >= ZipLogLevel.BRIEF.level) {
            String repeat = S.repeat(">", getRepeatTimes(unzipTimes) * REPEAT_FACTOR);
            if (S.isEmpty(zipFileName)) {
                logs("{} 解压ID：[{}]，当前unzipTimes为：[{}]，正在解压【{}】…… <<{}>>", repeat, unzipId, unzipTimes, entryFileName, logSource);
            } else {
                logs("{} 解压ID：[{}]，当前unzipTimes为：[{}]，正在解压【{}】中的压缩包【{}】…… <<{}>>", repeat, unzipId, unzipTimes, zipFileName, entryFileName, logSource);
            }
        }
    }

    public static void printAfterUnzip(String unzipId, int unzipTimes, String zipFileName, String entryFileName, ZipLogLevel zipLogLevel, String logSource, Stopwatch stopwatch) {
        if (zipLogLevel.level >= ZipLogLevel.BRIEF.level) {
            String repeat = S.repeat("<", getRepeatTimes(unzipTimes) * REPEAT_FACTOR);
            if (S.isEmpty(zipFileName)) {
                logs("{} 解压ID：[{}]，当前unzipTimes为：[{}]，完成解压【{}】！耗时：【{}】 <<{}>>", repeat, unzipId, unzipTimes, entryFileName, stopwatch, logSource);
            } else {
                logs("{} 解压ID：[{}]，当前unzipTimes为：[{}]，完成解压【{}】中的压缩包【{}】！耗时：【{}】 <<{}>>", repeat, unzipId, unzipTimes, zipFileName, entryFileName, stopwatch, logSource);
            }
        }
    }

    public static void printBeforeWriteZip(String unzipId, int unzipTimes, String zipFileName, String entryFileName, ZipLogLevel zipLogLevel, String logSource) {
        if (zipLogLevel.level >= ZipLogLevel.BRIEF.level) {
            String repeat = S.repeat("\\", getRepeatTimes(unzipTimes) * REPEAT_FACTOR);
            logs("{} 解压ID：[{}]，当前unzipTimes为：[{}]，正在将文件【{}】写入压缩包【{}】…… <<{}>>", repeat, unzipId, unzipTimes, entryFileName, zipFileName, logSource);
        }
    }

    public static void printAfterWriteZip(String unzipId, int unzipTimes, String zipFileName, String entryFileName, ZipLogLevel zipLogLevel, String logSource, Stopwatch stopwatch, long byteLength) {
        if (zipLogLevel.level >= ZipLogLevel.BRIEF.level) {
            String repeat = S.repeat("/", getRepeatTimes(unzipTimes) * REPEAT_FACTOR);
            String byteFormat = Bytes.ofBs((double) byteLength, true).format();
            logs("{} 解压ID：[{}]，当前unzipTimes为：[{}]，【{}】写入压缩包【{}】完成，写入大小：【{}】！耗时：【{}】 <<{}>>", repeat, unzipId, unzipTimes, entryFileName, zipFileName, byteFormat, stopwatch, logSource);
        }
    }

    public static void printBeforeOther(String unzipId, int unzipTimes, String zipFileName, String entryFileName, ZipLogLevel zipLogLevel, String logSource) {
        if (zipLogLevel.level >= ZipLogLevel.DETAIL.level) {
            String repeat = S.repeat("(", getRepeatTimes(unzipTimes) * REPEAT_FACTOR);
            logs("{} 解压ID：[{}]，当前unzipTimes为：[{}]，正在处理【{}】中的文件【{}】…… <<{}>>", repeat, unzipId, unzipTimes, zipFileName, entryFileName, logSource);
        }
    }

    public static void printAfterOther(String unzipId, int unzipTimes, String zipFileName, String entryFileName, ZipLogLevel zipLogLevel, String logSource, Stopwatch stopwatch) {
        if (zipLogLevel.level >= ZipLogLevel.DETAIL.level) {
            String repeat = S.repeat(")", getRepeatTimes(unzipTimes) * REPEAT_FACTOR);
            logs("{} 解压ID：[{}]，当前unzipTimes为：[{}]，完成处理【{}】中的文件【{}】！耗时：【{}】 <<{}>>", repeat, unzipId, unzipTimes, zipFileName, entryFileName, stopwatch, logSource);
        }
    }

    public static void printDeleteLogs(String unzipId, int unzipTimes, String zipFileName, String entryFileName, ZipLogLevel zipLogLevel, String logSource) {
        if (zipLogLevel.level >= ZipLogLevel.DETAIL.level) {
            String repeat = S.repeat("-", getRepeatTimes(unzipTimes) * REPEAT_FACTOR);
            logs("{} 解压ID：[{}]，当前unzipTimes为：[{}]，正在删除【{}】中的文件【{}】…… <<{}>>", repeat, unzipId, unzipTimes, zipFileName, entryFileName, logSource);
        }
    }

    public static void printAppendLogs(String unzipId, int unzipTimes, String zipFileName, String entryFileName, ZipLogLevel zipLogLevel, String logSource) {
        if (zipLogLevel.level >= ZipLogLevel.DETAIL.level) {
            String repeat = S.repeat("+", getRepeatTimes(unzipTimes) * REPEAT_FACTOR);
            logs("{} 解压ID：[{}]，当前unzipTimes为：[{}]，正在向【{}】中添加文件【{}】…… <<{}>>", repeat, unzipId, unzipTimes, zipFileName, entryFileName, logSource);
        }
    }

    public static void printFilterLogs(String unzipId, int unzipTimes, String zipFileName, String entryFileName, ZipLogLevel zipLogLevel, String logSource) {
        if (zipLogLevel.level >= ZipLogLevel.DETAIL.level) {
            String repeat = S.repeat("#", getRepeatTimes(unzipTimes) * REPEAT_FACTOR);
            logs("{} 解压ID：[{}]，当前unzipTimes为：[{}]，不处理【{}】中【{}】的文件！ <<{}>>", repeat, unzipId, unzipTimes, zipFileName, entryFileName, logSource);
        }
    }

    public static void printDeleteActionLogs(String unzipId, int unzipTimes, String zipFileName, String entryFileName, ZipLogLevel zipLogLevel, String logSource) {
        if (zipLogLevel.level >= ZipLogLevel.ALL.level) {
            String repeat = S.repeat("=", getRepeatTimes(unzipTimes) * REPEAT_FACTOR);
            logs("{} 解压ID：[{}]，当前unzipTimes为：[{}]，删除前处理【{}】中的文件【{}】…… <<{}>>", repeat, unzipId, unzipTimes, zipFileName, entryFileName, logSource);
        }
    }

    public static void printBeforeAfter(String unzipId, int unzipTimes, String zipFileName, String entryFileName, ZipLogLevel zipLogLevel, String logSource, String extMsg) {
        if (zipLogLevel.level >= ZipLogLevel.ALL.level) {
            String repeat = S.repeat("&", getRepeatTimes(unzipTimes) * REPEAT_FACTOR);
            logs("{} 解压ID：[{}]，当前unzipTimes为：[{}]，【{}】中的压缩包【{}】解压缩【{}】处理！ <<{}>>", repeat, unzipId, unzipTimes, zipFileName, entryFileName, extMsg, logSource);
        }
    }

    public static void printSkipEntryLogs(String unzipId, int unzipTimes, String zipFileName, String entryFileName, ZipLogLevel zipLogLevel, String logSource) {
        if (zipLogLevel.level >= ZipLogLevel.BRIEF.level) {
            warnLogs("解压ID：[{}]，当前unzipTimes为：[{}]，包含【路径遍历风险】，【{}】中的文件【{}】被跳过！！！ <<{}>>", unzipId, unzipTimes, zipFileName, entryFileName, logSource);
        }
    }

    private static int getRepeatTimes(int unzipTimes) {
        return Math.max(MAX_REPEAT_TIMES - (unzipTimes - SuperACs.INIT_UNZIP_TIMES), MIN_REPEAT_TIMES);
    }

}

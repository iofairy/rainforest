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
package com.iofairy.rainforest.zip.config;

import com.iofairy.falcon.zip.ArchiveFormat;
import com.iofairy.rainforest.zip.ZipAdvanced;
import com.iofairy.rainforest.zip.attr.*;
import com.iofairy.top.G;

import java.util.*;

import static com.iofairy.falcon.zip.ArchiveFormat.*;

/**
 * 解压缩高级类 {@link ZipAdvanced} 的配置
 *
 * @since 0.0.3
 */
public class ZAConfig {
    /**
     * 解压缩高级类 {@link ZipAdvanced} 支持的解压缩格式
     */
    public static final Set<ArchiveFormat> SUPPORTED_ARCHIVE_FORMATS = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(ZIP, GZIP)));
    /**
     * 默认 {@link ZipAdvanced} 配置<br>
     * 注：{@code DEFAULT_ZACONFIG} 的初始化依赖 {@link #SUPPORTED_ARCHIVE_FORMATS}，
     * 因此 {@code DEFAULT_ZACONFIG} 一定要放在 {@link #SUPPORTED_ARCHIVE_FORMATS} 之后初始化
     */
    public static final ZAConfig DEFAULT_ZACONFIG = new ZAConfig();
    /**
     * 在 {@link ZipAdvanced} 需要解压的格式，在此格式以外，当作普通格式处理
     */
    private final Set<ArchiveFormat> needUnzipFormats = new HashSet<>();

    private final Map<ArchiveFormat, ArchiveInputProperty> inputPropertyMap = new HashMap<>();
    private final Map<ArchiveFormat, ArchiveOutputProperty> outputPropertyMap = new HashMap<>();

    private PasswordProvider passwordProvider = PasswordProvider.of();

    private ZAConfig() {
        this(null);
    }

    public ZAConfig(ArchiveFormat[] needUnZipFormatArray, ArchiveProperty... archiveProperties) {
        addToNeedUnZipFormats(needUnZipFormatArray);
        fillProperties();                       // 先填充必要的压缩包属性
        putPropertiesToMap(archiveProperties);  // 再根据外部提供的属性进行填充覆盖
    }

    public static ZAConfig of() {
        return new ZAConfig();
    }

    public static ZAConfig of(ArchiveFormat[] needUnZipFormats, ArchiveProperty... archiveProperties) {
        return new ZAConfig(needUnZipFormats, archiveProperties);
    }

    public static ZAConfig of(ArchiveFormat[] needUnZipFormats, PasswordProvider passwordProvider, ArchiveProperty... archiveProperties) {
        ZAConfig zaConfig = new ZAConfig(needUnZipFormats, archiveProperties);
        zaConfig.passwordProvider = passwordProvider == null ? PasswordProvider.of() : passwordProvider;
        return zaConfig;
    }

    private void fillProperties() {
        inputPropertyMap.put(ZIP, new ZipInputProperty());
        inputPropertyMap.put(GZIP, new GzipInputProperty());
        outputPropertyMap.put(ZIP, new ZipOutputProperty());
        outputPropertyMap.put(GZIP, new GzipOutputProperty());
    }

    private void putPropertiesToMap(ArchiveProperty... archiveProperties) {
        if (!G.isEmpty(archiveProperties)) {
            for (ArchiveProperty archiveProperty : archiveProperties) {
                if (archiveProperty != null) {
                    ArchiveFormat archiveFormat = getArchiveFormat(archiveProperty);
                    if (archiveFormat != null) {
                        if (archiveProperty instanceof ArchiveInputProperty) {
                            inputPropertyMap.put(archiveFormat, (ArchiveInputProperty) archiveProperty);
                        } else if (archiveProperty instanceof ArchiveOutputProperty) {
                            outputPropertyMap.put(archiveFormat, (ArchiveOutputProperty) archiveProperty);
                        }
                    }
                }
            }
        }
    }

    public ZAConfig putProperties(ArchiveProperty... archiveProperties) {
        putPropertiesToMap(archiveProperties);
        return this;
    }

    public ArchiveInputProperty getInputProperty(ArchiveFormat archiveFormat) {
        return inputPropertyMap.get(archiveFormat);
    }

    public ArchiveOutputProperty getOutputProperty(ArchiveFormat archiveFormat) {
        return outputPropertyMap.get(archiveFormat);
    }

    public Map<ArchiveFormat, ArchiveInputProperty> getInputPropertyMap() {
        return Collections.unmodifiableMap(inputPropertyMap);
    }

    public Map<ArchiveFormat, ArchiveOutputProperty> getOutputPropertyMap() {
        return Collections.unmodifiableMap(outputPropertyMap);
    }

    public PasswordProvider getPasswordProvider() {
        return passwordProvider;
    }

    public ZAConfig setPasswordProvider(PasswordProvider passwordProvider) {
        this.passwordProvider = passwordProvider == null ? PasswordProvider.of() : passwordProvider;
        return this;
    }

    /**
     * 判断解压缩高级类 {@link ZipAdvanced} 支持的类型
     *
     * @param archiveFormat archiveFormat
     * @return 如果支持，返回 {@code true}，否则 {@code false}
     */
    public static boolean isSupported(ArchiveFormat archiveFormat) {
        return SUPPORTED_ARCHIVE_FORMATS.contains(archiveFormat);
    }

    /**
     * 判断解压缩高级类 {@link ZipAdvanced} <b>当前需要</b>支持的类型 {@link #needUnzipFormats}
     *
     * @param archiveFormat archiveFormat
     * @return 如果支持，返回 {@code true}，否则 {@code false}
     */
    public boolean isSupportedFormat(ArchiveFormat archiveFormat) {
        return needUnzipFormats.contains(archiveFormat);
    }

    public ZAConfig setNeedUnZipFormats(ArchiveFormat... needUnZipFormatArray) {
        this.needUnzipFormats.clear();
        return addToNeedUnZipFormats(needUnZipFormatArray);
    }

    public ZAConfig addToNeedUnZipFormats(ArchiveFormat... needUnZipFormatArray) {
        if (needUnZipFormatArray == null) {
            this.needUnzipFormats.addAll(SUPPORTED_ARCHIVE_FORMATS);
        } else {
            for (ArchiveFormat needUnZipFormat : needUnZipFormatArray) {
                if (SUPPORTED_ARCHIVE_FORMATS.contains(needUnZipFormat)) {
                    this.needUnzipFormats.add(needUnZipFormat);
                }
            }
        }
        return this;
    }

    public Set<ArchiveFormat> getNeedUnZipFormats() {
        return Collections.unmodifiableSet(this.needUnzipFormats);
    }

    private static ArchiveFormat getArchiveFormat(ArchiveProperty property) {
        if (property instanceof ZipInputProperty || property instanceof ZipOutputProperty) return ZIP;
        if (property instanceof GzipInputProperty || property instanceof GzipOutputProperty) return GZIP;
        return null;
    }

    @Override
    public String toString() {
        return "ZAConfig{" +
                "needUnzipFormats=" + needUnzipFormats +
                ", inputPropertyMap=" + inputPropertyMap +
                ", outputPropertyMap=" + outputPropertyMap +
                '}';
    }
}

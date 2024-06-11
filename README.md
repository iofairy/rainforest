
# 📚rainforest
Rainforest, a library of Java utility classes for Java 8+

## 🛠️Environment（开发环境）
+ JDK 8
+ Apache maven 3.6.1


## 💿集成方式
### Maven
```xml
<dependency>
  <groupId>com.iofairy</groupId>
  <artifactId>rainforest</artifactId>
  <version>0.5.6</version>
</dependency>
```

### Gradle
```
implementation 'com.iofairy:rainforest:0.5.6'
```


## 🗺️使用指南（User Guide）
- [🔥SuperAC（压缩包处理）](#SuperAC压缩包处理)
  - [unzip（无限解压工具）](#unzip无限解压工具))
  - [unzipFast（快速无限解压工具）](#unzipFast快速无限解压工具)
  - [reZip（无限解压与修改压缩包内容重新压缩）](#reZip无限解压与修改压缩包内容重新压缩)
  - [支持的解压/压缩格式](#支持的解压与压缩格式)

## 🔥SuperAC（压缩包处理）
**SuperAC**是Super **Archiver** and **Compressor**的简称。可用于处理复杂的压缩包业务逻辑。  
+ 支持**无限解压**（即压缩包内嵌多层压缩包也能解压）
+ 支持**修改压缩包内（包括嵌套的压缩包）的任意内容并重新打包**成原来格式的压缩包
+ 提供强大的过滤器。**灵活控制**解压的**层级深度**；解压的**压缩包文件**；解压过程中**要修改的文件**

### 📘unzip（无限解压工具）

```java
/**
 * 解压并处理
 */
@Test
void testUnzip() {
    String zipFileName = "unzip.zip";
    File zipDir = new File(new File("src/test/resources"), "zip-files");
  
    try (FileInputStream is = new FileInputStream(new File(zipDir, zipFileName))) {
      List<Tuple2<String, Long>> result = SuperAC.unzip(
              is,
              ArchiveFormat.ZIP,                                  // 压缩包的类型
              null,                                               // 压缩包名称
              -1,                                                 // 解压几层，-1则无限解压
              (times, zipName, entryName) -> true,                // 压缩包内部的压缩包是否解压
              (times, zipName, entryName) -> entryName.endsWith(".csv"),    // 非压缩包中，只处理 .csv 的文件
              (times, zipName, entryName) -> false,
              null,
              (input, times, zipName, entryName) -> {             // 如何处理非压缩包
                // 这里必须复制一份，否则内部流会被关闭，导致异常
                MultiByteArrayInputStream inputStream = IOs.toMultiBAIS(input);
                return Tuple.of(zipName + "/" + entryName, unzipCountLine(inputStream, "UTF-8"));
              },
              ZipLogLevel.DETAIL,
              SuperACs.allSupportedSuperACs()
      );
      log.info("===============================================================================");
      System.out.println(result);
      log.info("===============================================================================");
  
    } catch (Exception e) {
      System.out.println(G.stackTrace(e));
    }
}

private static long unzipCountLine(InputStream inputStream, String charsetName) throws IOException {
    long rowCount = 0;
  
    /*
     * 必须自己关闭流！
     * 必须自己关闭流！
     * 必须自己关闭流！
     */
    try (InputStreamReader in = new InputStreamReader(inputStream, charsetName);
         BufferedReader br = new BufferedReader(in)) {
      String line;
      while ((line = br.readLine()) != null) {
        rowCount++;
      }
    }
    return rowCount;
}
```
### 📘unzipFast（快速无限解压工具）
```java
/**
 * 快速解压
 */
@Test
void testUnzipFast() {
    File zipDir = new File(new File("src/test/resources"), "zip-files");
    
    PasswordProvider provider = PasswordProvider.of(ZipPassword.of("*.7z", "fdskafj#$7zip"));
    Super7Zip super7Zip = Super7Zip.of().setUnzipPasswordProvider(provider);

    String zipFileName = "统计行数.7z.gz";

    try (FileInputStream is = new FileInputStream(new File(zipDir, zipFileName))) {
        List<SuperAC> superACs = SuperACs.allSupportedSuperACs();
        superACs.add(super7Zip);

        List<Tuple2<String, Long>> result = SuperAC.unzipFast(
                is,
                ArchiveFormat.GZIP,                                 // 压缩包的类型
                zipFileName,                                        // 压缩包名称
                -1,                                                 // 解压几层，-1则无限解压
                (times, zipName, entryName) -> true,                // 压缩包内部的压缩包是否解压
                (times, zipName, entryName) -> entryName.endsWith(".csv"),    // 非压缩包中，只处理 .csv 的文件
                (input, times, zipName, entryName, closeables) -> {     // 如何处理非压缩包
                    return Tuple.of(zipName + "/" + entryName, unzipFastCountLine(input, "UTF-8", closeables));
                },
                ZipLogLevel.DETAIL,
                superACs
        );

        log.info("===============================================================================");
        System.out.println(result);
        log.info("===============================================================================");

    } catch (Exception e) {
        System.out.println(G.stackTrace(e));
    }
}


private static long unzipFastCountLine(InputStream is, String charsetName, Set<AutoCloseable> closeables) throws Exception {
    long rowCount = 0;
  
    InputStreamReader in = null;
    BufferedReader br = null;
    try {
      in = new InputStreamReader(is, charsetName);
      br = new BufferedReader(in);
      while (br.readLine() != null) {
        rowCount++;
      }
      return rowCount;
    } finally {
      /*
       * 必须将要关闭的对象放入 closeables 集合中，交由 SuperAC.unzipFast 统一关闭！
       * 必须将要关闭的对象放入 closeables 集合中，交由 SuperAC.unzipFast 统一关闭！
       * 必须将要关闭的对象放入 closeables 集合中，交由 SuperAC.unzipFast 统一关闭！
       */
      if (closeables != null) {
        closeables.add(br);
        closeables.add(in);
      }
    }
}


```

### 📘reZip（无限解压与修改压缩包内容重新压缩）
```java

/**
 * 混合压缩包测试
 */
@Test
void testRezip() {
    File zipDir = new File(new File("src/test/resources"), "zip-files");
    String zipFileName = "tar（1）.tar.bz2";

    try (FileInputStream is = new FileInputStream(new File(zipDir, zipFileName))) {
        ZipResult<String> zipResult = SuperAC.reZip(
                is,
                ArchiveFormat.TAR_BZ2,
                null,
                -1,
                (times, zipName) -> true,
                (times, zipName, entryName) -> entryName.startsWith("要删除的" + times),
                null,
                (times, zipName, entryName) -> entryName.endsWith("txt"),
                null,
                (times, zipName, entryName) -> true,
                /*
                 设置要往压缩包中添加的文件，如果没有，直接设置为 null
                 */
                (times, zipName) -> {
                    List<File> files = Arrays.asList(new File(zipDir, "add-files/++新建 DOCX 文档 - 副本.docx"));
                    List<AddFile> addFiles = files.stream().map(e -> AddFile.of(e, "添加的文件/" + e.getName(), e.isDirectory())).collect(Collectors.toList());
                    addFiles.add(AddFile.of(null, "添加的目录/", true));

                    List<String> returnList = files.stream().map(e -> e.getName()).collect(Collectors.toList());
                    return Tuple.of(addFiles, returnList);
                },
                /*
                 设置要往压缩包中添加的字节，如果没有，直接设置为 null
                 */
                (times, zipName) -> {
                    ArrayList<AddBytes> addBytes = new ArrayList<>();
                    try (FileInputStream fis1 = new FileInputStream(new File(zipDir, "add-files/++新建 文本文档.txt"));
                         FileInputStream fis2 = new FileInputStream(new File(zipDir, "add-files/++新建 DOCX 文档.docx"));
                    ) {
                        byte[][] bytes1 = IOs.toMultiBAOS(fis1).toByteArrays();
                        byte[][] bytes2 = IOs.toMultiBAOS(fis2).toByteArrays();
                        AddBytes addBytes1 = AddBytes.of(bytes1, "添加字节/++新建 文本文档.txt", false);
                        AddBytes addBytes2 = AddBytes.of(bytes2, "添加字节/++新建 DOCX 文档.docx", false);
                        addBytes.add(addBytes1);
                        addBytes.add(addBytes2);
                    }

                    /*
                     是目录，带 /    --------- 这个被当作目录
                     */
                    AddBytes addBytes1 = AddBytes.of(null, "AddBytes添加的目录1/", true);
                    // 是目录，不带 /   --------- 这个被当作 0 字节的文件
                    AddBytes addBytes2 = AddBytes.of(null, "AddBytes添加的目录2", true);
                    /*
                     不是目录，带 /，字节为0      --------- 这个被当作目录
                     */
                    AddBytes addBytes3 = AddBytes.of(new byte[0][], "AddBytes添加的目录3/", false);
                    // 不是目录，不带 /，但字节为0      --------- 这个被当作 0 字节的文件
                    AddBytes addBytes4 = AddBytes.of(new byte[0][], "AddBytes添加的目录4", false);

                    addBytes.add(addBytes1);
                    addBytes.add(addBytes2);
                    addBytes.add(addBytes3);
                    addBytes.add(addBytes4);

                    List<String> returnList = addBytes.stream().map(e -> e.getEntryFileName()).collect(Collectors.toList());
                    return Tuple.of(addBytes, returnList);
                },
                null,
                null,
                null,
                (input, output, times, zipName, entryName) -> {
                    System.out.println("处理其他文件：" + zipName + "---" + entryName);

                    IOs.copy(input, output);
                    output.write(("\n" + DateTime.nowDate() + ">>>>>>>这是这是新增加的一行\n").getBytes());

                    return "其他文件处理：" + entryName;
                },
                ZipLogLevel.DETAIL,
                SuperACs.allSupportedSuperACs()
        );

        List<String> results = zipResult.getResults();
        log.info("===============================================================================");
        System.out.println(results);

        byte[][] bytes = zipResult.getBytes();
        String outputFilename = "重压缩输出_" + Numbers.randomInt(4) + ".tar.bz2";
        log.info("输出的文件名：{}", outputFilename);
        File dest = new File(rezipOutputDir, outputFilename);

        MultiByteArrayInputStream multiByteArrayInputStream = new MultiByteArrayInputStream(bytes);
        FileUtil.writeFromStream(multiByteArrayInputStream, dest, true);

        log.info("===============================================================================");
    } catch (Exception e) {
        System.out.println("\n" + G.stackTrace(e));
    }
}

```
### 📘支持的解压与压缩格式
**内置的解压/压缩格式**如下，也可以通过实现`com.iofairy.rainforest.zip.ac.SuperAC`接口支持更多**解压/压缩格式**：
```java
List<SuperAC> superACS = SuperACs.allSupportedSuperACs();
List<ArchiveFormat> formats = superACS.stream().map(e -> e.format()).collect(Collectors.toList());
System.out.println(formats);    // [SEVEN_ZIP, BZIP2, GZIP, TAR, TAR_BZ2, TAR_GZ, TAR_XZ, XZ, ZIP]
```

## ⭐点个赞哟
如果你喜欢 rainforest，感觉 rainforest 帮助到了你，可以点右上角 **Star** 支持一下哦，感谢感谢！

## Copyright

**Copyright (C) 2021 iofairy**, <https://github.com/iofairy/rainforest>

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.





package com.pch.office;
/**
 * @author pch
 * 填坑记录: 一直追求，用最简单、最易懂、最易扩展的方式进行开发
 * <p>
 * 整个工具开发很容易，中间使用了很多fork/join多线程框架、使用很多Files的api处理文件流,中间加了许多try-catch来捕获异常，输出更准确
 * 的log，但是丢失的代码的美观性
 * <p>
 * Excel处理API常用的有poi和jxl，但是jxl只能处理xsl格式，如果只处理xsl格式建议使用jxl，而poi能够对所有格式的
 * excel进行读操作，但是只能对xlsx、xsl格式的excel进行创建然后在写的操作，poi不能创建xlsm文件但是能重复写xlsm。相比较而
 * 言poi更强大一些。同时，poi对公式cell有比较好的支持。
 * <p>
 * 随着数据的增多，想进一步优化合表，是否可以采用多线程进行合表操作，答案是可以的，poi的源码中，可以知道Row的存储是TreeMap，
 * 并且cell的shareCell是hashMap,这样在做多线程时，需要加锁，大家都知道激烈的锁竞争和频繁的线程切换对性能是有影响的，而在
 * 现在的场景下就不能体现多线程的优势！所以没有采用多线程。同时大家都知道文件IO的写操作肯定是单线程的。
 * <p>
 * 随着使用poi的深入，发现poi好多的限制，蛋疼的要死，cellstype上限是64000，并且不支持自定义等等，不过poi的强大之处体现的
 * 淋漓尽致，能够操作到cell级别，这样就能按照类型设置cell，屌炸天。最操蛋的还是xlsm格式excel的支持，就是说poi对宏命令没有很好的支持
 * <p>
 * excel的好处每次保存都会更新文件及上层文件夹时间，这一点很好，动态检测可以直接监测文件夹即可，要不然就得自己维护文件变更时间戳，类似
 * 自己做一个VFS的简单实现，这一点excel跟vim一样，在某些情况下带来很多方便。
 * <p>
 * 有的时候就是想创建xlsm表怎么办呢？ 查了资料后，阅读poi的api后，发现不管采用model的方式还是什么样，都不算太给力。总结一下就是建好模板
 * 然后根据模板复制，觉得怎么着都很蛋疼。简单实现了一下，采用复制model的方式，并且对xlsm的写必须直接操作xlsm文件，否则，直接写到workbook
 * 中在向字节流里写，会造成文件打不开的问题！蛋疼的xlsm。(注意：一定要注意svn lock的问题，svn lock的文件，w操作时会报错，可以通过get lock
 * 处理此种问题，但是，还是尽量减少这种加锁带来的不必要的操作)
 * <p>
 * 蛋疼的cellstype设置：上面已经说到cellstype上限是64000,大家有时候想自己设置cell的cellstype，那就要注意了。大家都知道cellstype的
 * 设置会造成excel变大，同时设置过多的cellstype，有可能造成文件不能更改的问题。谨慎处理cellstype的设置问题。
 * <p>
 * poi中有两种方法获取列数：
 * getPhysicalNumberOfCells 是获取不为空的列个数。
 * getLastCellNum 是获取最后一个不为空的列是第几个。
 * 对于合表操作要区分key，所以强制要求必须定义name字段，不能为空，使用getPhysicalNumberOfCells获取列数；而对于无需合表的可以为空，
 * 使用getLastCellNum获取列数。
 * 获取行数也有类似的方法。
 * 但是需要注意的是getLastRow是从0开始的
 * <p>
 * excel转csv，遇到的第一个难题分隔符默认是逗号，而配置里好多逗号，咋整啊；可以包装一下每个cell的字符串，也可以采用其他分隔符。
 * 采用其他分隔符，如何再一次在配置中出现还得处理，所以采用第一种；乱码问题，导成csv后，excel打开会乱码，nodpad++没问题，蛋疼的要死，
 * 原来excel带bom，那就得手动加啊，本来以为opencsv能够更好地解决，然而并没多大鸟用，还是得自己处理。自力更生吧，不过对于读csv，
 * openvcsv确实方便很多，不过readALL返回的是LinkedList，不知道为啥用这个，生成时写了个bug造成生成时间很长，就是因为重复调用get造成的
 * ，屌丝的要死。有机会可以尝试一下apache commons csv！！！也许单纯在使用上更加ok！！！
 * <p>
 * csv的默认分隔符是逗号，因为配置里的字符串有可能出现逗号，所以数据会统一用双引号"*"包起来，如果内容中有双引号则需要替换成""*""包裹.
 * 然鹅，excel打开变动保存会干掉双引号就有可能引起问题。建议直接修改csv用nodpad++等修改。一定要注意csv使用excel打开，有可能显示有
 * 问题，尤其是货币类型时，一定要注意。
 * <p>
 * 问题：sqlite中没有\,csv中有\，又一个大坑
 * 原来：csv进行读取的时候会丢失\，但是你不能保证字符串中不出现\，例如：\n。所有在存入到csv文件时先进行转义即\n转义成\\n，并且
 * 一定要注意java中\\\\代表一个\, 即：str.replaceAll("\\\\", "\\\\\\\\")，这样在进行存储到数据库中不会丢失\
 * <p>
 * 无意间发现的一个难题，使用poi进行合表时，会造成cpu瞬间爆满，同时内存飙升：
 * 经查资料发现：
 * poi读取excel有两种模式：一是用户模式，这种方式同jxl的使用很类似，使用简单，都是将文件一次性读到内存，文件小的时候，没有什么问题，
 * 当文件大的时候，就有可能出现OOM的问题。第二种是事件驱动模式，其实就是用sax解析excel。拿Excel2007来说，其内容采用XML的格式来存储，
 * 所以处理excel就是解析XML，而sax读取excel时，并没有将整个文档读入内存，而是按顺序将整个文档解析完，在解析过程中，会主动产生事件交
 * 给程序中相应的处理函数来处理当前内容。因此这种方式对系统资源要求不高，可以处理海量数据。
 * 而对sax做的封装比较好的就是阿里的easyExcel，我是用了阿里的easyExcel做了一些改进，顺利解决了这个问题，但是easyExcel是异步的方式，
 * 感觉特符合java的事件通知的流派思想。其实在使用easyExcel过程中发现对于读取db写入excel过程中可以进行大批量写入，sql的快慢对性能的
 * 影响很大。
 * 实现思路：先读取Model到一个txt，再读取文件内容到txt，然后读取txt文件内容到excel，文件内容存储用json。（其实就是先读取到类似db的
 * 结构中，然后在读取到excel）,对于easyexcel来讲无需进行多线程操作，本身的计算已经是异步的了
 * <p>
 * 如何获取cell的颜色，目前感觉没有很好的办法
 * 设置背景色（两个方法需要一起使用）
 * style.setFillForegroundColor(IndexedColors.RED.getIndex());
 * style .setFillPattern(FillPatternType.SOLID_FOREGROUND);
 * 但是通过getFillForegroundColor等方法获取颜色index，获取不到，不是0就是64，poi的痛点
 * 但是可以用下面的方法：
 * var writeCell = writeRow.createCell(k);
 * var cellStyle = writeWorkbook.createCellStyle();
 * writeCell.setCellStyle(cellStyle);
 * var readStyle = readCell.getCellStyle();
 * writeCell.getCellStyle().cloneStyleFrom(rStyle);
 * 复制单元格颜色，64000不要忘记，蛋疼的限制
 * <p>
 * 数字类型包括日期类型和数值类型，当是数值类型时需要用到org.apache.poi.ss.util.NumberToTextConverter.toText进行转换，获得准确的
 * 值，因为poi读取出来的值有可能丢失精度
 * <p>
 */

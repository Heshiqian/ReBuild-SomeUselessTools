# SimpleFileDatabase 简单文件数据库

使用Java对象流实现的简单数据库存储，使用对象操作习惯，保存持久化写入到文件

*仅可存储本地简单数据，复杂数据还是考虑JDBC吧*

# Highlight

* 运行时支持同时打开多个库，多个库之间操作互不影响
* 操作时基于内存，存取速度快
* 在单表数据量1w-1.2w左右时，保存时的耗时可低于1s(取决于磁盘)
* 数据操作基于对象，不用写语句

# Bad

* 载入库时，吃内存较多
* 显式调用save方法保存时，若为多表可能存在文件需要扩容移动字节，耗时和内存占用可能激增
* 显式调用saveAll方法时，没有备份文件，强制停止或异常都将导致数据文件损坏 (Issue: 考虑修复)
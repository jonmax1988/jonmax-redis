# JonMax-Redis
Jon Max Redis

## 1 学习要求

1. 贴合实际
2. 与时俱进
3. 知识全面
4. 理论结合实战
5. 由浅入深
6. 通俗易懂

## 2 quick start

键值对型数据库 NoSQL  no only sql    no sql   MongoDB elasticSearch

key                   value

id    json{ data } 、list、map、set

1. Structured（结构化） Query language

2009 redis ANTIREZ

1. 键值型 、单线程、低延迟、速度快（基于内存 IO多路复用 良好的编码）
2. 支持数据持久化
3. 支持主从集群、分片集群
4. 支持多语言客户端

# 3 redis 命令

### 3-1 Redis数据结构介绍

Key 一般是String   value 多种多样

基础数据类型： String Hash List Set SortedSet 

特殊类型：GEO BitMap HyperLog

ttl key 查看key有效时间

expire key 设置Key 有效期

### 3-2 Redis通用命令

查看Redis版本

127.0.0.1:6379> INFO server  

### 3-4 String类型

最大空间不能超过512M

- SET 添加或修改
- GET 获取
- MSET 批量添加或修改
- MGET 批量获取
- INCR  increate 让一个整型key 自增 1
- INCRBY  按指定增长
- INCRBYFLOAT
- SETNX 不存在就新增
- SETEX  添加并设置有效期

### 3-5 Hash类型

key的命名规则例如 				项目名称：业务名：类型：id

例如：user id为1的相关的key ，cmp:user:1

​			product id为1的相关的key，cmp:product:1

> set cmp:user:1 '{"id":1,"name":"Jon Max",“age”:18}'
> OK
> set cmp:user:2 '{"id":2,"name":"Rose",“age”:19}'
> OK

![image-20251031223822157](C:\Users\hushu\AppData\Roaming\Typora\typora-user-images\image-20251031223822157.png)

Hash类型，也叫散列，其value是一个无序字典，类似与java的HashMap

### 3-6 List类型

大概

### 3-7 Set类型

大概

### 3-8 SortedSet类型

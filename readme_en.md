### 项目所属
``` text
FHM - 为了人类
Born to promote human development. 
We hope to use technology to change the future of humanity, 
thereby realize the value of FHM, 
as well as the value of each of us, so that life is no longer empty, 
and that each of us can leave a declaration of 'I have come' in this world.
```
### 项目背景
``` text
    在项目研发中，时常会遇到需要对象在VM中是单例，通常使用单例模式的方式处理，以求在VM中有高效的内存管理，但是这样对象数量变多就不能很好管理。
Spring-Framework使用IOC解决了此类问题，随着Spring-Framework迭代，在当下有三个问题需要解决：一、随着版本迭代，因为需要兼顾早期版本的需求，
致使目前Spring-Framework磁盘体积渝重、启动速度愈慢。在研发中小型的程序、插件时使用Spring-Framework就显得冗余；二、Spring-Framework实现复杂，
源码阅读困难在一些特殊场景，我们需要对IOC做自定义修改源码实现困难，比如现有需求：用户做分类注入的对象，根据注解的不同做对象的分类，再做不同管理。
三、针对配置文件属性注入繁琐。在此背景下，fhm-ioc诞生了。
（在个人看来Spring-Framework最大的优势是Spring-Boot的体系依赖于它。Spring-Boot成功的利用操作系统BIOS程序的思想创建了优秀、完善的框架体系。）
```
### 项目简介
```text
    FHM-IOC是轻量级的对象容器，他不但兼顾了Spring-Framework中IOC的功能、沿用其注解注入对象的方式、针对Map、抽象、接口特殊注入的方式、
生命周期方法等特性。具有对象初始化速度快、研发项目占用磁盘小、对象占用VM内存小的优秀特点。更重要的是其代码实现简单、阅读难度低、透明度高，
找寻项目BUG速度快、自定义IOC功能、代码逻辑安全方面都有很好的保障。另外还增加了多项特性。
```
### 项目特性
```text
    项目特性请阅读[quick-start_cn.md](quick-start_cn.md)
```



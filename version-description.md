# 版本说明

## 版本号规则

```text
 大版本迭代.中间数字表示bug修复.末尾数字表示功能添加
```

## 1.0.0

### 基础版本

## 1.0.1

### 添加获得bean方法

* 调用示例
```java
   @ScanPackageConfig("scan.package.name")
   public class DemoApplication {
       public static void main(String[] args) {
           Bootstrap.open(args, DemoStarter.class);
           // 获得bean，并调用测试方法
           Bootstrap.getBean(DemoStarter.class).test();
       }
   }
```  
* 日志输出
```text
   13:46:23.477 [main] INFO org.fhm.substrate.service.DemoStarter - demo starter test
```
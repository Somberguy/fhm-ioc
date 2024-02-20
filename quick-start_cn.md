# 功能特性
## Quick Start
详情请看[DemoApplication.java](src%2Ftest%2Fjava%2Forg%2Ffhm%2Fioc%2FDemoApplication.java)
### 示例：  
```java
    @ScanPackageConfig("scan/package/name")
    public class DemoApplication {
        public static void main(String[] args) {
            Bootstrap.open(args, DemoStarter.class);
        }
        
    }

    public class DemoStarter implements IStarter {
        @Override
        public List<Class<? extends Annotation>> newManageMembers() {
            return null;
        }
    
        @Override
        public void manageNotify(List<?> beans, Class<? extends Annotation> clazz) {
    
        }
    
        @Override
        public void start(String[] args) throws Exception {
    
        }
    
        @Override
        public void close() throws Exception {
    
        }
    }
```
### 说明：  

|         类         |       说明       |
|:-----------------:|:--------------:|
| `DemoApplication` |     程序启动类      |
|   `DemoStarter`   | `IStarter`接口实现 |  

|                                  方法                                   |               参数                |   返回值   | 异常 |
|:---------------------------------------------------------------------:|:-------------------------------:|:-------:|:--:|
|               `Bootstrap.open(args, DemoStarter.class)`               | 1. 程序入口参数；2. 自定义接口`IStarter`实现类 |    无    | 无  |
|        `List<Class<? extends Annotation>> newManageMembers()`         |                无                | 自定义注解集合 |    |
| `void manageNotify(List<?> beans, Class<? extends Annotation> clazz)` |                                 |         |    |
|             `void start(String[] args) throws Exception`              |                                 |         |    |
|                    `void close() throws Exception`                    |                                 |         |    |

|                    注解                     | 描述                                                                     | 值说明   |
|:-----------------------------------------:|------------------------------------------------------------------------|-------|
| `@ScanPackageConfig("scan/package/name")` | 配置注入对象包扫描范围，默认"org/fhm"，表示扫描org/fhm下的所有子包，如org/fhm、org/fhm/a、org/fhm/b | 包扫描路径 |

### 运行结果日记：  

15:16:05.871 [main] INFO org.fhm.ioc.manager.Bootstrap - read VM parameter  
15:16:06.095 [main] INFO org.fhm.ioc.manager.Bootstrap - start initial class and resource container  
15:16:06.124 [main] INFO org.fhm.ioc.manager.Bootstrap - start configure resource scanner  
15:16:06.143 [main] INFO org.fhm.ioc.manager.Bootstrap - start filter out the required resource path  
15:16:06.144 [main] INFO org.fhm.ioc.manager.Bootstrap - scan the path to obtain the required resources and class files  
15:16:06.149 [main] INFO org.fhm.ioc.service.ResourceScanner - start to obtain the class files of CP  
15:16:06.394 [main] INFO org.fhm.ioc.service.ResourceScanner - start to obtain the class files in nested packages  
15:16:06.433 [main] INFO org.fhm.ioc.manager.Bootstrap - start auto setup bean  
15:16:06.434 [main] INFO org.fhm.ioc.manager.Bootstrap - initial auto setup container  
15:16:06.434 [main] INFO org.fhm.ioc.manager.Bootstrap - auto setup obj  
15:16:06.447 [main] INFO org.fhm.ioc.manager.Bootstrap - auto setup map obj  
15:16:06.449 [main] INFO org.fhm.ioc.manager.Bootstrap - distribute bean  
15:16:06.453 [main] INFO org.fhm.ioc.manager.Bootstrap - start initial configuration  
15:16:06.456 [main] INFO org.fhm.ioc.manager.Bootstrap - start optimize bean  
15:16:06.457 [main] INFO org.fhm.ioc.manager.Bootstrap - clear not necessary implement and cache  
15:16:06.458 [main] INFO org.fhm.ioc.manager.Bootstrap - start bean initial  
15:16:06.484 [main] INFO org.fhm.ioc.Demo - `demo start initialize`  
15:16:06.485 [main] INFO org.fhm.ioc.manager.Bootstrap - start bean enable  
15:16:06.485 [main] INFO org.fhm.ioc.Demo - `demo start enable`  
15:16:06.485 [main] INFO org.fhm.ioc.manager.Bootstrap - clear cache data  
15:16:06.516 [main] INFO org.fhm.ioc.manager.Bootstrap - current maximum heap memory: 1820MB  
15:16:06.517 [main] INFO org.fhm.ioc.manager.Bootstrap - current cost memory: 2MB 480KB  
15:16:06.517 [main] INFO org.fhm.ioc.service.IOCCostTimer - enable project cost: 0s 650ms  
15:16:06.517 [main] INFO org.fhm.ioc.manager.Bootstrap - enable project complete  
15:16:06.523 [main] INFO org.fhm.ioc.Demo - `demo test successful`  
    
# 功能特性

## Quick Start

_详情请阅览_[DemoApplication.java](src%2Ftest%2Fjava%2Forg%2Ffhm%2Fioc%2FDemoApplication.java)

### 示例：

**程序启动类**

```java
    @ScanPackageConfig("scan/package/name")
    public class DemoApplication {
        public static void main(String[] args) {
            Bootstrap.open(args, DemoStarter.class);
        }
        
    }
```  

**`IStarter`接口实现**

```java
    @Component // Inject into the IOC
    public class DemoStarter implements IStarter {
    
        @Setup("Demo") // Load from the IOC
        private Demo demo;
    
        @Override
        public List<Class<? extends Annotation>> newManageMembers() {
            return Collections.singletonList(DemoComponent.class); // Returns a collection of annotations for custom injection containers
        }
    
        @Override
        public void manageNotify(List<?> beans, Class<? extends Annotation> clazz) {
            if (DemoComponent.class.isAssignableFrom(clazz)) { // Determines whether the bean is marked by the DemoComponent annotation
                // Beans marked with DemoComponent annotations are treated independently
            }
        }
    
        @Override
        public void start(String[] args) throws Exception {
            demo.test(); // Runs test method of the demo
        }
    
        @Override
        public void close() throws Exception {
            // Runs before the IOC ends
        }
    
    }
```  

**自定义注入IOC注解标记**

```java
    @Component // Specify a custom annotation
    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface DemoComponent {
    
        String value();
    
    }
```

**自定义注解注入bean**

```java
    @DemoComponent("Demo")
    public class Demo {
    
        private final ILoggerHandler logger = LoggerHandler.getLogger(Demo.class);
    
        public void test() {
            logger.info("demo test successful");
        }
    
    
        @BeanInitial
        private void beanInitial() throws Exception{
            // The bean to do initial
            logger.info("demo start initialize");
        }
    
        @BeanEnable
        private void beanEnable() throws Exception{
            // The bean to do enable
            logger.info("demo start enable");
        }
    
    }
```

### 说明：

|         类         |                说明                 | 类型  |
|:-----------------:|:---------------------------------:|:---:|
| `DemoApplication` |               程序启动类               | 普通类 |
|   `DemoStarter`   |          `IStarter`接口实现           | 普通类 |  
|  `DemoComponent`  | 自定义注入`IOC`注解标记，**需要添加@Component** | 注解  |
|      `Demo`       |           `自定义注解注入bean`           | 普通类 |

|                                  方法                                   |                 参数                 |   返回值   |    异常    |                说明                |
|:---------------------------------------------------------------------:|:----------------------------------:|:-------:|:--------:|:--------------------------------:|
|               `Bootstrap.open(args, DemoStarter.class)`               |  1. 程序入口参数；2. 自定义接口`IStarter`实现类   |    无    |    无     |            `IOC`启动方法             |
|        `List<Class<? extends Annotation>> newManageMembers()`         |                 无                  | 自定义注解集合 |  自定义处理   |          添加自定义注入`IOC`注解          |
| `void manageNotify(List<?> beans, Class<? extends Annotation> clazz)` | 1. 被参数2自定义注入注解管理bean集合； 2. 自定义注入注解 |    无    | 自定义处理或抛出 |       对参数2标记的对象集合做用户自定义处理        |
|             `void start(String[] args) throws Exception`              |             1. 程序入口参数              |    无    | 自定义处理或抛出 |             开始执行用户程序             |
|                    `void close() throws Exception`                    |             `IOC`关闭回调              |    无    | 自定义处理或抛出 |            `IOC`关闭回调             |
|                         `void beanInitial()`                          |                 无                  |    无    | 自定义处理或抛出 | bean经过装载后初始化，**生命周期方法禁止有参数和返回值** |
|                          `void beanEnable()`                          |                 无                  |    无    | 自定义处理或抛出 | bean经过初始化后启动，**生命周期方法禁止有参数和返回值** |

|                    注解                     |                                   描述                                   |      值说明      |
|:-----------------------------------------:|:----------------------------------------------------------------------:|:-------------:|
| `@ScanPackageConfig("scan/package/name")` | 配置注入对象包扫描范围，默认"org/fhm"，表示扫描org/fhm下的所有子包，如org/fhm、org/fhm/a、org/fhm/b |     包扫描路径     |
|               `@Component`                |                              `IOC`默认注入注解                               |   bean注入名称    |
|             `@Setup("Demo")`              |                                bean装载注解                                | 需要装载的bean注入名称 |
|              `@BeanInitial`               |                              标记bean初始化方法                               |       无       |
|               `@BeanEnable`               |                               标记bean启动方法                               |       无       |

### 运行结果日记：

        ███████ ██   ██ ███    ███       ██  ██████   ██████
        ██      ██   ██ ████  ████       ██ ██    ██ ██
        █████   ███████ ██ ████ ██ █████ ██ ██    ██ ██
        ██      ██   ██ ██  ██  ██       ██ ██    ██ ██
        ██      ██   ██ ██      ██       ██  ██████   ██████
        ============================version 1.0.0 release===

15:13:45.494 [main] INFO org.fhm.ioc.manager.Bootstrap - read VM parameter
16:15:52.158 [main] INFO org.fhm.ioc.manager.Bootstrap - start initial class and resource container  
16:15:52.173 [main] INFO org.fhm.ioc.manager.Bootstrap - start configure resource scanner  
16:15:52.213 [main] INFO org.fhm.ioc.manager.Bootstrap - start filter out the required resource path  
16:15:52.216 [main] INFO org.fhm.ioc.manager.Bootstrap - scan the path to obtain the required resources and class
files  
16:15:52.238 [main] INFO org.fhm.ioc.service.ResourceScanner - start to obtain the class files of CP  
16:15:52.518 [main] INFO org.fhm.ioc.service.ResourceScanner - start to obtain the class files in nested packages  
16:15:52.519 [main] INFO org.fhm.ioc.manager.Bootstrap - start auto setup bean  
16:15:52.519 [main] INFO org.fhm.ioc.manager.Bootstrap - initial auto setup container  
16:15:52.519 [main] INFO org.fhm.ioc.manager.Bootstrap - auto setup obj  
16:15:52.531 [main] INFO org.fhm.ioc.manager.Bootstrap - auto setup map obj  
16:15:52.532 [main] INFO org.fhm.ioc.manager.Bootstrap - distribute bean  
16:15:52.535 [main] INFO org.fhm.ioc.manager.Bootstrap - start initial configuration  
16:15:52.599 [main] WARN org.fhm.ioc.config.AbstractConfiguration - the default configuration file demo.properties was
not scanned  
16:15:52.616 [main] INFO org.fhm.ioc.manager.Bootstrap - start optimize bean  
16:15:52.616 [main] INFO org.fhm.ioc.manager.Bootstrap - clear not necessary implement and cache  
16:15:52.617 [main] INFO org.fhm.ioc.manager.Bootstrap - start bean initial  
16:15:52.621 [main] INFO org.fhm.ioc.bean.Demo - `demo start initialize`  // bean初始化打印  
16:15:52.622 [main] INFO org.fhm.ioc.bean.Demo - `desc: hello,reality, lucky number: 66`  // 配置文件信息  
16:15:52.624 [main] INFO org.fhm.ioc.manager.Bootstrap - start bean enable  
16:15:52.626 [main] INFO org.fhm.ioc.bean.Demo - `demo start enable`  // bean启动打印  
16:15:52.626 [main] INFO org.fhm.ioc.bean.Demo - `desc: hello,reality, lucky number: 66` // 配置文件信息  
16:15:52.626 [main] INFO org.fhm.ioc.manager.Bootstrap - clear cache data  
16:15:52.645 [main] INFO org.fhm.ioc.manager.Bootstrap - current maximum heap memory: 1820MB  
16:15:52.646 [main] INFO org.fhm.ioc.manager.Bootstrap - current cost memory: 2MB 534KB  
16:15:52.646 [main] INFO org.fhm.ioc.service.IOCCostTimer - enable project cost: 0s 689ms  
16:15:52.646 [main] INFO org.fhm.ioc.manager.Bootstrap - enable project complete  
16:15:52.652 [main] INFO org.fhm.ioc.bean.Demo - `demo test successful`  // 调用bean测试方法  
16:15:52.652 [main] INFO org.fhm.ioc.bean.Demo - `desc: hello,reality, lucky number: 66`  // 配置文件信息

## 配置管理

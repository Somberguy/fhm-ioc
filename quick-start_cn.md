# 功能特性

## _使用须知注意事项：_

1. 被注入`bean`必须含有无参构造器，即被@Component、@Configuration或者自定义注入注解标记的类必须含有无参构造器。

## Quick Start

_详情请阅览_[DemoApplication.java](src%2Ftest%2Fjava%2Forg%2Ffhm%2Fioc%2FDemoApplication.java)

### 示例：

#### ***程序启动类***

```java

@ScanPackageConfig("scan.package.name")
public class DemoApplication {
    public static void main(String[] args) {
        Bootstrap.open(args, DemoStarter.class);
    }

}
```  

#### ***`IStarter`接口实现***

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

#### ***自定义注入`IOC`注解标记***

```java
    @Component // Specify a custom annotation
    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface DemoComponent {
    
        String value();
    
    }
```

#### ***自定义注解注入`bean`***

```java

@DemoComponent("Demo")
public class Demo {

    private final ILoggerHandler logger = LoggerHandler.getLogger(Demo.class);

    public void test() {
        logger.info("demo test successful");
    }


    @BeanInitial
    private void beanInitial() throws Exception {
        // The bean to do initial
        logger.info("demo start initialize");
    }

    @BeanEnable
    private void beanEnable() throws Exception {
        // The bean to do enable
        logger.info("demo start enable");
    }

}
```

### 示例说明：
#### ***类***

|         类         |                说明                 | 类型  |
|:-----------------:|:---------------------------------:|:---:|
| `DemoApplication` |               程序启动类               | 普通类 |
|   `DemoStarter`   |          `IStarter`接口实现           | 普通类 |  
|  `DemoComponent`  | 自定义注入`IOC`注解标记，**需要添加@Component** | 注解  |
|      `Demo`       |           `自定义注解注入bean`           | 普通类 |

#### ***方法***

|                                  方法                                   |                  参数                  |   返回值   |  异常   |           说明            |
|:---------------------------------------------------------------------:|:------------------------------------:|:-------:|:-----:|:-----------------------:|
|               `Bootstrap.open(args, DemoStarter.class)`               |   1. 程序入口参数；2. 自定义接口`IStarter`实现类    |    无    |   无   |        `IOC`启动方法        |
|        `List<Class<? extends Annotation>> newManageMembers()`         |                  无                   | 自定义注解集合 |  处理   |     添加自定义注入`IOC`注解      |
| `void manageNotify(List<?> beans, Class<? extends Annotation> clazz)` | 1. 被自定义注入注解管理`bean`集合； 2. 自定义注入注解类对象 |    无    | 处理或抛出 | 对参数`beans`标记的对象集合做自定义处理 |
|             `void start(String[] args) throws Exception`              |              1. 程序入口参数               |    无    | 处理或抛出 |        开始执行用户程序         |
|                    `void close() throws Exception`                    |              `IOC`关闭回调               |    无    | 处理或抛出 |        `IOC`关闭回调        |
|                         `void beanInitial()`                          |                  无                   |    无    | 处理或抛出 |      [生命周期](#生命周期)      |
|                          `void beanEnable()`                          |                  无                   |    无    | 处理或抛出 |      [生命周期](#生命周期)      |

#### ***注解***

|                    注解                     |         描述          |      值说明      |
|:-----------------------------------------:|:-------------------:|:-------------:|
| `@ScanPackageConfig("scan.package.name")` | [扫描包匹配规则](#扫描包匹配规则) |     包扫描路径     |
|               `@Component`                |     `IOC`默认注入注解     |   bean注入名称    |
|             `@Setup("Demo")`              |      bean装载注解       | 需要装载的bean注入名称 |
|              `@BeanInitial`               |     标记bean初始化方法     |       无       |
|               `@BeanEnable`               |     标记bean启动方法      |       无       |

### 关键点详解：

#### ***扫描包匹配规则***
1. 注解`@ScanPackageConfig`必须在程序入口方法对应的类上。
2. 注解`@ScanPackageConfig`的值必须至少包含被扫描包第一级目录。  
   如`scan.package.name`必须含有`scan`。

#### ***生命周期***


### 运行结果日记：

        ███████ ██   ██ ███    ███       ██  ██████   ██████
        ██      ██   ██ ████  ████       ██ ██    ██ ██
        █████   ███████ ██ ████ ██ █████ ██ ██    ██ ██
        ██      ██   ██ ██  ██  ██       ██ ██    ██ ██
        ██      ██   ██ ██      ██       ██  ██████   ██████
        ============================version 1.0.0 release===
11:16:14.856 [main] INFO org.fhm.ioc.manager.Bootstrap - read VM parameter  
11:16:14.864 [main] INFO org.fhm.ioc.manager.Bootstrap - start collect configuration file and class file  
11:16:14.869 [main] INFO org.fhm.ioc.manager.Bootstrap - start initialize resource scanner  
11:16:14.870 [main] INFO org.fhm.ioc.service.ResourceScanner - start configure resource scanner  
11:16:14.889 [main] INFO org.fhm.ioc.manager.Bootstrap - start filter out the required CP  
11:16:14.889 [main] INFO org.fhm.ioc.manager.Bootstrap - start fixed-point scanning  
11:16:14.890 [main] INFO org.fhm.ioc.manager.Bootstrap - start scan the path to obtain the required resources and class files  
11:16:15.025 [main] INFO org.fhm.ioc.manager.Bootstrap - start clear cache and create beans  
11:16:15.033 [main] INFO org.fhm.ioc.manager.Bootstrap - start auto setup bean  
11:16:15.033 [main] INFO org.fhm.ioc.manager.Bootstrap - initial auto setup container  
11:16:15.033 [main] INFO org.fhm.ioc.manager.Bootstrap - auto setup obj  
11:16:15.046 [main] INFO org.fhm.ioc.manager.Bootstrap - auto setup map obj  
11:16:15.052 [main] INFO org.fhm.ioc.manager.Bootstrap - distribute bean  
11:16:15.054 [main] INFO org.fhm.ioc.manager.Bootstrap - start initial configuration  
11:16:15.063 [main] INFO org.fhm.ioc.manager.Bootstrap - start optimize bean  
11:16:15.063 [main] INFO org.fhm.ioc.manager.Bootstrap - clear not necessary implement and cache  
11:16:15.070 [main] INFO org.fhm.ioc.manager.Bootstrap - start bean initial  
11:16:15.073 [main] INFO org.fhm.ioc.bean.Demo - `demo start initialize` // bean初始化调用  
11:16:15.073 [main] INFO org.fhm.ioc.bean.Demo - `desc: hello,reality, lucky number: 66` // bean初始化调用获取配置文件信息  
11:16:15.076 [main] INFO org.fhm.ioc.manager.Bootstrap - start bean enable  
11:16:15.076 [main] INFO org.fhm.ioc.bean.Demo - `demo start enable` // bean启动调用  
11:16:15.076 [main] INFO org.fhm.ioc.bean.Demo - `desc: hello,reality, lucky number: 66` // bean启动调用获取配置文件信息  
11:16:15.076 [main] INFO org.fhm.ioc.manager.Bootstrap - clear cache data  
11:16:15.083 [main] INFO org.fhm.ioc.manager.Bootstrap - current the number of available processors : 16  
11:16:15.083 [main] INFO org.fhm.ioc.manager.Bootstrap - current maximum heap memory: 3890MB  
11:16:15.083 [main] INFO org.fhm.ioc.manager.Bootstrap - current cost memory: 2MB 945KB  
11:16:15.083 [main] INFO org.fhm.ioc.service.IOCCostTimer - enable project cost: 0s 232ms  
11:16:15.083 [main] INFO org.fhm.ioc.manager.Bootstrap - enable project complete  
11:16:15.085 [main] INFO org.fhm.ioc.bean.Demo - `demo test successful`  // 调用bean测试方法  以下为接口方式装载调用  
11:16:15.085 [main] INFO org.fhm.ioc.bean.Demo - `desc: hello,reality, lucky number: 66`  // 配置文件信息  
11:16:15.085 [main] INFO org.fhm.ioc.bean.DemoAttach - `demoAttach demo test successful`  // 调用bean测试方法  
11:16:15.085 [main] INFO org.fhm.ioc.bean.Demo - `demo test successful`  // 调用bean测试方法  以下为Map方式装载调用  
11:16:15.085 [main] INFO org.fhm.ioc.bean.Demo - `desc: hello,reality, lucky number: 66`  // 配置文件信息  
11:16:15.085 [main] INFO org.fhm.ioc.bean.DemoAttach - `demoAttach demo test successful`  // 调用bean测试方法  

## 配置管理

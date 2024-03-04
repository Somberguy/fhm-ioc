# 功能特性

## 配置管理

### 注意：

* 目前只支持`.properties`格式文件
* 文件默认存放位置为src/main/java/resources/，可以通过`VM`参数`fhm.substrate.config.file.path`自行设置，[VM参数说明](#VM参数说明)

### 示例：

#### ***自定义配置抽象类***
```java
   public abstract class AbstractDemoConfiguration extends AbstractConfiguration {
       protected AbstractDemoConfiguration() {
           super("demo.properties"); 
           // Invoked the parent class constructor to 
           // set the member attributes of the current configuration object with 
           // the name of the configuration file it resides in.
       }
   }
```

#### ***自定义配置类***

```java
   @Configuration("test.demo") 
   // The annotation of mark configuration object, value is the prefix of configuration properties names
   public class TestDemoConfiguration extends AbstractDemoConfiguration {
   
   
       @Value("desc")
       // Members annotated with this indicate that 
       // they have a mapping in the corresponding configuration file 
       // for the current configuration object and require assignment,
       // value is the names of configuration properties.
       private String desc;
   
       @Value("lucky.number")
       private Integer luckyNumber;
   
       @Value("bean.name")
       private String beanName;
   
   
       public String getDesc() {
           return desc;
       }
   
       public Integer getLuckyNumber() {
           return luckyNumber;
       }
   
       public String getBeanName() {
           return beanName;
       }
   }
```

#### ***配置对象使用样例类***

```java
   @DemoComponent("Demo")
   public class Demo implements IDemoTest {
   
       private final ILogger logger = LoggerHandler.getLogger(Demo.class);
   
       @Setup
       private TestDemoConfiguration testDemoConfiguration;
   
       @Override
       public void test() {
           logger.info("demo test successful");
           logger.info("desc: {}, lucky number: {}", testDemoConfiguration.getDesc(), testDemoConfiguration.getLuckyNumber());
       }
   
   
       @BeanInitial
       private void beanInitial() throws Exception {
           // The bean to do initial
           logger.info("demo start initialize");
           logger.info("desc: {}, lucky number: {}", testDemoConfiguration.getDesc(), testDemoConfiguration.getLuckyNumber());
       }
   
       @BeanEnable
       private void beanEnable() throws Exception {
           // The bean to do enable
           logger.info("demo start enable");
           logger.info("desc: {}, lucky number: {}", testDemoConfiguration.getDesc(), testDemoConfiguration.getLuckyNumber());
       }
   
   }
```

### 说明：

#### ***类***

|                        类                         |                    说明                    | 类型  |
|:------------------------------------------------:|:----------------------------------------:|:---:|
| `org.fhm.substrate.config.AbstractConfiguration` |            [配置对象注入](#配置对象注入)             | 抽象类 |
|           `AbstractDemoConfiguration`            |     所有继承此类配置`bean`，其属性值都集中在此类配置的文件中      | 抽象类 |
|             `TestDemoConfiguration`              | 配置`bean`，[配置文件扫描和属性赋值机制](#配置文件扫描和属性赋值机制) | 普通类 |
|                      `Demo`                      |                 `bean`对象                 | 普通类 |

#### ***注解***

|            注解            |     描述     |         值说明         |
|:------------------------:|:----------:|:-------------------:|
|         `@Setup`         | 装载配置`bean` |         不使用         |
| `@Value("lucky.number")` | 标记当前属性需要赋值 | [属性名映射机制](#属性名映射机制) |

### 关键点详解：

#### ***配置对象注入***

* 配置`bean`都需要继承类`org.fhm.substrate.config.AbstractConfiguration`，并调用其构造方法，设置自定义配置文件名称。
* 配置`bean`需要被注解`@Configuration`标记，以被`IOC`扫描，其注解值为配置文件属性名前缀。

#### ***配置文件扫描和属性赋值机制***

* 假设`XxxConfiguration`已经完成了上述注入操作，其设置的配置文件名为`xxx.properties`： 
  1. 当`IOC`未扫描到`xxx.properties`文件，其会去读取`default-xxx.properties`文件并属性映射赋值，如果存在的话。
  2. 当`IOC`扫描到`xxx.properties`文件，但是在赋值配置`bean`时候，其某个属性在`xxx.properties`文件中不存在，`IOC`就会去读取
  `default-xxx.properties`文件中对应属性值并取其赋值，如果存在的话。

#### ***属性名映射机制***

* 假设`XxxConfiguration`已经完成了上述注入操作，标记注解`@Configuration`值设置为`xx.xx`
    1. 配置属性名为拼接得来： 
       即在配置`bean``XxxConfiguration`中需要赋值成员`luckyNumber`，其`@Value`的值为`lucky.number`，那么在配置文件对应属性名为`xx.xx.lucky.number`

## 日志管理

### 示例：

#### ***导入相关字节码***

```java
   import org.fhm.substrate.service.LoggerHandler;
   import org.fhm.substrate.standard.ILogger;
```

#### ***声明日记对象***

```java
    private final ILogger logger = LoggerHandler.getLogger(Demo.class);
```

### 说明：

#### ***类***

|        类        | 描述      | 使用                     |
|:---------------:|---------|------------------------|
| `LoggerHandler` | 获取日志对象类 | 调用`getLogger`方法，参数为类对象 |

#### ***接口***

|    接口     |  描述  |                  说明                  |         使用          |
|:---------:|:----:|:------------------------------------:|:-------------------:|
| `ILogger` | 日记接口 | 封装日记打印方法，解耦项目和日志框架，方便自定义日志功能或者更换日志框架 | [自定义日志框架](#自定义日志框架) |

### 关键点详解：

#### ***自定义日志框架***

1. 创建自定义日志类，实现`ILogger`接口，并重写方法。
2. 修改[LoggerHandler.java](src%2Fmain%2Fjava%2Forg%2Ffhm%2Fsubstrate%2Fservice%2FLoggerHandler.java)中`initializeLoggerHandler`方法，将创建自定义日志对象的`Function`(参数为类对象，返回自定义日志对象)赋值给`create`变量

## `IOC`仓库规划

### 示例：

#### ***`IStarter`接口实现***

```java
  @Component // Inject into the IOC
  public class DemoStarter implements IStarter {
  
      @Setup // Load from the IOC 
      private DemoAttach attach;
      
      @Setup("Demo") 
      // Load from the IOC by interface or abstract-class.
      // Multiple implementations need to be annotated with values that 
      // correspond to the injection names of their respective implementation objects.
      private IDemoTest demo;
  
      @Setup("->test.demo.bean.name") 
      // Specifies that the reference of the test.demo.bean.name
      // attribute in the configuration
      // file is the name of the loading object
      private IDemoTest demoAttach;
  
      @Setup // Mapping loads bean mechanisms
      private Map<String, IDemoTest> iDemoTestMap;
  
      @Override
      public List<Class<? extends Annotation>> newManageMembers() {
          ArrayList<Class<? extends Annotation>> classes = new ArrayList<>();
          classes.add(DemoComponent.class);
          classes.add(DemoTestComponent.class);
          return classes; // Returns a collection of annotations for custom injection containers
      }
  
      @Override
      public void manageNotify(List<?> beans, Class<? extends Annotation> clazz) {
          if (DemoComponent.class.isAssignableFrom(clazz)) { // Determines whether the bean is marked by the DemoComponent annotation
              // Beans marked with DemoComponent annotations are treated independently
          }
      }
  
      @Override
      public void start(String[] args) throws Exception {
          demo.test(); // Runs test method of the Demo
          demoAttach.test(); // Runs test method of the DemoAttach
          iDemoTestMap.forEach((k, v) -> v.test());
      }
  
      @Override
      public void close() throws Exception {
          // Runs before the IOC ends
      }
  
  }
```

#### ***自定义注入注解***

```java
    @Component // Specify a custom annotation
    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface DemoComponent {
    
        String value();
    
    }
```

#### ***自定义注入注解注入`IOC`***

```java
  @DemoComponent("Demo")
  public class Demo {
  
      private final ILogger logger = LoggerHandler.getLogger(Demo.class);
  
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

### 说明：

#### ***类***

|         类         |              说明              | 类型  |
|:-----------------:|:----------------------------:|:---:|
|  `DemoComponent`  | 自定义注入注解，**需要添加`@Component`** | 注解  |
|      `Demo`       |        自定义注入注解注入`IOC`        | 普通类 |

#### ***方法***

|                                  方法                                   |                  参数                  |   返回值   |  异常   |       说明        |
|:---------------------------------------------------------------------:|:------------------------------------:|:-------:|:-----:|:---------------:|
|        `List<Class<? extends Annotation>> newManageMembers()`         |                  无                   | 自定义注解集合 |  处理   | [自定义规划](#自定义规划) |
| `void manageNotify(List<?> beans, Class<? extends Annotation> clazz)` | 1. 被自定义注入注解管理`bean`集合； 2. 自定义注入注解类对象 |    无    | 处理或抛出 | [自定义规划](#自定义规划) |

### 关键点详解：

#### ***自定义规划***

* 通过`newManageMembers`方法返回自定义注入注解集合，在所有`bean`启动（即`IOC`调用`BeanEnable`方法）后，`IOC`会回调`manageNotify`方法，执行用户  
  自定义对不同标记注解的规划。
* 假设`newManageMembers`返回注解集合有`@UtilComponet`、`@BusinessComponet`：
    1. `IOC`会将`@UtilComponet`标记的`bean`集合作为参数，调用`manageNotify`方法，该方法另一个参数表示`@UtilComponet`类，用于区分`bean`集合被哪个注解标记，`@BusinessComponet`同理。
  
## 定点扫描注入

### 注意：

* 目前只支持扫描`jar`包。
* 由于被扫描对象无法在编译期间直接使用，因此需要配合***`IOC`仓库规划***中描述的`IStarter`接口实现方法`manageNotify`使用。

### 示例：

```shell
  java -jar $程序名称 -Dfhm.substrate.registry.bean.dir.path=$bean_path -Dfhm.substrate.registry.package.name=$package_name
```

### 说明：

#### ***参数说明***

* 程序名称：指用户打包用户程序。
* bean_path：指用户需要定点扫描的目录。
* package_name：指用户在`bean_path`下需要扫描的包名称。

### ***使用须知***

* `IOC`会注入扫描到的被自定义注入注解标记的类。

## 其他VM参数说明

### 示例：

#### 启动命令配置入参
```shell
  java -jar $程序名称 -Dfhm.substrate.vm.options.file.path=.
```

#### 启动命令配置入参

```shell
  java -jar $程序名称 -Dfhm.substrate.config.file.path=$config_path
```

### 说明：

#### ***VM参数说明***

* `fhm.substrate.vm.options.file.path`：
  1. 设置`substrate.vmoptions`文件所在目录。
  2. 用户创建`substrate.vmoptions`文件，写入`fhm.substrate`的相关`VM`参数，`IOC`会读取。
  3. `substrate.vmoptions`文件优先级高于命令方式设置。
  4. `.`表示目录设置为和用户程序同级。

* `fhm.substrate.config.file.path`：
  1. 设置注入的配置`bean`的配置文件所在目录，**需要绝对路径**。
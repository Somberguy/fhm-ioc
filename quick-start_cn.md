## IOC配置
### 启动：
示例如下：
```java
    @ScanPackageConfig("your/package/name")
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
示例说明：
```text
    DemoApplication类为主类启动类。MAIN方法中Bootstrap的open方法为启动方法，参数分别为
```
    
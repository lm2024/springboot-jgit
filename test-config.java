// 测试配置读取的简单代码
@SpringBootApplication
public class ConfigTest {
    
    @Autowired
    private ServicesConfig servicesConfig;
    
    @PostConstruct
    public void testConfig() {
        System.out.println("ServicesConfig: " + servicesConfig);
        System.out.println("Services count: " + (servicesConfig.getServices() != null ? servicesConfig.getServices().size() : 0));
        if (servicesConfig.getServices() != null && !servicesConfig.getServices().isEmpty()) {
            System.out.println("First service: " + servicesConfig.getFirstService());
        }
    }
}

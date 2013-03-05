import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

/**
 * Created with IntelliJ IDEA.
 * User: huixiao200068
 * Date: 13-3-5
 * Time: 上午10:38
 * To change this template use File | Settings | File Templates.
 */
public class TestGuavaCache {

    private final static String file = SystemParam.properties.getProperty("server_resources") + "/conf.properties";

    LoadingCache<String, Properties> cache = CacheBuilder.newBuilder().maximumSize(1000).
            refreshAfterWrite(5000, TimeUnit.MILLISECONDS).build(
            new CacheLoader<String, Properties>() {
                @Override
                public Properties load(String s) throws Exception {
                    return loadConf();
                }
            }
    );

    public Properties loadConf() {
        Properties prop = new Properties();
        try{
            prop.load(new BufferedInputStream(new FileInputStream(file)));
            System.out.println("has loaded conf file");
        }catch (FileNotFoundException e1) {
            e1.printStackTrace();
        }catch (Exception e) {
            e.printStackTrace();
        }
        return prop;
    }

    static class SystemParam {
        private final static Properties properties;

        static {
            properties = System.getProperties();
        }
    }

    public static void main(String[] args) throws Exception {
//        System.out.println(SystemParam.properties.getProperty("server_resources"));
//        for(String key : SystemParam.properties.stringPropertyNames()){
//            System.out.println(key);
//        }
        TestGuavaCache guavaCache = new TestGuavaCache();
        while(true) {
            System.out.println(guavaCache.cache.get("userinfo"));
            Thread.sleep(1000);
        }
    }
}

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Random;
import java.util.logging.Logger;

/**
 * Created with IntelliJ IDEA.
 * User: huixiao200068
 * Date: 13-3-4
 * Time: 下午3:53
 * To change this template use File | Settings | File Templates.
 */
public class TestRestLi {

    public static void main(String[] args) {
        for(int i=0; i<5000; i++) {
            new Thread(new RestLiClient()).start();
        }
    }

    static class RestLiClient implements Runnable {
        private static Logger logger = Logger.getLogger(RestLiClient.class.getName());
        @Override
        public void run() {
            try{
                Random random = new Random();
//                String url = "http://10.13.81.93:8090/server/fortunes/" + random.nextInt(10);
                String url = "http://localhost:8080/users/" + random.nextInt(10);
                logger.info(url);

                HttpClient httpClient = new DefaultHttpClient();
                HttpGet httpget = new HttpGet(url);
                HttpResponse response = httpClient.execute(httpget);
                HttpEntity entity = response.getEntity();

                if (entity != null) {
                    InputStream instream = entity.getContent();
                    try {
                         BufferedReader reader = new BufferedReader(new InputStreamReader(instream));
                         System.out.println(reader.readLine());
                     } catch (IOException ex) {
                       throw ex;
                     } catch (RuntimeException ex) {
                         httpget.abort();
                         throw ex;
                     } finally {
                         instream.close();
                     }
                }
                 httpClient.getConnectionManager().shutdown();
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }
}

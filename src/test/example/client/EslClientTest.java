package example.client;

import com.freeswitch.esl.client.EslClient;
import com.freeswitch.esl.client.handler.EslConnectionFailure;

import java.util.concurrent.TimeUnit;

/**
 * Created by zhouhl on 2017/2/23.
 */
public class EslClientTest {

    public static void main(String[] args) throws EslConnectionFailure, InterruptedException {
        EslClient eslClient = new EslClient();

        eslClient.connect("172.16.74.13", 8021, "ClueCon", 3000);
//        eslClient.connect("192.168.254.212", 8021,"ClueCon", 3000);
        eslClient.setEventSubscriptions("plain", "all");
        eslClient.addEventListener(new EslHandler());


        TimeUnit.SECONDS.sleep(5);
        System.out.println("=======EslClient======stop================");
        eslClient.stop();
        System.out.println("=======EslClient======stopped================");

    }
}

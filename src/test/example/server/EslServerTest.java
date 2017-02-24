package example.server;

import com.freeswitch.esl.server.EslServer;

/**
 * Created by zhouhl on 2017/2/24.
 */
public class EslServerTest {
    public static void main(String[] args) {
        EslServer eslServer = new EslServer("eslServer",9000,new SimpleHangupPipelineFactory());
        eslServer.start();


        EslServer eslServer2 = new EslServer("eslServer2",9001,new SimpleHangupPipelineFactory());
        eslServer2.start();
    }
}

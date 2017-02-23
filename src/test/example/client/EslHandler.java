package example.client;

import com.freeswitch.esl.client.IEslEventListener;
import com.freeswitch.esl.transport.event.EslEvent;

/**
 * Created by zhouhl on 2017/2/23.
 */
public class EslHandler implements IEslEventListener {

    @Override
    public void eventReceived(EslEvent event) {
//        System.out.println(event);
    }

    @Override
    public void backgroundJobResultReceived(EslEvent event) {
//        System.out.println(event);
    }
}

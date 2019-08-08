package example.initializer;

import example.config.DistributedStateMachineClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;

@Component("Startup")
public class StartUp {
    @Value("${appId}")
    private String appId;

    @PostConstruct
    public void initial() {

        List<String> test = new ArrayList<>();
        test.add("app1");
        test.add("app2");
        test.add("app3");
        DistributedStateMachineClient sca = new DistributedStateMachineClient("localhost:2181,localhost:2182,localhost:2183",
                appId, /////!!!!!
                "app",
                "/app",
                "/appleader",
                1000,
                5,
                test);
    }
}

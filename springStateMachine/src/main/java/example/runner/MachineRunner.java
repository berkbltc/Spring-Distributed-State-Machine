package example.runner;

import example.entity.TransitionContext;
import example.enums.OrderEvents;
import example.enums.OrderStates;
import example.repository.ExampleRepository;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.statemachine.support.DefaultStateMachineContext;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.util.HashMap;

@Slf4j
@Component
public class MachineRunner {

    HashMap<String, StateMachine<OrderStates, OrderEvents>> stateMachinesHashMap = new HashMap<>();

    @Autowired
    public ExampleRepository exampleRepository;

    @Autowired
    private StateMachineFactory<OrderStates, OrderEvents> factory;

    private Logger log = LoggerFactory.getLogger(MachineRunner.class);

    @Value("${appId}")
    public String appId;

    public MachineRunner() {
    }

    public MachineRunner(StateMachineFactory<OrderStates, OrderEvents> aFactory) {
        this.factory = aFactory;
    }

    public void provision(String machineId) {
        try {
            if ((stateMachinesHashMap.get(machineId) == null)) { // if machine is not working
                StateMachine<OrderStates, OrderEvents> machine;
                machine = this.factory.getStateMachine(machineId);
                machine.getExtendedState().getVariables().putIfAbsent("machineId", machineId);

                if ((getLatestStatus(machine) != null)) { // if there is a context for machine in DB
                    System.out.println("Latest state of machine:" + machineId + " is taking from DB");
                    OrderStates latestState = getLatestStatus(machine);
                    machine = setStateMachine(machine, latestState);
                }
                machine.start();
                if (stateMachinesHashMap.get(machineId) == null) {
                    stateMachinesHashMap.put(machine.getId(), machine);
                }
                log.info("<<<Machine {} current State: {} >>", machine.getId(), machine.getState().getId().name());
            }

        } catch (Exception e) {
            System.out.println(e);
        }

    }

    public void sendEventAndUpdateDBonStateChange(StateMachine<OrderStates, OrderEvents> machine, OrderEvents event) {
        OrderStates previousState = machine.getState().getId();
        machine.sendEvent(event);
        Timestamp time = new Timestamp(System.currentTimeMillis());
        OrderStates currentState = machine.getState().getId();
        if (!((currentState.name()).equals(previousState.name()))) {    //If State changed
            TransitionContext transitionContext = new TransitionContext(machine.getId(), currentState, previousState, event, time, appId);
            exampleRepository.save(transitionContext);      //write latest state to DB
            System.out.print("MachineId: " + machine.getId() + " saved to DB!");

        }
    }

    public OrderStates getLatestStatus(StateMachine<OrderStates, OrderEvents> machine) {

        if (exampleRepository.existsById(machine.getId())) {   // make StateMachine continue from latest state
            TransitionContext tContext = exampleRepository.findByStateMachineID(machine.getId());
            OrderStates latestState = OrderStates.valueOf(tContext.getCurrentState());
            return latestState;
        }
        return null;

    }

    public StateMachine<OrderStates, OrderEvents> setStateMachine(StateMachine<OrderStates, OrderEvents> machine, OrderStates latestState) {
        machine.getStateMachineAccessor()
                .doWithAllRegions(access -> {
                    access.resetStateMachine(new DefaultStateMachineContext<>
                            (latestState, null, null, null, null, machine.getId()));
                });
        return machine;
    }

    public void deleteFromDBIfTerminated(StateMachine<OrderStates, OrderEvents> machine) {
        OrderStates currentState = machine.getState().getId();
        if ((currentState == OrderStates.FULFILLED) || (currentState == OrderStates.CANCELLED)) {
            exampleRepository.deleteById(machine.getId());
            log.info("machine {} deleted from DB as it is terminated", machine.getId());
            stateMachinesHashMap.remove(machine.getId());
        }
    }

    public StateMachine<OrderStates, OrderEvents> getMachine(String id) {
        try {
            return stateMachinesHashMap.get(id);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

}
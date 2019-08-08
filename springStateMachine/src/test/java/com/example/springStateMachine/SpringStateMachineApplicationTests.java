//package com.example.springStateMachine;
//
//import example.OrderEvents;
//import example.OrderStates;
//import org.assertj.core.api.Assertions;
//import org.junit.Test;
//import org.junit.runner.RunWith;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.statemachine.StateMachine;
//import org.springframework.statemachine.StateMachineContext;
//import org.springframework.statemachine.StateMachinePersist;
//import org.springframework.statemachine.config.StateMachineFactory;
//import org.springframework.statemachine.persist.DefaultStateMachinePersister;
//import org.springframework.statemachine.persist.StateMachinePersister;
//import org.springframework.test.context.junit4.SpringRunner;
//
//import java.util.HashMap;
//import java.util.UUID;
//
//@RunWith(SpringRunner.class)
//@SpringBootTest
//public class SpringStateMachineApplicationTests {
//
//    private StateMachineFactory<OrderStates, OrderEvents> stateMachineFactory;
//
//    class InMemoryPersist
//            implements StateMachinePersist<OrderStates, OrderEvents, UUID> {
//
//        private HashMap<UUID, StateMachineContext<OrderStates, OrderEvents>> storage
//                = new HashMap<>();
//
//        @Override
//        public void write(StateMachineContext<OrderStates, OrderEvents> stateMachineContext, UUID uuid) throws Exception {
//            storage.put(uuid, stateMachineContext);
//        }
//
//        @Override
//        public StateMachineContext<OrderStates, OrderEvents> read(UUID uuid) throws Exception {
//            return storage.get(uuid);
//        }
//    }
//
//    @Test
//    public void testPersist() throws Exception {
//
//        StateMachinePersister<OrderStates, OrderEvents, UUID> persister =
//                new DefaultStateMachinePersister<>(new InMemoryPersist());
//
//        StateMachine<OrderStates, OrderEvents> firstMachine = stateMachineFactory.getStateMachine();
//        StateMachine<OrderStates, OrderEvents> secondMachine = stateMachineFactory.getStateMachine();
//
//        firstMachine.sendEvent(OrderEvents.PAY);
//        firstMachine.sendEvent(OrderEvents.FULFILL);
//
//        persister.persist(firstMachine, firstMachine.getUuid());
//        persister.persist(secondMachine, secondMachine.getUuid());
//        persister.restore(secondMachine, firstMachine.getUuid());
//
//        Assertions.assertThat(secondMachine.getState().getId())
//                .isEqualTo(OrderStates.FULFILLED);
//
//    }
//
//}

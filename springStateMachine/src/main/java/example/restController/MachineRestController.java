package example.restController;

import example.enums.OrderEvents;
import example.enums.OrderStates;
import example.repository.ExampleRepository;
import example.runner.MachineRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.statemachine.StateMachine;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/events")
@RestController
public class MachineRestController {

    @Autowired
    MachineRunner machineRunner;

    private final ExampleRepository repository;

    public MachineRestController(ExampleRepository repository) {
        this.repository = repository;
    }

    @GetMapping("/{id}/{eventName}")
    public ResponseEntity sendEventToMachine(@PathVariable String id, @PathVariable String eventName) {
        try {
            StateMachine<OrderStates, OrderEvents> machine = machineRunner.getMachine(id);
            OrderStates previousState = machine.getState().getId();
            machineRunner.sendEventAndUpdateDBonStateChange(machine, OrderEvents.valueOf(eventName));
            machineRunner.deleteFromDBIfTerminated(machine);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity provisionMachine(@PathVariable String id) {
        try {
            machineRunner.provision(id);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}

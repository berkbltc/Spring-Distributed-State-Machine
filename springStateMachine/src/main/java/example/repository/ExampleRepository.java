package example.repository;

import example.entity.TransitionContext;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface ExampleRepository extends CrudRepository<TransitionContext, String> {
    TransitionContext findByStateMachineID(String stateMachineID);

    List<TransitionContext> findAllByAppId(String appId);

    List<TransitionContext> findAll();

}

package example.entity;

import example.enums.OrderEvents;
import example.enums.OrderStates;
import org.springframework.beans.factory.annotation.Value;

import javax.persistence.Id;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.sql.Timestamp;
import java.util.UUID;


@Entity
@Table(name = "transitions_table")
public class TransitionContext {


    @Id
    @Column(name = "smID")
    private String stateMachineID;
    @Column(name = "Current_State")
    private String currentState;
    @Column(name = "Previous_State")
    private String previousState;
    @Column(name = "Event_Occured")
    private String eventOccured;
    @Column(name = "Date")
    private Timestamp date;
    @Column(name = "app_id")
    private String appId;

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public String getStateMachineID() {
        return stateMachineID;
    }

    public String getCurrentState() {
        return currentState;
    }

    public String getPreviousState() {
        return previousState;
    }

    public String getEventOccured() {
        return eventOccured;
    }

    public Timestamp getDate() {
        return date;
    }

    public TransitionContext() {
    }

    public TransitionContext(String stateMachineID, OrderStates currentState, OrderStates previousState, OrderEvents event, Timestamp date, String appID) {
        this.stateMachineID = stateMachineID;
        this.currentState = currentState.name();
        this.previousState = previousState.name();
        eventOccured = event.name();
        this.date = date;
        this.appId = appID;
    }

}

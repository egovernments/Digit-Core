# Autocomplete Parallel Workflows Consumer

The **Autocomplete Parallel Workflows Consumer** is a Kafka-based service that listens to `save-wf-transitions` Kafka topic, which receives events triggered when a workflow state transition occurs (via the `/_transition` endpoint). The service's main responsibility is to **automatically transition or terminate parallel workflows** based on certain configuration.

### DB UML Diagram
- NA

### Service Dependencies
- egov-workflow-v2

## Service Details

### Functionalities

When a `ProcessInstanceRequest` is received:

- The `AutocompleteConsumer` checks if the action in the request matches any action specified in a configured **autocomplete action map**.
- If there's a match, it delegates processing to `AutocompleteService`.
- The service fetches the workflow configuration (`BusinessService`) using `WorkflowUtil`.
- From this configuration, it extracts a list of parallel workflows that may need to be acted upon.
- For each parallel workflow:
  - It checks the current state of the workflow.
  - If the workflow exists and is not in a terminal state, it prepares a transition using a predefined action from the config and invokes the workflow transition API to terminate it.

Finally, after processing all applicable parallel workflows, the message is acknowledged, completing the operation.

  | Environment Variables                | Description                                                                                                                                                   |
  |--------------------------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------|
  | `autocomplete.parent.wf.action.map`  | A map where each key is a terminate action from a parent workflow, and the value is the corresponding terminate action to be used for its parallel workflows. |

### Configuration Details

- In parallel workflows, every state—except the start and terminal states—should define terminate actions that correspond to the terminate actions configured in the parent workflow.

### Kafka Consumers

- Following are the Consumer topic.
    - **save-wf-transitions** :- Receive ProcessInstanceRequest (triggered by /_transition endpoint)

### Kafka Producers

- NA

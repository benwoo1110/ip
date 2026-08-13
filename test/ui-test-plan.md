# Kachow UI Test Plan

## Test setup

- Runtime: Java 25.
- Compile before testing with `javac -d _temp/ui-test-classes src/main/java/Deadline.java src/main/java/Event.java src/main/java/Kachow.java src/main/java/Task.java src/main/java/Todo.java`.
- Run every case from the repository root in a fresh JVM. Kachow stores tasks only in memory, so cases do not share state.
- Compare combined console output exactly after normalizing CRLF line endings to LF.

## Test Case: UI-01 Start and exit cleanly

### Aim

Verify that Kachow displays its welcome banner and exits with the documented farewell when the user enters `bye`.

### Command

```json
["java", "-cp", "_temp/ui-test-classes", "Kachow"]
```

### Inputs

```text
bye
```

### Expected output

```text
    ____________________________________________________________
     _  __          _                    
    | |/ /__ _  ___| |__   _____      __
    | ' // _` |/ __| '_ \ / _ \ \ /\ / /
    | . \ (_| | (__| | | | (_) \ V  V / 
    |_|\_\__,_|\___|_| |_|\___/ \_/\_/  
    Ka-chow! I'm Kachow, the fastest chatbot on the track.
    What can I do for you before the next lap?
    ____________________________________________________________
    ____________________________________________________________
    Race complete! Catch you on the next lap. Ka-chow!
    ____________________________________________________________
```

## Test Case: UI-02 Manage todos, deadlines, and events

### Aim

Verify that all three task types can be added and listed, and that marking and unmarking a task updates its displayed status without changing the other tasks.

### Command

```json
["java", "-cp", "_temp/ui-test-classes", "Kachow"]
```

### Inputs

```text
todo read book
deadline return book /by Sunday
event project meeting /from Mon 2pm /to 4pm
list
mark 2
list
unmark 2
list
bye
```

### Expected output

```text
    ____________________________________________________________
     _  __          _                    
    | |/ /__ _  ___| |__   _____      __
    | ' // _` |/ __| '_ \ / _ \ \ /\ / /
    | . \ (_| | (__| | | | (_) \ V  V / 
    |_|\_\__,_|\___|_| |_|\___/ \_/\_/  
    Ka-chow! I'm Kachow, the fastest chatbot on the track.
    What can I do for you before the next lap?
    ____________________________________________________________
    ____________________________________________________________
    Ka-chow! A new racer joined the starting grid:
      [T][ ] read book
    Now you've got 1 racer ready to roll.
    ____________________________________________________________
    ____________________________________________________________
    Ka-chow! A new racer joined the starting grid:
      [D][ ] return book (by: Sunday)
    Now you've got 2 racers ready to roll.
    ____________________________________________________________
    ____________________________________________________________
    Ka-chow! A new racer joined the starting grid:
      [E][ ] project meeting (from: Mon 2pm to: 4pm)
    Now you've got 3 racers ready to roll.
    ____________________________________________________________
    ____________________________________________________________
    Rev up! Here are the tasks in today's race:
    1.[T][ ] read book
    2.[D][ ] return book (by: Sunday)
    3.[E][ ] project meeting (from: Mon 2pm to: 4pm)
    ____________________________________________________________
    ____________________________________________________________
    Ka-chow! This task crossed the finish line:
      [D][X] return book (by: Sunday)
    ____________________________________________________________
    ____________________________________________________________
    Rev up! Here are the tasks in today's race:
    1.[T][ ] read book
    2.[D][X] return book (by: Sunday)
    3.[E][ ] project meeting (from: Mon 2pm to: 4pm)
    ____________________________________________________________
    ____________________________________________________________
    Back to the starting grid! This task is not done yet:
      [D][ ] return book (by: Sunday)
    ____________________________________________________________
    ____________________________________________________________
    Rev up! Here are the tasks in today's race:
    1.[T][ ] read book
    2.[D][ ] return book (by: Sunday)
    3.[E][ ] project meeting (from: Mon 2pm to: 4pm)
    ____________________________________________________________
    ____________________________________________________________
    Race complete! Catch you on the next lap. Ka-chow!
    ____________________________________________________________
```

## Test Case: UI-03 Reject malformed and unknown commands

### Aim

Verify that missing task details and unknown instructions produce guidance while leaving the application running for the next command.

### Command

```json
["java", "-cp", "_temp/ui-test-classes", "Kachow"]
```

### Inputs

```text
todo
deadline return book
event project meeting /from Mon 2pm
dance
bye
```

### Expected output

```text
    ____________________________________________________________
     _  __          _                    
    | |/ /__ _  ___| |__   _____      __
    | ' // _` |/ __| '_ \ / _ \ \ /\ / /
    | . \ (_| | (__| | | | (_) \ V  V / 
    |_|\_\__,_|\___|_| |_|\___/ \_/\_/  
    Ka-chow! I'm Kachow, the fastest chatbot on the track.
    What can I do for you before the next lap?
    ____________________________________________________________
    ____________________________________________________________
    This racer needs a name. Use: todo DESCRIPTION
    ____________________________________________________________
    ____________________________________________________________
    That deadline missed its checkpoint. Use: deadline DESCRIPTION /by DATE_OR_TIME
    ____________________________________________________________
    ____________________________________________________________
    This event needs a full race route. Use: event DESCRIPTION /from START /to END
    ____________________________________________________________
    ____________________________________________________________
    That command took a wrong turn. Try todo, deadline, event, list, mark, or unmark.
    ____________________________________________________________
    ____________________________________________________________
    Race complete! Catch you on the next lap. Ka-chow!
    ____________________________________________________________
```

## Test Case: UI-04 Validate task numbers

### Aim

Verify that nonnumeric and out-of-range task numbers are rejected, while valid mark and unmark commands still update the selected task.

### Command

```json
["java", "-cp", "_temp/ui-test-classes", "Kachow"]
```

### Inputs

```text
mark 1
todo pit stop
mark two
mark 2
mark 1
unmark 0
unmark 1
bye
```

### Expected output

```text
    ____________________________________________________________
     _  __          _                    
    | |/ /__ _  ___| |__   _____      __
    | ' // _` |/ __| '_ \ / _ \ \ /\ / /
    | . \ (_| | (__| | | | (_) \ V  V / 
    |_|\_\__,_|\___|_| |_|\___/ \_/\_/  
    Ka-chow! I'm Kachow, the fastest chatbot on the track.
    What can I do for you before the next lap?
    ____________________________________________________________
    ____________________________________________________________
    That task number isn't in the race. Check the list and try again.
    ____________________________________________________________
    ____________________________________________________________
    Ka-chow! A new racer joined the starting grid:
      [T][ ] pit stop
    Now you've got 1 racer ready to roll.
    ____________________________________________________________
    ____________________________________________________________
    I need a valid task number to make that pit stop.
    ____________________________________________________________
    ____________________________________________________________
    That task number isn't in the race. Check the list and try again.
    ____________________________________________________________
    ____________________________________________________________
    Ka-chow! This task crossed the finish line:
      [T][X] pit stop
    ____________________________________________________________
    ____________________________________________________________
    That task number isn't in the race. Check the list and try again.
    ____________________________________________________________
    ____________________________________________________________
    Back to the starting grid! This task is not done yet:
      [T][ ] pit stop
    ____________________________________________________________
    ____________________________________________________________
    Race complete! Catch you on the next lap. Ka-chow!
    ____________________________________________________________
```

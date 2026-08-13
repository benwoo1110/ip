# Kachow UI Test Plan

## Test setup

- Runtime: Java 25.
- Compile before testing with `javac -d _temp/ui-test-classes src/main/java/Deadline.java src/main/java/Event.java src/main/java/Kachow.java src/main/java/KachowException.java src/main/java/Task.java src/main/java/Todo.java`.
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

Verify that missing task details and unknown instructions produce specific, themed guidance while leaving the application running for the next command.

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
    Pit stop! This racer needs a name. Use: todo DESCRIPTION
    ____________________________________________________________
    ____________________________________________________________
    Pit stop! That deadline is missing its /by checkpoint. Use: deadline DESCRIPTION /by DATE_OR_TIME
    ____________________________________________________________
    ____________________________________________________________
    Pit stop! That event is missing its /to finish line. Use: event DESCRIPTION /from START /to END
    ____________________________________________________________
    ____________________________________________________________
    Pit stop! That command took a wrong turn. Try todo, deadline, event, list, mark, unmark, or bye.
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
    Pit stop! Racer 1 isn't on the grid. Use list to check the task numbers.
    ____________________________________________________________
    ____________________________________________________________
    Ka-chow! A new racer joined the starting grid:
      [T][ ] pit stop
    Now you've got 1 racer ready to roll.
    ____________________________________________________________
    ____________________________________________________________
    Pit stop! That racer number isn't a whole positive number. Use: mark TASK_NUMBER
    ____________________________________________________________
    ____________________________________________________________
    Pit stop! Racer 2 isn't on the grid. Use list to check the task numbers.
    ____________________________________________________________
    ____________________________________________________________
    Ka-chow! This task crossed the finish line:
      [T][X] pit stop
    ____________________________________________________________
    ____________________________________________________________
    Pit stop! That racer number isn't a whole positive number. Use: unmark TASK_NUMBER
    ____________________________________________________________
    ____________________________________________________________
    Back to the starting grid! This task is not done yet:
      [T][ ] pit stop
    ____________________________________________________________
    ____________________________________________________________
    Race complete! Catch you on the next lap. Ka-chow!
    ____________________________________________________________
```

## Test Case: UI-05 Reject empty commands and unexpected arguments

### Aim

Verify that an empty command and extra arguments for argument-free commands receive specific correction guidance, and that listing an empty task collection remains safe.

### Command

```json
["java", "-cp", "_temp/ui-test-classes", "Kachow"]
```

### Inputs

```text

list
list turbo
bye now
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
    Pit stop! That command stalled on the starting line. Enter a command to keep racing.
    ____________________________________________________________
    ____________________________________________________________
    The starting grid is empty. Add a racer with todo, deadline, or event.
    ____________________________________________________________
    ____________________________________________________________
    Pit stop! The list command has extra cargo. Use: list
    ____________________________________________________________
    ____________________________________________________________
    Pit stop! The bye command has extra cargo. Use: bye
    ____________________________________________________________
    ____________________________________________________________
    Race complete! Catch you on the next lap. Ka-chow!
    ____________________________________________________________
```

## Test Case: UI-06 Explain every malformed command component

### Aim

Verify that each missing, duplicated, misplaced, or invalid command component receives guidance that identifies the exact correction needed.

### Command

```json
["java", "-cp", "_temp/ui-test-classes", "Kachow"]
```

### Inputs

```text
deadline
deadline /by Sunday
deadline service /by
deadline service /by Friday /by Monday
event
event /from Mon /to Tue
event meeting /to Tue /from Mon
event meeting /from Mon
event meeting /from /to Tue
event meeting /from Mon /to
event meeting /from Mon /to Tue /to Wed
mark
unmark 999999999999999999999
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
    Pit stop! This deadline racer needs a task description. Use: deadline DESCRIPTION /by DATE_OR_TIME
    ____________________________________________________________
    ____________________________________________________________
    Pit stop! This deadline racer needs a task description. Use: deadline DESCRIPTION /by DATE_OR_TIME
    ____________________________________________________________
    ____________________________________________________________
    Pit stop! That deadline needs a date or time after /by. Use: deadline DESCRIPTION /by DATE_OR_TIME
    ____________________________________________________________
    ____________________________________________________________
    Pit stop! That deadline has too many /by checkpoints. Use exactly one: deadline DESCRIPTION /by DATE_OR_TIME
    ____________________________________________________________
    ____________________________________________________________
    Pit stop! This event racer needs a description. Use: event DESCRIPTION /from START /to END
    ____________________________________________________________
    ____________________________________________________________
    Pit stop! This event racer needs a description. Use: event DESCRIPTION /from START /to END
    ____________________________________________________________
    ____________________________________________________________
    Pit stop! That event's /from must come before /to. Use: event DESCRIPTION /from START /to END
    ____________________________________________________________
    ____________________________________________________________
    Pit stop! That event is missing its /to finish line. Use: event DESCRIPTION /from START /to END
    ____________________________________________________________
    ____________________________________________________________
    Pit stop! That event needs a start after /from. Use: event DESCRIPTION /from START /to END
    ____________________________________________________________
    ____________________________________________________________
    Pit stop! That event needs an end after /to. Use: event DESCRIPTION /from START /to END
    ____________________________________________________________
    ____________________________________________________________
    Pit stop! That event has extra route markers. Use one /from and one /to: event DESCRIPTION /from START /to END
    ____________________________________________________________
    ____________________________________________________________
    Pit stop! Tell me which racer to mark. Use: mark TASK_NUMBER
    ____________________________________________________________
    ____________________________________________________________
    Pit stop! That racer number isn't a whole positive number. Use: unmark TASK_NUMBER
    ____________________________________________________________
    ____________________________________________________________
    Race complete! Catch you on the next lap. Ka-chow!
    ____________________________________________________________
```

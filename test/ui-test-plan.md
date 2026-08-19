# Kachow UI Test Plan

## Test setup

- Runtime: Java 25.
- Compile before testing with `javac -d _temp/ui-test-classes src/main/java/Command.java src/main/java/Deadline.java src/main/java/Event.java src/main/java/Kachow.java src/main/java/KachowException.java src/main/java/Storage.java src/main/java/Task.java src/main/java/Todo.java`.
- Make the test launcher executable with `chmod +x test/run-kachow-isolated.sh`.
- Run every case from the repository root in a fresh JVM. The launcher uses a fresh working directory for each case, so generated task data cannot leak between cases. A case may instead name a read-only fixture directory when it needs predefined stored data.
- Compare combined console output exactly after normalizing CRLF line endings to LF.

## Test Case: UI-01 Start and exit cleanly

### Aim

Verify that Kachow displays its welcome banner and exits with the documented farewell when the user enters `bye`.

### Command

```json
["test/run-kachow-isolated.sh"]
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

## Test Case: UI-10 Load persisted tasks on startup

### Aim

Verify that Kachow restores every task type and its completion status from `data/kachow.txt` when a new chatbot process starts.

### Command

```json
["test/run-kachow-isolated.sh", "test/fixtures/persisted-tasks"]
```

### Inputs

```text
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
    Rev up! Here are the tasks in today's race:
    1.[T][X] read book
    2.[D][ ] return book (by: June 6th)
    3.[E][ ] project meeting (from: Aug 6th 2pm to: 4pm)
    4.[T][X] join sports club
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
["test/run-kachow-isolated.sh"]
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
["test/run-kachow-isolated.sh"]
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
    Pit stop! That command took a wrong turn. Try todo, deadline, event, list, mark, unmark, delete, or bye.
    ____________________________________________________________
    ____________________________________________________________
    Race complete! Catch you on the next lap. Ka-chow!
    ____________________________________________________________
```

## Test Case: UI-07 Delete a task and renumber the list

### Aim

Verify that deleting a task shows the removed task and updated task count, and that the remaining tasks are renumbered without otherwise changing their details or completion statuses.

### Command

```json
["test/run-kachow-isolated.sh"]
```

### Inputs

```text
todo read book
deadline return book /by June 6th
event project meeting /from Aug 6th 2pm /to 4pm
todo join sports club
todo borrow book
mark 1
mark 2
mark 4
list
delete 3
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
      [D][ ] return book (by: June 6th)
    Now you've got 2 racers ready to roll.
    ____________________________________________________________
    ____________________________________________________________
    Ka-chow! A new racer joined the starting grid:
      [E][ ] project meeting (from: Aug 6th 2pm to: 4pm)
    Now you've got 3 racers ready to roll.
    ____________________________________________________________
    ____________________________________________________________
    Ka-chow! A new racer joined the starting grid:
      [T][ ] join sports club
    Now you've got 4 racers ready to roll.
    ____________________________________________________________
    ____________________________________________________________
    Ka-chow! A new racer joined the starting grid:
      [T][ ] borrow book
    Now you've got 5 racers ready to roll.
    ____________________________________________________________
    ____________________________________________________________
    Ka-chow! This task crossed the finish line:
      [T][X] read book
    ____________________________________________________________
    ____________________________________________________________
    Ka-chow! This task crossed the finish line:
      [D][X] return book (by: June 6th)
    ____________________________________________________________
    ____________________________________________________________
    Ka-chow! This task crossed the finish line:
      [T][X] join sports club
    ____________________________________________________________
    ____________________________________________________________
    Rev up! Here are the tasks in today's race:
    1.[T][X] read book
    2.[D][X] return book (by: June 6th)
    3.[E][ ] project meeting (from: Aug 6th 2pm to: 4pm)
    4.[T][X] join sports club
    5.[T][ ] borrow book
    ____________________________________________________________
    ____________________________________________________________
    Ka-chow! This racer has left the track:
      [E][ ] project meeting (from: Aug 6th 2pm to: 4pm)
    Now you've got 4 racers still in the race.
    ____________________________________________________________
    ____________________________________________________________
    Rev up! Here are the tasks in today's race:
    1.[T][X] read book
    2.[D][X] return book (by: June 6th)
    3.[T][X] join sports club
    4.[T][ ] borrow book
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
["test/run-kachow-isolated.sh"]
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
["test/run-kachow-isolated.sh"]
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
["test/run-kachow-isolated.sh"]
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

## Test Case: UI-08 Preserve state after invalid delete commands

### Aim

Verify that missing, nonnumeric, nonpositive, extra, and out-of-range delete arguments are rejected without changing task data or completion state, while valid commands still work between the rejected commands.

### Command

```json
["test/run-kachow-isolated.sh"]
```

### Inputs

```text
delete 1
todo tire change
delete
list
delete zero
mark 1
delete -1
list
delete 1 turbo
unmark 1
delete 2
list
delete 1
list
delete 1
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
      [T][ ] tire change
    Now you've got 1 racer ready to roll.
    ____________________________________________________________
    ____________________________________________________________
    Pit stop! Tell me which racer to delete. Use: delete TASK_NUMBER
    ____________________________________________________________
    ____________________________________________________________
    Rev up! Here are the tasks in today's race:
    1.[T][ ] tire change
    ____________________________________________________________
    ____________________________________________________________
    Pit stop! That racer number isn't a whole positive number. Use: delete TASK_NUMBER
    ____________________________________________________________
    ____________________________________________________________
    Ka-chow! This task crossed the finish line:
      [T][X] tire change
    ____________________________________________________________
    ____________________________________________________________
    Pit stop! That racer number isn't a whole positive number. Use: delete TASK_NUMBER
    ____________________________________________________________
    ____________________________________________________________
    Rev up! Here are the tasks in today's race:
    1.[T][X] tire change
    ____________________________________________________________
    ____________________________________________________________
    Pit stop! That racer number isn't a whole positive number. Use: delete TASK_NUMBER
    ____________________________________________________________
    ____________________________________________________________
    Back to the starting grid! This task is not done yet:
      [T][ ] tire change
    ____________________________________________________________
    ____________________________________________________________
    Pit stop! Racer 2 isn't on the grid. Use list to check the task numbers.
    ____________________________________________________________
    ____________________________________________________________
    Rev up! Here are the tasks in today's race:
    1.[T][ ] tire change
    ____________________________________________________________
    ____________________________________________________________
    Ka-chow! This racer has left the track:
      [T][ ] tire change
    Now you've got 0 racers still in the race.
    ____________________________________________________________
    ____________________________________________________________
    The starting grid is empty. Add a racer with todo, deadline, or event.
    ____________________________________________________________
    ____________________________________________________________
    Pit stop! Racer 1 isn't on the grid. Use list to check the task numbers.
    ____________________________________________________________
    ____________________________________________________________
    Race complete! Catch you on the next lap. Ka-chow!
    ____________________________________________________________
```

## Test Case: UI-09 Delete boundary tasks and continue using the list

### Aim

Verify that deleting the first, last, and only tasks preserves task details and completion state through renumbering, invalid deletion does not mutate the list, and new tasks can be added after the list becomes empty.

### Command

```json
["test/run-kachow-isolated.sh"]
```

### Inputs

```text
todo pole position
deadline refuel /by lap 20
event sponsor event /from 3pm /to 4pm
todo victory lap
mark 2
delete 1
delete 4
list
unmark 1
delete 3
list
delete 1
delete 1
list
todo new race
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
      [T][ ] pole position
    Now you've got 1 racer ready to roll.
    ____________________________________________________________
    ____________________________________________________________
    Ka-chow! A new racer joined the starting grid:
      [D][ ] refuel (by: lap 20)
    Now you've got 2 racers ready to roll.
    ____________________________________________________________
    ____________________________________________________________
    Ka-chow! A new racer joined the starting grid:
      [E][ ] sponsor event (from: 3pm to: 4pm)
    Now you've got 3 racers ready to roll.
    ____________________________________________________________
    ____________________________________________________________
    Ka-chow! A new racer joined the starting grid:
      [T][ ] victory lap
    Now you've got 4 racers ready to roll.
    ____________________________________________________________
    ____________________________________________________________
    Ka-chow! This task crossed the finish line:
      [D][X] refuel (by: lap 20)
    ____________________________________________________________
    ____________________________________________________________
    Ka-chow! This racer has left the track:
      [T][ ] pole position
    Now you've got 3 racers still in the race.
    ____________________________________________________________
    ____________________________________________________________
    Pit stop! Racer 4 isn't on the grid. Use list to check the task numbers.
    ____________________________________________________________
    ____________________________________________________________
    Rev up! Here are the tasks in today's race:
    1.[D][X] refuel (by: lap 20)
    2.[E][ ] sponsor event (from: 3pm to: 4pm)
    3.[T][ ] victory lap
    ____________________________________________________________
    ____________________________________________________________
    Back to the starting grid! This task is not done yet:
      [D][ ] refuel (by: lap 20)
    ____________________________________________________________
    ____________________________________________________________
    Ka-chow! This racer has left the track:
      [T][ ] victory lap
    Now you've got 2 racers still in the race.
    ____________________________________________________________
    ____________________________________________________________
    Rev up! Here are the tasks in today's race:
    1.[D][ ] refuel (by: lap 20)
    2.[E][ ] sponsor event (from: 3pm to: 4pm)
    ____________________________________________________________
    ____________________________________________________________
    Ka-chow! This racer has left the track:
      [D][ ] refuel (by: lap 20)
    Now you've got 1 racer still in the race.
    ____________________________________________________________
    ____________________________________________________________
    Ka-chow! This racer has left the track:
      [E][ ] sponsor event (from: 3pm to: 4pm)
    Now you've got 0 racers still in the race.
    ____________________________________________________________
    ____________________________________________________________
    The starting grid is empty. Add a racer with todo, deadline, or event.
    ____________________________________________________________
    ____________________________________________________________
    Ka-chow! A new racer joined the starting grid:
      [T][ ] new race
    Now you've got 1 racer ready to roll.
    ____________________________________________________________
    ____________________________________________________________
    Rev up! Here are the tasks in today's race:
    1.[T][ ] new race
    ____________________________________________________________
    ____________________________________________________________
    Race complete! Catch you on the next lap. Ka-chow!
    ____________________________________________________________
```

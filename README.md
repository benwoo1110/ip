# Kachow

Kachow is a chatbot project developed as part of an introductory software engineering course. Given below are instructions on how to use it.

## Setting up in Intellij

Prerequisites: JDK 25, update Intellij to the most recent version.

1. Open Intellij (if you are not in the welcome screen, click `File` > `Close Project` to close the existing project first)
1. Open the project into Intellij as follows:
   1. Click `Open`.
   1. Select the project directory, and click `OK`.
   1. If there are any further prompts, accept the defaults.
1. Configure the project to use **JDK 25** (not other versions) as explained in [here](https://www.jetbrains.com/help/idea/sdk.html#set-up-jdk).<br>
   In the same dialog, set the **Project language level** field to the `SDK default` option.
1. After that, locate the `src/main/java/com/benthecat/kachow/Kachow.java` file, right-click it, and choose `Run Kachow.main()` (if the code editor is showing compile errors, try restarting the IDE). If the setup is correct, you should see something like the below as the output:
   ```
       ____________________________________________________________
        _  __          _
       | |/ /__ _  ___| |__   _____      __
       | ' // _` |/ __| '_ \ / _ \ \ /\ / /
       | . \ (_| | (__| | | | (_) \ V  V /
       |_|\_\__,_|\___|_| |_|\___/ \_/\_/
       Ka-chow! I'm Kachow, the fastest chatbot on the track.
       What can I do for you before the next lap?
       ____________________________________________________________

   todo read book
       ____________________________________________________________
       Ka-chow! A new racer joined the starting grid:
         [T][ ] read book
       Now you've got 1 racer ready to roll.
       ____________________________________________________________

   deadline return book /by 2/12/2019 1800
       ____________________________________________________________
       Ka-chow! A new racer joined the starting grid:
         [D][ ] return book (by: Dec 02 2019, 6:00 PM)
       Now you've got 2 racers ready to roll.
       ____________________________________________________________

   event project meeting /from 3/12/2019 1400 /to 1600
       ____________________________________________________________
       Ka-chow! A new racer joined the starting grid:
         [E][ ] project meeting (from: Dec 03 2019, 2:00 PM to: Dec 03 2019, 4:00 PM)
       Now you've got 3 racers ready to roll.
       ____________________________________________________________

   list
       ____________________________________________________________
       Rev up! Here are the tasks in today's race:
       1.[T][ ] read book
       2.[D][ ] return book (by: Dec 02 2019, 6:00 PM)
       3.[E][ ] project meeting (from: Dec 03 2019, 2:00 PM to: Dec 03 2019, 4:00 PM)
       ____________________________________________________________

   mark 2
       ____________________________________________________________
       Ka-chow! This task crossed the finish line:
         [D][X] return book (by: Dec 02 2019, 6:00 PM)
       ____________________________________________________________

   list
       ____________________________________________________________
       Rev up! Here are the tasks in today's race:
       1.[T][ ] read book
       2.[D][X] return book (by: Dec 02 2019, 6:00 PM)
       3.[E][ ] project meeting (from: Dec 03 2019, 2:00 PM to: Dec 03 2019, 4:00 PM)
       ____________________________________________________________

   unmark 2
       ____________________________________________________________
       Back to the starting grid! This task is not done yet:
         [D][ ] return book (by: Dec 02 2019, 6:00 PM)
       ____________________________________________________________

   bye
       ____________________________________________________________
       Race complete! Catch you on the next lap. Ka-chow!
       ____________________________________________________________
   ```

All output produced by Kachow is indented by four spaces to distinguish it from
the user's input. Add tasks with `todo DESCRIPTION`, `deadline DESCRIPTION /by
DATE_OR_TIME`, or `event DESCRIPTION /from START /to END`. Deadline and event
dates accept `yyyy-MM-dd`, `yyyy/M/d`, `d/M/yyyy`, or padded US-style
`MM/dd/yyyy`; add a time as `HHmm`, `HH:mm`, or an
AM/PM time such as `6pm` or `6 PM`. A time-only event end uses the event's start date and must not be
earlier than its start; use a full end date for an overnight event.
Use `on DATE` to list deadlines due and events occurring on a date. The command
retains the original task numbers, making its results usable with `mark`,
`unmark`, and `delete`. To preserve `2/12/2019` as 2 December, ambiguous US
dates must be zero-padded, for example `12/02/2019` for December 2.
Use `find KEYWORD` to search task descriptions without regard to letter case.
Search results also retain the tasks' original numbers.
Tasks are loaded from
`./data/kachow.txt` when Kachow starts and are saved there automatically after
every add, mark, unmark, or delete command. Kachow creates the `data` directory
and file automatically on first use. Enter `list` to display the saved tasks.
Use `mark NUMBER` or `unmark NUMBER` to change a task's completion status; task
numbers start at 1 as shown by `list`. Use `delete NUMBER` to remove a task from
the race; the remaining tasks are renumbered automatically.

**Warning:** Keep the `src\main\java` folder as the root folder for Java files (i.e., don't rename those folders or move Java files to another folder outside of this folder path), as this is the default location some tools (e.g., Gradle) expect to find Java files.

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
1. After that, locate the `src/main/java/Kachow.java` file, right-click it, and choose `Run Kachow.main()` (if the code editor is showing compile errors, try restarting the IDE). If the setup is correct, you should see something like the below as the output:
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

   read book
       ____________________________________________________________
       Added to the race lineup: read book
       ____________________________________________________________

   return book
       ____________________________________________________________
       Added to the race lineup: return book
       ____________________________________________________________

   buy bread
       ____________________________________________________________
       Added to the race lineup: buy bread
       ____________________________________________________________

   list
       ____________________________________________________________
       Rev up! Here are the tasks in today's race:
       1.[ ] read book
       2.[ ] return book
       3.[ ] buy bread
       ____________________________________________________________

   mark 2
       ____________________________________________________________
       Ka-chow! This task crossed the finish line:
         [X] return book
       ____________________________________________________________

   list
       ____________________________________________________________
       Rev up! Here are the tasks in today's race:
       1.[ ] read book
       2.[X] return book
       3.[ ] buy bread
       ____________________________________________________________

   unmark 2
       ____________________________________________________________
       Back to the starting grid! This task is not done yet:
         [ ] return book
       ____________________________________________________________

   bye
       ____________________________________________________________
       Race complete! Catch you on the next lap. Ka-chow!
       ____________________________________________________________
   ```

All output produced by Kachow is indented by four spaces to distinguish it from
the user's input. Tasks are kept in memory for the duration of the program and
can be displayed by entering `list`. Use `mark NUMBER` or `unmark NUMBER` to
change a task's completion status; task numbers start at 1 as shown by `list`.

**Warning:** Keep the `src\main\java` folder as the root folder for Java files (i.e., don't rename those folders or move Java files to another folder outside of this folder path), as this is the default location some tools (e.g., Gradle) expect to find Java files.

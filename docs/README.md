# Pudding User Guide

**Pudding** is a command-line chatbot that helps you track your tasks — todos, deadlines, and events — stored persistently so your list survives restarts.

---

## Quick Start

1. Run the application.
2. Type commands at the prompt and press **Enter**.
3. Type `bye` to exit.

---

## Command Summary

| Command | Format |
|---|---|
| Add todo | `todo DESCRIPTION` |
| Add deadline | `deadline DESCRIPTION /by DATE` |
| Add event | `event DESCRIPTION /from DATE /to DATE` |
| List all tasks | `list` |
| Mark as done | `mark INDEX` |
| Mark as not done | `unmark INDEX` |
| Delete a task | `delete INDEX` |
| Find tasks | `find KEYWORD` |
| Exit | `bye` |

> **DATE format:** `yyyy-MM-dd` (e.g. `2025-12-31`) or `d/M/yyyy` (e.g. `31/12/2025`)

---

## Features

### Add a Todo — `todo`

Adds a task with no time constraint.

**Format:** `todo DESCRIPTION`

**Example:**
```
todo read textbook
```
**Output:**
```
Got it. I've added this task:
  [T][ ] read textbook
Now you have 1 task in the list.
```

---

### Add a Deadline — `deadline`

Adds a task that must be done by a specific date.

**Format:** `deadline DESCRIPTION /by DATE`

**Example:**
```
deadline submit report /by 2025-03-15
```
**Output:**
```
Got it. I've added this task:
  [D][ ] submit report (by: Mar 15 2025)
Now you have 2 tasks in the list.
```

---

### Add an Event — `event`

Adds a task that spans a date range.

**Format:** `event DESCRIPTION /from DATE /to DATE`

**Example:**
```
event team camp /from 2025-06-01 /to 2025-06-03
```
**Output:**
```
Got it. I've added this task:
  [E][ ] team camp (from: Jun 01 2025, to: Jun 03 2025)
Now you have 3 tasks in the list.
```

---

### List All Tasks — `list`

Displays every task in your list with its index and status.

**Format:** `list`

**Output:**
```
Here are the tasks in your list:
1.[T][ ] read textbook
2.[D][ ] submit report (by: Mar 15 2025)
3.[E][ ] team camp (from: Jun 01 2025, to: Jun 03 2025)
```

Task types: `[T]` = Todo, `[D]` = Deadline, `[E]` = Event  
Status: `[X]` = done, `[ ]` = not done

---

### Mark a Task as Done — `mark`

Marks the task at the given index as completed.

**Format:** `mark INDEX`

**Example:**
```
mark 1
```
**Output:**
```
Nice! I've marked this task as done:
  [T][X] read textbook
```

---

### Mark a Task as Not Done — `unmark`

Marks the task at the given index as incomplete.

**Format:** `unmark INDEX`

**Example:**
```
unmark 1
```
**Output:**
```
OK, I've marked this task as not done yet:
  [T][ ] read textbook
```

---

### Delete a Task — `delete`

Permanently removes the task at the given index.

**Format:** `delete INDEX`

**Example:**
```
delete 2
```
**Output:**
```
Noted. I've removed this task:
  [D][ ] submit report (by: Mar 15 2025)
Now you have 2 tasks in the list.
```

---

### Find Tasks — `find`

Lists all tasks whose description contains the given keyword (case-insensitive).

**Format:** `find KEYWORD`

**Example:**
```
find camp
```
**Output:**
```
Here are the matching tasks in your list:
1.[E][ ] team camp (from: Jun 01 2025, to: Jun 03 2025)
```

---

### Exit — `bye`

Saves all tasks and exits the application.

**Format:** `bye`

---

## Data Storage

Tasks are saved automatically to `src/main/java/Pudding/dataLog.txt` after every change and loaded on startup — no manual saving required.

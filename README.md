# Riker tasks

## About

One of a number of Riker pseudo AI/assistant apps in the long term probably.

Retrieves tasks from task managers and transforms and represents in portable formats for other tools.

Currently supported task manager:

* Todoist (REST API)

Currently supported portable formats:

* Markdown (including Kanban plugin compatible)

Currently makes an assumption that using filters provides a nice general way to answer to many usecases

This is written in Clojure and distributed as a jar file ready for running on Java. Only MacOS and Linux tested so far.

## Usage

### Create a secrets file with your task manager auth token

Create a secrets file in your home directory and place your Todoist API auth token in there.

For example '.riker-tasks-secrets.edn' below.


``` edn
{
  :auth-token "your-token-here"
}
```

### Configure riker-tasks for your use case

This example will create two kanban columns, one with personal tasks with the highest priority (which in this system means set for today) and the other with work tasks with the highest priority.

Create a 'config.edn' in the same directory where you will run the riker-task jar file.

``` edn
{
  :secrets #include #join [#env HOME "/.riker-task-secrets.edn"]

  :api-auth-token #profile {:default #ref [:secrets :auth-token]}

  :todoist-uri "https://api.todoist.com/rest/v1/"

  :task-list-filters [{:list-name "Personal tasks" :resource "tasks" :filter "p1 & @Personal"} {:list-name "Work tasks" :resource "tasks" :filter "p1 & @Work"}]
}
```

### Running the app using the jar file

``` bash
$ java -jar riker-tasks.jar
```

## Developing

### Dependencies

Assumes that [Seancorfield dot-clojure](https://github.com/seancorfield/dot-clojure) is installed locally in ~/.clojure and is functional.

Update 'resources/app.edn' setting the version to the appropriate value.

### Creating jar file

``` bash
$ clj -X:uberjar :jar riker-tasks.jar :main-class rikertask.core
```

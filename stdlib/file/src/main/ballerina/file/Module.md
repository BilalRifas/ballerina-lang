## Module overview
This module contains services that register listeners against a local folder and identify events that create, modify, and delete files.

## Samples
The sample given below shows how a `Listener` is used to listen to the local folder. The `onCreate()` resource method gets invoked when a file is created inside the `target/fs` folder. Use the `onDelete()` and `onModify()` methods to listen to the delete and modify events.
```ballerina
import ballerina/file;

listener file:Listener localFolder = new ({
    path: "target/fs",
    recursive: false
});

service fileSystem on localFolder {

    resource function onCreate(file:FileEvent m) {
    
    }
}
```

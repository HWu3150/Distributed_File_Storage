# JPaxos Setup Instructions

The JPaxos library is not available in the Maven Central Repository. To use it in this project, we have manually built 
the JPaxos source code using Ant and packaged it as `jpaxos.jar`. You will need to install this JAR file into your local 
Maven repository before running the project.

To simplify this process, a `setup.sh` script has been provided. This script is designed to work across 
different operating systems (Linux, macOS, and Windows).

## Prerequisites

1. Ensure you have the following installed on your system:
    - Java Development Kit (JDK) version 11
    - Maven
    - Bash shell (for Linux, macOS, or Git Bash / WSL on Windows)

2. Verify that `jpaxos.jar` is located in the `libs` directory within the root of this project.

## Installation Steps

1. Open a terminal and navigate to the project root directory:
   ```bash
   cd /path/to/Distributed_File_Storage
   
2. Run the setup.sh script:
   ```bash
    ./setup.sh
   ```

3. The script will:
   - Detect your operating system.
   - Check for the existence of the ```jpaxos.jar``` file in the ```libs``` directory.
   - Install the ```jpaxos.jar``` file into your local Maven repository with the following details:
     - Group ID: ```github_jpaxos```
     - Artifact ID: ```jpaxos```
     - Version: ```1.0```
     - Packaging: ```jar```

## Script Output
- If successful, you will see:
    ```cmd
    Setup completed successfully!
    ```
- If there are errors, the script will notify you with a message and terminate.

# Running the Test Program in IntelliJ IDEA
Please follow the instructions below to run the test program using IntelliJ IDEA. This guide includes setting up the 
necessary run configurations and arguments.

### 1. Running the Replica
To run a replica, you need to create a run configuration in IntelliJ IDEA. The configuration requires JDK 11 and 
specific arguments to identify the replica.

#### Steps:
1. Open IntelliJ IDEA and go to ```Run``` -> ```Edit Configurations```.

2. Click the ```+``` icon and select ```Application```.

3. In the Name field, enter whatever name preferred.

4. In the ```Main class``` field, specify the class for the replica (e.g., ```jpaxos.JPaxosReplica```).

5. Set the JDK to version 11.

6. In the ```Program arguments``` field, enter the replica ID. For the current ```jpaxos.properties``` configuration, 
the valid IDs are ```0```, ```1```, or ```2```.

7. Click ```OK``` to save the configuration.

#### Screenshot:
![run_replica](https://imgur.com/a/FLtCJlx)

### 2. Running the Service Manager
To run the Service Manager, please follow the steps below:

1. Open ```Run``` -> ```Edit Configurations```.
2. Click the ```+``` icon and select ```Application```.
3. In the Name field, enter whatever name preferred.
4. In the ```Main class``` field, specify the class for the Service Manager (e.g., ```Application```).
5. Set the JDK to version 11.
6. Click ```OK``` to save the configuration.

#### Screenshot:
![run_service_manager](https://imgur.com/a/vJhZu1A)

### 3. Simplifying Steps with Compound Configuration
To simplify the process of starting multiple applications, you can combine them into a ```Compound configuration```. 
This allows you to start the ```Replica``` and ```Service Manager``` together with one click.

#### Steps:

1. Open ```Run``` -> ```Edit Configurations```.
2. Click the ```+``` icon and select ```Compound```.
3. In the Name field, enter whatever name preferred.
4. In the ```Run/Debug Configurations``` section, click ```Add``` and select the previously created Replica and 
Service Manager configurations.
5. Click ```OK``` to save the compound configuration.

#### Screenshot:
![run_all](https://imgur.com/a/8cy7LNv)

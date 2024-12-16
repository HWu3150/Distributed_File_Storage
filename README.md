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
   On Windows:
   ```bash
   bash .\setup.sh    # Using Git Bash
   ```
   ```bash
   wsl bash ./setup.sh    # Using WSL
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

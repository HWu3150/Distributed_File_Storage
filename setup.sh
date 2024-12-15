#!/bin/bash

echo "Detecting OS..."
# Set line separator for different OS
if [[ "$OSTYPE" == "msys" || "$OSTYPE" == "cygwin" || "$OSTYPE" == "win32" ]]; then
    # Windows
    SEP="\\"
    LIB_PATH="libs${SEP}jpaxos.jar"
else
    # Linux/macOS
    SEP="/"
    LIB_PATH="libs${SEP}jpaxos.jar"
fi

echo "Detected OS: $OSTYPE"
echo "Using path: $LIB_PATH"

# Check for existence
if [[ ! -f "$LIB_PATH" ]]; then
    echo "Error: $LIB_PATH does not exist. Please ensure jpaxos.jar is in the 'libs' directory."
    exit 1
fi

# Install JPaxos
echo "Installing jpaxos.jar to local Maven repository..."
mvn install:install-file -Dfile="$LIB_PATH" -DgroupId=github_jpaxos -DartifactId=jpaxos -Dversion="1.0" -Dpackaging=jar

# Install status
if [[ $? -eq 0 ]]; then
    echo "Setup completed successfully!"
else
    echo "Error: Failed to install jpaxos.jar to local Maven repository."
    exit 2
fi

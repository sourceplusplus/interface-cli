#!/bin/bash

# Treat unset variables and parameters other than the special parameters ‘@’ or ‘*’ as an error.
set -u

# Exit the script with a message.
abort() {
  printf "%s\n" "$@"
  exit 1
}

# Check if there is a bash.
if [ -z "${BASH_VERSION:-}" ]; then
  abort "Bash is required to interpret this install script."
fi

# Check OS.
OS="$(uname)"
if [[ "$OS" != "Darwin" && "$OS" != "Linux" ]]; then
  abort "The install script is only supported on macOS and Linux."
fi

check_cmd() {
    if ! command -v "$@" &> /dev/null 
    then
        abort "You must install "$@" before running the install script."
    fi
}

# Check if the commands to be used exist.
for cmd in curl unzip awk; do
  check_cmd $cmd
done

get_latest_release_number() {
  curl --silent "https://github.com/sourceplusplus/interface-cli/releases/latest" | sed 's#.*tag/\(.*\)\".*#\1#'
}

# Convert the string to lower case.
OS=$(echo $OS | awk '{print tolower($0)}')

# Get the latest version of spp-cli.
VERSION=$(get_latest_release_number)
echo "Installing spp-cli $VERSION"

# Download the binary package.
curl -sSLO "https://github.com/sourceplusplus/interface-cli/releases/download/$VERSION/spp-cli-$VERSION-${OS}64.zip" > /dev/null
if [ -f "spp-cli-$VERSION-${OS}64.zip" ]; then
    unzip -q spp-cli-$VERSION-${OS}64.zip

    echo "Adding spp-cli to your PATH"
    # Add spp-cli to the environment variable PATH.
    sudo mv spp-cli /usr/local/bin/spp-cli

    # Delete unnecessary files.
    rm "./spp-cli-$VERSION-${OS}64.zip"
    echo "Installation complete."

    echo "Type 'spp-cli --help' to get more information."
else
    abort "Failed to download spp-cli-$VERSION-${OS}64.zip"
fi

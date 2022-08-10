# ![](https://github.com/sourceplusplus/live-platform/blob/master/.github/media/sourcepp_logo.svg)

[![License](https://camo.githubusercontent.com/93398bf31ebbfa60f726c4f6a0910291b8156be0708f3160bad60d0d0e1a4c3f/68747470733a2f2f696d672e736869656c64732e696f2f6769746875622f6c6963656e73652f736f75726365706c7573706c75732f6c6976652d706c6174666f726d)](LICENSE)
![GitHub release](https://img.shields.io/github/v/release/sourceplusplus/interface-cli?include_prereleases)
[![Build](https://github.com/sourceplusplus/interface-cli/actions/workflows/build.yml/badge.svg)](https://github.com/sourceplusplus/interface-cli/actions/workflows/build.yml)

## What is this?

This project provides a command-line interface to [Source++](https://github.com/sourceplusplus/live-platform), the open-source live coding platform.

## Install

### Linux or macOS

Install the latest version with the following command:

```shell
/bin/bash -c "$(curl -fsSL https://raw.githubusercontent.com/sourceplusplus/interface-cli/master/scripts/install.sh)"
```

### Windows

Note: You will need to start cmd in administrator mode.

```shell
curl -LO "https://raw.githubusercontent.com/sourceplusplus/interface-cli/master/scripts/install.bat" && .\install.bat
```

### Install by available binaries

Go to the [releases page](https://github.com/sourceplusplus/interface-cli/releases) to download all available binaries,
including macOS, Linux, Windows.

## How to use?

Try executing `spp-cli --help` to output the usage instructions like so:

```
Usage: spp-cli [OPTIONS] COMMAND [ARGS]...

Options:
  -v, --verbose            Enable verbose mode
  -p, --platform TEXT      Source++ platform host
  -c, --certificate PATH   Source++ platform certificate
  -k, --key PATH           Source++ platform key
  -a, --access-token TEXT  Developer access token
  -h, --help               Show this message and exit

Commands:
  admin      Administrator commands
  developer  Developer commands
  add        Add live instruments/views
  get        Get live instruments/views
  remove     Remove live instruments/views
  subscribe  Subscribe to live instrument/view events
  version    Display version information
```

To get information about a sub-command, try `spp-cli <command> --help`:

```
Usage: spp-cli add [OPTIONS] COMMAND [ARGS]...

  Add live instruments/views

Options:
  -h, --help  Show this message and exit

Commands:
  breakpoint  Add a live breakpoint instrument
  log         Add a live log instrument
  meter       Add a live meter instrument
  span        Add a live span instrument
  view        Add a live view subscription
```

## Documentation
- [Developer Commands](https://docs.sourceplus.plus/implementation/tools/clients/cli/developer/) / [Admin Commands](https://docs.sourceplus.plus/implementation/tools/clients/cli/admin/)

## Bugs & Features

Bug reports and feature requests can be created [here](https://github.com/sourceplusplus/live-platform/issues).

# ![](https://github.com/sourceplusplus/live-platform/blob/master/.github/media/sourcepp_logo.svg)

[![License](https://img.shields.io/github/license/sourceplusplus/interface-cli)](LICENSE)
![GitHub release](https://img.shields.io/github/v/release/sourceplusplus/interface-cli?include_prereleases)
[![Build](https://github.com/sourceplusplus/interface-cli/actions/workflows/build.yml/badge.svg)](https://github.com/sourceplusplus/interface-cli/actions/workflows/build.yml)

# What is this?

This project provides a command-line interface to [Source++](https://github.com/sourceplusplus/live-platform), the open-source live coding platform.

# Usage

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
  subscribe  Subscribe to live instrument/view streams
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
  view        Add a live view
```

# Documentation
- [Developer Commands](https://docs.sourceplusplus.com/implementation/tools/clients/cli/developer/) / [Admin Commands](https://docs.sourceplusplus.com/implementation/tools/clients/cli/admin/)

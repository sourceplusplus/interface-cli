# ![](https://github.com/sourceplusplus/live-platform/blob/master/.github/media/sourcepp_logo.svg)

[![License](https://img.shields.io/github/license/sourceplusplus/interface-cli)](LICENSE)
![GitHub release](https://img.shields.io/github/v/release/sourceplusplus/interface-cli?include_prereleases)
[![Build](https://github.com/sourceplusplus/interface-cli/actions/workflows/build.yml/badge.svg)](https://github.com/sourceplusplus/interface-cli/actions/workflows/build.yml)

# What is this?

This project provides a command-line interface to [Source++](https://github.com/sourceplusplus/live-platform), the open-source live coding platform.

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
  admin
  add-breakpoint
  add-log
  add-meter
  add-span
  get-breakpoints
  get-instruments
  get-logs
  get-meters
  get-spans
  remove-instrument
  remove-instruments
  clear-instruments
  subscribe-events    Listens for and outputs live events. Subscribes to all events by default
  get-self
  version
```

To get information about a sub-command, try `spp-cli <command> --help`:

```
Usage: spp-cli admin [OPTIONS] COMMAND [ARGS]...

Options:
  -h, --help  Show this message and exit

Commands:
  add-role
  get-developer-roles
  get-roles
  remove-role
  add-developer-role
  remove-developer-role
  add-role-permission
  get-developer-permissions
  get-role-permissions
  remove-role-permission
  add-developer
  get-developers
  remove-developer
  refresh-developer-token
  add-access-permission
  add-role-access-permission
  get-access-permissions
  get-developer-access-permissions
  get-role-access-permissions
  remove-access-permission
  remove-role-access-permission
  reset
```

# Documentation
- [Developer Commands](https://docs.sourceplusplus.com/implementation/tools/clients/cli/developer/) / [Admin Commands](https://docs.sourceplusplus.com/implementation/tools/clients/cli/admin/)

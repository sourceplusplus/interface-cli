# ![](https://github.com/sourceplusplus/live-platform/blob/master/.github/media/sourcepp_logo.svg)

[![License](https://img.shields.io/github/license/sourceplusplus/interface-cli)](LICENSE)
![GitHub release](https://img.shields.io/github/v/release/sourceplusplus/interface-cli?include_prereleases)
[![Build](https://github.com/sourceplusplus/interface-cli/actions/workflows/build.yml/badge.svg)](https://github.com/sourceplusplus/interface-cli/actions/workflows/build.yml)

## What is this?

This project provides a command-line interface client to the [Source++](https://github.com/sourceplusplus/live-platform) open-source live coding platform.

## Basic Usage

### Admin

#### Add Developer

```sh
./spp-cli admin add-developer bob@email.com
```

### Developer

#### Add Live Instrument

```sh
./spp-cli developer add-live-breakpoint com.company.Webapp 42
```

## Documentation
- [Admin CLI](https://docs.sourceplusplus.com/implementation/tools/clients/cli/admin/) / [Developer CLI](https://docs.sourceplusplus.com/implementation/tools/clients/cli/developer/)

# ![](https://github.com/sourceplusplus/live-platform/blob/master/.github/media/sourcepp_logo.svg)

[![License](https://img.shields.io/github/license/sourceplusplus/interface-cli)](LICENSE)
![GitHub release](https://img.shields.io/github/v/release/sourceplusplus/interface-cli?include_prereleases)
[![Build](https://github.com/sourceplusplus/interface-cli/actions/workflows/build.yml/badge.svg)](https://github.com/sourceplusplus/interface-cli/actions/workflows/build.yml)

# What is this?

This project provides a command-line interface client to the [Source++](https://github.com/sourceplusplus/live-platform) open-source live coding platform.

# Basic Usage

## Admin Guide

- [ ] Create developers
- [ ] Create roles
- [ ] Configure role permissions
- [ ] Add roles to developers

### Create developers

<details>
  <summary>Command</summary>

  ```sh
  ./spp-cli admin add-developer bob@email.com
  ./spp-cli admin add-developer john@email.com
  
  ./spp-cli admin add-role contractors
  ```
</details>

## Developer Guide

- [ ] Create live instruments
- [ ] Listen for live instrument events

### Add Live Instrument

<details>
  <summary>Command</summary>

  ```sh
  ./spp-cli add-breakpoint com.company.Webapp 42
  ```
</details>

# Documentation
- [Admin Commands](https://docs.sourceplusplus.com/implementation/tools/clients/cli/admin/) / [Developer Commands](https://docs.sourceplusplus.com/implementation/tools/clients/cli/developer/)

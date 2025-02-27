[![semantic-release](https://img.shields.io/badge/semantic-release-e10079.svg?logo=semantic-release)](https://github.com/semantic-release/semantic-release)

# lsd-distributed-pubsub-connector

![GitHub](https://img.shields.io/github/license/lsd-consulting/lsd-distributed-pubsub-connector)
![Codecov](https://img.shields.io/codecov/c/github/lsd-consulting/lsd-distributed-pubsub-connector)

[![CI](https://github.com/lsd-consulting/lsd-distributed-pubsub-connector/actions/workflows/ci.yml/badge.svg)](https://github.com/lsd-consulting/lsd-distributed-pubsub-connector/actions/workflows/ci.yml)
[![Nightly Build](https://github.com/lsd-consulting/lsd-distributed-pubsub-connector/actions/workflows/nightly.yml/badge.svg)](https://github.com/lsd-consulting/lsd-distributed-pubsub-connector/actions/workflows/nightly.yml)
[![GitHub release](https://img.shields.io/github/release/lsd-consulting/lsd-distributed-pubsub-connector)](https://github.com/lsd-consulting/lsd-distributed-pubsub-connector/releases)
![Maven Central](https://img.shields.io/maven-central/v/io.github.lsd-consulting/lsd-distributed-pubsub-connector)

## About

This is a Pubsub implementation of the data access connector for the distributed data storage.

## Important notes

* You must specify the pubsub topic using the LSD property `lsd.dist.connectionString`.
* The format of this property is `lsd.dist.connectionString=pubsub://$topicName` where `$topicName` is an **existing**
  topic in GCP Pubsub.
* You should include as a dependency `spring-cloud-gcp-core` in order to autoconfigure the credentials and projectId
  used by this library.

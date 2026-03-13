# VAT Correspondence Details Frontend

[![Build Status](https://travis-ci.org/hmrc/vat-correspondence-details-frontend.svg)](https://travis-ci.org/hmrc/vat-correspondence-details-frontend) [ ![Download](https://api.bintray.com/packages/hmrc/releases/vat-correspondence-details-frontend/images/download.svg) ](https://bintray.com/hmrc/releases/vat-correspondence-details-frontend/_latestVersion)

## Summary

This is the repository for VAT Correspondence Details Frontend.

This service provides the functionality to update business correspondence details for a VAT account.

## Requirements

This service is written in [Scala](http://www.scala-lang.org/) and [Play](http://playframework.com/), so needs at least a [JRE](https://www.java.com/en/download/) to run.

## Running Locally

### Prerequisites
Start required services locally as follows:

```bash
sm2 --start VAT_SUBSCRIPTION_DYNAMIC_STUB DIGITAL_COMMS_DYNAMIC_STUB CHANGE_VAT_DETAILS_ALL`
```

### Populating stub data
To run you will need to first populate the dynamic stubs with data via [VAT View & Change Stub Data](https://github.com/hmrc/vat-view-change-stub-data)
by running the `./populate_stub.sh` script. See README.md of the repo for full information.

### Starting the service

```bash
sm2 --stop VAT_CORRESPONDENCE_DETAILS_FRONTEND
./run.sh
```

### Local URL
Once the service is running the first page of the journey is on:
`http://localhost:9148/vat-through-software/account/correspondence/{endpoint}`

This service doesn't have landing page, so the specific endpoint is necessary.

I.E. `http://localhost:9148/vat-through-software/account/correspondence/new-landline-number`

## Testing

Use the following command to run unit and integration tests:

`sbt test it:test`

### Acceptance and Performance Tests
[change-vat-details-acceptance-tests](https://github.com/hmrc/change-vat-details-acceptance-tests)

[change-vat-details-performance-tests](https://github.com/hmrc/change-vat-details-performance-tests)


change-vat-details-acceptance-tests

## License

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html")



#!/usr/bin/env bash

sbt 'run 9148 -Dlogger.resource=logback-test.xml -Dplay.http.router=testOnlyDoNotUseInAppConf.Routes'
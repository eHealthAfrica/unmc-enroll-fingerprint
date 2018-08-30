#!/usr/bin/env bash

cd app/Biometrics/Java/enroll-finger-from-scanner/

set -e

case "${1}" in
    build)
        gradle build
    ;;
    lint)
        gradle checkstyleMain
    ;;
esac
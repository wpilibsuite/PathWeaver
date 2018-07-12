#! /bin/bash

set -e -x

Xvfb :99 & export DISPLAY=:99

./gradlew --version --console=plain
./gradlew check --stacktrace --console=plain -Pheadless

#bash <(curl -s https://codecov.io/bash)

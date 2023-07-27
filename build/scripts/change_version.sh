#!/bin/bash
#
# Copyright (c) 2023 OceanBase
# OCP Express is licensed under Mulan PSL v2.
# You can use this software according to the terms and conditions of the Mulan PSL v2.
# You may obtain a copy of Mulan PSL v2 at:
#          http://license.coscl.org.cn/MulanPSL2
# THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
# EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
# MERCHANTABILITY OR FIT FOR A PARTICULAR PURPOSE.
# See the Mulan PSL v2 for more details.
#

cd "$(git rev-parse --show-toplevel)" || exit

change_rpm_info() {
  MAIN_VERSION=$(echo "$VERSION" | cut -d - -f 1)
  sed "s/Version: .*/Version: $MAIN_VERSION/" rpm/ocp-express.spec > rpm/ocp-express.spec.bak && mv -f rpm/ocp-express.spec.bak rpm/ocp-express.spec
}

change_version() {
  mvn versions:set -DnewVersion="$VERSION" -DgroupId='*' -DartifactId='*'
  echo "$VERSION" > rpm/ocp-express-version.txt
  change_rpm_info
}

case X$1 in
    Xset-version)
  VERSION="$2"
  echo "set-version $VERSION"
  change_version
        ;;
    Xset-release)
  BUILD_ID=$(date +%Y%m%d)
  MAIN_VERSION=$(mvn help:evaluate -Dexpression=project.version -q -DforceStdout|cut -d - -f 1)
  VERSION="${MAIN_VERSION}-${BUILD_ID}"
  echo "release $VERSION"
  change_version
  ;;
    Xshow-version)
  mvn help:evaluate -Dexpression=project.version -q -DforceStdout|xargs echo
  ;;
    Xshow-main-version)
  mvn help:evaluate -Dexpression=project.version -q -DforceStdout|cut -d - -f 1
  ;;
    *)
  echo "Usage: change_version.sh set-version|set-release|show-version|show-main-version"
  echo "Examples:"
  echo "       change_version.sh set-version 2.4.0"
  echo "       change_version.sh set-release"
  echo "       change_version.sh show-version"
  echo "       change_version.sh show-main-version"
esac

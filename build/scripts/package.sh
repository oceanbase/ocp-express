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

BASEDIR=$(cd "$(git rev-parse --show-toplevel)" && pwd)

frontend_build_cmd=1

function pre_check_frontend() {
  ret=0
  if [ "$(command -v cnpm)" ]; then
    echo "cnpm installed."
    frontend_build_cmd=1
  elif [ "$(command -v npm)" ]; then
    echo "npm installed."
    frontend_build_cmd=2
  else
    ret=1
    echo "Neither cnpm nor npm installed."
  fi

  if [ "$(command -v node)" ]; then
    echo "nodejs installed, version=$(node -v)"
  else
    ret=1
    echo "nodejs not installed"
  fi
  return $ret
}

function pre_check_backend() {
  ret=0
  if [ ! "$(command -v mvn)" ]; then
      echo "maven not installed, please install maven and configure the environments."
      ret=1
  fi
  echo "maven installed."
  return $ret
}

function pre_check() {
  pre_check_frontend
  r1=$?
  pre_check_backend
  r2=$?
  if [ "$r1" -ne 0 ] || [ "$r2" -ne 0 ]; then
    exit 1
  fi
}

function build_frontend() {
  cd frontend || exit
  if [ $frontend_build_cmd -eq 1 ]; then
    cnpm install
    cnpm run build
  else
    npm install
    npm run build
  fi
}

function packaging_jar() {
  pre_check
  build_frontend
  cd "$BASEDIR" || exit
  rm -rf server/src/main/resources/static
  mkdir -p server/src/main/resources/static
  cp -r frontend/dist/* server/src/main/resources/static
  mvn -v
  mvn clean package -Dmaven.test.skip=true
  echo "Jar path: $(find server/target -name '*.jar')"
}

function packaging_rpm() {
  packaging_jar
  mkdir -p rpmbuild/BUILD rpmbuild/BUILDROOT rpmbuild/RPMS rpmbuild/SRPMS rpmbuild/SOURCES rpmbuild/SOURCES
  if [ -z "$RELEASE" ];then
    RELEASE=$(date +%Y%m%d%H%M%S)
    export RELEASE
  fi
  rpmbuild --define "_topdir $BASEDIR/rpmbuild" --define "_workspace $BASEDIR" -bb --quiet "$BASEDIR"/rpm/ocp-express.spec
  echo "RPM path: $(find "$BASEDIR"/rpmbuild/RPMS -name '*.rpm')"
}

case X$1 in
  Xjar)
    packaging_jar
    ;;
  Xrpm)
    packaging_rpm
    ;;
  *)
    echo "Usage: package.sh jar"
    echo "Description:"
    echo "    jar : Compile the frontend and backend code and package jar"
    echo "    rpm : Compile the frontend and backend code and package jar and rpm"
esac

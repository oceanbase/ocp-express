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

PROJECT_DIR=$1
PROJECT_NAME=$2
VERSION=$3
RELEASE=$4

OS_RELEASE_VERSION_ID=$(grep -Po '(?<=release )\d' /etc/redhat-release)
RELEASE=${RELEASE}.el${OS_RELEASE_VERSION_ID}

echo "[BUILD] args: PROJECT_DIR=${PROJECT_DIR} PROJECT_NAME=${PROJECT_NAME} VERSION=${VERSION} RELEASE=${RELEASE}"

# build frontend
cd "${PROJECT_DIR}"/frontend || exist
tnpm install
tnpm run build

# copy static resources
cd "${PROJECT_DIR}" || exist
rm -rf server/src/main/resources/static
mkdir -p server/src/main/resources/static
cp -r frontend/dist/* server/src/main/resources/static

# build rpm
cd "${PROJECT_DIR}" || exist
mvn clean package -q -Dmaven.test.skip=true
mkdir -p rpmbuild/BUILD rpmbuild/BUILDROOT rpmbuild/RPMS rpmbuild/SRPMS rpmbuild/SOURCES rpmbuild/SOURCES
rpmbuild --define "_topdir ${PROJECT_DIR}/rpmbuild" --define "_workspace ${PROJECT_DIR}" -bb rpm/ocp-express.spec
find rpmbuild/RPMS/ -name "*.rpm" -exec mv {} ./rpm 2>/dev/null \;

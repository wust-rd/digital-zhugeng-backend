#!/usr/bin/env bash
#
# 后端打包：只构建 web-api 及其依赖模块，产出 web-api/target/web.war
# 用法：bash build.sh
set -euo pipefail

cd "$(dirname "${BASH_SOURCE[0]}")" || exit 1

mvn -pl web-api -am clean package "-Dmaven.test.skip=true"

echo
echo "打包完成：$(pwd)/web-api/target/web.war"
